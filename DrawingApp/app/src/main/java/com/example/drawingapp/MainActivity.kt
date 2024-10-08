package com.example.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.drawingapp.data.AppDatabase
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.viewmodel.DrawingViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var repository: DrawingRepository
    private lateinit var viewModelFactory: DrawingViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Repository and ViewModelFactory
        val db = AppDatabase.getDatabase(this)
        repository = DrawingRepository(db.drawingDao())
        viewModelFactory = DrawingViewModelFactory(repository)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
