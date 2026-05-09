package com.example.cafeteriafoodpreorder

data class CartItem(
    val foodId: String,
    val name: String,
    val price: Double,
    val imageUrl: String = "",
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = price * quantity
}