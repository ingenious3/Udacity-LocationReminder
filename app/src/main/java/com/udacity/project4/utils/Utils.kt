package com.udacity.project4.utils

import android.content.Context
import android.location.Geocoder
import java.lang.Exception
import java.util.*

fun getAddress(context: Context, latitude: Double, longitude: Double): String {
    try {
        val geoCoder = Geocoder(context, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(latitude, longitude, 1)
        return addresses[0].getAddressLine(0) ?: "Unknown address"
    } catch (e: Exception) {
        return "Unknown address"
    }
}

