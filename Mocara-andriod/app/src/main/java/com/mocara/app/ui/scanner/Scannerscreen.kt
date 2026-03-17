package com.mocara.app.ui.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mocara.app.viewmodel.ScannerState
import com.mocara.app.viewmodel.ScannerViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mocara.app.util.BarcodeAnalyzer
import java.util.concurrent.Executors

/**
 * ScannerScreen
 * Entry point for the app - scans barcode or OCR
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onScanSuccess: (String) -> Unit
) {
    val scannerState by viewModel.scannerState.collectAsState()
    val context = LocalContext.current

    // Handle successful scan
    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.Success) {
            val drugId = (scannerState as ScannerState.Success).drugId
            onScanSuccess(drugId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Medication") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (scannerState) {
                is ScannerState.Idle -> {
                    IdleState(onStartScan = { viewModel.startScanning() })
                }

                is ScannerState.Scanning -> {
//                    ScanningState(
//                        onBarcodeMock = { viewModel.onBarcodeScanned("default") },//需要在这边实现真实的scan
//                        onCancel = { viewModel.resetScanner() }
//                    )

                    // 这里集成了真实的相机与权限检查逻辑
                    CameraScanningSection(
                        onBarcodeScanned = { code ->
                            viewModel.onBarcodeScanned(code)
                        },
                        onCancel = { viewModel.resetScanner() }
                    )
                }

                is ScannerState.Success -> {
                    SuccessState(drugId = (scannerState as ScannerState.Success).drugId)
                }

                is ScannerState.Error -> {
                    ErrorState(
                        message = (scannerState as ScannerState.Error).message,
                        onRetry = { viewModel.resetScanner() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Manual selection for testing
            if (scannerState is ScannerState.Idle) {
                ManualDrugSelection(
                    onDrugSelected = { viewModel.selectDrugManually(it) }
                )
            }
        }
    }
}

/**
 * 负责处理权限请求和显示相机的组合组件
 * The combined component responsible for handling permission requests and displaying the camera.
 */
@Composable
private fun CameraScanningSection(
    onBarcodeScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission is required to scan", Toast.LENGTH_SHORT)
                    .show()
                onCancel() // 拒绝权限则退出扫描状态. If permission is denied, exit the scanning state.
            }
        }
    )

    // 进入此状态时立即检查/请求权限. Immediately check/request permissions upon entering this state.
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        // 权限通过，显示真实的 CameraX 预览. Permission granted, displaying the actual CameraX preview.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            CameraPreview(onBarcodeScanned = onBarcodeScanned)

            // 扫描框覆盖层 (Overlay) Scan frame overlay
            Surface(
                modifier = Modifier.size(250.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.medium
            ) { }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Position barcode in the center",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel Scanning")
        }
    } else {
        // 等待权限响应时的占位. placeholder while waiting for permission response.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * 封装 CameraX 逻辑的 Composable
 */
@Composable
private fun CameraPreview(
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // 使用 AndroidView 将原生的 PreviewView 嵌入 Compose
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraExecutor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // 预览用例
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // 图像分析用例 (集成 BarcodeAnalyzer)
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                            // 回调给 ViewModel
                            onBarcodeScanned(barcode)
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // 绑定生命周期
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
private fun IdleState(onStartScan: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scanner",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Scan Your Medication",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Point your camera at the medication barcode or label",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartScan,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Scanning")
        }
    }
}

//@Composable
//private fun ScanningState(
//    onBarcodeMock: () -> Unit,
//    onCancel: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        // Camera preview would go here
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(400.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Surface(
//                modifier = Modifier.fillMaxSize(),
//                color = MaterialTheme.colorScheme.surfaceVariant
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxSize(),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(48.dp)
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "📷 Camera Preview",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Text(
//                        text = "Position barcode in center",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Mock scan button for testing
//        Button(
//            onClick = onBarcodeMock,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("🔍 Simulate Scan (Mock)")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        OutlinedButton(
//            onClick = onCancel,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Cancel")
//        }
//    }
//}

@Composable
private fun SuccessState(drugId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Medication Detected!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = drugId.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(modifier = Modifier.size(24.dp))

        Text(
            text = "Loading protocol...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scan Failed",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Composable
private fun ManualDrugSelection(onDrugSelected: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text(
            text = "Or select manually for testing:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onDrugSelected("ozempic") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Ozempic")
            }

            OutlinedButton(
                onClick = { onDrugSelected("insulin") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Insulin")
            }
        }
    }
}