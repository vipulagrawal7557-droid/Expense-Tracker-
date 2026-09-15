package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdminCredentials
import com.example.data.model.AuthPreferences
import com.example.data.model.Investment
import com.example.data.model.InvestmentType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserAccount
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val authPreferences: AuthPreferences = AuthPreferences(application)

    private val _currentUser = MutableStateFlow<UserAccount?>(authPreferences.getSavedSession())
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = FinanceRepository(db.transactionDao(), db.investmentDao())
        viewModelScope.launch {
            repository.prepopulateSampleDataIfEmpty()
        }
    }

    fun login(email: String, pass: String): Pair<Boolean, String?> {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()

        if (cleanEmail.equals(AdminCredentials.DEVELOPER_EMAIL, ignoreCase = true) &&
            cleanPass == AdminCredentials.DEVELOPER_PASSWORD) {
            val adminUser = AdminCredentials.DEVELOPER_ACCOUNT
            authPreferences.saveSession(adminUser)
            _currentUser.value = adminUser
            return Pair(true, null)
        }

        return Pair(false, "Invalid credentials. Use Developer Admin ID & Password.")
    }

    fun loginAsDeveloperAdmin() {
        val adminUser = AdminCredentials.DEVELOPER_ACCOUNT
        authPreferences.saveSession(adminUser)
        _currentUser.value = adminUser
    }

    fun loginAsGuest() {
        val guest = AdminCredentials.GUEST_ACCOUNT
        authPreferences.saveSession(guest)
        _currentUser.value = guest
    }

    fun logout() {
        authPreferences.clearSession()
        _currentUser.value = null
    }

    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeTransactions: StateFlow<List<Transaction>> = repository.incomeTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseTransactions: StateFlow<List<Transaction>> = repository.expenseTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.totalIncome
        .combine(MutableStateFlow(0.0)) { inc, _ -> inc ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.totalExpense
        .combine(MutableStateFlow(0.0)) { exp, _ -> exp ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allInvestments: StateFlow<List<Investment>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalInvested: StateFlow<Double> = repository.totalInvested
        .combine(MutableStateFlow(0.0)) { inv, _ -> inv ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalInvestmentValue: StateFlow<Double> = repository.totalCurrentValue
        .combine(MutableStateFlow(0.0)) { cv, _ -> cv ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Currency symbol preference
    val currencySymbol = MutableStateFlow("₹")

    fun addTransaction(
        type: TransactionType,
        title: String,
        amount: Double,
        category: String,
        dateMillis: Long, // User specified date, enables back-dating!
        paymentMode: String,
        notes: String
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                title = title.trim(),
                amount = amount,
                category = category,
                dateMillis = dateMillis,
                paymentMode = paymentMode,
                notes = notes.trim()
            )
            repository.addTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addInvestment(
        name: String,
        type: InvestmentType,
        investedAmount: Double,
        currentValue: Double,
        returnRate: Double,
        startDateMillis: Long,
        maturityDateMillis: Long?,
        institution: String,
        notes: String
    ) {
        viewModelScope.launch {
            val inv = Investment(
                name = name.trim(),
                type = type,
                investedAmount = investedAmount,
                currentValue = currentValue,
                expectedReturnRate = returnRate,
                startDateMillis = startDateMillis,
                maturityDateMillis = maturityDateMillis,
                institution = institution.trim(),
                notes = notes.trim()
            )
            repository.addInvestment(inv)
        }
    }

    fun updateInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.updateInvestment(investment)
        }
    }

    fun deleteInvestment(investment: Investment) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun exportTransactionsCsv(context: Context): File? {
        val list = allTransactions.value
        if (list.isEmpty()) return null
        return try {
            val file = File(context.cacheDir, "Expense_Income_Report.csv")
            val writer = FileWriter(file)
            writer.append("ID,Type,Title,Amount,Category,Date,Payment Mode,Notes\n")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (t in list) {
                val dateStr = sdf.format(Date(t.dateMillis))
                val sanitizedTitle = t.title.replace(",", " ")
                val sanitizedNotes = t.notes.replace(",", " ").replace("\n", " ")
                writer.append("${t.id},${t.type.name},$sanitizedTitle,${t.amount},${t.category},$dateStr,${t.paymentMode},$sanitizedNotes\n")
            }
            writer.flush()
            writer.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
