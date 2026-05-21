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
class NewsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var getAllNews: GetAllNewsUseCase
    private lateinit var getByGameId: GetNewsByGameIdUseCase
    private lateinit var getByFavs: GetNewsByFavoritesUseCase
    private lateinit var getFavs: GetFavoritesUseCase

    private val sampleNews = News(id = 1, title = "Title", content = "Content", imageUrl = "url",
        date = "2024-01-01", gameId = 1, platform = "Steam", category = "discount")
    private val sampleNews2 = News(id = 2, title = "Title2", content = "C2", imageUrl = "url",
        date = "2024-06-01", gameId = 2, platform = "Epic", category = "event")

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        getAllNews = mock()
        getByGameId = mock()
        getByFavs = mock()
        getFavs = mock()
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    private fun createViewModel(): NewsViewModel {
        return NewsViewModel(getAllNews, getByGameId, getByFavs, getFavs)
    }

    @Test
    fun `init loads all news sorted by date desc`() = runTest(dispatcher) {
        whenever(getAllNews.invoke()).thenReturn(Result.success(listOf(sampleNews, sampleNews2)))
        val vm = createViewModel()
        advanceUntilIdle()
        assertEquals(2, vm.allNews.value.size)
        assertEquals("2024-06-01", vm.allNews.value.first().date)
        assertFalse(vm.isLoading.value)
    }

    @Test
    fun `loadFavoritesNews loads and sets filter`() = runTest(dispatcher) {
        whenever(getAllNews.invoke()).thenReturn(Result.success(emptyList()))
        val games = listOf(Game(id = 1, name = "G", description = "D", releaseDate = "2024",
            imageUrl = "u", rating = 8.0, currentPrices = emptyMap()))
        whenever(getFavs.invoke()).thenReturn(Result.success(games))
        whenever(getByFavs.invoke(listOf(1))).thenReturn(Result.success(listOf(sampleNews)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadFavoritesNews()
        advanceUntilIdle()
        assertEquals(1, vm.favoritesNews.value.size)
        assertEquals(NewsViewModel.FilterType.FAVORITES, vm.filterType.value)
    }

    @Test
    fun `loadNewsByGameId loads and sets filter`() = runTest(dispatcher) {
        whenever(getAllNews.invoke()).thenReturn(Result.success(emptyList()))
        whenever(getByGameId.invoke(5)).thenReturn(Result.success(listOf(sampleNews)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.loadNewsByGameId(5)
        advanceUntilIdle()
        assertEquals(1, vm.favoritesNews.value.size)
        assertEquals(NewsViewModel.FilterType.BY_GAME, vm.filterType.value)
    }

    @Test
    fun `resetFilter resets to ALL and reloads`() = runTest(dispatcher) {
        whenever(getAllNews.invoke()).thenReturn(Result.success(listOf(sampleNews)))
        val vm = createViewModel()
        advanceUntilIdle()
        vm.resetFilter()
        advanceUntilIdle()
        assertEquals(NewsViewModel.FilterType.ALL, vm.filterType.value)
        verify(getAllNews, atLeast(2)).invoke()
    }
}

