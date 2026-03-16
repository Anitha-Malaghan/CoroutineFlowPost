package com.example.coroutineflowpost.domain.repository

import com.example.coroutineflowpost.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<List<Post>>
    suspend fun refresh()
}