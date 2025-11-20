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
package com.fasttimes.data.settings

import java.time.DayOfWeek

enum class TimeFormat {
    TWELVE_HOUR,
    TWENTY_FOUR_HOUR
}

data class AppSettings(
    val defaultFastingProfileId: String = "16/8",
    val goalMetNotificationEnabled: Boolean = true,
    val milestoneNotificationsEnabled: Boolean = true,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY,
    val timeFormat: TimeFormat = TimeFormat.TWENTY_FOUR_HOUR,
    val showFab: Boolean = true
)
