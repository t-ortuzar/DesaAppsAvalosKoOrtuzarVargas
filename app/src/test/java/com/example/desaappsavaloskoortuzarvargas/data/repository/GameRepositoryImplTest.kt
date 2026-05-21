package com.example.desaappsavaloskoortuzarvargas.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {

    private lateinit var repo: GameRepositoryImpl

    @Before
    fun setup() { repo = GameRepositoryImpl() }

    @Test
    fun `getAllGames returns non-empty list`() = runTest {
        val result = repo.getAllGames()
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun `getGameById returns correct game`() = runTest {
        val result = repo.getGameById(1)
        assertTrue(result.isSuccess)
        assertEquals("Elden Ring", result.getOrNull()?.name)
    }

    @Test
    fun `getGameById with invalid id returns failure`() = runTest {
        val result = repo.getGameById(99999)
        assertTrue(result.isFailure)
    }

    @Test
    fun `searchGames by name`() = runTest {
        val result = repo.searchGames("Elden")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.any { it.name.contains("Elden") })
    }

    @Test
    fun `searchGames by tag`() = runTest {
        val result = repo.searchGames("RPG")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun `searchGames with no match returns empty`() = runTest {
        val result = repo.searchGames("ZZZZZZZ_NONEXISTENT")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getGamesByTag returns filtered games`() = runTest {
        val result = repo.getGamesByTag("Action")
        assertTrue(result.isSuccess)
        result.getOrNull()!!.forEach { assertTrue(it.tags.contains("Action")) }
    }

    @Test
    fun `getGamesByTag with no matching tag returns empty`() = runTest {
        val result = repo.getGamesByTag("NonExistentTag")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `addToFavorites and getFavorites`() = runTest {
        val game = repo.getAllGames().getOrNull()!!.first()
        repo.addToFavorites(game)
        val favs = repo.getFavorites()
        assertTrue(favs.isSuccess)
        assertTrue(favs.getOrNull()!!.any { it.id == game.id })
        assertTrue(favs.getOrNull()!!.all { it.isFavorite })
    }

    @Test
    fun `removeFromFavorites works`() = runTest {
        val game = repo.getAllGames().getOrNull()!!.first()
        repo.addToFavorites(game)
        repo.removeFromFavorites(game.id)
        val favs = repo.getFavorites()
        assertFalse(favs.getOrNull()!!.any { it.id == game.id })
    }

    @Test
    fun `isFavorite returns correct state`() = runTest {
        assertFalse(repo.isFavorite(1).getOrNull()!!)
        val game = repo.getAllGames().getOrNull()!!.first()
        repo.addToFavorites(game)
        assertTrue(repo.isFavorite(1).getOrNull()!!)
    }

    @Test
    fun `getAllGames reflects favorite state`() = runTest {
        val game = repo.getAllGames().getOrNull()!!.first()
        repo.addToFavorites(game)
        val allGames = repo.getAllGames().getOrNull()!!
        assertTrue(allGames.first { it.id == game.id }.isFavorite)
    }

    @Test
    fun `getGameById reflects favorite state`() = runTest {
        val game = repo.getAllGames().getOrNull()!!.first()
        repo.addToFavorites(game)
        assertTrue(repo.getGameById(game.id).getOrNull()!!.isFavorite)
    }

    @Test
    fun `getPriceHistory returns data`() = runTest {
        val result = repo.getPriceHistory(1)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isNotEmpty())
    }

    @Test
    fun `getPriceHistory for non-tracked game returns empty`() = runTest {
        val result = repo.getPriceHistory(99999)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }
}

