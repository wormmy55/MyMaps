package com.jonathan.mymaps.views

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.jonathan.mymaps.R

@Composable
fun CustomMap(){
    val Toronto = LatLng(43.6532, -79.3832)
    val cameraPositionState = rememberCameraPositionState{
        position = CameraPosition.fromLatLngZoom(Toronto, 10f)
    }
    val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_style)
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        //mapStyleOptions = mapStyleOptions
    ){
        Marker(
            state = MarkerState(position = Toronto),
            title = "Toronto",
            snippet = "This is a marker in Toronto."
        )
    }
}