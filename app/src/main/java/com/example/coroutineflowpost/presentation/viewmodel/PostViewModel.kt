package com.example.coroutineflowpost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.usecase.GetPostsUseCase
import com.example.coroutineflowpost.domain.usecase.RefreshPostsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModel() {

    val uiState: StateFlow<PostUiState> = getPostsUseCase()
        .map<List<Post>, PostUiState> { posts ->
            if (posts.isEmpty()) PostUiState.Empty
            else PostUiState.Success(posts)
        }
        .catch { emit(PostUiState.Error(it.message ?: "Something went wrong")) }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = PostUiState.Loading
        )

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            runCatching { refreshPostsUseCase() }
        }
    }
}

sealed interface PostUiState {
    data object Loading                    : PostUiState
    data object Empty                      : PostUiState
    data class  Success(val posts: List<Post>) : PostUiState
    data class  Error(val message: String) : PostUiState
}