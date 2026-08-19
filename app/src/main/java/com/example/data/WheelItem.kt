package com.example.data

data class WheelItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String,
    val quantity: Int = 1
)

data class WheelSlice(
    val sliceIndex: Int,
    val itemId: String,
    val name: String,
    val colorHex: String
)
