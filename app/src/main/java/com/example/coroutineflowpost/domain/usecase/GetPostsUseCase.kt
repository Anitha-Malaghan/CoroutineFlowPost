package com.example.coroutineflowpost.domain.usecase

import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.model.Result
import com.example.coroutineflowpost.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow

class GetPostsUseCase(private val repository: PostRepository) {
    operator fun invoke(): Flow<Result<List<Post>>> = repository.observePosts()
}