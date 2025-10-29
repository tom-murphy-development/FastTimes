package com.fasttimes.data

import android.content.Context
import android.net.Uri
import com.fasttimes.data.fast.Fast
import com.fasttimes.data.fast.FastsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
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