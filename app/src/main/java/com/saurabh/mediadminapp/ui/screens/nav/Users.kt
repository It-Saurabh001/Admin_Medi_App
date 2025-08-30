package com.saurabh.mediadminapp.ui.screens.nav

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.network.response.UserItem

// Data class for User
data class User(
    val id: String,
    val userId: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val pinCode: String,
    val dateOfAccountCreation: String,
    val isApproved: Boolean,
    val block: Boolean
)

// Mock data
val mockUsers1 = listOf(
    User(
        id = "1",
        userId = "USR001",
        name = "Dr. Rajesh Kumar",
        email = "rajesh@hospital.com",
        phoneNumber = "+91 9876543210",
        address = "MG Road, Mumbai",
        pinCode = "400001",
        dateOfAccountCreation = "2024-01-15",
        isApproved = true,
        block = false
    ),
    User(
        id = "2",
        userId = "USR002",
        name = "Priya Sharma",
        email = "priya.sharma@clinic.com",
        phoneNumber = "+91 9876543211",
        address = "Sector 18, Noida",
        pinCode = "201301",
        dateOfAccountCreation = "2024-01-20",
        isApproved = false,
        block = false
    ),
    User(
        id = "3",
        userId = "USR003",
        name = "Dr. Amit Singh",
        email = "amit@medcare.com",
        phoneNumber = "+91 9876543212",
        address = "Park Street, Kolkata",
        pinCode = "700016",
        dateOfAccountCreation = "2024-01-25",
        isApproved = true,
        block = true
    )
)

// Filter status enum
enum class FilterStatus { ALL, APPROVED, PENDING, BLOCKED }

//// Filter data class
data class FilterOption1(
    val key: FilterStatus,
    val label: String,
    val icon: ImageVector
)
val mockUsers = listOf(
    UserItem(
        id = 1,
        user_id ="58d6518c-091c-4a0a-a293-d2f45e7c1ba3",
        name = "Rahul Prajapati",
        email = "rahul@gmail.com",
        phone_number = "6345128501",
        address = "MG Road, Mumbai",
        password = "1234",
        pin_code = "400001",
        date_of_account_creation = "2024-01-15",
        _isApproved = true,
        _block = false
    ),
    UserItem(
        id = 1,
        user_id = "59e359d8-4ae3-4134-b0f6-c5e5d59eefb0",
        name = "Saurabh Yadav",
        email = "saurabh@gmail.com",
        phone_number = "7236040803",
        address = "Jankipuram",
        password = "saurabh",
        pin_code = "226031",
        date_of_account_creation = "2024-01-15",
        _isApproved = true,
        _block = false
    ),
    UserItem(
        id = 2,
        user_id = "3087df43-64d3-42df-8f55-aa6ec1eb669d",
        name = "Vivek Kumar",
        email = "vivek@gmail.com",
        phone_number = "+91 9876543211",
        address = "Sector 18, Noida",
        password =  "vivek",
        pin_code = "201301",
        date_of_account_creation = "2024-01-20",
        _isApproved = false,
        _block = false
    ),
    UserItem(
        id = 3,
        user_id = "fe9950db-8b78-47d3-9fc2-5d5cea973a01",
        name =  "Vinay Yadav",
        email = "vinay@gmail.com",
        phone_number = "8197006499",
        address = "Jankipuram",
        password = "vinay",
        pin_code = "700016",
        date_of_account_creation = "2024-01-25",
        _isApproved = true,
        _block = true
    )
)
@Preview
@Composable
fun UsersScreen() {
    var users by remember { mutableStateOf(mockUsers) }
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    // Filter users based on search term and filter status
    val filteredUsers = users.filter { user ->
        val matchesSearch = user.name.lowercase().contains(searchTerm.lowercase()) ||
                user.email.lowercase().contains(searchTerm.lowercase()) ||
                user.user_id.lowercase().contains(searchTerm.lowercase())

        val matchesFilter = when (filterStatus) {
            FilterStatus.ALL -> true
            FilterStatus.APPROVED -> user.isApproved && !user.block
            FilterStatus.PENDING -> !user.isApproved
            FilterStatus.BLOCKED -> user.block
        }

        matchesSearch && matchesFilter
    }

    // Calculate stats
    val stats = mapOf(
        "total" to users.size,
        "approved" to users.count { it.isApproved && !it.block },
        "pending" to users.count { !it.isApproved },
        "blocked" to users.count { it.block }
    )

    // Medical gradient colors
    val medicalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4F46E5), // Indigo
            Color(0xFF06B6D4)  // Cyan
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                modifier = Modifier.weight(1f),
                value = stats["total"].toString(),
                label = "Total Users",
                valueColor = MaterialTheme.colorScheme.onSurface
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                value = stats["approved"].toString(),
                label = "Approved",
                valueColor = Color(0xFF4F46E5)
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                value = stats["pending"].toString(),
                label = "Pending",
                valueColor = Color(0xFFD97706)
            )
            StatsCard(
                modifier = Modifier.weight(1f),
                value = stats["blocked"].toString(),
                label = "Blocked",
                valueColor = MaterialTheme.colorScheme.error
            )
        }

        // Search and Filter
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Search Input
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search users...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Filter Buttons
            val filterOptions = listOf(
                FilterOption1(FilterStatus.ALL, "All Users", Icons.Default.List),
                FilterOption1(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterOption1(FilterStatus.PENDING, "Pending", Icons.Default.List),
                FilterOption1(FilterStatus.BLOCKED, "Blocked", Icons.Default.Add)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterButton(
                        filterOption = filter,
                        isSelected = filterStatus == filter.key,
                        onClick = { filterStatus = filter.key },
                        medicalGradient = medicalGradient
                    )
                }
            }
        }

        // Add New User Button
        Button(
            onClick = { /* Handle add new user */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(medicalGradient, RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add New User",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Users List
        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCard(
                        user = user,
                        onView = { println("View user: ${it}") },
                        onApprove = { println("Approve user: ${it}") },
                        onBlock = { println("Block/Unblock user: ${it}") }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard1(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FilterButton(
    filterOption: FilterOption1,
    isSelected: Boolean,
    onClick: () -> Unit,
    medicalGradient: Brush
) {
    val backgroundColor = if (isSelected) {
        medicalGradient
    } else {
        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Transparent else Color.Transparent
        ),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = filterOption.icon,
                contentDescription = filterOption.label,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = filterOption.label,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UserCard(
    user: UserItem,
    onView: (String) -> Unit,
    onApprove: (String) -> Unit,
    onBlock: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.user_id,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                val (statusText, statusColor) = when {
                    user.block -> "Blocked" to MaterialTheme.colorScheme.error
                    user.isApproved -> "Approved" to Color(0xFF10B981)
                    else -> "Pending" to Color(0xFFD97706)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User details
            Text(
                text = user.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = user.phone_number,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${user.address}, ${user.pin_code}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Created: ${user.date_of_account_creation}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onView(user.id.toString()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 14.sp)
                }

                if (!user.isApproved && !user.block) {
                    Button(
                        onClick = { onApprove(user.id.toString()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Approve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { onBlock(user.id.toString()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.block) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = if (user.block) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = if (user.block) "Unblock" else "Block",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.block) "Unblock" else "Block",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}