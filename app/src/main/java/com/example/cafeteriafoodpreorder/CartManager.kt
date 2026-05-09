package com.example.cafeteriafoodpreorder

object CartManager {
    private val cartItems = mutableListOf<CartItem>()

    fun addItem(foodItem: FoodItem) {
        val existing = cartItems.find { it.foodId == foodItem.id }
        if (existing != null) {
            existing.quantity++
        } else {
            cartItems.add(
                CartItem(
                    foodId = foodItem.id,
                    name = foodItem.name,
                    price = foodItem.price,
                    imageUrl = foodItem.imageUrl,
                    quantity = 1
                )
            )
        }
    }
    fun getItems(): List<CartItem> = cartItems.toList()

    fun updateQuantity(foodId: String, newQuantity: Int) {
        val item = cartItems.find { it.foodId == foodId }
        if (item != null) {
            if (newQuantity <= 0) {
                cartItems.remove(item)
            } else {
                item.quantity = newQuantity
            }
        }
    }

    fun removeItem(foodId: String) {
        cartItems.removeAll { it.foodId == foodId }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getTotalPrice(): Double = cartItems.sumOf { it.totalPrice }
}