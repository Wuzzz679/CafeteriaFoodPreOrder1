package com.example.cafeteriafoodpreorder

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvOrders: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyOrders: TextView
    private lateinit var adapter: OrderAdapter
    private lateinit var btnBackToMenu: MaterialButton
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Orders"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvOrders = findViewById(R.id.rvOrders)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)

        rvOrders.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(orderList) { order ->
            Toast.makeText(this, "Order status: ${order.status}", Toast.LENGTH_SHORT).show()
        }
        rvOrders.adapter = adapter


        btnBackToMenu.setOnClickListener {
            finish()
        }

        loadUserOrders()
    }

    private fun loadUserOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = ProgressBar.GONE
                val orders = result.documents.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                }
                orderList.clear()
                orderList.addAll(orders)
                adapter.updateList(orderList)

                if (orderList.isEmpty()) {
                    tvEmptyOrders.visibility = TextView.VISIBLE
                    rvOrders.visibility = RecyclerView.GONE
                } else {
                    tvEmptyOrders.visibility = TextView.GONE
                    rvOrders.visibility = RecyclerView.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}