package com.example.cafeteriafoodpreorder

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class CartActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvCart: RecyclerView
    private lateinit var adapter: CartAdapter
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnPlaceOrder: MaterialButton
    private lateinit var btnBackToMenu: MaterialButton  // added
    private lateinit var tvEmptyCart: TextView
    private var cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Your Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvCart = findViewById(R.id.rvCart)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        tvEmptyCart = findViewById(R.id.tvEmptyCart)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)  // initialize

        rvCart.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter(cartItems,
            { item, newQuantity ->
                CartManager.updateQuantity(item.foodId, newQuantity)
                refreshCart()
            },
            { item ->
                CartManager.removeItem(item.foodId)
                refreshCart()
            }
        )
        rvCart.adapter = adapter

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }

        // Back to Menu button click listener
        btnBackToMenu.setOnClickListener {
            finish()  // returns to MenuActivity
        }

        refreshCart()
    }

    private fun refreshCart() {
        cartItems.clear()
        cartItems.addAll(CartManager.getItems())
        adapter.updateList(cartItems)
        updateTotal()
        if (cartItems.isEmpty()) {
            tvEmptyCart.visibility = TextView.VISIBLE
            btnPlaceOrder.isEnabled = false
        } else {
            tvEmptyCart.visibility = TextView.GONE
            btnPlaceOrder.isEnabled = true
        }
    }

    private fun updateTotal() {
        val total = CartManager.getTotalPrice()
        tvTotalPrice.text = "Total: ₱${String.format("%.2f", total)}"
    }

    private fun placeOrder() {
        val userId = auth.currentUser?.uid ?: return
        val orderId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val itemsMap = cartItems.map { item ->
            mapOf(
                "foodId" to item.foodId,
                "name" to item.name,
                "price" to item.price,
                "quantity" to item.quantity
            )
        }

        val order = hashMapOf(
            "orderId" to orderId,
            "userId" to userId,
            "items" to itemsMap,
            "totalAmount" to CartManager.getTotalPrice(),
            "status" to "Order Placed",
            "timestamp" to timestamp,
            "pickupTime" to (timestamp + 30 * 60 * 1000) // 30 min from now
        )

        db.collection("orders").document(orderId)
            .set(order)
            .addOnSuccessListener {
                CartManager.clearCart()
                refreshCart()
                Toast.makeText(this, "Order placed! Pickup in 30 min.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}