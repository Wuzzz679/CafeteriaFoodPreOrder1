package com.example.cafeteriafoodpreorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FoodAdapter(
    private var foodList: List<FoodItem>,
    private val onAddToCart: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food)
        holder.btnAddToCart.setOnClickListener {
            onAddToCart(food)
        }
    }

    override fun getItemCount() = foodList.size

    fun updateList(newList: List<FoodItem>) {
        foodList = newList
        notifyDataSetChanged()
    }

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvFoodDescription)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvFoodPrice)
        private val ivImage: ImageView = itemView.findViewById(R.id.ivFoodImage)
        val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)

        fun bind(food: FoodItem) {
            tvName.text = food.name
            tvDescription.text = food.description
            tvPrice.text = String.format("₱%.2f", food.price)
            // Load image using Glide (add dependency)
            if (food.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(food.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivImage)
            }
        }
    }
}