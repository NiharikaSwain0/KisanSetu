package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kisansetu.app.databinding.ItemCartBinding;
import com.kisansetu.app.models.CartItem;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onDeleteClick(CartItem cartItem);
    }

    public CartAdapter(List<CartItem> cartItemList, OnCartItemClickListener listener) {
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ItemCartBinding binding;

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem cartItem) {
            binding.productNameTextView.setText(cartItem.getProductName());
            binding.productPriceTextView.setText("₹ " + cartItem.getPrice() + " x " + cartItem.getQuantity());
            binding.totalPriceTextView.setText("₹ " + (cartItem.getPrice() * cartItem.getQuantity()));

            binding.deleteCartItemButton.setOnClickListener(v -> listener.onDeleteClick(cartItem));
        }
    }
}
