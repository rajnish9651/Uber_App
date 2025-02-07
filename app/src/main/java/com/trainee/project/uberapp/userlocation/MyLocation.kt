package com.trainee.project.uberapp
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.transition.Visibility.Mode
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MyLocation(private val mainContext: MainActivity) {

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainContext)

    // function to get current location of the device
    fun getCurrentLocation() {

        if (isLocationEnabled()) {

            if (checkPermissions()) {

                fusedLocationClient.lastLocation.addOnCompleteListener{ task ->
                    val location: Location? = task.result
                    location?.let {
                        // if location is not null
                        Log.d("cityy", it.latitude.toString())
                        Log.d("cityy", it.longitude.toString())

                        // get city name from latitude and longitude
                        val city = getCityName(it.latitude, it.longitude)
                        city?.let { cityName ->
                            Log.d("cityy", cityName)
//                            viewModel.getWeatherViewModel(cityName)
                            val sharedPreferences =
                                mainContext.getSharedPreferences("address", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putString("location",cityName).apply()
                        }
                    } ?: run {
                        // if location is null
                        Toast.makeText(mainContext, "Null location received", Toast.LENGTH_SHORT).show()

                    }
                }
            }
            else {

                // request permissions if not granted
                requestPermissions()
            }
        } else {
            // request user to enable location
            enableLocation()
        }

    }

    // function to get city name from latitude and longitude
    private fun getCityName(lat: Double, long: Double): String? {
        val geoCoder = Geocoder(mainContext, Locale.getDefault())
        val address = geoCoder.getFromLocation(lat, long, 1)
//        return address?.get(0)?.locality ?: address?.get(0)?.subAdminArea
        return  address?.get(0)?.getAddressLine(0) ?: address?.get(0)?.subAdminArea
    }

    // function to check if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(mainContext, LocationManager::class.java) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // function to request permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            mainContext,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    // function to check if permissions are granted
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Handling permission requests
    fun onRequestPermissionsResultMyLocation(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mainContext, "Permission granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                Toast.makeText(mainContext, "Permission denied", Toast.LENGTH_SHORT).show()
                permissionDeniedDialogBox()
            }
        }
    }

    private  fun permissionDeniedDialogBox(){
        val builder=AlertDialog.Builder(mainContext)
        builder.setMessage("permission is required for this app, please enable it in the app settings")
        builder.setTitle("Location permission denied")
        builder.setPositiveButton("open setting"){dialog, which->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", mainContext.packageName, null)
            intent.data = uri
            mainContext.startActivity(intent)
        }
        builder.setNegativeButton("Cancel"){dialog,which->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // enable location s
    private fun enableLocation() {
        val builder = AlertDialog.Builder(mainContext)
        builder.setMessage("To continue, enable location services for better accuracy.")
            .setTitle("Location Disabled")
//            .setIcon(R.drawable.location)
            .setPositiveButton("OK") { dialog, which ->
                mainContext.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("CANCEL") { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100//define the unique code for permission request
    }
}
