package com.jonathan.mymaps.data

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.*
import androidx.compose.ui.*

@Composable
fun EventHandlingMap(){
    val Toronto = LatLng(43.6532, -79.3832)
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(Toronto, 10f)
    }
    var markerPosition by remember { mutableStateOf(Toronto) }
    val context = LocalContext.current

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            markerPosition = latLng
        },
        /*onMarkerClick = { marker ->
            Toast.makeText(context, "Marker clicked: ${marker.title}", Toast.LENGTH_SHORT)
                .show()
            true
        }*/
    ){
        Marker(
            state = MarkerState(position = markerPosition),
            title = "New Marker",
            snippet = "This is a dynamically added marker."
        )
    }
}