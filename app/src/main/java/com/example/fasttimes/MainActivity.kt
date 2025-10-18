package com.example.fasttimes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple launcher Activity so the app has a MAIN/LAUNCHER entrypoint.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a simple built-in layout if the project doesn't provide one.
        setContentView(android.R.layout.simple_list_item_1)
    }
}
