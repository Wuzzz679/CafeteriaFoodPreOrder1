package com.example.cafeteriafoodpreorder

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "Order Placed",
    val timestamp: Long = 0L,
    val pickupTime: Long = 0L
)

data class OrderItem(
    val foodId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1
)