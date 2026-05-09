package com.example.cafeteriafoodpreorder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MenuActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvMenu: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var adapter: FoodAdapter
    private val fullFoodList = mutableListOf<FoodItem>()   // original data from Firestore
    private val filteredList = mutableListOf<FoodItem>()   // displayed after filter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        rvMenu = findViewById(R.id.rvMenu)
        progressBar = findViewById(R.id.progressBar)
        etSearch = findViewById(R.id.etSearch)

        rvMenu.layoutManager = LinearLayoutManager(this)
        // Use filteredList for adapter
        adapter = FoodAdapter(filteredList) { foodItem ->
            addToCart(foodItem)
        }
        rvMenu.adapter = adapter

        // Set up search listener
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadMenuItems()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> true
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, OrderHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadMenuItems() {
        progressBar.visibility = ProgressBar.VISIBLE
        db.collection("menu")
            .whereEqualTo("available", true)
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = ProgressBar.GONE
                val items = result.documents.mapNotNull { doc ->
                    doc.toObject(FoodItem::class.java)?.copy(id = doc.id)
                }
                fullFoodList.clear()
                fullFoodList.addAll(items)
                filterItems("") // show all items initially
                if (fullFoodList.isEmpty()) {
                    Toast.makeText(this, "No items available", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(this, "Error loading menu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterItems(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(fullFoodList)
        } else {
            val lowerQuery = query.lowercase()
            for (item in fullFoodList) {
                if (item.name.lowercase().contains(lowerQuery) ||
                    item.description.lowercase().contains(lowerQuery) ||
                    item.category.lowercase().contains(lowerQuery)) {
                    filteredList.add(item)
                }
            }
        }
        adapter.updateList(filteredList)
        if (filteredList.isEmpty() && query.isNotEmpty()) {
            Toast.makeText(this, "No matching items", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToCart(foodItem: FoodItem) {
        CartManager.addItem(foodItem)
        Toast.makeText(this, "${foodItem.name} added to cart", Toast.LENGTH_SHORT).show()
    }
}