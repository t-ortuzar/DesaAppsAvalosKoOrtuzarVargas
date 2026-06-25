package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.data.api.ArgentineTaxCalculator
import com.example.desaappsavaloskoortuzarvargas.data.api.DolarService
import com.example.desaappsavaloskoortuzarvargas.data.api.EpicPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.PriceRefreshManager
import com.example.desaappsavaloskoortuzarvargas.data.api.StorePrice
import com.example.desaappsavaloskoortuzarvargas.data.local.ConnectivityObserver
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GameImageDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GameImageEntity
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
    private val priceRefreshManager: PriceRefreshManager? = null,
    private val dolarService: DolarService? = null,
    private val epicPriceService: EpicPriceService? = null,
    private val gameImageDao: GameImageDao? = null,
    private val connectivityObserver: ConnectivityObserver? = null
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

    // Real Argentine prices from all stores
    private val _storePrices = MutableStateFlow<List<StorePrice>>(emptyList())
    val storePrices: StateFlow<List<StorePrice>> = _storePrices.asStateFlow()

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices.asStateFlow()

    // Connectivity state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Whether the displayed prices come from cache
    private val _pricesFromCache = MutableStateFlow(false)
    val pricesFromCache: StateFlow<Boolean> = _pricesFromCache.asStateFlow()

    // Dólar tarjeta sell rate (for GOG USD→ARS conversion display)
    private val _dolarTarjetaRate = MutableStateFlow<Double?>(null)
    val dolarTarjetaRate: StateFlow<Double?> = _dolarTarjetaRate.asStateFlow()

    // Dynamic game image URL fetched from store APIs (used as fallback for non-Steam games)
    private val _gameDetailImageUrl = MutableStateFlow<String?>(null)
    val gameDetailImageUrl: StateFlow<String?> = _gameDetailImageUrl.asStateFlow()

    // Persistent cache of fetched image URLs (gameId → imageUrl).
    // Populated by fetchMissingImages() and applied to search/filter results so that
    // non-Steam games (e.g. Alan Wake 2) always display their image in the catalog list.
    private val _imageCache = mutableMapOf<Int, String>()

    init {
        loadAllGames()
        observeConnectivity()
        loadDolarRate()
    }

    private fun observeConnectivity() {
        val observer = connectivityObserver ?: return
        viewModelScope.launch {
            observer.observe().collect { connected ->
                _isOnline.value = connected
            }
        }
    }

    private fun loadDolarRate() {
        val service = dolarService ?: return
        viewModelScope.launch {
            try {
                val cotizacion = service.getDolarTarjeta()
                if (cotizacion != null) {
                    _dolarTarjetaRate.value = cotizacion.venta
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Convert a USD price to ARS using dólar tarjeta rate.
     */
    fun convertToArs(usdPrice: Float): Float {
        return ArgentineTaxCalculator.usdToArs(usdPrice, _dolarTarjetaRate.value)
    }

    fun loadAllGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _selectedTag.value = null
            getAllGamesUseCase().onSuccess { games ->
                _allGames.value = games
                cacheGameImages(games)
                // Populate Steam App ID map for the PriceRefreshManager
                priceRefreshManager?.setSteamAppIds(
                    games.filter { it.steamAppId > 0 }
                        .associate { it.name to it.steamAppId }
                )
                // Populate platform map so the background refresh only queries
                // stores where each game is actually sold (prevents GOG from being
                // called for Steam-only games, etc.)
                priceRefreshManager?.setGamePlatforms(
                    games.associate { it.name to it.availablePlatforms }
                )
                // Fetch images for non-Steam games from their available stores
                fetchMissingImages(games)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    private suspend fun cacheGameImages(games: List<Game>) {
        val dao = gameImageDao ?: return
        val entities = games.map { game ->
            GameImageEntity(gameId = game.id, gameName = game.name, imageUrl = game.imageUrl)
        }
        try { dao.insertAll(entities) } catch (_: Exception) { }
    }

    /**
     * For games with no image (empty imageUrl), fetch from Epic Games Store.
     * Only targets games that list "Epic Games" in their available platforms.
     * Updates the games list in-place with the fetched image URLs.
     */
    private fun fetchMissingImages(games: List<Game>) {
        val epic = epicPriceService ?: return
        val needsImage = games.filter { it.imageUrl.isEmpty() && it.availablePlatforms.contains("Epic Games") }
        if (needsImage.isEmpty()) return

        viewModelScope.launch {
            var updated = false
            val currentGames = _allGames.value.toMutableList()
            for (game in needsImage) {
                try {
                    val imageUrl = epic.fetchGameImage(game.name)
                    if (!imageUrl.isNullOrEmpty()) {
                        // Store in persistent cache so search/filter results also get the image
                        _imageCache[game.id] = imageUrl
                        val idx = currentGames.indexOfFirst { it.id == game.id }
                        if (idx >= 0) {
                            currentGames[idx] = currentGames[idx].copy(imageUrl = imageUrl)
                            updated = true
                        }
                    }
                } catch (_: Exception) { }
            }
            if (updated) {
                _allGames.value = currentGames
            }
        }
    }

    /**
     * Apply any cached image URLs (fetched at startup) to a list of games.
     * Ensures that non-Steam games (e.g. Alan Wake 2) always show their image
     * even when the list came from a search/filter that bypasses the in-memory update.
     */
    private fun applyImageCache(games: List<Game>): List<Game> {
        if (_imageCache.isEmpty()) return games
        return games.map { game ->
            if (game.imageUrl.isEmpty()) {
                val cached = _imageCache[game.id]
                if (!cached.isNullOrEmpty()) game.copy(imageUrl = cached) else game
            } else game
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
                    val enriched = applyImageCache(games)
                    _allGames.value = if (_selectedTag.value != null) {
                        enriched.filter { it.tags.contains(_selectedTag.value) }
                    } else enriched
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
                _allGames.value = applyImageCache(games)
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

    fun loadRealPrices(gameName: String, steamAppId: Int? = null, platforms: List<String>? = null) {
        val manager = priceRefreshManager ?: return
        viewModelScope.launch {
            _isLoadingPrices.value = true
            _pricesFromCache.value = false
            _gameDetailImageUrl.value = null

            val online = _isOnline.value

            if (online) {
                // Always fetch fresh data when online, filtered by platforms
                try {
                    val fresh = manager.fetchAndCachePrices(gameName, steamAppId, platforms)
                    if (fresh.isNotEmpty()) {
                        _storePrices.value = fresh
                        _pricesFromCache.value = false
                        val img = fresh.firstNotNullOfOrNull {
                            it.imageUrl.takeIf { url -> url.isNotEmpty() }
                        }
                        if (img != null) _gameDetailImageUrl.value = img
                    } else {
                        val cached = manager.getCachedPrices(gameName)
                        _storePrices.value = cached
                        _pricesFromCache.value = false
                    }
                } catch (_: Exception) {
                    val cached = manager.getCachedPrices(gameName)
                    _storePrices.value = cached
                    _pricesFromCache.value = cached.isNotEmpty()
                }
            } else {
                val cached = manager.getCachedPrices(gameName)
                _storePrices.value = cached
                _pricesFromCache.value = cached.isNotEmpty()
            }

            _isLoadingPrices.value = false
        }
    }

    fun clearRealPrices() {
        _storePrices.value = emptyList()
        _pricesFromCache.value = false
        _gameDetailImageUrl.value = null
    }

    fun toggleFavorite(game: Game) {
        viewModelScope.launch {
            _error.value = null
            if (game.isFavorite) {
                removeFromFavoritesUseCase(game.id).onSuccess {
                    refreshGames()
                    loadFavorites()
                }.onFailure { _error.value = it.message }
            } else {
                addToFavoritesUseCase(game).onSuccess {
                    refreshGames()
                    loadFavorites()
                }.onFailure { _error.value = it.message }
            }
        }
    }

    private fun refreshGames() {
        viewModelScope.launch {
            val tag = _selectedTag.value
            if (tag != null) {
                getGamesByTagUseCase(tag).onSuccess { _allGames.value = applyImageCache(it) }
            } else {
                getAllGamesUseCase().onSuccess { _allGames.value = applyImageCache(it) }
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getFavoritesUseCase().onSuccess { _favorites.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
