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
class OffersViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var getCurrentDiscounts: GetCurrentDiscountsUseCase
    private lateinit var getFavDiscounts: GetFavoriteDiscountsUseCase
    private lateinit var getHistLow: GetHistoricalLowDiscountsUseCase
    private lateinit var getFreeGames: GetFreeGamesUseCase
    private lateinit var getFavs: GetFavoritesUseCase

    private val sampleDiscount = DiscountedGame(gameId = 1, gameName = "Test", imageUrl = "url",
        platform = "Steam", originalPrice = 59.99f, currentPrice = 29.99f, discountPercentage = 50)
    private val f2pGame = DiscountedGame(gameId = 2, gameName = "F2P", imageUrl = "url",
        platform = "Steam", originalPrice = 0f, currentPrice = 0f, discountPercentage = 0, isF2P = true)
    private val tempFree = DiscountedGame(gameId = 3, gameName = "Free", imageUrl = "url",
        platform = "Epic", originalPrice = 39.99f, currentPrice = 0f, discountPercentage = 100, isTemporarilyFree = true)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        getCurrentDiscounts = mock()
        getFavDiscounts = mock()
        getHistLow = mock()
        getFreeGames = mock()
        getFavs = mock()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(): OffersViewModel {
        return OffersViewModel(getCurrentDiscounts, getFavDiscounts, getHistLow, getFreeGames, getFavs)
    }

    @Test
    fun `init loads current discounts and free games`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(listOf(sampleDiscount)))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(1, vm.currentDiscounts.value.size)
    }

    @Test
    fun `loadFavoriteDiscounts loads from favorites`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val games = listOf(Game(id = 1, name = "G", description = "D", releaseDate = "2024",
            imageUrl = "u", rating = 8.0, currentPrices = emptyMap()))
        whenever(getFavs.invoke()).thenReturn(Result.success(games))
        whenever(getFavDiscounts.invoke(listOf(1))).thenReturn(Result.success(listOf(sampleDiscount)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadFavoriteDiscounts()
        advanceUntilIdle()
        assertEquals(1, vm.favoriteDiscounts.value.size)
        assertEquals(OffersViewModel.FilterType.FAVORITES, vm.filterType.value)
    }

    @Test
    fun `loadHistoricalLowDiscounts`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getHistLow.invoke()).thenReturn(Result.success(listOf(sampleDiscount)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadHistoricalLowDiscounts()
        advanceUntilIdle()
        assertEquals(1, vm.historicalLowDiscounts.value.size)
        assertEquals(OffersViewModel.FilterType.HISTORICAL_LOW, vm.filterType.value)
    }

    @Test
    fun `showFreeGames sets filter type`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.showFreeGames()
        assertEquals(OffersViewModel.FilterType.FREE, vm.filterType.value)
    }

    @Test
    fun `setFreeFilter F2P_ONLY filters correctly`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFreeFilter(OffersViewModel.FreeFilter.F2P_ONLY)
        assertEquals(1, vm.freeGames.value.size)
        assertEquals("F2P", vm.freeGames.value[0].gameName)
    }

    @Test
    fun `setFreeFilter TEMP_FREE_ONLY filters correctly`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFreeFilter(OffersViewModel.FreeFilter.TEMP_FREE_ONLY)
        assertEquals(1, vm.freeGames.value.size)
        assertEquals("Free", vm.freeGames.value[0].gameName)
    }

    @Test
    fun `setFreeFilter ALL shows all`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setFreeFilter(OffersViewModel.FreeFilter.ALL)
        assertEquals(2, vm.freeGames.value.size)
    }

    @Test
    fun `setPlatformFilter filters by platform`() = runTest(dispatcher) {
        val epicDiscount = sampleDiscount.copy(gameId = 2, platform = "Epic")
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(listOf(sampleDiscount, epicDiscount)))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setPlatformFilter("Steam")
        advanceUntilIdle()
        assertEquals(1, vm.currentDiscounts.value.size)
        assertEquals("Steam", vm.selectedPlatform.value)
    }

    @Test
    fun `setPlatformFilter null shows all`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(listOf(sampleDiscount)))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.setPlatformFilter(null)
        advanceUntilIdle()
        assertNull(vm.selectedPlatform.value)
    }

    @Test
    fun `resetFilter resets to ALL`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(listOf(sampleDiscount)))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.resetFilter()
        advanceUntilIdle()
        assertEquals(OffersViewModel.FilterType.ALL, vm.filterType.value)
    }

    @Test
    fun `setPlatformFilter re-applies based on current filter type`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        whenever(getHistLow.invoke()).thenReturn(Result.success(listOf(sampleDiscount)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadHistoricalLowDiscounts()
        advanceUntilIdle()
        vm.setPlatformFilter("Steam")
        advanceUntilIdle()
        verify(getHistLow, atLeast(2)).invoke()
    }

    @Test
    fun `setPlatformFilter with FAVORITES re-applies favorites filter`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val games = listOf(Game(id = 1, name = "G", description = "D", releaseDate = "2024",
            imageUrl = "u", rating = 8.0, currentPrices = emptyMap()))
        whenever(getFavs.invoke()).thenReturn(Result.success(games))
        whenever(getFavDiscounts.invoke(listOf(1))).thenReturn(Result.success(listOf(sampleDiscount)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadFavoriteDiscounts()
        advanceUntilIdle()
        vm.setPlatformFilter("Steam")
        advanceUntilIdle()
        verify(getFavDiscounts, atLeast(2)).invoke(listOf(1))
    }

    @Test
    fun `setPlatformFilter with FREE re-applies free filter`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(listOf(f2pGame, tempFree)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.showFreeGames()
        vm.setPlatformFilter("Steam")
        // Free filter with platform should filter by platform
        val freeGames = vm.freeGames.value
        freeGames.forEach { assertEquals("Steam", it.platform) }
    }

    @Test
    fun `loadCurrentDiscounts failure does not crash`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.failure(RuntimeException("Err")))
        whenever(getFreeGames.invoke()).thenReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.currentDiscounts.value.isEmpty())
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadFreeGames failure does not crash`() = runTest(dispatcher) {
        whenever(getCurrentDiscounts.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getFreeGames.invoke()).thenReturn(Result.failure(RuntimeException("Err")))
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.freeGames.value.isEmpty())
    }
}
