package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.data.api.CheapSharkService
import com.example.desaappsavaloskoortuzarvargas.data.api.GamePrice
import com.example.desaappsavaloskoortuzarvargas.domain.model.Game
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.AddToFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetAllGamesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetGameByIdUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetGamesByTagUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.GetPriceHistoryUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.RemoveFromFavoritesUseCase
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.SearchGamesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamesViewModel(
    private val getAllGamesUseCase: GetAllGamesUseCase,
    private val getGameByIdUseCase: GetGameByIdUseCase,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val getGamesByTagUseCase: GetGamesByTagUseCase,
    private val cheapSharkService: CheapSharkService? = null
) : ViewModel() {

    private val _allGames = MutableStateFlow<List<Game>>(emptyList())
    val allGames: StateFlow<List<Game>> = _allGames.asStateFlow()

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame.asStateFlow()

    private val _favorites = MutableStateFlow<List<Game>>(emptyList())
    val favorites: StateFlow<List<Game>> = _favorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _showDLCs = MutableStateFlow(false)
    val showDLCs: StateFlow<Boolean> = _showDLCs.asStateFlow()

    // CheapShark real prices for the currently viewed game
    private val _realPrices = MutableStateFlow<List<GamePrice>>(emptyList())
    val realPrices: StateFlow<List<GamePrice>> = _realPrices.asStateFlow()

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices.asStateFlow()

    init {
        loadAllGames()
    }

    fun loadAllGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedTag.value = null
            getAllGamesUseCase().onSuccess { games ->
                _allGames.value = games
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun getGameById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getGameByIdUseCase(id).onSuccess { game ->
                _selectedGame.value = game
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun searchGames(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            if (query.isEmpty()) {
                if (_selectedTag.value != null) {
                    filterByTag(_selectedTag.value!!)
                } else {
                    loadAllGames()
                }
            } else {
                searchGamesUseCase(query).onSuccess { games ->
                    _allGames.value = if (_selectedTag.value != null) {
                        games.filter { it.tags.contains(_selectedTag.value) }
                    } else games
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            }
            _isLoading.value = false
        }
    }

    fun filterByTag(tag: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedTag.value = tag
            getGamesByTagUseCase(tag).onSuccess { games ->
                _allGames.value = games
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun clearTagFilter() {
        _selectedTag.value = null
        loadAllGames()
    }

    fun toggleShowDLCs() {
        _showDLCs.value = !_showDLCs.value
    }

    /**
     * Load real prices from CheapShark API for a game
     */
    fun loadRealPrices(gameName: String) {
        val service = cheapSharkService ?: return
        viewModelScope.launch {
            _isLoadingPrices.value = true
            _realPrices.value = emptyList()
            try {
                val searchResults = service.searchGame(gameName)
                if (searchResults.isNotEmpty()) {
                    val cheapSharkId = searchResults.first().gameID
                    val prices = service.getGameDeals(cheapSharkId)
                    _realPrices.value = prices
                }
            } catch (_: Exception) {
                // Silently fail, mock prices will be shown
            }
            _isLoadingPrices.value = false
        }
    }

    fun clearRealPrices() {
        _realPrices.value = emptyList()
    }

    fun toggleFavorite(game: Game) {
        viewModelScope.launch {
            _error.value = null
            if (game.isFavorite) {
                removeFromFavoritesUseCase(game.id).onSuccess {
                    refreshGames()
                    loadFavorites()
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } else {
                addToFavoritesUseCase(game).onSuccess {
                    refreshGames()
                    loadFavorites()
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    private fun refreshGames() {
        viewModelScope.launch {
            val tag = _selectedTag.value
            if (tag != null) {
                getGamesByTagUseCase(tag).onSuccess { _allGames.value = it }
            } else {
                getAllGamesUseCase().onSuccess { _allGames.value = it }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getFavoritesUseCase().onSuccess { favorites ->
                _favorites.value = favorites
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
