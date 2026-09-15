package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val title: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long, // User specified date, supports back-dated entries
    val paymentMode: String = "UPI",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

object TransactionCategories {
    val expenseCategories = listOf(
        "Food & Dining",
        "Groceries",
        "Shopping",
        "Transportation",
        "Rent & Utilities",
        "Bills & Recharges",
        "Health & Medical",
        "Entertainment",
        "Education",
        "Travel",
        "Personal Care",
        "Other Expense"
    )

    val incomeCategories = listOf(
        "Salary",
        "Freelance & Consulting",
        "Business",
        "Investment Dividend",
        "Rental Income",
        "Interest",
        "Bonus & Rewards",
        "Gift",
        "Refund",
        "Other Income"
    )

    val paymentModes = listOf(
        "UPI",
        "Cash",
        "Credit Card",
        "Debit Card",
        "Net Banking",
        "Wallet"
    )
}
