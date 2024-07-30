package com.example.onlineshopapp.Activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Adapter.OrderStatusAdapter;
import com.example.onlineshopapp.Adapter.ProductOrderDetailAdapter;
import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.Model.Status;
import com.example.onlineshopapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvCustomerName, tvAddress, tvPhone, tvPaymentMethod, tvTotalPrice;
    private RecyclerView recyclerViewProducts, recyclerViewStatuses;
    private ProductOrderDetailAdapter productOrderDetailAdapter;
    private List<Product> productList;
    private OrderStatusAdapter statusAdapter;
    private List<Status> statusList;
    private Order order;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewStatuses = findViewById(R.id.recyclerViewStatuses);

        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productOrderDetailAdapter = new ProductOrderDetailAdapter(this, productList);
        recyclerViewProducts.setAdapter(productOrderDetailAdapter);

        recyclerViewStatuses.setLayoutManager(new LinearLayoutManager(this));

        Order order = (Order) getIntent().getSerializableExtra("order");

        statusList = new ArrayList<>();
        statusList.add(new Status("Chờ duyệt", order.getOrder_date()));
        statusList.add(new Status("Đã duyệt", order.getApproval_date()));
        statusList.add(new Status("Đang giao hàng", order.getDelivery_date()));
        statusList.add(new Status("Đã hoàn thành", order.getCompletion_date()));
        statusList.add(new Status("Đã hủy", order.getCancellation_date()));

        statusAdapter = new OrderStatusAdapter(this, statusList);
        recyclerViewStatuses.setAdapter(statusAdapter);

        if (order != null) {
            tvOrderId.setText(order.getId());
            tvOrderDate.setText(order.getOrder_date());
            tvOrderStatus.setText(getOrderStatus(order));
            tvCustomerName.setText(order.getCustomer_name());
            tvAddress.setText(order.getAddress());
            tvPhone.setText(order.getPhone());
            tvPaymentMethod.setText(order.getPayment_method());
            tvTotalPrice.setText(convertToVND(String.valueOf(order.getTotal_price())));

            loadProducts(order);
        }
    }

    private String getOrderStatus(Order order) {
        if (order.getCancellation_date() != null && !order.getCancellation_date().isEmpty()) {
            return "Đã hủy";
        } else if (order.getCompletion_date() != null && !order.getCompletion_date().isEmpty()) {
            return "Đã hoàn thành";
        } else if (order.getDelivery_date() != null && !order.getDelivery_date().isEmpty()) {
            return "Đang giao hàng";
        } else if (order.getApproval_date() != null && !order.getApproval_date().isEmpty()) {
            return "Đã duyệt";
        } else {
            return "Chờ duyệt";
        }
    }

    private void loadProducts(Order order) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");

        for (String productId : order.getProducts().keySet()) {
            productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(snapshot.getKey());
                        product.setQuantity(order.getProducts().get(productId).getQuantity());
                        productList.add(product);
                        productOrderDetailAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if needed
                }
            });
        }
    }

    public String convertToVND(String priceString) {
        String cleanPriceString = priceString.replaceAll("[^\\d]", "");
        long priceLong = Long.parseLong(cleanPriceString);
        String vndString = String.format("%,d", priceLong) + " VND";

        return vndString;
    }
}
