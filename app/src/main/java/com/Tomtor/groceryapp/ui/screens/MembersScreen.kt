package com.Tomtor.groceryapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Tomtor.groceryapp.data.model.Member
import com.Tomtor.groceryapp.viewmodel.ListViewModel
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.Tomtor.groceryapp.data.local.TokenStore
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MembersScreen(
    listId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: ListViewModel = viewModel(key = listId)
    val state by viewModel.state.collectAsState()
    var memberToRemove by remember { mutableStateOf<Member?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current

    LaunchedEffect(listId) {
        val tokenStore = TokenStore(context)
        val userId = tokenStore.userId.first() ?: ""
        // set current user ID first
        viewModel.setCurrentUserId(userId)
        viewModel.loadMembers(listId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Members") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(state.members) { member ->
                val isCurrentUser = member.user_id == state.currentUserId
                val isAdmin = state.currentUserRole == "admin"
                val canLongPress = isAdmin && !isCurrentUser

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = {
                                if (canLongPress) memberToRemove = member
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (member.role == "admin")
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isCurrentUser) "${member.username} (you)"
                                else member.username,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                member.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (member.role == "admin")
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                            )
                        }
                        if (isAdmin && !isCurrentUser) {
                            TextButton(onClick = {
                                val newRole = if (member.role == "admin") "guest" else "admin"
                                viewModel.updateMemberRole(listId, member.user_id, newRole)
                            }) {
                                Text(if (member.role == "admin") "Make guest" else "Make admin")
                            }
                        }
                    }
                }
            }
        }
    }

    // confirm remove dialog
    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text("Remove member") },
            text = { Text("Remove ${member.username} from this list?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeMember(listId, member.user_id)
                        memberToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}