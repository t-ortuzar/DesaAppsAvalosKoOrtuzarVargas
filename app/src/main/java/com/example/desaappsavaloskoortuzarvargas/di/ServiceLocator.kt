package com.example.desaappsavaloskoortuzarvargas.di

import com.example.desaappsavaloskoortuzarvargas.data.repository.DiscountRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.GameRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.NewsRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.data.repository.UserSettingsRepositoryImpl
import com.example.desaappsavaloskoortuzarvargas.domain.repository.DiscountRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.GameRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.NewsRepository
import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository

object ServiceLocator {
    val gameRepository: GameRepository by lazy { GameRepositoryImpl() }
    val newsRepository: NewsRepository by lazy { NewsRepositoryImpl() }
    val discountRepository: DiscountRepository by lazy { DiscountRepositoryImpl() }
    val userSettingsRepository: UserSettingsRepository by lazy { UserSettingsRepositoryImpl() }
}
