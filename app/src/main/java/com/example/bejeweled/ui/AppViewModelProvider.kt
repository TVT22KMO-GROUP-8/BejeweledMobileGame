package com.example.bejeweled.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bejeweled.BejeweledApplication
import com.example.bejeweled.data.ScoreboardListViewModel
import com.example.bejeweled.data.ScoreboardViewModel





/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ScoreboardListViewModel
        initializer {
            ScoreboardListViewModel(BejeweledApplication().container.scoreboardRepository)
        }

        // Initializer for ItemEntryViewModel
        initializer {
            ScoreboardViewModel(BejeweledApplication().container.scoreboardRepository)
        }

    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [BejeweledApplication].
 */
fun CreationExtras.BejeweledApplication(): BejeweledApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as BejeweledApplication)