package com.Tomtor.groceryapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Tomtor.groceryapp.data.model.GroceryList
import com.Tomtor.groceryapp.viewmodel.AuthViewModel
import com.Tomtor.groceryapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToList: (String) -> Unit,
    onLogout: () -> Unit
) {
    val homeViewModel: HomeViewModel = viewModel()
    val state by homeViewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var listToLeave by remember { mutableStateOf<GroceryList?>(null) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { homeViewModel.loadLists() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Lists") },
                actions = {
                    TextButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create list")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Join a list with invite code")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.lists.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No lists yet. Create one!")
                    }
                } else {
                    LazyColumn {
                        items(state.lists) { list ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .combinedClickable(
                                        onClick = { onNavigateToList(list.id) },
                                        onLongClick = { listToLeave = list }
                                    )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        list.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // leave list confirmation dialog
    listToLeave?.let { list ->
        AlertDialog(
            onDismissRequest = { listToLeave = null },
            title = { Text("Leave list") },
            text = {
                Text("Leave \"${list.name}\"? It will be removed from your lists but others can still access it.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        homeViewModel.leaveList(list.id)
                        listToLeave = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { listToLeave = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        homeViewModel.createList(newListName)
                        newListName = ""
                        showCreateDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join List") },
            text = {
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Invite code") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (inviteCode.isNotBlank()) {
                        homeViewModel.joinList(inviteCode)
                        inviteCode = ""
                        showJoinDialog = false
                    }
                }) { Text("Join") }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("Cancel") }
            }
        )
    }
}