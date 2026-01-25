/*
 * Copyright (C) 2025 tom-murphy-development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fasttimes.features

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Play Store Implementation: Features are locked until verified via Billing.
 */
@Singleton
class FeatureManagerImpl @Inject constructor() : FeatureManager {
    // These would be updated by your Billing logic later
    private val _isPremiumThemesUnlocked = MutableStateFlow(false)
    private val _isAdvancedStatsUnlocked = MutableStateFlow(false)

    override val isPremiumThemesUnlocked: StateFlow<Boolean> = _isPremiumThemesUnlocked.asStateFlow()
    override val isAdvancedStatsUnlocked: StateFlow<Boolean> = _isAdvancedStatsUnlocked.asStateFlow()
}
