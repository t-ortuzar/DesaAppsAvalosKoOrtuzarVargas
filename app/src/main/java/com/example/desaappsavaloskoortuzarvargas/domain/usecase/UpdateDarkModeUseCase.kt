package com.example.desaappsavaloskoortuzarvargas.domain.usecase

import com.example.desaappsavaloskoortuzarvargas.domain.repository.UserSettingsRepository

class UpdateDarkModeUseCase(private val repository: UserSettingsRepository) {
    suspend operator fun invoke(isDark: Boolean) = repository.updateDarkMode(isDark)
}

