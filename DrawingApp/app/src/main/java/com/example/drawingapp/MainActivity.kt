package com.example.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider

/**
 * MainActivity serves as the entry point of the application.
 * It is responsible for setting up the initial UI and managing fragments.
 */
class MainActivity : AppCompatActivity() {
    // ViewModel that manages the drawing state
    private lateinit var viewModel: DrawingViewModel

    /**
     * Called when the activity is first created.
     * This method initializes the ViewModel and sets up the initial fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this).get(DrawingViewModel::class.java)

        // Load the HomeFragment if this is the first creation of the activity
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
}