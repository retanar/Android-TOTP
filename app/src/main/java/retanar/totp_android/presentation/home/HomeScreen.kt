package retanar.totp_android.presentation.home

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import retanar.totp_android.R
import retanar.totp_android.presentation.composables.LaunchedSnackbar
import retanar.totp_android.presentation.composables.PopupMenuDialog
import retanar.totp_android.presentation.composables.PopupMenuTextItem

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    navigateExport: () -> Unit,
    navigateImport: () -> Unit,
) {
    val state by viewModel.homeState
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val clipboard = LocalClipboardManager.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold) },
                actions = {
                    Box(Modifier.align(Alignment.CenterVertically)) {
                        IconButton(onClick = { showMoreMenu = !showMoreMenu }) {
                            Icon(Icons.Filled.MoreVert, "More options")
                        }
                        ThreeDotMenu(
                            showMoreMenu,
                            onDismiss = { showMoreMenu = false },
                            items = listOf(
                                "Export" to navigateExport,
                                "Import" to navigateImport,
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add TOTP")
            }
        },
    ) {
        TotpCardListView(
            list = state.totpList,
            onRemove = viewModel::removeTotpById,
            onCopy = { code ->
                clipboard.setText(AnnotatedString(code))
                showCopiedSnackbar = true
            },
            onEdit = viewModel::requestEdit,
            contentPadding = it
        )

        AddTotpDialog(showAddDialog, { showAddDialog = false }, viewModel::addTotp, viewModel::isSecretCorrect)
        EditTotpDialog(
            state.editingTotp,
            { viewModel.requestEdit(-1) },
            viewModel::editTotp,
            viewModel::isSecretCorrect,
        )
    }

    // Android 13 (API 33) shows system confirmation on copy
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
        LaunchedSnackbar("Copied", scaffoldState.snackbarHostState, showCopiedSnackbar, { showCopiedSnackbar = false })
}

@Composable
fun TotpCardListView(
    list: List<TotpCardState>,
    onRemove: (id: Int) -> Unit,
    onCopy: (code: String) -> Unit,
    onEdit: (id: Int) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(
        Modifier
            .padding(contentPadding)
            .padding(vertical = 4.dp),
        lazyListState,
    ) {
        items(items = list, key = { it.id }) { item ->
            TotpCard(item, onRemove, onCopy, onEdit)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TotpCard(
    totpCardState: TotpCardState,
    onRemove: (id: Int) -> Unit,
    onCopy: (code: String) -> Unit,
    onEdit: (id: Int) -> Unit
) {
    var showPopupMenu by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = {
                showPopupMenu = true
            }),
        elevation = 2.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val codeString = totpCardState.oneTimeCode.toString().padStart(6, '0')
            Column(
                Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .weight(1f)
            ) {
                Text(
                    text = totpCardState.name,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(text = codeString, style = MaterialTheme.typography.h4)
            }
            Text(text = totpCardState.secondsLeft.toString())
            IconButton(onClick = { onCopy(codeString) }) {
                Icon(painterResource(R.drawable.ic_content_copy), "Copy to clipboard")
            }
        }
    }
    if (showPopupMenu) {
        PopupMenuDialog(
            { showPopupMenu = false },
            PopupMenuTextItem("Edit") {
                onEdit(totpCardState.id)
                showPopupMenu = false
            },
            PopupMenuTextItem("Remove") {
                onRemove(totpCardState.id)
                showPopupMenu = false
            },
        )
    }
}

@Composable
fun ThreeDotMenu(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    items: List<Pair<String, () -> Unit>>,
) {
    DropdownMenu(showMenu, onDismiss) {
        items.forEach { (text, action) ->
            DropdownMenuItem(onClick = action) {
                Text(text, style = MaterialTheme.typography.subtitle1)
            }
        }
    }
}