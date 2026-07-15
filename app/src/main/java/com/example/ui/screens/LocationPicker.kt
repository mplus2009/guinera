package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerMap(
    selectedLocation: GeoPoint?,
    onLocationSelected: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    DisposableEffect(mapViewRef, locationPermissionsState.allPermissionsGranted) {
        val mapView = mapViewRef
        var overlay: MyLocationNewOverlay? = null
        
        val hasFinePermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val hasCoarsePermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (mapView != null && locationPermissionsState.allPermissionsGranted && hasFinePermission && hasCoarsePermission) {
            val provider = GpsMyLocationProvider(context)
            provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER)
            provider.addLocationSource(android.location.LocationManager.GPS_PROVIDER)
            overlay = MyLocationNewOverlay(provider, mapView).apply {
                enableMyLocation()
            }
            mapView.overlays.add(overlay)
            myLocationOverlay = overlay
        }

        onDispose {
            overlay?.disableMyLocation()
            if (overlay != null && mapView != null) {
                mapView.overlays.remove(overlay)
            }
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    val cubaBoundingBox = org.osmdroid.util.BoundingBox(23.3, -74.1, 19.8, -85.0)
                    setScrollableAreaLimitDouble(cubaBoundingBox)
                    minZoomLevel = 7.0
                    maxZoomLevel = 18.0
                    
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(23.1136, -82.3666)) // Default to Havana

                    val mReceive = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onLocationSelected(p)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean {
                            return false
                        }
                    }
                    overlays.add(MapEventsOverlay(mReceive))
                    mapViewRef = this
                }
            },
            update = { mapView ->
                // Clear previous markers (except my location and events)
                mapView.overlays.removeAll { it is Marker }
                
                selectedLocation?.let { loc ->
                    val marker = Marker(mapView).apply {
                        position = loc
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Ubicación seleccionada"
                    }
                    mapView.overlays.add(marker)
                    mapView.controller.animateTo(loc)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = {
                val hasFinePermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                val hasCoarsePermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (locationPermissionsState.allPermissionsGranted && hasFinePermission && hasCoarsePermission) {
                    myLocationOverlay?.myLocation?.let { loc ->
                        onLocationSelected(loc)
                        mapViewRef?.controller?.animateTo(loc)
                    } ?: run {
                        android.widget.Toast.makeText(context, "Buscando ubicación...", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(Icons.Filled.MyLocation, "Usar Mi Ubicación")
        }
    }
}
