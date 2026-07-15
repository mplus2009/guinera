import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

marker_code = """
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
                        coil.imageLoader(context).enqueue(req)
"""

old_marker_code = """
                        val marker = Marker(this).apply {
                            position = GeoPoint(space.latitude, space.longitude)
                            title = space.brandName
                            snippet = space.description
                            icon = androidx.core.content.ContextCompat.getDrawable(context, android.R.drawable.ic_menu_myplaces)
                            setOnMarkerClickListener { _, _ ->
                                onSpaceSelected(space.id)
                                true
                            }
                        }
                        overlays.add(marker)
"""

content = content.replace(old_marker_code.strip(), marker_code.strip())

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)

