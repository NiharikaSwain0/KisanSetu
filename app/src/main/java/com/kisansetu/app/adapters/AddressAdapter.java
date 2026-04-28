package com.kisansetu.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.kisansetu.app.R;
import com.kisansetu.app.models.AddressModel;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<AddressModel> addressList;
    private int selectedPosition = 0;
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(AddressModel address);
    }

    public AddressAdapter(List<AddressModel> addressList, OnAddressSelectedListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressModel address = addressList.get(position);
        holder.label.setText(address.getLabel());
        holder.fullAddress.setText(address.getFullAddress());
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            listener.onAddressSelected(address);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public AddressModel getSelectedAddress() {
        if (addressList.isEmpty()) return null;
        return addressList.get(selectedPosition);
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView label, fullAddress;
        RadioButton radioButton;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.addressLabelText);
            fullAddress = itemView.findViewById(R.id.fullAddressText);
            radioButton = itemView.findViewById(R.id.addressRadioButton);
        }
    }
}