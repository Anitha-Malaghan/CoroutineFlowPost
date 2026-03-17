package com.example.coroutineflowpost.data.repository

import com.example.coroutineflowpost.data.local.PostDao
import com.example.coroutineflowpost.data.local.PostEntity
import com.example.coroutineflowpost.data.remote.PostApiService
import com.example.coroutineflowpost.data.remote.PostDto
import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.repository.PostRepository
import com.example.coroutineflowpost.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch                              // ✅ catch operator
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.pow

class PostRepositoryImpl(
    private val apiService: PostApiService,
    private val postDao: PostDao
) : PostRepository {

   /* override fun observePosts(): Flow<List<Post>> =
        postDao.observeAll().map { entities -> entities.map { it.toDomain() } }*/
   //retryWhen + exponential backoff on the Flow
    override fun observePosts(): Flow<Result<List<Post>>> =
       postDao.observeAll()
           .map { entities ->
               Result.Success(entities.map { it.toDomain() }) as Result<List<Post>>
           }
           .retryWhen { cause, attempt ->
               if (cause is IOException && attempt < 3) {
                   delay(2.0.pow(attempt.toInt()).toLong() * 1000)
                   true
               } else false
           }
           .catch { throwable ->
               emit(Result.Error(throwable))                     // ✅ emit is a Flow builder function
           }
           .flowOn(Dispatchers.IO)

    //withTimeout on the API call
    //supervisorScope — If we ever fetch multiple endpoints at once isolate failures in parallel calls
    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            supervisorScope {
                val postsDeferred = async { withTimeout(10_000L) { apiService.getPosts() } }
                // future: val usersDeferred = async { apiService.getUsers() }
                try {
                    val posts = postsDeferred.await()
                    postDao.upsertAll(posts.map { it.toEntity() })
                } catch (e: Exception) {
                    // log, don't crash — supervisorScope keeps other children alive
                }
            }
        }
    }

    private fun PostEntity.toDomain() = Post(id, userId, title, body)
    private fun PostDto.toEntity()    = PostEntity(id, userId, title, body)
}
