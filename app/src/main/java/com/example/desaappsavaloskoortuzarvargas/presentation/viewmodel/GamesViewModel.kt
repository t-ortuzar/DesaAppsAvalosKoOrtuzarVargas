package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.desaappsavaloskoortuzarvargas.data.api.ArgentineTaxCalculator
import com.example.desaappsavaloskoortuzarvargas.data.api.CheapSharkService
import com.example.desaappsavaloskoortuzarvargas.data.api.DolarService
import com.example.desaappsavaloskoortuzarvargas.data.api.GamePrice
import com.example.desaappsavaloskoortuzarvargas.data.api.SteamPriceService
import com.example.desaappsavaloskoortuzarvargas.data.local.ConnectivityObserver
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GameImageDao
import com.example.desaappsavaloskoortuzarvargas.data.local.dao.GamePriceDao
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GameImageEntity
import com.example.desaappsavaloskoortuzarvargas.data.local.entity.GamePriceEntity
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
    private val cheapSharkService: CheapSharkService? = null,
    private val steamPriceService: SteamPriceService? = null,
    private val dolarService: DolarService? = null,
    private val gamePriceDao: GamePriceDao? = null,
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

    // CheapShark real prices for the currently viewed game
    private val _realPrices = MutableStateFlow<List<GamePrice>>(emptyList())
    val realPrices: StateFlow<List<GamePrice>> = _realPrices.asStateFlow()

    private val _isLoadingPrices = MutableStateFlow(false)
    val isLoadingPrices: StateFlow<Boolean> = _isLoadingPrices.asStateFlow()

    // Connectivity state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Whether the displayed prices come from cache
    private val _pricesFromCache = MutableStateFlow(false)
    val pricesFromCache: StateFlow<Boolean> = _pricesFromCache.asStateFlow()

    // Currency toggle: true = show ARS, false = show USD
    private val _showArs = MutableStateFlow(false)
    val showArs: StateFlow<Boolean> = _showArs.asStateFlow()

    // Dólar tarjeta sell rate (fetched from DolarAPI)
    private val _dolarTarjetaRate = MutableStateFlow<Double?>(null)
    val dolarTarjetaRate: StateFlow<Double?> = _dolarTarjetaRate.asStateFlow()

    // Steam header image URL (for non-CheapShark games or better quality)
    private val _steamHeaderImage = MutableStateFlow<String?>(null)
    val steamHeaderImage: StateFlow<String?> = _steamHeaderImage.asStateFlow()

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

    /**
     * Fetch the dólar tarjeta rate for USD→ARS conversion.
     */
    private fun loadDolarRate() {
        val service = dolarService ?: return
        viewModelScope.launch {
            try {
                val cotizacion = service.getDolarTarjeta()
                if (cotizacion != null) {
                    _dolarTarjetaRate.value = cotizacion.venta
                }
            } catch (_: Exception) {
                // Will use fallback rate
            }
        }
    }

    fun toggleCurrency() {
        _showArs.value = !_showArs.value
        // Refresh dolar rate if switching to ARS and rate is stale
        if (_showArs.value && _dolarTarjetaRate.value == null) {
            loadDolarRate()
        }
    }

    /**
     * Convert a USD price to ARS using the current dólar tarjeta rate.
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
                // Cache game images in Room
                cacheGameImages(games)
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    /**
     * Cache game images in Room for offline use.
     */
    private suspend fun cacheGameImages(games: List<Game>) {
        val dao = gameImageDao ?: return
        val entities = games.map { game ->
            GameImageEntity(
                gameId = game.id,
                gameName = game.name,
                imageUrl = game.imageUrl
            )
        }
        try {
            dao.insertAll(entities)
        } catch (_: Exception) { }
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
     * Load real prices from CheapShark API for a game.
     * Also fetches the Steam header image for better quality.
     * If online: fetch from API, compare with cached prices, update Room if changed.
     * If offline: load from Room cache.
     */
    fun loadRealPrices(gameName: String, steamAppId: Int? = null) {
        val service = cheapSharkService ?: return
        viewModelScope.launch {
            _isLoadingPrices.value = true
            _realPrices.value = emptyList()
            _pricesFromCache.value = false
            _steamHeaderImage.value = null

            val online = connectivityObserver?.isConnected() ?: true

            if (online) {
                // Fetch Steam header image if app ID is known
                if (steamAppId != null && steamAppId > 0) {
                    try {
                        val steamDetails = steamPriceService?.getAppDetails(steamAppId)
                        if (steamDetails != null && steamDetails.headerImageUrl.isNotEmpty()) {
                            _steamHeaderImage.value = steamDetails.headerImageUrl
                        }
                    } catch (_: Exception) { }
                }

                try {
                    val searchResults = service.searchGame(gameName)
                    if (searchResults.isNotEmpty()) {
                        val cheapSharkId = searchResults.first().gameID
                        val prices = service.getGameDeals(cheapSharkId)
                        _realPrices.value = prices

                        // Cache prices in Room, only update if prices changed
                        cachePricesIfChanged(gameName, prices)
                    }
                } catch (_: Exception) {
                    // Network failed — try loading from cache
                    loadCachedPrices(gameName)
                }
            } else {
                // Offline — load from cache
                loadCachedPrices(gameName)
            }
            _isLoadingPrices.value = false
        }
    }

    /**
     * Compare fetched prices with cached ones. Only update Room if there's a difference.
     */
    private suspend fun cachePricesIfChanged(gameName: String, newPrices: List<GamePrice>) {
        val dao = gamePriceDao ?: return
        try {
            val cached = dao.getPricesForGameByName(gameName)
            val cachedMap = cached.associate { it.storeName to it.currentPrice }

            val hasChanges = newPrices.any { price ->
                cachedMap[price.storeName] != price.currentPrice
            } || newPrices.size != cached.size

            if (hasChanges) {
                dao.deletePricesForGameByName(gameName)
                dao.insertAll(newPrices.map { price ->
                    GamePriceEntity(
                        gameId = 0,
                        gameName = gameName,
                        storeName = price.storeName,
                        currentPrice = price.currentPrice,
                        retailPrice = price.retailPrice,
                        savings = price.savings,
                        dealUrl = price.dealUrl
                    )
                })
            }
        } catch (_: Exception) { }
    }

    /**
     * Load prices from Room cache when offline.
     */
    private suspend fun loadCachedPrices(gameName: String) {
        val dao = gamePriceDao ?: return
        try {
            val cached = dao.getPricesForGameByName(gameName)
            if (cached.isNotEmpty()) {
                _realPrices.value = cached.map { entity ->
                    GamePrice(
                        storeName = entity.storeName,
                        currentPrice = entity.currentPrice,
                        retailPrice = entity.retailPrice,
                        savings = entity.savings,
                        dealUrl = entity.dealUrl
                    )
                }
                _pricesFromCache.value = true
            }
        } catch (_: Exception) { }
    }

    fun clearRealPrices() {
        _realPrices.value = emptyList()
        _pricesFromCache.value = false
        _steamHeaderImage.value = null
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
