package retanar.totp_android.presentation.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retanar.totp_android.domain.usecases.SavingMode
import retanar.totp_android.presentation.composables.PasswordTextField

val exportOptions = listOf(
    "No encryption" to SavingMode.NoEncryption,
    "Encrypt only keys" to SavingMode.KeyEncryption,
    "Encrypt everything" to SavingMode.FullEncryption
)

@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onPopBack: () -> Unit,
) {
    var currentExportChoice by rememberSaveable { mutableStateOf(0) }
    var encryptionPassword by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val getOutputStreamLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { content ->
        if (content == null)
            return@rememberLauncherForActivityResult
        coroutineScope.launch {
            context.contentResolver.openOutputStream(content)?.use { exportOutputStream ->
                viewModel.export(exportOptions[currentExportChoice].second, encryptionPassword, exportOutputStream)
            }
            onPopBack()
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Export", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onPopBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
    }) {
        Column(
            Modifier
                .padding(it)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            ExportTypeMenu(
                currentChoice = currentExportChoice,
                onChange = { newChoice -> currentExportChoice = newChoice }
            )

            if (currentExportChoice != 0) {
                Spacer(Modifier.height(32.dp))
                PasswordTextField(
                    label = "Password",
                    value = encryptionPassword,
                    onValueChange = { newPass -> encryptionPassword = newPass },
                )
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = { getOutputStreamLauncher.launch("export.json") },
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("EXPORT", style = MaterialTheme.typography.button)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExportTypeMenu(
    currentChoice: Int,
    onChange: (Int) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = !isExpanded }) {
        TextField(
            value = exportOptions[currentChoice].first,
            readOnly = true,
            singleLine = true,
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "Show other options") },
            onValueChange = {},
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            exportOptions.forEachIndexed { index, s ->
                DropdownMenuItem(
                    onClick = {
                        onChange(index)
                        isExpanded = false
                    },
                ) {
                    Text(s.first)
                }
            }
        }
    }
}