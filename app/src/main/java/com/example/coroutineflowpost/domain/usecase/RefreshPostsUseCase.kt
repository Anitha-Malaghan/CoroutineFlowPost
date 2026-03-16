package com.example.coroutineflowpost.domain.usecase

import com.example.coroutineflowpost.domain.repository.PostRepository

class RefreshPostsUseCase(private val repository: PostRepository) {
    suspend operator fun invoke() = repository.refresh()
}