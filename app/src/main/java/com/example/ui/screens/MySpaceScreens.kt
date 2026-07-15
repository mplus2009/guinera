package com.example.ui.screens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import coil.imageLoader

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.BusinessSpace
import com.example.data.SpaceProduct
import com.example.ui.AppViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MySpaceMapScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onCreateSpace: () -> Unit,
    onManageBusinesses: () -> Unit,
    onSpaceSelected: (String) -> Unit,
    onOpenBusinessChats: () -> Unit = {}
) {
    val context = LocalContext.current
    val spaces by viewModel.businessSpaces.collectAsState()
    
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
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

        if (mapView != null && locationPermissionsState.allPermissionsGranted && hasFinePermission && hasCoarsePermission && isMyLocationEnabled && isLocationEnabled) {
            val provider = GpsMyLocationProvider(context)
            provider.addLocationSource(android.location.LocationManager.NETWORK_PROVIDER)
            provider.addLocationSource(android.location.LocationManager.GPS_PROVIDER)
            overlay = MyLocationNewOverlay(provider, mapView).apply {
                enableMyLocation()
                enableFollowLocation()
            }
            mapView.overlays.add(overlay)
            myLocationOverlay = overlay
        } else if (!isLocationEnabled && isMyLocationEnabled) {
             android.widget.Toast.makeText(context, "Por favor, activa el GPS del dispositivo", android.widget.Toast.LENGTH_SHORT).show()
        }

        onDispose {
            overlay?.disableMyLocation()
            overlay?.disableFollowLocation()
            if (overlay != null && mapView != null) {
                mapView.overlays.remove(overlay)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Espacio - Mapa de Cuba") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenBusinessChats) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Chat, contentDescription = "Chats de Negocios")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FloatingActionButton(
                    onClick = onManageBusinesses,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Filled.Store, "Mis Negocios")
                }
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
                                mapController?.animateTo(location)
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
                ExtendedFloatingActionButton(
                    onClick = onCreateSpace,
                    icon = { Icon(Icons.Filled.Add, "Crear Espacio") },
                    text = { Text("Crear mi Espacio") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    // Restrict map strictly to Cuba
                    val cubaBoundingBox = org.osmdroid.util.BoundingBox(23.3, -74.1, 19.8, -85.0)
                    setScrollableAreaLimitDouble(cubaBoundingBox)
                    minZoomLevel = 7.0
                    maxZoomLevel = 18.0
                    
                    mapController = controller
                    controller.setZoom(7.0)
                    controller.setCenter(GeoPoint(21.5, -79.5)) // Center of Cuba

                    spaces.forEach { space ->
                        val marker = Marker(this).apply {
                            position = GeoPoint(space.latitude, space.longitude)
                            title = space.brandName
                            snippet = space.description
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                onSpaceSelected(space.id)
                                true
                            }
                        }
                        overlays.add(marker)

                        val req = coil.request.ImageRequest.Builder(context)
                            .data(space.logoUri.ifEmpty { android.R.drawable.ic_menu_gallery })
                            .size(80)
                            .transformations(coil.transform.CircleCropTransformation())
                            .target { logo ->
                                val pinBmp = android.graphics.Bitmap.createBitmap(120, 160, android.graphics.Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(pinBmp)
                                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                                
                                paint.color = android.graphics.Color.WHITE
                                val path = android.graphics.Path()
                                path.moveTo(60f, 160f)
                                path.cubicTo(60f, 160f, 0f, 90f, 0f, 60f)
                                path.arcTo(android.graphics.RectF(0f, 0f, 120f, 120f), 180f, 180f, false)
                                path.cubicTo(120f, 90f, 60f, 160f, 60f, 160f)
                                path.close()
                                
                                paint.style = android.graphics.Paint.Style.FILL
                                canvas.drawPath(path, paint)
                                
                                paint.color = android.graphics.Color.BLACK
                                paint.style = android.graphics.Paint.Style.STROKE
                                paint.strokeWidth = 6f
                                canvas.drawPath(path, paint)
                                
                                if (logo is android.graphics.drawable.BitmapDrawable) {
                                    val logoBmp = android.graphics.Bitmap.createScaledBitmap(logo.bitmap, 90, 90, true)
                                    canvas.drawBitmap(logoBmp, 15f, 15f, null)
                                }
                                
                                marker.icon = android.graphics.drawable.BitmapDrawable(context.resources, pinBmp)
                                this.invalidate()
                            }
                            .build()
                        context.imageLoader.enqueue(req)
                    }
                    mapViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBusinessScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var brandName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        logoUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear mi Espacio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Completa los datos de tu negocio.", style = MaterialTheme.typography.bodyLarge)
            }
            
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (logoUri != null) {
                        AsyncImage(
                            model = logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text("Añadir Logo (Opcional)")
                        }
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Nombre de la marca") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / ¿De qué trata?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Teléfono de contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            item {
                Text("Ubicación del Negocio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Toca el mapa o usa tu ubicación actual para establecer el punto.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                LocationPickerMap(
                    selectedLocation = selectedLocation,
                    onLocationSelected = { selectedLocation = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            item {
                Button(
                    onClick = {
                        if (brandName.isNotBlank() && selectedLocation != null) {
                            isLoading = true
                            viewModel.addBusinessSpace(
                                brandName, 
                                description, 
                                phoneNumber, 
                                selectedLocation!!.latitude, 
                                selectedLocation!!.longitude,
                                logoUri,
                                null
                            ) {
                                isLoading = false
                                onBack()
                            }
                        } else if (selectedLocation == null) {
                            android.widget.Toast.makeText(context, "Por favor, selecciona una ubicación en el mapa", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = !isLoading && brandName.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Crear Espacio")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditBusiness: () -> Unit,
    onEditProduct: (String) -> Unit,
    onContactOwner: (BusinessSpace) -> Unit = {}
) {
    val spaces by viewModel.businessSpaces.collectAsState()
    val space = spaces.find { it.id == spaceId }
    val productsFlow = remember(spaceId) { viewModel.getSpaceProducts(spaceId) }
    val products by productsFlow.collectAsState(initial = emptyList())
    val currentUserId = viewModel.currentUserId
    var isGridView by remember { mutableStateOf(false) }

    if (space == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Espacio no encontrado")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(space.brandName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (space.ownerId == currentUserId) {
                ExtendedFloatingActionButton(
                    onClick = onAddProduct,
                    icon = { Icon(Icons.Filled.Add, "Añadir Producto") },
                    text = { Text("Añadir Producto") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (space.bannerUri.isNotEmpty()) {
                        AsyncImage(
                            model = space.bannerUri,
                            contentDescription = "Banner",
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        if (space.logoUri.isNotEmpty()) {
                            AsyncImage(
                                model = space.logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(64.dp))
                            }
                        }
                    }
                }
                
                val context = androidx.compose.ui.platform.LocalContext.current
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(space.brandName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, "la.g./space/${space.id}")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir negocio"))
                        }) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "Compartir Negocio", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (space.ownerId == currentUserId) {
                        OutlinedButton(onClick = onEditBusiness) {
                            Text("Editar Negocio")
                        }
                    } else {
                        Button(onClick = { onContactOwner(space) }) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Contactar al Dueño")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Sobre el negocio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(space.description, style = MaterialTheme.typography.bodyLarge)
                        if (space.phoneNumber.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(Modifier.width(8.dp))
                                    Text(space.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Catálogo de Productos", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView, 
                            contentDescription = "Alternar vista"
                        )
                    }
                }
            }

            if (isGridView) {
                val chunks = products.chunked(2)
                items(chunks) { rowProducts ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (product in rowProducts) {
                            ProductGridItem(
                                product = product, 
                                modifier = Modifier.weight(1f),
                                onEdit = if (space.ownerId == currentUserId) { { onEditProduct(product.id) } } else null
                            )
                        }
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(products) { product ->
                    ProductListItem(
                        product = product,
                        onEdit = if (space.ownerId == currentUserId) { { onEditProduct(product.id) } } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpaceProductScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("CUP") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        imageUris = imageUris + uris
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del producto") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = currency,
                    onValueChange = { currency = it },
                    label = { Text("Moneda") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Seleccionar Imágenes")
            }
            
            if (imageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        isUploading = true
                        viewModel.addSpaceProduct(spaceId, name, description, price, currency, imageUris) {
                            isUploading = false
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading && name.isNotBlank()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Publicar Producto")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageBusinessesScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onBusinessSelected: (String) -> Unit
) {
    val currentUserId = viewModel.currentUserId
    val spaces by viewModel.businessSpaces.collectAsState()
    val mySpaces = spaces.filter { it.ownerId == currentUserId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Negocios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (mySpaces.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No has creado ningún negocio aún.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(mySpaces) { space ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBusinessSelected(space.id) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (space.logoUri.isNotEmpty()) {
                                AsyncImage(
                                    model = space.logoUri,
                                    contentDescription = "Logo de ${space.brandName}",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                            }
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(space.brandName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (space.description.isNotBlank()) {
                                    Text(
                                        text = space.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBusinessScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val spaces by viewModel.businessSpaces.collectAsState()
    val space = spaces.find { it.id == spaceId }
    val context = LocalContext.current
    
    if (space == null) return

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var bannerUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> logoUri = uri }

    val bannerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> bannerUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Negocio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Banner y Logo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    // Banner
                    if (bannerUri != null || space.bannerUri.isNotEmpty()) {
                        AsyncImage(
                            model = bannerUri ?: space.bannerUri,
                            contentDescription = "Banner",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clickable { bannerPickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { bannerPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Añadir Banner", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                    
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        if (logoUri != null || space.logoUri.isNotEmpty()) {
                            AsyncImage(
                                model = logoUri ?: space.logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .clickable { logoPickerLauncher.launch("image/*") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { logoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Logo", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.updateBusinessSpace(space, logoUri, bannerUri) {
                            isLoading = false
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSpaceProductScreen(
    productId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var product by remember { mutableStateOf<com.example.data.SpaceProduct?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("CUP") }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    LaunchedEffect(productId) {
        val p = viewModel.getSpaceProduct(productId)
        if (p != null) {
            product = p
            name = p.name
            description = p.description
            price = p.price.toString()
            currency = p.currency
        }
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteSpaceProduct(productId)
                        onBack()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar Producto", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Precio") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text("Moneda") },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        val priceValue = price.toDoubleOrNull()
                        if (name.isNotBlank() && priceValue != null) {
                            isLoading = true
                            val updatedProduct = product!!.copy(
                                name = name,
                                description = description,
                                price = priceValue,
                                currency = currency
                            )
                            viewModel.updateSpaceProduct(updatedProduct) {
                                isLoading = false
                                onBack()
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Verifica los datos", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && name.isNotBlank() && price.toDoubleOrNull() != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}
