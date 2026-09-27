package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SaffronGold

@Composable
fun CameraCaptureScreen(
    onPhotoCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var shutterPressed by remember { mutableStateOf(false) }

    val shutterScale by animateFloatAsState(
        targetValue = if (shutterPressed) 0.88f else 1f,
        label = "shutter_scale"
    )

    // Fallback Gallery Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onPhotoCaptured(bitmap)
            } catch (e: Exception) {
                Log.e("CameraCaptureScreen", "Gallery decode error", e)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // CameraX Live Viewfinder
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(previewView.display?.rotation ?: 0)
                        .build()

                    imageCapture = capture

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCaptureScreen", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                // Handle lens facing change or re-binding
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val capture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(previewView.display?.rotation ?: 0)
                        .build()
                    imageCapture = capture

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            capture
                        )
                        camera?.cameraControl?.enableTorch(isTorchOn)
                    } catch (e: Exception) {
                        Log.e("CameraCaptureScreen", "Camera update failed", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Document Guide Framing Box
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val guideWidth = canvasWidth * 0.88f
            val guideHeight = canvasHeight * 0.60f
            val guideLeft = (canvasWidth - guideWidth) / 2f
            val guideTop = (canvasHeight - guideHeight) / 2.3f

            // Shaded dark mask around the document box
            // Document boundary frame
            drawRoundRect(
                color = SaffronGold,
                topLeft = Offset(guideLeft, guideTop),
                size = Size(guideWidth, guideHeight),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), 0f)
                )
            )

            // High-visibility corner guide ticks
            val tickLength = 36.dp.toPx()
            val strokeWidth = 5.dp.toPx()

            // Top-Left corner
            drawLine(EmeraldGreen, Offset(guideLeft, guideTop), Offset(guideLeft + tickLength, guideTop), strokeWidth)
            drawLine(EmeraldGreen, Offset(guideLeft, guideTop), Offset(guideLeft, guideTop + tickLength), strokeWidth)

            // Top-Right corner
            drawLine(EmeraldGreen, Offset(guideLeft + guideWidth, guideTop), Offset(guideLeft + guideWidth - tickLength, guideTop), strokeWidth)
            drawLine(EmeraldGreen, Offset(guideLeft + guideWidth, guideTop), Offset(guideLeft + guideWidth, guideTop + tickLength), strokeWidth)

            // Bottom-Left corner
            drawLine(EmeraldGreen, Offset(guideLeft, guideTop + guideHeight), Offset(guideLeft + tickLength, guideTop + guideHeight), strokeWidth)
            drawLine(EmeraldGreen, Offset(guideLeft, guideTop + guideHeight), Offset(guideLeft, guideTop + guideHeight - tickLength), strokeWidth)

            // Bottom-Right corner
            drawLine(EmeraldGreen, Offset(guideLeft + guideWidth, guideTop + guideHeight), Offset(guideLeft + guideWidth - tickLength, guideTop + guideHeight), strokeWidth)
            drawLine(EmeraldGreen, Offset(guideLeft + guideWidth, guideTop + guideHeight), Offset(guideLeft + guideWidth, guideTop + guideHeight - tickLength), strokeWidth)
        }

        // Top Controls Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(Color(0x99000000))
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0x55000000), CircleShape)
                        .testTag("camera_close_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📸 उषा व कमल खाता स्कैनर",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "पुराना पर्चा / डायरी फ्रेम में रखें",
                        color = SaffronGold,
                        fontSize = 11.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Flash / Torch toggle
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            camera?.cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(if (isTorchOn) SaffronGold else Color(0x55000000), CircleShape)
                            .testTag("camera_flash_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) Color.Black else Color.White
                        )
                    }

                    // Flip camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0x55000000), CircleShape)
                            .testTag("camera_flip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip camera",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Helper instruction banner over viewfinder
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCC000000))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "✨ हाथ की लिखावट, आधा हिंदी आधा अंग्रेज़ी",
                color = Color(0xFFFDE68A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Controls (Shutter, Gallery, Indicator)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xCC000000))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button
                IconButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("camera_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Prominent Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(shutterScale)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(if (isCapturing) EmeraldGreen else SaffronGold)
                        .clickable(enabled = !isCapturing) {
                            val capture = imageCapture ?: return@clickable
                            isCapturing = true
                            shutterPressed = true

                            capture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        try {
                                            val rotationDegrees = image.imageInfo.rotationDegrees
                                            val rawBitmap = image.toBitmap()

                                            val rotatedBitmap = if (rotationDegrees != 0) {
                                                val matrix = Matrix().apply {
                                                    postRotate(rotationDegrees.toFloat())
                                                }
                                                Bitmap.createBitmap(
                                                    rawBitmap,
                                                    0,
                                                    0,
                                                    rawBitmap.width,
                                                    rawBitmap.height,
                                                    matrix,
                                                    true
                                                )
                                            } else {
                                                rawBitmap
                                            }

                                            onPhotoCaptured(rotatedBitmap)
                                        } catch (e: Exception) {
                                            Log.e("CameraCaptureScreen", "Error processing captured image", e)
                                        } finally {
                                            image.close()
                                            isCapturing = false
                                            shutterPressed = false
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraCaptureScreen", "Photo capture error", exception)
                                        isCapturing = false
                                        shutterPressed = false
                                    }
                                }
                            )
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Take Photo",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Placeholder / Instruction Icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI OCR",
                        color = EmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
