package com.example.drawingapp.ui

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.drawingapp.R
import com.example.drawingapp.data.AppDatabase
import com.example.drawingapp.data.Drawing
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.ui.components.HomeScreen
import com.example.drawingapp.viewmodel.DrawingViewModel
import com.example.drawingapp.viewmodel.DrawingViewModelFactory
import androidx.compose.runtime.livedata.observeAsState
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private val viewModel: DrawingViewModel by viewModels {
        val repository =
            DrawingRepository(AppDatabase.getDatabase(requireContext()).drawingDao())
        DrawingViewModelFactory(repository)
    }
    private val auth = FirebaseAuth.getInstance()
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNetworkCallback()
    }

    // Setup network callback to monitor connectivity changes
    private fun setupNetworkCallback() {
        connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.runOnUiThread {
                    viewModel.loadAllDrawings()
                    viewModel.loadSharedDrawings()
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val drawings by viewModel.allDrawings.observeAsState(initial = emptyList())
                    val sharedDrawings by viewModel.sharedDrawings.observeAsState(initial = emptyList())
                    val isLoading by viewModel.isLoading.observeAsState(initial = false)
                    val errorMessage by viewModel.errorEvent.observeAsState()

                    HomeScreen(
                        onStartDrawingClick = {
                            findNavController().navigate(R.id.action_homeFragment_to_drawingFragment)
                        },
                        drawings = drawings,
                        sharedDrawings = sharedDrawings,
                        onDrawingClick = { drawing ->
                            val bundle = Bundle().apply {
                                putInt("drawingId", drawing.id)
                            }
                            findNavController().navigate(
                                R.id.action_homeFragment_to_drawingFragment,
                                bundle
                            )
                        },
                        onDeleteClick = { drawing ->
                            showDeleteConfirmationDialog(drawing)
                        },
                        onShareClick = { drawing ->
                            showShareDialog(drawing)
                        },
                        onLogoutClick = {
                            showLogoutConfirmationDialog()
                        },
                        onRefresh = { // Refresh drawings and shared drawings
                            viewModel.loadAllDrawings()
                            viewModel.loadSharedDrawings()
                        },
                        isLoading = isLoading
                    )

                    // Show error messages as a toast
                    errorMessage?.getContentIfNotHandled()?.let { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Show confirmation dialog for sharing a drawing
    private fun showShareDialog(drawing: Drawing) {
        if (drawing.userId != auth.currentUser?.uid) {
            Toast.makeText(
                requireContext(),
                "You can only share your own drawings",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Share Drawing")
            .setMessage("Do you want to share this drawing with other users?")
            .setPositiveButton("Share") { _, _ ->
                viewModel.shareDrawing(drawing)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Show confirmation dialog for deleting a drawing
    private fun showDeleteConfirmationDialog(drawing: Drawing) {
        val isShared = drawing.isShared
        val isOwner = drawing.userId == auth.currentUser?.uid

        val message = when {
            isShared && isOwner -> "Are you sure you want to delete your shared drawing \"${drawing.name}\"?"
            isShared && !isOwner -> "You cannot delete someone else's shared drawing."
            else -> "Are you sure you want to delete \"${drawing.name}\"?"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Drawing")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                when {
                    isShared && isOwner -> {
                        // Delete shared drawing
                        viewModel.deleteSharedDrawing(drawing)
                    }
                    isShared && !isOwner -> {
                        // Cannot delete someone else's drawing
                        Toast.makeText(
                            requireContext(),
                            "You cannot delete someone else's drawing.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        // Delete local drawing
                        viewModel.deleteDrawing(drawing)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Show confirmation dialog for logging out
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            return
        }
        viewModel.loadAllDrawings()
        viewModel.loadSharedDrawings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
    }
}
