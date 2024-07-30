package com.example.onlineshopapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.example.onlineshopapp.Adapter.AddressAdapter;
import com.example.onlineshopapp.Model.Address;
import com.example.onlineshopapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddressBookActivity extends AppCompatActivity implements AddressAdapter.OnAddressSelectedListener {

    private RecyclerView recyclerViewAddresses;
    private AddressAdapter addressAdapter;
    FloatingActionButton btnAddAddress;
    FirebaseAuth auth;
    FirebaseUser user;
    ValueEventListener eventListener;
    List<Address> addressList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerViewAddresses = findViewById(R.id.recyclerViewAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        recyclerViewAddresses.setLayoutManager(new LinearLayoutManager(this));

        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(AddressBookActivity.this, addressList, this);
        recyclerViewAddresses.setAdapter(addressAdapter);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        displayUserAddresses();

        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddressBookActivity.this, AddAddressActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayUserAddresses() {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("address");
        eventListener = addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                        Address address = addressSnapshot.getValue(Address.class);
                        if (address != null) {
                            address.setId(addressSnapshot.getKey());
                            addressList.add(address);
                        }
                    }
                }

                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAddressSelected(Address address) {

    }
}