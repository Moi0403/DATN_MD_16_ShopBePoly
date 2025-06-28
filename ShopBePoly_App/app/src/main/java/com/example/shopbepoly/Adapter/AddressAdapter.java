package com.example.shopbepoly.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopbepoly.DTO.Address;
import com.example.shopbepoly.R;
import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    public interface AddressListener {
        void onEdit(Address address);
        void onDelete(Address address);
        void onSetDefault(Address address);
    }
    private List<Address> addressList;
    private AddressListener listener;
    private Context context;

    public AddressAdapter(Context context, List<Address> addressList, AddressListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.tvName.setText(address.getName());
        holder.tvPhone.setText("SĐT: " + address.getPhone());
        holder.tvAddress.setText(address.getAddress());
        holder.tvLabel.setText(address.getLabel());
        holder.tvDefault.setVisibility(address.isDefault() ? View.VISIBLE : View.GONE);
        holder.checkboxDefault.setChecked(address.isDefault());
        holder.checkboxDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !address.isDefault()) {
                listener.onSetDefault(address);
            }
        });
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(address));
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvAddress, tvLabel, tvDefault;
        CheckBox checkboxDefault;
        ImageButton btnEdit, btnDelete;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDefault = itemView.findViewById(R.id.tvDefault);
            checkboxDefault = itemView.findViewById(R.id.checkboxDefault);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 