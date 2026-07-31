package pl.septicwolf818.calltamer.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.septicwolf818.calltamer.R
import pl.septicwolf818.calltamer.ui.role.rememberCallScreeningRoleRequester

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requestRole = rememberCallScreeningRoleRequester { viewModel.nextStep() }

    LaunchedEffect(Unit) {
        val steps = mutableListOf<PermissionStepType>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(android.content.Context.ROLE_SERVICE) as android.app.role.RoleManager
            if (!roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_SCREENING)) {
                steps.add(PermissionStepType.SCREENING_ROLE)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                steps.add(PermissionStepType.NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            steps.add(PermissionStepType.CONTACTS)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            steps.add(PermissionStepType.CALL_LOG)
        }

        viewModel.initSteps(steps)
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.nextStep() }

    val contactsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.nextStep() }

    val callLogLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { viewModel.nextStep() }

    val currentStep = state.currentStep

    Surface(modifier = Modifier.fillMaxSize()) {
        WelcomeStep(
            stepType = currentStep,
            totalSteps = state.totalSteps,
            stepIndex = state.currentIndex,
            loading = state.steps.isEmpty() && !state.isComplete,
            onStart = { viewModel.nextStep() },
            roleContent = {
                if (currentStep == PermissionStepType.SCREENING_ROLE) {
                    ScreeningRoleStep(
                        onGrant = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                requestRole()
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        onSkip = { viewModel.nextStep() }
                    )
                }
            },
            permissionContent = { step ->
                when (step) {
                    PermissionStepType.NOTIFICATIONS -> PermissionStep(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary) },
                        title = stringResource(R.string.onboarding_notif_title),
                        body = stringResource(R.string.onboarding_notif_body),
                        buttonLabel = stringResource(R.string.onboarding_notif_allow),
                        onGrant = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        onSkip = { viewModel.nextStep() }
                    )
                    PermissionStepType.CONTACTS -> PermissionStep(
                        icon = { Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary) },
                        title = stringResource(R.string.onboarding_contacts_title),
                        body = stringResource(R.string.onboarding_contacts_body),
                        buttonLabel = stringResource(R.string.onboarding_contacts_allow),
                        onGrant = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) },
                        onSkip = { viewModel.nextStep() }
                    )
                    PermissionStepType.CALL_LOG -> PermissionStep(
                        icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary) },
                        title = stringResource(R.string.onboarding_calllog_title),
                        body = stringResource(R.string.onboarding_calllog_body),
                        buttonLabel = stringResource(R.string.onboarding_calllog_allow),
                        onGrant = { callLogLauncher.launch(Manifest.permission.READ_CALL_LOG) },
                        onSkip = { viewModel.nextStep() }
                    )
                    PermissionStepType.SCREENING_ROLE -> { }
                }
            }
        )
    }
}

@Composable
private fun WelcomeStep(
    stepType: PermissionStepType?,
    totalSteps: Int,
    stepIndex: Int,
    loading: Boolean,
    onStart: () -> Unit,
    roleContent: @Composable () -> Unit,
    permissionContent: @Composable (PermissionStepType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            Text(stringResource(R.string.onboarding_checking), style = MaterialTheme.typography.bodyLarge)
            return
        }

        if (stepType == null) return

        if (stepIndex == 0) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.onboarding_welcome_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.onboarding_permissions_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.onboarding_permissions_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.onboarding_get_started))
            }
        } else if (stepType == PermissionStepType.SCREENING_ROLE) {
            roleContent()
        } else {
            permissionContent(stepType)
        }
    }
}

@Composable
private fun ScreeningRoleStep(onGrant: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.onboarding_role_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.onboarding_role_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.onboarding_role_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_role_grant))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}

@Composable
private fun PermissionStep(
    icon: @Composable () -> Unit,
    title: String,
    body: String,
    buttonLabel: String,
    onGrant: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(24.dp))
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = body,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text(buttonLabel)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}
