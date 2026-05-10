package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.domain.model.News
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllNewsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByGameIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetNewsByFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val getAllNewsUseCase: GetAllNewsUseCase,
    private val getNewsByGameIdUseCase: GetNewsByGameIdUseCase,
    private val getNewsByFavoritesUseCase: GetNewsByFavoritesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _allNews = MutableStateFlow<List<News>>(emptyList())
    val allNews: StateFlow<List<News>> = _allNews.asStateFlow()

    private val _favoritesNews = MutableStateFlow<List<News>>(emptyList())
    val favoritesNews: StateFlow<List<News>> = _favoritesNews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterType = MutableStateFlow<FilterType>(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    enum class FilterType {
        ALL, FAVORITES, BY_GAME
    }

    init {
        loadAllNews()
    }

    fun loadAllNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getAllNewsUseCase().onSuccess { news ->
                _allNews.value = news.sortedByDescending { it.date }
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadFavoritesNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getFavoritesUseCase().onSuccess { favorites ->
                val favoriteIds = favorites.map { it.id }
                getNewsByFavoritesUseCase(favoriteIds).onSuccess { news ->
                    _favoritesNews.value = news.sortedByDescending { it.date }
                    _filterType.value = FilterType.FAVORITES
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadNewsByGameId(gameId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getNewsByGameIdUseCase(gameId).onSuccess { news ->
                _favoritesNews.value = news.sortedByDescending { it.date }
                _filterType.value = FilterType.BY_GAME
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun resetFilter() {
        _filterType.value = FilterType.ALL
        loadAllNews()
    }

    fun clearError() {
        _error.value = null
    }
}

