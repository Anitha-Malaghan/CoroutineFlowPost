package com.example.coroutineflowpost

import android.content.Context
import androidx.room.Room
import com.example.coroutineflowpost.data.local.AppDatabase
import com.example.coroutineflowpost.data.local.PostDao
import com.example.coroutineflowpost.data.remote.PostApiService
import com.example.coroutineflowpost.data.remote.RetrofitClient
import com.example.coroutineflowpost.data.repository.PostRepositoryImpl
import com.example.coroutineflowpost.domain.repository.PostRepository
import com.example.coroutineflowpost.domain.usecase.GetPostsUseCase
import com.example.coroutineflowpost.domain.usecase.RefreshPostsUseCase

class AppContainer(context: Context) {

    // --- Network ---
    private val retrofit: PostApiService = RetrofitClient.create()

    // --- Database ---
    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "coroutineflowpost.db"
    ).build()

    private val postDao: PostDao = database.postDao()

    // --- Repository ---
    private val postRepository: PostRepository = PostRepositoryImpl(
        apiService = retrofit,
        postDao    = postDao
    )

    // --- Use cases (created fresh each time — they're stateless) ---
    val getPostsUseCase    get() = GetPostsUseCase(postRepository)
    val refreshPostsUseCase get() = RefreshPostsUseCase(postRepository)
}
