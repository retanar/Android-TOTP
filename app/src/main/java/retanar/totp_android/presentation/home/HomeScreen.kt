@file:Suppress("FunctionName")

package retanar.totp_android.presentation.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import retanar.totp_android.R
import retanar.totp_android.presentation.DependencyContainer
import retanar.totp_android.presentation.composables.PopupMenuDialog
import retanar.totp_android.presentation.composables.PopupMenuTextItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            DependencyContainer.totpKeyRepository,
            DependencyContainer.secretEncryptor,
            DependencyContainer.totpCodeGenerator,
        )
    )
) {
    val state by viewModel.homeState
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, "Add TOTP", tint = Color.White)
            }
        },
    ) {
        TotpCardListView(state.totpList, onRemove = { id -> viewModel.removeTotpById(id) })

        AddTotpDialog(showDialog, { showDialog = false }, viewModel::addTotp)
    }
}

@Composable
fun TotpCardListView(list: List<TotpCardState>, onRemove: (id: Int) -> Unit) {
    LazyColumn {
        items(items = list) { item ->
            TotpCard(item, onRemove)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TotpCard(totpCardState: TotpCardState, onRemove: (id: Int) -> Unit) {
    var showPopupMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = {
                showPopupMenu = true
            }),
        elevation = 2.dp,
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(text = totpCardState.name)
            Text(fontSize = 28.sp, text = totpCardState.oneTimeCode.toString().padStart(6, '0'))
        }
    }
    if (showPopupMenu) {
        PopupMenuDialog(
            { showPopupMenu = false },
            PopupMenuTextItem("Remove") { onRemove(totpCardState.id) },
        )
    }
}

@Composable
fun AddTotpDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAdd: (name: String, secret: String) -> Unit,
) {
    if (!showDialog) return

    var name by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }

    Dialog(onDismiss) {
        Card {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.padding(all = 8.dp).fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 22.sp),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                )
                TextField(
                    value = secret,
                    onValueChange = { secret = it },
                    label = { Text("Secret") },
                    singleLine = true,
                    modifier = Modifier.padding(all = 8.dp).fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 22.sp),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL")
                    }
                    TextButton(onClick = {
                        onAdd(name, secret)
                        onDismiss()
                    }) {
                        Text("ADD")
                    }
                }
            }
        }
    }
}
