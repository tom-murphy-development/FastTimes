package com.fasttimes

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
