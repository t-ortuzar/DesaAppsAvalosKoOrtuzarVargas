package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.domain.model.DiscountedGame
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetCurrentDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoriteDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetHistoricalLowDiscountsUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFreeGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OffersViewModel(
    private val getCurrentDiscountsUseCase: GetCurrentDiscountsUseCase,
    private val getFavoriteDiscountsUseCase: GetFavoriteDiscountsUseCase,
    private val getHistoricalLowDiscountsUseCase: GetHistoricalLowDiscountsUseCase,
    private val getFreeGamesUseCase: GetFreeGamesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _currentDiscounts = MutableStateFlow<List<DiscountedGame>>(emptyList())
    val currentDiscounts: StateFlow<List<DiscountedGame>> = _currentDiscounts.asStateFlow()

    private val _favoriteDiscounts = MutableStateFlow<List<DiscountedGame>>(emptyList())
    val favoriteDiscounts: StateFlow<List<DiscountedGame>> = _favoriteDiscounts.asStateFlow()

    private val _historicalLowDiscounts = MutableStateFlow<List<DiscountedGame>>(emptyList())
    val historicalLowDiscounts: StateFlow<List<DiscountedGame>> = _historicalLowDiscounts.asStateFlow()

    private val _freeGames = MutableStateFlow<List<DiscountedGame>>(emptyList())
    val freeGames: StateFlow<List<DiscountedGame>> = _freeGames.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _filterType = MutableStateFlow(FilterType.ALL)
    val filterType: StateFlow<FilterType> = _filterType.asStateFlow()

    // F2P filter: show all free, only temp free, or only F2P
    private val _freeFilter = MutableStateFlow(FreeFilter.TEMP_FREE_ONLY)
    val freeFilter: StateFlow<FreeFilter> = _freeFilter.asStateFlow()

    // Platform filter
    private val _selectedPlatform = MutableStateFlow<String?>(null)
    val selectedPlatform: StateFlow<String?> = _selectedPlatform.asStateFlow()

    // All free games unfiltered
    private var allFreeGamesCache = emptyList<DiscountedGame>()

    enum class FilterType {
        ALL, FAVORITES, HISTORICAL_LOW, FREE
    }

    enum class FreeFilter {
        ALL, TEMP_FREE_ONLY, F2P_ONLY
    }

    init {
        loadCurrentDiscounts()
        loadFreeGames()
    }

    fun loadCurrentDiscounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getCurrentDiscountsUseCase().onSuccess { discounts ->
                _currentDiscounts.value = applyPlatformFilter(discounts.sortedByDescending { it.discountPercentage })
                _filterType.value = FilterType.ALL
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadFavoriteDiscounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getFavoritesUseCase().onSuccess { favorites ->
                val favoriteIds = favorites.map { it.id }
                getFavoriteDiscountsUseCase(favoriteIds).onSuccess { discounts ->
                    _favoriteDiscounts.value = applyPlatformFilter(discounts.sortedByDescending { it.discountPercentage })
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

    fun loadHistoricalLowDiscounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getHistoricalLowDiscountsUseCase().onSuccess { discounts ->
                _historicalLowDiscounts.value = applyPlatformFilter(discounts.sortedByDescending { it.discountPercentage })
                _filterType.value = FilterType.HISTORICAL_LOW
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun loadFreeGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getFreeGamesUseCase().onSuccess { freeGames ->
                allFreeGamesCache = freeGames
                applyFreeFilter()
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun showFreeGames() {
        _filterType.value = FilterType.FREE
        applyFreeFilter()
    }

    fun setFreeFilter(filter: FreeFilter) {
        _freeFilter.value = filter
        applyFreeFilter()
    }

    private fun applyFreeFilter() {
        val filtered = when (_freeFilter.value) {
            FreeFilter.ALL -> allFreeGamesCache
            FreeFilter.TEMP_FREE_ONLY -> allFreeGamesCache.filter { it.isTemporarilyFree }
            FreeFilter.F2P_ONLY -> allFreeGamesCache.filter { it.isF2P }
        }
        _freeGames.value = applyPlatformFilter(filtered)
    }

    fun setPlatformFilter(platform: String?) {
        _selectedPlatform.value = platform
        // Re-apply current tab
        when (_filterType.value) {
            FilterType.ALL -> loadCurrentDiscounts()
            FilterType.FAVORITES -> loadFavoriteDiscounts()
            FilterType.HISTORICAL_LOW -> loadHistoricalLowDiscounts()
            FilterType.FREE -> applyFreeFilter()
        }
    }

    private fun applyPlatformFilter(list: List<DiscountedGame>): List<DiscountedGame> {
        val platform = _selectedPlatform.value ?: return list
        return list.filter { it.platform == platform }
    }

    fun resetFilter() {
        _filterType.value = FilterType.ALL
        loadCurrentDiscounts()
    }

    fun clearError() {
        _error.value = null
    }
}

