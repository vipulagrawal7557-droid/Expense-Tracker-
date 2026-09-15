package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: FinanceViewModel) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val incomeTransactions by viewModel.incomeTransactions.collectAsState()
    val expenseTransactions by viewModel.expenseTransactions.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Income, 2: Expenses
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var dateFilterMode by remember { mutableStateOf("ALL") } // ALL, THIS_MONTH, LAST_MONTH, CUSTOM
    var customFilterDateMillis by remember { mutableStateOf<Long?>(null) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Source list based on tab
    val baseList = when (selectedTabIndex) {
        1 -> incomeTransactions
        2 -> expenseTransactions
        else -> allTransactions
    }

    // Filter by search, category, and date
    val filteredList = remember(baseList, searchQuery, selectedCategoryFilter, dateFilterMode, customFilterDateMillis) {
        baseList.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true) ||
                    item.notes.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryFilter == null || item.category == selectedCategoryFilter

            val matchesDate = when (dateFilterMode) {
                "THIS_MONTH" -> {
                    val calNow = Calendar.getInstance()
                    val calItem = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                    calNow.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                            calNow.get(Calendar.MONTH) == calItem.get(Calendar.MONTH)
                }
                "LAST_MONTH" -> {
                    val calLast = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                    val calItem = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                    calLast.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                            calLast.get(Calendar.MONTH) == calItem.get(Calendar.MONTH)
                }
                "CUSTOM" -> {
                    if (customFilterDateMillis == null) true
                    else {
                        val calTarget = Calendar.getInstance().apply { timeInMillis = customFilterDateMillis!! }
                        val calItem = Calendar.getInstance().apply { timeInMillis = item.dateMillis }
                        calTarget.get(Calendar.YEAR) == calItem.get(Calendar.YEAR) &&
                                calTarget.get(Calendar.DAY_OF_YEAR) == calItem.get(Calendar.DAY_OF_YEAR)
                    }
                }
                else -> true
            }

            matchesQuery && matchesCategory && matchesDate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transactions Ledger",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionToEdit = null
                    showAddDialog = true
                },
                containerColor = if (selectedTabIndex == 2) ExpenseRed else if (selectedTabIndex == 1) IncomeGreen else PrimaryGreen,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Tabs: All, Income, Expenses
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("All (${allTransactions.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Income (${incomeTransactions.size})",
                            color = if (selectedTabIndex == 1) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Expenses (${expenseTransactions.size})",
                            color = if (selectedTabIndex == 2) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }

            // Search Bar & Date Filter Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by title, category, notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date filter chips (All, This Month, Last Month, Specific Date)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = dateFilterMode == "ALL",
                        onClick = { dateFilterMode = "ALL" },
                        label = { Text("All Time") }
                    )

                    FilterChip(
                        selected = dateFilterMode == "THIS_MONTH",
                        onClick = { dateFilterMode = "THIS_MONTH" },
                        label = { Text("This Month") }
                    )

                    FilterChip(
                        selected = dateFilterMode == "LAST_MONTH",
                        onClick = { dateFilterMode = "LAST_MONTH" },
                        label = { Text("Last Month") }
                    )

                    FilterChip(
                        selected = dateFilterMode == "CUSTOM",
                        onClick = {
                            dateFilterMode = "CUSTOM"
                            showCustomDatePicker = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = {
                            Text(
                                if (customFilterDateMillis != null && dateFilterMode == "CUSTOM")
                                    dateFormat.format(Date(customFilterDateMillis!!))
                                else "Select Date"
                            )
                        }
                    )
                }
            }

            // Transactions list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching transactions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button below to add an entry with any custom or back-dated date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        TransactionDetailCard(
                            transaction = item,
                            currencyFormatter = currencyFormatter,
                            dateFormat = dateFormat,
                            onEdit = {
                                transactionToEdit = item
                                showAddDialog = true
                            },
                            onDelete = {
                                viewModel.deleteTransaction(item)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Transaction Dialog with full date picker
    if (showAddDialog) {
        val initialType = when (selectedTabIndex) {
            1 -> TransactionType.INCOME
            else -> TransactionType.EXPENSE
        }
        AddEditTransactionDialog(
            initialType = initialType,
            transactionToEdit = transactionToEdit,
            onDismissRequest = {
                showAddDialog = false
                transactionToEdit = null
            },
            onSaveTransaction = { type, title, amount, category, dateMillis, paymentMode, notes ->
                if (transactionToEdit != null) {
                    viewModel.updateTransaction(
                        transactionToEdit!!.copy(
                            type = type,
                            title = title,
                            amount = amount,
                            category = category,
                            dateMillis = dateMillis,
                            paymentMode = paymentMode,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addTransaction(
                        type = type,
                        title = title,
                        amount = amount,
                        category = category,
                        dateMillis = dateMillis,
                        paymentMode = paymentMode,
                        notes = notes
                    )
                }
            }
        )
    }

    // Custom Date Filter Dialog
    if (showCustomDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = customFilterDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            customFilterDateMillis = it
                        }
                        showCustomDatePicker = false
                    }
                ) {
                    Text("Filter By Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun TransactionDetailCard(
    transaction: Transaction,
    currencyFormatter: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"} ${currencyFormatter.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = transaction.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = transaction.paymentMode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Date display showing user-specified back-dated entry
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(transaction.dateMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (transaction.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2
                )
            }

            // Edit & Delete row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
