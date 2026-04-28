package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kisansetu.app.databinding.ItemFarmBinding;
import com.kisansetu.app.models.User;
import java.util.List;

public class FarmAdapter extends RecyclerView.Adapter<FarmAdapter.FarmViewHolder> {

    private List<User> farmList;
    private OnFarmClickListener listener;

    public interface OnFarmClickListener {
        void onFarmClick(User farm);
    }

    public FarmAdapter(List<User> farmList, OnFarmClickListener listener) {
        this.farmList = farmList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFarmBinding binding = ItemFarmBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FarmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmViewHolder holder, int position) {
        User farm = farmList.get(position);
        holder.bind(farm);
    }

    @Override
    public int getItemCount() {
        return farmList.size();
    }

    class FarmViewHolder extends RecyclerView.ViewHolder {
        private ItemFarmBinding binding;

        public FarmViewHolder(ItemFarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User farm) {
            binding.farmNameTextView.setText(farm.getFarmName() != null ? farm.getFarmName() : "My Farm");
            binding.farmLocationTextView.setText(farm.getAddress());
            
            if (farm.getProfileImage() != null && !farm.getProfileImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(farm.getProfileImage())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.farmImageView);
            } else {
                binding.farmImageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            itemView.setOnClickListener(v -> listener.onFarmClick(farm));
        }
    }
}
