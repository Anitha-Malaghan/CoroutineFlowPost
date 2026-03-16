package com.example.coroutineflowpost.data.repository

import com.example.coroutineflowpost.data.local.PostDao
import com.example.coroutineflowpost.data.local.PostEntity
import com.example.coroutineflowpost.data.remote.PostApiService
import com.example.coroutineflowpost.data.remote.PostDto
import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PostRepositoryImpl(
    private val apiService: PostApiService,
    private val postDao: PostDao
) : PostRepository {

    override fun observePosts(): Flow<List<Post>> =
        postDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val posts = apiService.getPosts()
            postDao.upsertAll(posts.map { it.toEntity() })
        }
    }

    private fun PostEntity.toDomain() = Post(id, userId, title, body)
    private fun PostDto.toEntity()    = PostEntity(id, userId, title, body)
}
