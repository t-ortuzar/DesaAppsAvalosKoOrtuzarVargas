package com.example.desaappsavaloskoortuzarvargas.data.api

import org.junit.Assert.*
import org.junit.Test

class StoreRegionAvailabilityTest {

    @Test
    fun `global store is available everywhere`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Steam", "AR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Steam", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("GOG", "CL"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Epic Games", "BR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Humble Store", "ES"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Fanatical", "MX"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("GreenManGaming", "PE"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("IndieGala", "UY"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("DreamGame", "PY"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("GameBillet", "CO"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("2Game", "AR"))
    }

    @Test
    fun `region restricted store available in allowed region`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Origin (EA)", "AR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("GamersGate", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("GamersGate", "ES"))
    }

    @Test
    fun `region restricted store NOT available in disallowed region`() {
        assertFalse(StoreRegionAvailability.isAvailableInRegion("WinGameStore", "AR"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("WinGameStore", "ES"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("GamersGate", "AR"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("GamersGate", "BR"))
    }

    @Test
    fun `unknown store is available everywhere`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("UnknownStore", "AR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("NewStore", "XX"))
    }

    @Test
    fun `DLGamer regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("DLGamer", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("DLGamer", "BR"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("DLGamer", "AR"))
    }

    @Test
    fun `Gamesplanet regional availability`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Gamesplanet", "US"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Gamesplanet", "ES"))
        assertFalse(StoreRegionAvailability.isAvailableInRegion("Gamesplanet", "AR"))
    }

    @Test
    fun `Blizzard is available in all LATAM and Europe`() {
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Blizzard", "AR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Blizzard", "BR"))
        assertTrue(StoreRegionAvailability.isAvailableInRegion("Blizzard", "ES"))
    }
}

