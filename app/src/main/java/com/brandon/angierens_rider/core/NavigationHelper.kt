package com.brandon.angierens_rider.core

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

object NavigationHelper {

    /**
     * Opens Google Maps with navigation to the specified destination
     */
    fun navigateWithGoogleMaps(
        context: Context,
        destinationLat: Double,
        destinationLng: Double,
        destinationLabel: String = "Delivery Location"
    ) {
        try {
            // Create URI for Google Maps navigation
            val uri = "google.navigation:q=$destinationLat,$destinationLng&mode=d".toUri()

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }

            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Google Maps not installed, open in browser
            openInBrowser(context, destinationLat, destinationLng)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open navigation",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Opens Waze with navigation to the specified destination
     */
    fun navigateWithWaze(
        context: Context,
        destinationLat: Double,
        destinationLng: Double
    ) {
        try {
            val uri = "waze://?ll=$destinationLat,$destinationLng&navigate=yes".toUri()

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.waze")
            }

            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Waze not installed, open in browser or show message
            Toast.makeText(
                context,
                "Waze is not installed. Please install it from Play Store.",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open Waze",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Shows a chooser to let user pick their preferred navigation app
     */
    fun openNavigationChooser(
        context: Context,
        destinationLat: Double,
        destinationLng: Double,
        destinationLabel: String = "Delivery Location"
    ) {
        try {
            // Generic navigation intent that works with multiple apps
            val uri =
                "geo:$destinationLat,$destinationLng?q=$destinationLat,$destinationLng($destinationLabel)".toUri()

            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Create chooser
            val chooser = Intent.createChooser(intent, "Navigate with:")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No navigation apps available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Fallback: Opens Google Maps in browser
     */
    private fun openInBrowser(
        context: Context,
        destinationLat: Double,
        destinationLng: Double
    ) {
        try {
            val uri =
                "https://www.google.com/maps/dir/?api=1&destination=$destinationLat,$destinationLng".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to open navigation",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Checks if Google Maps is installed
     */
    fun isGoogleMapsInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.apps.maps", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if Waze is installed
     */
    fun isWazeInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.waze", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}