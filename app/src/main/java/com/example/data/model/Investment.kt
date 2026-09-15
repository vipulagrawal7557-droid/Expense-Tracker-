package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvestmentType(val displayName: String) {
    MUTUAL_FUND("Mutual Fund (SIP/Lump)"),
    FIXED_DEPOSIT("Fixed Deposit (FD)"),
    RECURRING_DEPOSIT("Recurring Deposit (RD)"),
    STOCKS("Direct Equity / Stocks"),
    GOLD("Digital / Physical Gold"),
    PPF("Public Provident Fund (PPF)"),
    NPS("National Pension Scheme"),
    SAVINGS("High-Yield Savings"),
    OTHER("Other Asset")
}

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: InvestmentType,
    val investedAmount: Double,
    val currentValue: Double,
    val expectedReturnRate: Double = 8.0, // Annual percentage
    val startDateMillis: Long = System.currentTimeMillis(),
    val maturityDateMillis: Long? = null,
    val institution: String = "",
    val notes: String = ""
)
