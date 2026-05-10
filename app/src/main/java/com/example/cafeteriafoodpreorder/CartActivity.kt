package com.example.cafeteriafoodpreorder

import android.os.Bundle
import android.widget.RadioGroup
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
    private lateinit var btnBackToMenu: MaterialButton
    private lateinit var tvEmptyCart: TextView
    private lateinit var rgPaymentMethod: RadioGroup   // added
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
        btnBackToMenu = findViewById(R.id.btnBackToMenu)
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)  // initialize

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

        btnBackToMenu.setOnClickListener {
            finish()
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
        // Check if a payment method is selected
        val selectedId = rgPaymentMethod.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentMethod = when (selectedId) {
            R.id.rbCounter -> "Pay at Counter"
            R.id.rbOnline -> "Online Payment"
            else -> "Unknown"
        }

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
            "pickupTime" to (timestamp + 30 * 60 * 1000), // 30 min from now
            "paymentMethod" to paymentMethod              // added field
        )

        db.collection("orders").document(orderId)
            .set(order)
            .addOnSuccessListener {
                CartManager.clearCart()
                refreshCart()
                val message = if (paymentMethod == "Online Payment") {
                    "Order placed! Proceed to online payment."
                } else {
                    "Order placed! Pay at counter on pickup."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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