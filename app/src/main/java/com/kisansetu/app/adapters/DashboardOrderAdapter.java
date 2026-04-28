package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kisansetu.app.databinding.ItemDashboardOrderBinding;
import com.kisansetu.app.models.CartItem;
import com.kisansetu.app.models.Order;
import java.util.List;

public class DashboardOrderAdapter extends RecyclerView.Adapter<DashboardOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onAccept(Order order);
        void onReject(Order order);
        void onSelfDelivery(Order order);
        void onAppDelivery(Order order);
    }

    public DashboardOrderAdapter(List<Order> orderList, OnOrderActionListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDashboardOrderBinding binding = ItemDashboardOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ItemDashboardOrderBinding binding;

        public OrderViewHolder(ItemDashboardOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            // For now using placeholder for customer name as it might not be in Order model
            binding.customerNameTextView.setText("Customer: " + (order.getCustomerId().length() > 8 ? order.getCustomerId().substring(0, 8) : order.getCustomerId()));
            
            StringBuilder products = new StringBuilder();
            for (CartItem item : order.getItems()) {
                products.append(item.getProductName()).append(", ");
            }
            if (products.length() > 2) products.setLength(products.length() - 2);
            binding.productNameTextView.setText(products.toString());
            
            int totalQty = 0;
            for (CartItem item : order.getItems()) totalQty += item.getQuantity();
            binding.quantityTextView.setText("Qty: " + totalQty);
            
            binding.priceTextView.setText("₹ " + order.getTotalAmount());
            binding.orderTypeTextView.setText("Type: Delivery"); // Default

            if ("Pending".equals(order.getStatus())) {
                binding.pendingActions.setVisibility(View.VISIBLE);
                binding.deliveryActions.setVisibility(View.GONE);
            } else if ("Accepted".equals(order.getStatus())) {
                binding.pendingActions.setVisibility(View.GONE);
                binding.deliveryActions.setVisibility(View.VISIBLE);
            } else {
                binding.pendingActions.setVisibility(View.GONE);
                binding.deliveryActions.setVisibility(View.GONE);
            }

            binding.acceptButton.setOnClickListener(v -> listener.onAccept(order));
            binding.rejectButton.setOnClickListener(v -> listener.onReject(order));
            binding.selfDeliveryButton.setOnClickListener(v -> listener.onSelfDelivery(order));
            binding.appDeliveryButton.setOnClickListener(v -> listener.onAppDelivery(order));
        }
    }
}
