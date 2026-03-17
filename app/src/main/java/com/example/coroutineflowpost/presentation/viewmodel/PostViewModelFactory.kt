package com.example.coroutineflowpost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coroutineflowpost.data.network.NetworkMonitor
import com.example.coroutineflowpost.domain.usecase.GetPostsUseCase
import com.example.coroutineflowpost.domain.usecase.RefreshPostsUseCase

// Without Hilt, PostViewModelFactory is the manual equivalent.
class PostViewModelFactory(
    private val getPostsUseCase:     GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase,
    private val networkMonitor:      NetworkMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PostViewModel(
            getPostsUseCase,
            refreshPostsUseCase,
            networkMonitor
        ) as T
    }
}