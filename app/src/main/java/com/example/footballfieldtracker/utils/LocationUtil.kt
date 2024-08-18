package com.example.locationserviceexample.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.example.footballfieldtracker.data.model.LocationData
import com.google.android.gms.maps.model.LatLng
import java.util.Locale


fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

// Todo: mora je pozoves iz ui ; convert langitude and longitude into actuall address
fun reverseGeocodeLocation(context: Context, coordinate: LatLng) : String {
    val geocoder = Geocoder(context, Locale.getDefault())
    // it can be multiple addresses that fir to certain location, zato je stavlja u listu
    val addresses:MutableList<Address>? = geocoder.getFromLocation(coordinate.latitude, coordinate.longitude, 1)
    return if(addresses?.isNotEmpty() == true) {
        addresses[0].getAddressLine(0)
    } else {
        "Addresses not found"
    }

}