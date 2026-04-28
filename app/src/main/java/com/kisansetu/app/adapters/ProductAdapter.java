package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kisansetu.app.databinding.ItemProductBinding;
import com.kisansetu.app.models.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onDeleteClick(Product product);
        void onItemClick(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.productNameTextView.setText(product.getName());
            binding.productPriceBadge.setText("₹" + (int)product.getPrice() + "/kg");
            binding.productWeightTextView.setVisibility(View.GONE);
            
            // Calculate dummy MRP and Discount for visual parity with screenshot
            double mrp = product.getPrice() * 1.5;
            double discount = mrp - product.getPrice();
            
            binding.productMrpTextView.setText("MRP ₹" + (int)mrp);
            binding.productDiscountTextView.setText("₹" + (int)discount + " OFF");
            binding.deliveryTimeTextView.setText("20 mins"); // Dummy for now

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.productImageView);

            binding.pickButton.setOnClickListener(v -> listener.onItemClick(product));
            itemView.setOnClickListener(v -> listener.onItemClick(product));
        }
    }
}
