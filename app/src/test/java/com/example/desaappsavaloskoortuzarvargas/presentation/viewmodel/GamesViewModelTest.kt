package com.example.desaappsavaloskoortuzarvargas.presentation.viewmodel

import com.example.desaappsavaloskoortuzarvargas.domain.model.*
import com.example.desaappsavaloskoortuzarvargas.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var getAllGames: GetAllGamesUseCase
    private lateinit var getGameById: GetGameByIdUseCase
    private lateinit var searchGames: SearchGamesUseCase
    private lateinit var addFav: AddToFavoritesUseCase
    private lateinit var removeFav: RemoveFromFavoritesUseCase
    private lateinit var getFavs: GetFavoritesUseCase
    private lateinit var getPriceHistory: GetPriceHistoryUseCase
    private lateinit var getByTag: GetGamesByTagUseCase

    private val sampleGame = Game(id = 1, name = "Test", description = "D", releaseDate = "2024",
        imageUrl = "url", rating = 8.0, currentPrices = mapOf("Steam" to 29.99f))

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        getAllGames = mock()
        getGameById = mock()
        searchGames = mock()
        addFav = mock()
        removeFav = mock()
        getFavs = mock()
        getPriceHistory = mock()
        getByTag = mock()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(): GamesViewModel {
        return GamesViewModel(getAllGames, getGameById, searchGames, addFav, removeFav, getFavs, getPriceHistory, getByTag)
    }

    @Test
    fun `init loads all games`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(1, vm.allGames.value.size)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `init handles failure`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.failure(RuntimeException("Error")))
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals("Error", vm.error.value)
    }

    @Test
    fun `getGameById loads game`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getGameById.invoke(1)).thenReturn(Result.success(sampleGame))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.getGameById(1)
        advanceUntilIdle()
        assertEquals("Test", vm.selectedGame.value?.name)
    }

    @Test
    fun `getGameById handles failure`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getGameById.invoke(99)).thenReturn(Result.failure(RuntimeException("Not found")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.getGameById(99)
        advanceUntilIdle()
        assertEquals("Not found", vm.error.value)
    }

    @Test
    fun `searchGames with query`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(searchGames.invoke("test")).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.searchGames("test")
        advanceUntilIdle()
        assertEquals(1, vm.allGames.value.size)
    }

    @Test
    fun `searchGames with empty query loads all`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.searchGames("")
        advanceUntilIdle()
        verify(getAllGames, atLeast(2)).invoke()
    }

    @Test
    fun `searchGames with empty query and tag active filters by tag`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("Action")).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("Action")
        advanceUntilIdle()
        vm.searchGames("")
        advanceUntilIdle()
        verify(getByTag, atLeast(2)).invoke("Action")
    }

    @Test
    fun `searchGames failure sets error`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(searchGames.invoke("x")).thenReturn(Result.failure(RuntimeException("Fail")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.searchGames("x")
        advanceUntilIdle()
        assertEquals("Fail", vm.error.value)
    }

    @Test
    fun `searchGames with tag filters results`() = runTest(dispatcher) {
        val tagged = sampleGame.copy(tags = listOf("Action"))
        val untagged = sampleGame.copy(id = 2, tags = listOf("RPG"))
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("Action")).thenReturn(Result.success(listOf(tagged)))
        whenever(searchGames.invoke("test")).thenReturn(Result.success(listOf(tagged, untagged)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("Action")
        advanceUntilIdle()
        vm.searchGames("test")
        advanceUntilIdle()
        assertEquals(1, vm.allGames.value.size)
    }

    @Test
    fun `filterByTag sets tag and loads games`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("RPG")).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("RPG")
        advanceUntilIdle()
        assertEquals("RPG", vm.selectedTag.value)
        assertEquals(1, vm.allGames.value.size)
    }

    @Test
    fun `filterByTag handles failure`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("X")).thenReturn(Result.failure(RuntimeException("Err")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("X")
        advanceUntilIdle()
        assertEquals("Err", vm.error.value)
    }

    @Test
    fun `clearTagFilter resets and loads all`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.clearTagFilter()
        advanceUntilIdle()
        assertNull(vm.selectedTag.value)
    }

    @Test
    fun `toggleShowDLCs toggles state`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.showDLCs.value)
        vm.toggleShowDLCs()
        assertTrue(vm.showDLCs.value)
        vm.toggleShowDLCs()
        assertFalse(vm.showDLCs.value)
    }

    @Test
    fun `loadRealPrices without service does nothing`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel() // no price services
        advanceUntilIdle()
        vm.loadRealPrices("test")
        advanceUntilIdle()
        assertTrue(vm.storePrices.value.isEmpty())
    }

    @Test
    fun `clearRealPrices clears list`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.clearRealPrices()
        assertTrue(vm.storePrices.value.isEmpty())
    }

    @Test
    fun `toggleFavorite adds non-favorite`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        whenever(addFav.invoke(sampleGame)).thenReturn(Result.success(Unit))
        whenever(getFavs.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleFavorite(sampleGame) // isFavorite = false -> add
        advanceUntilIdle()
        verify(addFav).invoke(sampleGame)
    }

    @Test
    fun `toggleFavorite removes favorite`() = runTest(dispatcher) {
        val favGame = sampleGame.copy(isFavorite = true)
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(favGame)))
        whenever(removeFav.invoke(1)).thenReturn(Result.success(Unit))
        whenever(getFavs.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleFavorite(favGame)
        advanceUntilIdle()
        verify(removeFav).invoke(1)
    }

    @Test
    fun `toggleFavorite add failure sets error`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(addFav.invoke(sampleGame)).thenReturn(Result.failure(RuntimeException("Fail")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleFavorite(sampleGame)
        advanceUntilIdle()
        assertEquals("Fail", vm.error.value)
    }

    @Test
    fun `toggleFavorite remove failure sets error`() = runTest(dispatcher) {
        val favGame = sampleGame.copy(isFavorite = true)
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(removeFav.invoke(1)).thenReturn(Result.failure(RuntimeException("Fail")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleFavorite(favGame)
        advanceUntilIdle()
        assertEquals("Fail", vm.error.value)
    }

    @Test
    fun `loadFavorites loads and sets state`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFavs.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadFavorites()
        advanceUntilIdle()
        assertEquals(1, vm.favorites.value.size)
    }

    @Test
    fun `loadFavorites failure sets error`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFavs.invoke()).thenReturn(Result.failure(RuntimeException("Err")))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadFavorites()
        advanceUntilIdle()
        assertEquals("Err", vm.error.value)
    }

    @Test
    fun `clearError clears error`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.failure(RuntimeException("E")))
        val vm = createViewModel()
        advanceUntilIdle()
        assertNotNull(vm.error.value)
        vm.clearError()
        assertNull(vm.error.value)
    }

    @Test
    fun `toggleFavorite add success refreshes with tag`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("Action")).thenReturn(Result.success(listOf(sampleGame)))
        whenever(addFav.invoke(sampleGame)).thenReturn(Result.success(Unit))
        whenever(getFavs.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("Action")
        advanceUntilIdle()
        vm.toggleFavorite(sampleGame)
        advanceUntilIdle()
        // refreshGames should use getByTag since tag is set
        verify(getByTag, atLeast(2)).invoke("Action")
    }

    @Test
    fun `toggleFavorite remove success refreshes with tag`() = runTest(dispatcher) {
        val favGame = sampleGame.copy(isFavorite = true)
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByTag.invoke("Action")).thenReturn(Result.success(listOf(favGame)))
        whenever(removeFav.invoke(1)).thenReturn(Result.success(Unit))
        whenever(getFavs.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.filterByTag("Action")
        advanceUntilIdle()
        vm.toggleFavorite(favGame)
        advanceUntilIdle()
        verify(getByTag, atLeast(2)).invoke("Action")
    }

    @Test
    fun `refreshGames without tag calls getAllGames`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        whenever(addFav.invoke(sampleGame)).thenReturn(Result.success(Unit))
        whenever(getFavs.invoke()).thenReturn(Result.success(listOf(sampleGame)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleFavorite(sampleGame)
        advanceUntilIdle()
        // refreshGames should call getAllGames since no tag set
        verify(getAllGames, atLeast(2)).invoke()
    }

    @Test
    fun `isLoadingPrices starts false`() = runTest(dispatcher) {
        whenever(getAllGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        assertFalse(vm.isLoadingPrices.value)
    }
}
