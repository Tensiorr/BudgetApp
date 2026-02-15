package com.tensiorr.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import com.tensiorr.budgetapp.data.dao.TransactionDao

class TransactionViewModel(private val dao: TransactionDao) : ViewModel() {
    fun getAllTransactions() = dao.getAllTransactions()
}