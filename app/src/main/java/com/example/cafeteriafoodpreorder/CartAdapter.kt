package com.example.cafeteriafoodpreorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CartAdapter(
    private var items: List<CartItem>,
    private val onQuantityChanged: (CartItem, Int) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.btnDecrease.setOnClickListener {
            onQuantityChanged(item, item.quantity - 1)
        }
        holder.btnIncrease.setOnClickListener {
            onQuantityChanged(item, item.quantity + 1)
        }
        holder.btnRemove.setOnClickListener {
            onRemove(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newList: List<CartItem>) {
        items = newList
        notifyDataSetChanged()
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCartImage: ImageView = itemView.findViewById(R.id.ivCartImage)
        val tvName: TextView = itemView.findViewById(R.id.tvCartItemName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCartItemPrice)
        val tvTotal: TextView = itemView.findViewById(R.id.tvCartItemTotal)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val btnDecrease: TextView = itemView.findViewById(R.id.btnDecreaseQty)
        val btnIncrease: TextView = itemView.findViewById(R.id.btnIncreaseQty)
        val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(item: CartItem) {
            tvName.text = item.name
            tvPrice.text = String.format("₱%.2f", item.price)
            tvTotal.text = String.format("Total: ₱%.2f", item.totalPrice)
            tvQuantity.text = item.quantity.toString()

            if (item.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivCartImage)
            } else {
                ivCartImage.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }
    }
}