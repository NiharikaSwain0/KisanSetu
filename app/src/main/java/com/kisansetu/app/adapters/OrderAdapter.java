package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kisansetu.app.databinding.ItemOrderBinding;
import com.kisansetu.app.models.CartItem;
import com.kisansetu.app.models.Order;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onAcceptClick(Order order);
        void onRejectClick(Order order);
        void onSelfDeliveryClick(Order order);
        void onAssignRiderClick(Order order);
        void onItemClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private ItemOrderBinding binding;

        public OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.orderIdTextView.setText("Order #" + order.getOrderId());
            binding.orderAmountTextView.setText("Total: ₹ " + order.getTotalAmount());
            binding.orderStatusTextView.setText("Status: " + order.getStatus());

            StringBuilder itemsStr = new StringBuilder();
            for (CartItem item : order.getItems()) {
                itemsStr.append(item.getProductName()).append(" x ").append(item.getQuantity()).append(", ");
            }
            if (itemsStr.length() > 2) {
                itemsStr.setLength(itemsStr.length() - 2);
            }
            binding.orderItemsTextView.setText(itemsStr.toString());

            if ("Pending".equals(order.getStatus())) {
                binding.actionLayout.setVisibility(View.VISIBLE);
                binding.pendingActions.setVisibility(View.VISIBLE);
                binding.deliveryActions.setVisibility(View.GONE);
            } else if ("Accepted".equals(order.getStatus())) {
                binding.actionLayout.setVisibility(View.VISIBLE);
                binding.pendingActions.setVisibility(View.GONE);
                binding.deliveryActions.setVisibility(View.VISIBLE);
            } else {
                binding.actionLayout.setVisibility(View.GONE);
            }

            binding.acceptButton.setOnClickListener(v -> listener.onAcceptClick(order));
            binding.rejectButton.setOnClickListener(v -> listener.onRejectClick(order));
            binding.selfDeliveryButton.setOnClickListener(v -> listener.onSelfDeliveryClick(order));
            binding.assignRiderButton.setOnClickListener(v -> listener.onAssignRiderClick(order));
            itemView.setOnClickListener(v -> listener.onItemClick(order));
        }
    }
}
