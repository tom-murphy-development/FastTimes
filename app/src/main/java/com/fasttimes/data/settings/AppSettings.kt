/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
