package com.Tomtor.groceryapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Tomtor.groceryapp.data.local.TokenStore
import com.Tomtor.groceryapp.data.model.Item
import com.Tomtor.groceryapp.viewmodel.ListViewModel
import com.Tomtor.groceryapp.viewmodel.SortOrder
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.activity.compose.BackHandler
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    val viewModel: ListViewModel = viewModel(key = listId)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    var isEditMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemLabel by remember { mutableStateOf("Other") }
    var newListName by remember { mutableStateOf("") }
    var itemToEdit by remember { mutableStateOf<Item?>(null) }
    var editItemName by remember { mutableStateOf("") }
    var editItemLabel by remember { mutableStateOf("") }

    // sort state — base sort and whether to separate unmarked/marked
    var sortByLabel by remember { mutableStateOf(false) }
    var separateUnmarked by remember { mutableStateOf(true) }

    LaunchedEffect(listId) {
        val tokenStore = TokenStore(context)
        val token = tokenStore.token.first() ?: ""
        val userId = tokenStore.userId.first() ?: ""
        viewModel.loadList(listId, token, userId)
    }

    // derive sort order from the two toggles
    LaunchedEffect(sortByLabel, separateUnmarked) {
        val order = when {
            sortByLabel && separateUnmarked -> SortOrder.NEED_TO_GET_LABEL
            sortByLabel && !separateUnmarked -> SortOrder.NAME_LABEL
            !sortByLabel && separateUnmarked -> SortOrder.NEED_TO_GET
            else -> SortOrder.NAME
        }
        viewModel.setSortOrder(order)
    }
    BackHandler(enabled = isEditMode) {
        isEditMode = false
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditMode && state.currentUserRole == "admin") {
                        TextButton(onClick = {
                            newListName = state.list?.name ?: ""
                            showRenameDialog = true
                        }) {
                            Text(
                                state.list?.name ?: "List",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Text(state.list?.name ?: "List")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Done editing" else "Edit"
                        )
                    }
                    IconButton(onClick = { showInviteDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onNavigateToMembers) {
                        Icon(Icons.Default.Group, contentDescription = "Members")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditMode) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add item")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // sort + reset row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when {
                            sortByLabel && separateUnmarked -> "Label · Unmarked first"
                            sortByLabel && !separateUnmarked -> "Label"
                            !sortByLabel && separateUnmarked -> "Unmarked first"
                            else -> "Name"
                        }
                    )
                }
                TextButton(onClick = { viewModel.resetItems(listId) }) {
                    Text("Reset all")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(state.items, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.is_got,
                                    onCheckedChange = { checked ->
                                        viewModel.markItem(listId, item.id, checked)
                                    }
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isEditMode) {
                                            itemToEdit = item
                                            editItemName = item.name
                                            editItemLabel = item.label
                                        }
                                ) {
                                    Text(
                                        text = item.name,
                                        textDecoration = if (item.is_got)
                                            TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    if (item.label.isNotBlank() && item.label != "Other") {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (isEditMode) {
                                    IconButton(onClick = {
                                        viewModel.deleteItem(listId, item.id)
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
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

    // sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort items") },
            text = {
                Column {
                    Text("Sort by", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !sortByLabel,
                            onClick = { sortByLabel = false }
                        )
                        Text("Name", modifier = Modifier.clickable { sortByLabel = false })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortByLabel,
                            onClick = { sortByLabel = true }
                        )
                        Text("Name grouped by label", modifier = Modifier.clickable { sortByLabel = true })
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Order", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = separateUnmarked,
                            onCheckedChange = { separateUnmarked = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unmarked items first")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // add item dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newItemName = ""
                newItemLabel = "Other"
            },
            title = { Text("Add Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Item name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LabelDropdown(
                        selected = newItemLabel,
                        onSelected = { newItemLabel = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newItemName.isNotBlank()) {
                        viewModel.addItem(listId, newItemName, newItemLabel)
                        newItemName = ""
                        newItemLabel = "Other"
                        showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newItemName = ""
                    newItemLabel = "Other"
                }) { Text("Cancel") }
            }
        )
    }

    // edit item dialog
    itemToEdit?.let { editingItem ->
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Item") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editItemName,
                        onValueChange = { editItemName = it },
                        label = { Text("Item name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LabelDropdown(
                        selected = editItemLabel,
                        onSelected = { editItemLabel = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editItemName.isNotBlank()) {
                        viewModel.editItem(listId, editingItem.id, editItemName, editItemLabel)
                        itemToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // invite code dialog
    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Code") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.list?.invite_code ?: "",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the button below to copy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        state.list?.invite_code?.let { code ->
                            val clipData = android.content.ClipData.newPlainText("invite_code", code)
                            clipboard.setClipEntry(androidx.compose.ui.platform.ClipEntry(clipData))
                        }
                    }
                    showInviteDialog = false
                }) { Text("Copy code") }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) { Text("Close") }
            }
        )
    }

    // rename list dialog
    if (showRenameDialog && state.currentUserRole == "admin") {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("New name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newListName.isNotBlank()) {
                        viewModel.renameList(listId, newListName)
                        newListName = ""
                        showRenameDialog = false
                    }
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelDropdown(
    selected: String,
    onSelected: (String) -> Unit
) {
    val labels = listOf(
        "Produce", "Dairy", "Meat", "Bakery", "Frozen",
        "Beverages", "Snacks", "Cleaning", "Personal Care",
        "Fish", "Dry Food", "Spices", "Other"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Label") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            labels.forEach { label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}