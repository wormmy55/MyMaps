package com.jonathan.mymaps

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.jonathan.mymaps.ui.theme.MyMapsTheme
import com.jonathan.mymaps.workers.MapsSyncWorker

@Suppress("OVERRIDE_DEPRECATION")
class MainActivity : ComponentActivity() {
    // Default location
    private val defaultLocation = LatLng(43.6532, -79.3832) // Toronto as fallback
    // Location permission request code
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1
    // Bundle key for saving map view state
    private val mapViewBundleKey = "MapViewBundleKey"
    // Initialize FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // Initialize LocationCallback
    private lateinit var locationCallback: LocationCallback
    // Initialize GeofencingClient
    private lateinit var geofencingClient: GeofencingClient
    // Shared state for user location
    private var userLocationState = mutableStateOf(defaultLocation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Register the permission launcher
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation { location ->
                    // Update the location state from the result
                    userLocationState.value = location
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        startMapSync()
        setContent {
            var locationPermissionGranted by remember { mutableStateOf(false) }
            var mapViewBundle by remember { mutableStateOf(Bundle()) }

            if(savedInstanceState != null){
                mapViewBundle = savedInstanceState.getBundle(mapViewBundleKey) ?: Bundle()
            }

            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            MapScreen(userLocationState.value, locationPermissionGranted)
        }

        // Initialize LocationCallback
        locationCallback = object: LocationCallback(){
            override fun onLocationResult(locationresult: LocationResult){
                locationresult.locations.forEach{location ->
                    //Update UI with the location data
                    val latLng = LatLng(location.latitude, location.longitude)
                    //Update map or UI
                }
            }
        }

        // Initialize GeofencingClient
        geofencingClient = LocationServices.getGeofencingClient(this)
        // Create a Geofence
        val geofence = Geofence.Builder()
            .setRequestId("MyGeofence")
            .setCircularRegion(43.6532, -79.3832, 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
        // Create a GeofencingRequest
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        // Create a PendingIntent to handle geofence transitions
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(this,GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        // Add geofence to the GeofencingClient
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            return
        }
        // Add geofences to the GeofencingClient
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run{
            addOnSuccessListener{
                //Geofences added
            }
            addOnFailureListener{
                //Failed to add geofences
            }
        }
        // Start location updates
        startLocationUpdates()
    }
    // Fetch current location using FusedLocationProviderClient
    private fun fetchCurrentLocation(onLocationFetched: (LatLng) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(latLng)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Check location permission
    private fun checkLocationPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)){
                //Show rationale
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("this app requires location access to function properly.")
                    .setPositiveButton("Ok"){_,_->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE
                        )
                    }
                    .create()
                    .show()
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun getLocation(){
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Check location permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        // Get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener{location ->
            location?.let {
                // Use location object
                val latLng = LatLng(it.latitude, it.longitude)
                //Update map or UI with the location
            }
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if((grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED)){
                //Permission granted
                getLocation()
            }else{
                //Permission denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Start location updates
    private fun startLocationUpdates(){
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .apply{
                setWaitForAccurateLocation(true)
                setMinUpdateIntervalMillis(5000)
                setMaxUpdateDelayMillis(10000)
        }.build()
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED){
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    // Stop location updates
    private fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
    override fun onPause(){
        super.onPause()
        stopLocationUpdates()
    }
    override fun onResume(){
        super.onResume()
        startLocationUpdates()
    }

    //Broadcast receiver for geofence events
    class GeofenceBroadcastReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context, intent:Intent){
            GeofencingEvent.fromIntent(intent)?.let{ geofencingEvent ->
                if(geofencingEvent.hasError()){
                    //Handle error
                    return
                }
                //Get the geofence transition type
                val geofenceTransition = geofencingEvent.geofenceTransition
                if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    val triggeringGeofences = geofencingEvent.triggeringGeofences
                    //Handle geofence transitions
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle){
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(mapViewBundleKey) ?: Bundle()
        //Save the map state
        outState.putBundle(mapViewBundleKey, mapViewBundle)
    }

    private fun startMapSync(){
        val syncMapWorkRequest = OneTimeWorkRequestBuilder<MapsSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "MapSyncWorker", ExistingWorkPolicy.KEEP, syncMapWorkRequest
        )
    }
}

//@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(userLocation: LatLng, isLocationEnabled: Boolean) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 14f)
    }
    // Animate the camera to the user's location
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationEnabled,
            isIndoorEnabled = true
        )
    ) {
        // Add a marker at the user's location
        Marker(
            state = MarkerState(position = userLocation),
            title = "You are here",
            snippet = "Current Location"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapPreview() {
    MyMapsTheme {
        MapScreen(LatLng(43.6532, -79.3832), true)
    }
}