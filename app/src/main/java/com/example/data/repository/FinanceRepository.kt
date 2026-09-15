package com.example.data.repository

import com.example.data.local.InvestmentDao
import com.example.data.local.TransactionDao
import com.example.data.model.Investment
import com.example.data.model.InvestmentType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val investmentDao: InvestmentDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val incomeTransactions: Flow<List<Transaction>> = transactionDao.getTransactionsByType(TransactionType.INCOME)
    val expenseTransactions: Flow<List<Transaction>> = transactionDao.getTransactionsByType(TransactionType.EXPENSE)
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()

    val allInvestments: Flow<List<Investment>> = investmentDao.getAllInvestments()
    val totalInvested: Flow<Double?> = investmentDao.getTotalInvested()
    val totalCurrentValue: Flow<Double?> = investmentDao.getTotalCurrentValue()

    suspend fun addTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun addInvestment(investment: Investment): Long {
        return investmentDao.insertInvestment(investment)
    }

    suspend fun updateInvestment(investment: Investment) {
        investmentDao.updateInvestment(investment)
    }

    suspend fun deleteInvestment(investment: Investment) {
        investmentDao.deleteInvestment(investment)
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        investmentDao.clearAll()
    }

    suspend fun prepopulateSampleDataIfEmpty() {
        val existing = transactionDao.getAllTransactions().first()
        if (existing.isNotEmpty()) return

        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // 1 day ago
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = cal.timeInMillis

        // 3 days ago (backdated)
        cal.add(Calendar.DAY_OF_MONTH, -2)
        val threeDaysAgo = cal.timeInMillis

        // 7 days ago (backdated)
        cal.add(Calendar.DAY_OF_MONTH, -4)
        val weekAgo = cal.timeInMillis

        // 15 days ago (backdated)
        cal.add(Calendar.DAY_OF_MONTH, -8)
        val midMonth = cal.timeInMillis

        // Sample Transactions
        val sampleTransactions = listOf(
            Transaction(
                type = TransactionType.INCOME,
                title = "Monthly Salary Credited",
                amount = 85000.0,
                category = "Salary",
                dateMillis = midMonth,
                paymentMode = "Net Banking",
                notes = "Full-time software engineering remuneration"
            ),
            Transaction(
                type = TransactionType.INCOME,
                title = "Client Freelance Payment",
                amount = 22500.0,
                category = "Freelance & Consulting",
                dateMillis = weekAgo,
                paymentMode = "UPI",
                notes = "Android app consultation milestone 2"
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                title = "Monthly Apartment Rent",
                amount = 24000.0,
                category = "Rent & Utilities",
                dateMillis = midMonth,
                paymentMode = "Net Banking",
                notes = "Paid to landlord"
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                title = "Weekly Grocery Mart",
                amount = 3850.0,
                category = "Groceries",
                dateMillis = threeDaysAgo,
                paymentMode = "UPI",
                notes = "Vegetables, staples, milk"
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                title = "Weekend Cafe & Dining",
                amount = 1420.0,
                category = "Food & Dining",
                dateMillis = yesterday,
                paymentMode = "Credit Card",
                notes = "Dinner with friends"
            ),
            Transaction(
                type = TransactionType.EXPENSE,
                title = "Electricity & Wi-Fi Bills",
                amount = 2150.0,
                category = "Bills & Recharges",
                dateMillis = now,
                paymentMode = "UPI",
                notes = "Fiber broadband + power bill"
            )
        )

        for (t in sampleTransactions) {
            transactionDao.insertTransaction(t)
        }

        // Sample Investments
        val sampleInvestments = listOf(
            Investment(
                name = "HDFC Bluechip Mutual Fund",
                type = InvestmentType.MUTUAL_FUND,
                investedAmount = 50000.0,
                currentValue = 58400.0,
                expectedReturnRate = 14.2,
                institution = "HDFC Mutual Fund",
                notes = "Monthly SIP of 5,000"
            ),
            Investment(
                name = "SBI 1-Year Fixed Deposit",
                type = InvestmentType.FIXED_DEPOSIT,
                investedAmount = 100000.0,
                currentValue = 107100.0,
                expectedReturnRate = 7.1,
                institution = "State Bank of India",
                notes = "Emergency reserve fund"
            ),
            Investment(
                name = "Digital Gold Savings",
                type = InvestmentType.GOLD,
                investedAmount = 25000.0,
                currentValue = 29300.0,
                expectedReturnRate = 11.5,
                institution = "MMTC-PAMP",
                notes = "Long term wealth hedge"
            )
        )

        for (inv in sampleInvestments) {
            investmentDao.insertInvestment(inv)
        }
    }
}
