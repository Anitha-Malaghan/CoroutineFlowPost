package com.example.coroutineflowpost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coroutineflowpost.domain.usecase.GetPostsUseCase
import com.example.coroutineflowpost.domain.usecase.RefreshPostsUseCase

class PostViewModelFactory(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PostViewModel(getPostsUseCase, refreshPostsUseCase) as T
    }
}