package com.mocara.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mocara.app.domain.model.StepType
import com.mocara.app.viewmodel.OnboardingViewModel


/**
 * OnboardingScreen
 * Step-based medication onboarding with progress tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    drugId: String,
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    onEscalation: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // Initialize with drug ID
    LaunchedEffect(drugId) {
        viewModel.initialize(drugId)
    }

    // Handle completion
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    // Handle escalation
    LaunchedEffect(uiState.shouldEscalate) {
        if (uiState.shouldEscalate) {
            val reason = uiState.escalation?.reason ?: "Medical attention needed"
            onEscalation(reason)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.protocol?.drugName ?: "Medication Onboarding")
                },
                navigationIcon = {
                    if (uiState.currentStepIndex > 0) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Progress indicator
            if (uiState.totalSteps > 0) {
                ProgressIndicator(
                    currentStep = uiState.currentStepIndex + 1,
                    totalSteps = uiState.totalSteps,
                    progress = viewModel.getProgress()
                )
            }

            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.initialize(drugId) },
                        onDismiss = { viewModel.clearError() }
                    )
                }
                uiState.currentStep != null -> {
                    StepContent(
                        step = uiState.currentStep!!,
                        selectedOption = selectedOption,
                        onOptionSelected = { selectedOption = it },
                        onNext = {
                            val response = selectedOption ?: ""
                            viewModel.onStepResponse(response)
                            selectedOption = null
                        },
                        onSkip = {
                            viewModel.skipStep()
                            selectedOption = null
                        },
                        canSkip = uiState.currentStep?.requiresConfirmation != true
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    progress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Step $currentStep of $totalSteps",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun StepContent(
    step: com.mocara.app.domain.model.ProtocolStep,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    canSkip: Boolean
) {

    // 新增：针对需要确认的步骤，初始化 checkbox 状态列表
    val confirmationItems = step.confirmationItems
    val hasConfirmationItems = step.requiresConfirmation && !confirmationItems.isNullOrEmpty()

    // 用 remember 保存每个条目的勾选状态
    val confirmationCheckedStates = remember(step.stepNumber) {
        mutableStateListOf<Boolean>().apply {
            if (hasConfirmationItems) {
                addAll(List(confirmationItems!!.size) { false })
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Step icon based on type
        Icon(
            imageVector = when (step.type) {
                StepType.INFO -> Icons.Default.Info
                StepType.QUESTION -> Icons.Default.QuestionMark
                StepType.CONFIRMATION -> Icons.Default.CheckCircle
            },
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.CenterHorizontally),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step title
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = step.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options for questions
        if (step.type == StepType.QUESTION && !step.options.isNullOrEmpty()) {
            Text(
                text = "Select your answer:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            step.options.forEach { option ->
                OptionCard(
                    option = option,
                    isSelected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

//        增加部分
        // 新增：当需要确认且有 confirmationItems 时，显示 checkbox 列表
        if (hasConfirmationItems && confirmationItems != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please confirm the following:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            confirmationItems.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmationCheckedStates.getOrNull(index) ?: false,
                        onCheckedChange = { checked ->
                            if (index < confirmationCheckedStates.size) {
                                confirmationCheckedStates[index] = checked
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
//        以上部分为增加部分



        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        // 修改：canProceed 逻辑，加入“所有 checkbox 必须勾选”的限制
        val allConfirmationsChecked = if (hasConfirmationItems) {
            confirmationCheckedStates.isNotEmpty() &&
                    confirmationCheckedStates.all { it }
        } else {
            true
        }

        // Action buttons
        val canProceed = when (step.type) {
            StepType.QUESTION -> selectedOption != null
            StepType.CONFIRMATION -> allConfirmationsChecked
            else -> true
        }

        Button(
            onClick = onNext,
            enabled = canProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                if (step.requiresConfirmation) "I Confirm" else "Continue"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }

        if (canSkip) {
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip this step")
            }
        }
    }
}

@Composable
private fun OptionCard(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading protocol...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Error, contentDescription = null)
        },
        title = {
            Text("Error")
        },
        text = {
            Text(error)
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}