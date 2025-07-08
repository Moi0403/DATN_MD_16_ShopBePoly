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
        void onSelect(Address address);
    }
    private List<Address> addressList;
    private AddressListener listener;
    private Context context;
    private boolean isDeleting = false;

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
        holder.checkboxDefault.setOnCheckedChangeListener(null);
        holder.checkboxDefault.setChecked(address.isDefault());
        holder.checkboxDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isDeleting) return;
            if (isChecked && !address.isDefault()) {
                listener.onSetDefault(address);
            } else if (!isChecked && address.isDefault()) {
                boolean hasOtherDefault = false;
                for (Address a : addressList) {
                    if (!a.getId().equals(address.getId()) && a.isDefault()) {
                        hasOtherDefault = true;
                        break;
                    }
                }
                if (!hasOtherDefault) {
                    buttonView.setChecked(true);
                    android.widget.Toast.makeText(context, "Bạn phải chọn một địa chỉ khác làm mặc định trước!", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(address));
        holder.btnDelete.setOnClickListener(v -> {
            isDeleting = true;
            listener.onDelete(address);
            isDeleting = false;
        });
        holder.itemView.setOnClickListener(v -> {
            if (!isDeleting) listener.onSelect(address);
        });
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