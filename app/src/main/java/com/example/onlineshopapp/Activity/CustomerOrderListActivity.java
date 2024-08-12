package com.example.onlineshopapp.Activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Adapter.AdminOrderAdapter;
import com.example.onlineshopapp.Adapter.CustomerOrderAdapter;
import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private CustomerOrderAdapter customerOrderAdapter;
    private List<Order> orderList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Query ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        customerOrderAdapter = new CustomerOrderAdapter(this, orderList);
        recyclerViewOrders.setAdapter(customerOrderAdapter);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
            loadOrders();
        }
    }

    private void loadOrders() {
        ordersRef.orderByChild("user_id").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        order.setId(orderSnapshot.getKey());
                        orderList.add(order);
                    }
                }
                orderList.sort((o1, o2) -> o2.getOrder_date().compareTo(o1.getOrder_date()));
                customerOrderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
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
}
