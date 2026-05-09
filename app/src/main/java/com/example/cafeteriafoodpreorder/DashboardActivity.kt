package com.example.cafeteriafoodpreorder

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvAvgOrder: TextView
    private lateinit var tvMostOrdered: TextView
    private lateinit var btnBackToMenu: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar = findViewById(R.id.progressBar)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvAvgOrder = findViewById(R.id.tvAvgOrder)
        tvMostOrdered = findViewById(R.id.tvMostOrdered)
        btnBackToMenu = findViewById(R.id.btnBackToMenu)

        // Back to Menu button click
        btnBackToMenu.setOnClickListener {
            finish()  // returns to MenuActivity
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = ProgressBar.GONE

                val orders = result.documents
                val totalOrders = orders.size
                val totalSpent = orders.sumOf { it.getDouble("totalAmount") ?: 0.0 }
                val avgOrder = if (totalOrders > 0) totalSpent / totalOrders else 0.0

                tvTotalOrders.text = totalOrders.toString()
                tvTotalSpent.text = "₱${String.format("%.2f", totalSpent)}"
                tvAvgOrder.text = "₱${String.format("%.2f", avgOrder)}"

                // Calculate most ordered item
                val itemCount = mutableMapOf<String, Int>()
                orders.forEach { doc ->
                    val items = doc.get("items") as? List<*> ?: return@forEach
                    items.forEach { item ->
                        val map = item as? Map<*, *>
                        val name = map?.get("name") as? String
                        val qty = (map?.get("quantity") as? Long)?.toInt() ?: 0
                        if (name != null) {
                            itemCount[name] = itemCount.getOrDefault(name, 0) + qty
                        }
                    }
                }

                val mostOrdered = itemCount.maxByOrNull { it.value }
                tvMostOrdered.text = mostOrdered?.key ?: "None"
            }
            .addOnFailureListener { e ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Failed to load: ${e.message}", Toast.LENGTH_SHORT).show()
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