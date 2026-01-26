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
package com.tmdev.fasttimes.data

import android.content.Context
import android.net.Uri
import com.tmdev.fasttimes.data.fast.Fast
import com.tmdev.fasttimes.data.fast.FastsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DataManagementUseCase
@Inject
constructor(
    private val fastsRepository: FastsRepository,
    @ApplicationContext private val context: Context,
) {

  /**
   * Exports the user's fasting history to a JSON file at the given URI.
   *
   * @param uri The destination URI for the export, obtained from the Storage Access Framework.
   * @return A Result object indicating success or failure.
   */
  suspend fun export(uri: Uri): Result<Unit> =
      withContext(Dispatchers.IO) {
        runCatching {
          val fasts = fastsRepository.getFasts().first()
          val jsonString = Json.encodeToString(fasts)
          context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray())
          }
              ?: throw Exception("Could not open output stream")
        }
      }

  /**
   * Imports a fasting history from a JSON file at the given URI. This is a destructive operation
   * and will replace all existing data.
   *
   * @param uri The URI of the file to import, obtained from the Storage Access Framework.
   * @return A Result object indicating success or failure.
   */
  suspend fun import(uri: Uri): Result<Unit> =
      withContext(Dispatchers.IO) {
        runCatching {
          val jsonString =
              context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
              }
                  ?: throw Exception("Could not open input stream")
          val fasts = Json.decodeFromString<List<Fast>>(jsonString)
          fastsRepository.replaceAll(fasts)
        }
      }
}