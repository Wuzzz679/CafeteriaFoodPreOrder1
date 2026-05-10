package com.example.cafeteriafoodpreorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(
    private var orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onOrderClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateList(newList: List<Order>) {
        orders = newList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tvOrderTime)
        private val tvPickupTime: TextView = itemView.findViewById(R.id.tvPickupTime)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)   // added

        fun bind(order: Order) {
            tvOrderId.text = "Order #${order.orderId.take(8)}"
            tvOrderStatus.text = order.status

            val statusColorRes = when (order.status) {
                "Order Placed" -> R.color.status_placed
                "Preparing" -> R.color.status_preparing
                "Ready for Pickup" -> R.color.status_ready
                "Completed" -> R.color.status_completed
                else -> R.color.purple_500
            }
            tvOrderStatus.setBackgroundColor(ContextCompat.getColor(itemView.context, statusColorRes))

            val itemsString = order.items.joinToString(", ") { "${it.name} (${it.quantity})" }
            tvOrderItems.text = itemsString

            tvOrderTotal.text = "Total: ₱${String.format("%.2f", order.totalAmount)}"

            val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            tvOrderTime.text = "Placed: ${dateFormat.format(Date(order.timestamp))}"
            tvPickupTime.text = "Pickup: ${dateFormat.format(Date(order.pickupTime))}"


            val paymentText = if (order.paymentMethod.isNotEmpty()) order.paymentMethod else "Payment: Not specified"
            tvPaymentMethod.text = paymentText
        }
    }
}