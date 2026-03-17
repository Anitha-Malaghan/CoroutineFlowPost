package com.example.coroutineflowpost.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutineflowpost.data.network.NetworkMonitor
import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.domain.model.Result
import com.example.coroutineflowpost.domain.usecase.GetPostsUseCase
import com.example.coroutineflowpost.domain.usecase.RefreshPostsUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostViewModel(
    private val getPostsUseCase:     GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase,
    private val networkMonitor:      NetworkMonitor
) : ViewModel() {

    // combine() — merge posts result + network state into single UI state
    val uiState: StateFlow<PostUiState> = combine(
        getPostsUseCase(),
        networkMonitor.isOnline
    ) { result: Result<List<Post>>, isOnline: Boolean ->
        when (result) {
            is Result.Loading -> PostUiState.Loading
            is Result.Success -> {
                if (result.data.isEmpty()) PostUiState.Empty
                else PostUiState.Success(result.data, isOnline)
            }
            is Result.Error -> PostUiState.Error(
                result.exception.message ?: "Something went wrong",
                isOnline
            )
        }
    }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = PostUiState.Loading
        )

    // SharedFlow — one-shot UI events (snackbar)
    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // StateFlow — tracks loading spinner for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()


    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _events.emit(UiEvent.ShowSnackbar("Unexpected error: ${throwable.message}"))
        }
    }

    init { refresh() }

    fun refresh() {
        viewModelScope.launch (exceptionHandler){
            _isRefreshing.value = true                           // show spinner
            runCatching { refreshPostsUseCase() }
                .onSuccess { _events.emit(UiEvent.ShowSnackbar("Feed updated")) }
                .onFailure { _events.emit(UiEvent.ShowSnackbar("Failed: ${it.message}")) }
            _isRefreshing.value = false                          // ✅ hide spinner
        }
    }
    // tracks selected filter chip index — survives rotation
    private val _selectedChipIndex = MutableStateFlow(0)
    val selectedChipIndex: StateFlow<Int> = _selectedChipIndex.asStateFlow()

    fun onChipSelected(index: Int) {
        _selectedChipIndex.value = index
    }
}





sealed interface PostUiState {
    data object Loading : PostUiState
    data object Empty   : PostUiState
    data class Success(
        val posts:    List<Post>,
        val isOnline: Boolean = true
    ) : PostUiState
    data class Error(
        val message:  String,
        val isOnline: Boolean = true
    ) : PostUiState
}

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}