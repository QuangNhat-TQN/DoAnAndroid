package com.example.onlineshopapp.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Activity.UpdateAddressActivity;
import com.example.onlineshopapp.Model.Address;
import com.example.onlineshopapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context mContext;
    private List<Address> mAddressList;
    private OnAddressSelectedListener onAddressSelectedListener;

    public AddressAdapter(Context context, List<Address> addressList, OnAddressSelectedListener listener) {
        mContext = context;
        mAddressList = addressList;
        onAddressSelectedListener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = mAddressList.get(position);
        if (address != null) {
            holder.bind(address);
        }
    }

    @Override
    public int getItemCount() {
        return mAddressList.size();
    }

    public class AddressViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewAddress;
        private ImageView btnUpdate;
        private ImageView btnDelete;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Address address) {
            String addressString = "Họ và tên: " + address.getFull_name() + "\n"
                    + "Địa chỉ: " + address.getAddress_detail() + "\n"
                    + "Số điện thoại: " + address.getPhone();
            textViewAddress.setText(addressString);

            btnUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UpdateAddressActivity.class);
                    intent.putExtra("addressId", address.getId());
                    mContext.startActivity(intent);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(address);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onAddressSelectedListener != null) {
                        onAddressSelectedListener.onAddressSelected(address);
                    }
                }
            });
        }

        private void showDeleteConfirmationDialog(Address address) {
            new AlertDialog.Builder(mContext)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
                    .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteAddress(address);
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        private void deleteAddress(Address address) {
            DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("address")
                    .child(address.getId());

            addressRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(mContext, "Xóa địa chỉ thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Xóa địa chỉ thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public interface OnAddressSelectedListener {
        void onAddressSelected(Address address);
    }
}



