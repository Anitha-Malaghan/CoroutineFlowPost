package com.example.coroutineflowpost.domain.repository

import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<Result<List<Post>>>
    suspend fun refresh()
}