package com.example.desaappsavaloskoortuzarvargas.di

import com.example.desaappsavaloskoortuzarvargas.data.api.BattleNetPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.DolarService
import com.example.desaappsavaloskoortuzarvargas.data.api.EAPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.EpicPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.GogPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.PriceRefreshManager
import com.example.desaappsavaloskoortuzarvargas.data.api.SteamNewsService
import com.example.desaappsavaloskoortuzarvargas.data.api.SteamPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.UbisoftPriceService
import com.example.desaappsavaloskoortuzarvargas.data.api.XboxPriceService
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
    val steamPriceService: SteamPriceService by lazy { SteamPriceService() }
    val steamNewsService: SteamNewsService by lazy { SteamNewsService() }
    val epicPriceService: EpicPriceService by lazy { EpicPriceService() }
    val gogPriceService: GogPriceService by lazy { GogPriceService() }
    val xboxPriceService: XboxPriceService by lazy { XboxPriceService() }
    val ubisoftPriceService: UbisoftPriceService by lazy { UbisoftPriceService() }
    val battleNetPriceService: BattleNetPriceService by lazy { BattleNetPriceService() }
    val eaPriceService: EAPriceService by lazy { EAPriceService() }
    val dolarService: DolarService by lazy { DolarService() }

    val priceRefreshManager: PriceRefreshManager by lazy {
        PriceRefreshManager(
            gamePriceDao = database.gamePriceDao(),
            priceHistoryDao = database.priceHistoryDao(),
            steamPriceService = steamPriceService,
            epicPriceService = epicPriceService,
            gogPriceService = gogPriceService,
            xboxPriceService = xboxPriceService,
            ubisoftPriceService = ubisoftPriceService,
            battleNetPriceService = battleNetPriceService,
            eaPriceService = eaPriceService,
            connectivityObserver = connectivityObserver
        )
    }

    val database: GameTrackerDatabase by lazy {
        GameTrackerDatabase.getInstance(GameTrackerApp.appContext)
    }

    val connectivityObserver: ConnectivityObserver by lazy {
        ConnectivityObserver(GameTrackerApp.appContext)
    }

    val gameRepository: GameRepository by lazy { GameRepositoryImpl() }
    val newsRepository: NewsRepository by lazy { NewsRepositoryImpl(steamNewsService) }
    val discountRepository: DiscountRepository by lazy {
        DiscountRepositoryImpl(
            gamePriceDao = database.gamePriceDao(),
            priceRefreshManager = priceRefreshManager,
            gameImageDao = database.gameImageDao()
        )
    }
    val userSettingsRepository: UserSettingsRepository by lazy {
        try {
            GameTrackerApp.appContext
        } catch (e: UninitializedPropertyAccessException) {
            error(
                "ServiceLocator.userSettingsRepository accessed before Application.onCreate() has run. " +
                    "Ensure GameTrackerApp is initialized before accessing this repository."
            )
        }
        UserSettingsRepositoryImpl(GameTrackerApp.appContext, discountRepository)
    }
}
