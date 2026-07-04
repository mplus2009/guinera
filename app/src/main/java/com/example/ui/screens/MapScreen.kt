package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import android.Manifest
import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: com.example.ui.AppViewModel,
    onLogout: () -> Unit,
    onBlockSelected: (String) -> Unit,
    onCartClicked: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
    onMyProductsClicked: () -> Unit = {},
    onDeveloperMode: () -> Unit = {},
    onChatClicked: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var showDevDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var devTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    val userSession by viewModel.userSession.collectAsState()
    
    if (showProfileDialog) {
        val session = userSession
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = if (session?.isAnonymous == false) "Tu Perfil de Google" else "Perfil Anónimo",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (session != null && !session.isAnonymous && session.avatarUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = session.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = session?.displayName ?: "Vecino de La Güinera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = session?.email ?: "Sin correo de Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (session != null && !session.isAnonymous) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verificado",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Sesión con Google Activa",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Tienes privilegios completos para comentar, publicar productos y votar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Advertencia",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Modo de Acceso Limitado",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "No podrás añadir nuevos productos, comentar en bloques ni participar en encuestas de decisiones del barrio.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (session != null && !session.isAnonymous) {
                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                    }
                } else {
                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        }
                    ) {
                        Text("Iniciar Sesión con Google")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
    
    if (showDevDialog) {
        var devUser by remember { mutableStateOf("") }
        var devPass by remember { mutableStateOf("") }
        var loginError by remember { mutableStateOf(false) }
        
        Dialog(
            onDismissRequest = { showDevDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    // Close button in upper right
                    IconButton(
                        onClick = { showDevDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Main column
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Welcome
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        // Developer
                        Text(
                            text = "Developer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Inputs card/container
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            tonalElevation = 4.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = devUser,
                                    onValueChange = { 
                                        devUser = it
                                        loginError = false
                                    },
                                    label = { Text("Usuario / Username") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = devPass,
                                    onValueChange = { 
                                        devPass = it
                                        loginError = false
                                    },
                                    label = { Text("Contraseña / Password") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )

                                if (loginError) {
                                    Text(
                                        text = "Credenciales incorrectas / Incorrect credentials",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (devUser == "mplus2009" && devPass == "@Mplus2009") {
                                            showDevDialog = false
                                            onDeveloperMode()
                                        } else {
                                            loginError = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Acceder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val context = LocalContext.current
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Configure osmdroid
    remember {
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

    val centerLat = 23.0461
    val centerLon = -82.3583
    val scale = 0.001

    fun pt(x: Double, y: Double): GeoPoint {
        return GeoPoint(centerLat + y * scale, centerLon + x * scale)
    }

    val pA = pt(-1.0, 4.0)
    val pB = pt(2.0, 3.0)
    val pC = pt(1.5, 0.0)
    val pD = pt(0.0, 0.0)
    val pE = pt(-0.5, 2.0)
    val pF = pt(-2.0, -1.0)
    val pG = pt(-3.0, 2.5)
    val pH = pt(5.0, 4.0)
    val pI = pt(4.0, 7.0)
    val pJ = pt(-1.5, 7.0)
    val pK = pt(7.0, -1.0)
    val pL = pt(3.0, -3.0)
    val pM = pt(1.0, -1.0)
    val pN = pt(-3.0, -3.0)
    val pO = pt(1.5, -4.0)
    val pP = pt(1.0, -8.0)
    val pQ = pt(-4.0, -6.0)
    val pR = pt(-4.0, -1.0)
    val pS = pt(-4.5, 2.5)
    val pT = pt(-5.5, -2.0)
    val pU = pt(-6.5, 0.0)
    val pV = pt(-6.0, 2.5)

    val blocks = listOf(
        BlockData("Bloque 1", listOf(pA, pB, pC, pD, pE), pt(0.5, 2.0), Color.argb(100, 255, 109, 0)), // Warm Orange
        BlockData("Bloque 2", listOf(pA, pE, pD, pF, pG), pt(-1.5, 1.5), Color.argb(100, 0, 104, 118)), // Teal
        BlockData("Bloque 3", listOf(pJ, pA, pB, pH, pI), pt(1.5, 5.0), Color.argb(100, 206, 141, 37)), // Amber
        BlockData("Bloque 4", listOf(pB, pH, pK, pL, pM, pC), pt(3.5, 0.0), Color.argb(100, 159, 66, 0)), // Rust
        BlockData("Bloque 5", listOf(pF, pD, pC, pM, pL, pO, pN), pt(0.0, -2.0), Color.argb(100, 0, 150, 136)), // Light Teal
        BlockData("Bloque 6", listOf(pN, pO, pL, pP, pQ), pt(0.0, -5.0), Color.argb(100, 255, 167, 38)), // Light Orange
        BlockData("Bloque 7", listOf(pG, pF, pR, pS), pt(-3.5, 0.5), Color.argb(100, 121, 85, 72)), // Warm Brown
        BlockData("Bloque 8", listOf(pS, pR, pT, pU, pV), pt(-5.0, 0.0), Color.argb(100, 233, 30, 99)) // Accent Pink
    )

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var mapController by remember { mutableStateOf<org.osmdroid.api.IMapController?>(null) }
    var isMyLocationEnabled by remember { mutableStateOf(false) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    DisposableEffect(mapViewRef, locationPermissionsState.allPermissionsGranted, isMyLocationEnabled) {
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
        
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
        val isLocationEnabled = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            locationManager?.isLocationEnabled == true
        } else {
            locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
            locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
        }
        
        if (mapView != null && isMyLocationEnabled && locationPermissionsState.allPermissionsGranted && hasFinePermission && hasCoarsePermission && isLocationEnabled) {
            try {
                val provider = GpsMyLocationProvider(context)
                val o = MyLocationNewOverlay(provider, mapView).apply {
                    enableMyLocation()
                }
                overlay = o
                myLocationOverlay = o
                mapView.overlays.add(o)
                mapView.invalidate()
            } catch (e: SecurityException) {
                android.util.Log.e("MapScreen", "SecurityException starting location provider", e)
            } catch (e: Exception) {
                android.util.Log.e("MapScreen", "Error starting location provider", e)
            }
        }
        
        onDispose {
            try {
                overlay?.disableMyLocation()
            } catch (e: Exception) {
                android.util.Log.e("MapScreen", "Error disabling location provider", e)
            }
            if (mapView != null) {
                if (overlay != null) {
                    mapView.overlays.remove(overlay)
                }
                mapView.onDetach()
            }
            myLocationOverlay = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "La Güinera", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime > 3000) {
                                devTapCount = 1
                            } else {
                                devTapCount += 1
                            }
                            lastTapTime = now
                            if (devTapCount >= 10) {
                                devTapCount = 0
                                showDevDialog = true
                            }
                        }
                    ) 
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .clip(CircleShape)
                            .clickable { showProfileDialog = true }
                    ) {
                        val session = userSession
                        if (session != null && !session.isAnonymous && session.avatarUrl.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = session.avatarUrl,
                                contentDescription = "Avatar de vecina",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Avatar de vecina",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onChatClicked, enabled = false) {
                        Icon(Icons.Filled.Chat, contentDescription = "Chats P2P (Deshabilitado)", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
                    }
                    IconButton(onClick = onSearchClicked) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onMyProductsClicked) {
                        Icon(Icons.Filled.Store, contentDescription = "Mis Productos", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onCartClicked) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
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
                        isMyLocationEnabled = true
                        myLocationOverlay?.myLocation?.let { location ->
                            val mapBounds = org.osmdroid.util.BoundingBox(23.0541, -82.3503, 23.0371, -82.3658)
                            if (mapBounds.contains(location)) {
                                mapController?.animateTo(location)
                            } else {
                                android.widget.Toast.makeText(context, "Tu ubicación está fuera del barrio de La Güinera", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } ?: run {
                            android.widget.Toast.makeText(context, "Buscando ubicación...", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Filled.MyLocation, "Mi Ubicación")
            }
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    mapController = controller
                    controller.setZoom(16.0)
                    controller.setCenter(GeoPoint(23.0461, -82.3583)) // Center on La Güinera
                    
                    val mapBounds = org.osmdroid.util.BoundingBox(23.0541, -82.3503, 23.0371, -82.3658)
                    setScrollableAreaLimitDouble(mapBounds)
                    minZoomLevel = 15.0
                    maxZoomLevel = 19.0

                    blocks.forEach { block ->
                        val marker = Marker(this).apply {
                            position = block.center
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = block.name
                            setOnMarkerClickListener { _, _ ->
                                onBlockSelected(block.name)
                                true
                            }
                        }
                        overlays.add(marker)
                        
                        val polygon = Polygon().apply {
                            points = block.bounds
                            fillPaint.color = block.color
                            
                            // Extract base color and create a darker/solid version for the outline
                            val a = android.graphics.Color.alpha(block.color)
                            val r = android.graphics.Color.red(block.color)
                            val g = android.graphics.Color.green(block.color)
                            val b = android.graphics.Color.blue(block.color)
                            
                            outlinePaint.color = Color.argb(255, (r * 0.8).toInt(), (g * 0.8).toInt(), (b * 0.8).toInt())
                            outlinePaint.strokeWidth = 5f
                            outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                        }
                        polygon.setOnClickListener { _, _, _ ->
                            onBlockSelected(block.name)
                            true
                        }
                        overlays.add(polygon)
                    }
                    mapViewRef = this
                }
            },
            update = { },
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

data class BlockData(val name: String, val bounds: List<GeoPoint>, val center: GeoPoint, val color: Int)
