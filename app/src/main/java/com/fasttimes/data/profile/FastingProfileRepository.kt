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
package com.fasttimes.data.profile

import kotlinx.coroutines.flow.Flow

interface FastingProfileRepository {
    fun getProfiles(): Flow<List<FastingProfile>>
    suspend fun addProfile(profile: FastingProfile): Long
    suspend fun updateProfile(profile: FastingProfile)
    suspend fun deleteProfile(profile: FastingProfile)
    suspend fun setFavoriteProfile(profile: FastingProfile)
}
