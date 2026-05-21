package com.example.desaappsavaloskoortuzarvargas.di

import com.example.desaappsavaloskoortuzarvargas.data.api.CheapSharkService
import com.example.desaappsavaloskoortuzarvargas.data.api.DolarService
import com.example.desaappsavaloskoortuzarvargas.data.api.SteamPriceService
import com.example.desaappsavaloskoortuzarvargas.data.local.ConnectivityObserver
import com.example.desaappsavaloskoortuzarvargas.data.local.GameTrackerDatabase
import com.example.desaappsavaloskoortuzarvargas.data.repository.DiscountRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.GameRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.NewsRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.UserSettingsRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository
import com.example.desaappsavaloskoortuzarvargas.GameTrackerApp

object ServiceLocator {
    val cheapSharkService: CheapSharkService by lazy { CheapSharkService() }
    val steamPriceService: SteamPriceService by lazy { SteamPriceService() }
    val dolarService: DolarService by lazy { DolarService() }

    val database: GameTrackerDatabase by lazy {
        GameTrackerDatabase.getInstance(GameTrackerApp.appContext)
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        ConnectivityObserver(GameTrackerApp.appContext)
    }

    val gameRepository: GameRepository by lazy { GameRepositoryImpl() }
    val newsRepository: NewsRepository by lazy { NewsRepositoryImpl() }
    val discountRepository: DiscountRepository by lazy { DiscountRepositoryImpl() }
    val userSettingsRepository: UserSettingsRepository by lazy {
        try {
            GameTrackerApp.appContext
        } catch (e: UninitializedPropertyAccessException) {
            error(
                "ServiceLocator.userSettingsRepository accessed before Application.onCreate() has run. " +
                    "Ensure GameTrackerApp is initialized before accessing this repository."
            )
        }
        UserSettingsRepositoryImpl(GameTrackerApp.appContext)
    }
}
