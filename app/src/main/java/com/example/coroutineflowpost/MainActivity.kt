package com.example.coroutineflowpost

import PostListScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coroutineflowpost.presentation.viewmodel.PostViewModel
import com.example.coroutineflowpost.presentation.viewmodel.PostViewModelFactory
import com.example.coroutineflowpost.ui.theme.CoroutineFlowPostTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CoroutineFlowPostApp
        val factory = PostViewModelFactory(
            app.container.getPostsUseCase,
            app.container.refreshPostsUseCase
        )

        setContent {
            CoroutineFlowPostTheme {
                val viewModel: PostViewModel = viewModel(factory = factory)
                PostListScreen(viewModel)
            }
        }
    }
}