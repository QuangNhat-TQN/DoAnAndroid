package com.example.onlineshopapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.onlineshopapp.Model.Address;
import com.example.onlineshopapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateAddressActivity extends AppCompatActivity {

    private EditText edtFullName, edtAddressDetail, edtPhone;
    private Button btnSave;
    private String addressId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_address);

        edtFullName = findViewById(R.id.edtFullName);
        edtAddressDetail = findViewById(R.id.edtAddress);
        edtPhone = findViewById(R.id.edtPhone);
        btnSave = findViewById(R.id.btnSave);

        addressId = getIntent().getStringExtra("addressId");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAddress();
            }
        });

        loadAddressData();
    }

    private void loadAddressData() {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("address")
                .child(addressId);

        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Address address = snapshot.getValue(Address.class);
                    if (address != null) {
                        edtFullName.setText(address.getFull_name());
                        edtAddressDetail.setText(address.getAddress_detail());
                        edtPhone.setText(address.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateAddressActivity.this, "Failed to load address data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAddress() {
        String fullName = edtFullName.getText().toString().trim();
        String addressDetail = edtAddressDetail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (fullName.isEmpty() || addressDetail.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Address updatedAddress = new Address(addressDetail, fullName, phone);
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("address")
                .child(addressId);

        addressRef.setValue(updatedAddress).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UpdateAddressActivity.this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(UpdateAddressActivity.this, "Cập nhật địa chỉ thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
