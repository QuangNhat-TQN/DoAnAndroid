package com.example.onlineshopapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.onlineshopapp.Adapter.Products_Adapter;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ProductTypeActivity extends AppCompatActivity {
    ArrayList<Product> productArrayList = new ArrayList<>();
    Products_Adapter productsAdapter;
    RecyclerView recyclerView_products;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_type);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addControls();
        Intent intent = getIntent();
        String categories = intent.getStringExtra("categories");
        String img = intent.getStringExtra("url_image");
        Glide.with(this).load(img).into(imageView);
        FirebaseApp.initializeApp(getApplicationContext());
        getDataFromFirebase_Products(categories);
        addEvents();
    }

    private void addControls() {
        recyclerView_products = findViewById(R.id.recyclerView_products_type);
        imageView = findViewById(R.id.imageView_products_type);
    }
    public void getDataFromFirebase_Products(String categories)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Products");

        myRef.orderByChild("category_id").equalTo(categories).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Product user = userSnapshot.getValue(Product.class);
                    user.setId(userSnapshot.getKey());
                    productArrayList.add(user);
                }
                Collections.reverse(productArrayList);

                recyclerView_products.addItemDecoration(new DividerItemDecoration(ProductTypeActivity.this, LinearLayoutManager.HORIZONTAL));
                productsAdapter = new Products_Adapter(productArrayList, ProductTypeActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(ProductTypeActivity.this, 2);
                recyclerView_products.setLayoutManager(layoutManager);
                recyclerView_products.setItemAnimator(new DefaultItemAnimator());
                recyclerView_products.setAdapter(productsAdapter);

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
    private void addEvents() {

    }
}