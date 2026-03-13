package com.example.myapp.Data

import androidx.lifecycle.ViewModel

class RecordViewModel : ViewModel() {
    val recordList = mutableListOf<RecordModel>()
}