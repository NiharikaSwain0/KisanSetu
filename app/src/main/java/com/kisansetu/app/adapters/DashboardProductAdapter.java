package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kisansetu.app.databinding.ItemDashboardProductBinding;
import com.kisansetu.app.models.Product;
import java.util.List;

public class DashboardProductAdapter extends RecyclerView.Adapter<DashboardProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onRemove(Product product);
        void onClose(Product product);
    }

    public DashboardProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDashboardProductBinding binding = ItemDashboardProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ItemDashboardProductBinding binding;

        public ProductViewHolder(ItemDashboardProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.productNameTextView.setText(product.getName());
            binding.priceTextView.setText("₹ " + product.getPrice());
            binding.quantityTextView.setText("Available: " + product.getQuantity());
            binding.harvestTimeTextView.setText("Harvest: " + (product.getHarvestTime() != null ? product.getHarvestTime() : "Not Set"));

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.productImageView);
            } else {
                binding.productImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            if (product.isClosed()) {
                binding.closeButton.setImageResource(android.R.drawable.ic_lock_idle_lock);
                itemView.setAlpha(0.6f);
            } else {
                binding.closeButton.setImageResource(android.R.drawable.ic_lock_lock);
                itemView.setAlpha(1.0f);
            }

            binding.editButton.setOnClickListener(v -> listener.onEdit(product));
            binding.removeButton.setOnClickListener(v -> listener.onRemove(product));
            binding.closeButton.setOnClickListener(v -> listener.onClose(product));
        }
    }
}
