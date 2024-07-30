package com.example.onlineshopapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.onlineshopapp.Adapter.Products_Adapter;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchProductActivity extends AppCompatActivity {

    String q;
    ArrayList<Product> productArrayList = new ArrayList<>();
    Products_Adapter productsAdapter;
    RecyclerView recyclerView_products;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView_products = findViewById(R.id.recyclerView_products);
        q = getIntent().getStringExtra("query");
        getSupportActionBar().setTitle("Kết quả cho: " + q);
        getProducts();

    }

    public void getProducts()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Products");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Product product = userSnapshot.getValue(Product.class);
                    product.setId(userSnapshot.getKey());
                    if (product.getName().toLowerCase().contains(q.toLowerCase())) {
                        productArrayList.add(product);
                    }

                }

                productArrayList.sort((o1, o2) -> o2.getCreated_at().compareTo(o1.getCreated_at()));

                productsAdapter = new Products_Adapter(productArrayList, SearchProductActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(SearchProductActivity.this, 2);
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
}