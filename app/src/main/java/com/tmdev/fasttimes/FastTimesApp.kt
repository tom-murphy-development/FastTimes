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
package com.tmdev.fasttimes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main [Application] class for the FastTimes app.
 *
 * This class is annotated with [@HiltAndroidApp] to trigger Hilt's code generation,
 * which creates a dependency injection container that is attached to the application's lifecycle.
 */
@HiltAndroidApp
class FastTimesApp : Application()
