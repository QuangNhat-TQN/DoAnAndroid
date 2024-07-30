package com.example.onlineshopapp.Activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Adapter.ThongKeOrderAdapter;
import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongKeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ThongKeOrderAdapter orderAdapter;
    private List<Order> orderList;
    private TextView noDataTextView, tvTotalPrice;
    private Button btnSelectStartDate, btnSelectEndDate, btnViewAllOrders;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        noDataTextView = findViewById(R.id.noDataTextView);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnViewAllOrders = findViewById(R.id.btnViewAllOrders);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        orderList = new ArrayList<>();
        orderAdapter = new ThongKeOrderAdapter(orderList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderAdapter);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Load tất cả đơn hàng khi activity khởi tạo
        loadAllOrders();

        btnSelectStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(btnSelectStartDate);
            }
        });

        btnSelectEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(btnSelectEndDate);
            }
        });

        btnViewAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllOrders();
            }
        });
    }

    private void loadAllOrders() {
        Query allOrdersQuery = FirebaseDatabase.getInstance().getReference("Orders");
        allOrdersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    order.setId(dataSnapshot.getKey());
                    orderList.add(order);
                }
                orderAdapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    noDataTextView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    noDataTextView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

                // Cập nhật tổng giá
                long totalPrice = 0;
                for (Order order : orderList) {
                    totalPrice += Integer.parseInt(String.valueOf(order.getTotal_price()));
                }
                tvTotalPrice.setText("Tổng tiền: " + convertToVND(String.valueOf(totalPrice)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showDatePickerDialog(final Button button) {
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Tạo DatePickerDialog và hiển thị
        DatePickerDialog datePickerDialog = new DatePickerDialog(ThongKeActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Chuyển đổi giá trị ngày, tháng, năm thành chuỗi với định dạng "dd/MM/yyyy"
                        String dayString = String.format("%02d", dayOfMonth);
                        String monthString = String.format("%02d", month + 1);
                        String yearString = String.valueOf(year);

                        // Cập nhật ngày đã chọn trên nút tương ứng
                        button.setText(dayString + "/" + monthString + "/" + yearString);

                        // Kiểm tra nếu cả hai ngày bắt đầu và kết thúc đã được chọn
                        if (!btnSelectStartDate.getText().toString().isEmpty() && !btnSelectEndDate.getText().toString().isEmpty()) {
                            // Lọc dữ liệu theo khoảng thời gian đã chọn
                            String startDateStr = btnSelectStartDate.getText().toString();
                            String endDateStr = btnSelectEndDate.getText().toString();

                            try {
                                Date startDate = dateFormat.parse(startDateStr);
                                Date endDate = dateFormat.parse(endDateStr);

                                if (startDate != null && endDate != null) {
                                    // Chuyển đổi ngày thành chuỗi định dạng cho Firebase Database
                                    SimpleDateFormat firebaseDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                                    String startAt = firebaseDateFormat.format(startDate);
                                    String endAt = firebaseDateFormat.format(endDate);

                                    // Lọc dữ liệu theo khoảng thời gian đã chọn
                                    Query filteredQuery = FirebaseDatabase.getInstance().getReference("Orders")
                                            .orderByChild("completion_date")
                                            .startAt(startAt)
                                            .endAt(endAt);

                                    // Thực hiện lại truy vấn với điều kiện lọc mới
                                    filteredQuery.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            orderList.clear();
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                Order order = dataSnapshot.getValue(Order.class);
                                                order.setId(dataSnapshot.getKey());
                                                orderList.add(order);
                                            }
                                            orderAdapter.notifyDataSetChanged();

                                            if (orderList.isEmpty()) {
                                                noDataTextView.setVisibility(View.VISIBLE);
                                                recyclerView.setVisibility(View.GONE);
                                            } else {
                                                noDataTextView.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                            }

                                            // Cập nhật tổng giá
                                            long totalPrice = 0;
                                            for (Order order : orderList) {
                                                totalPrice += Integer.parseInt(String.valueOf(order.getTotal_price())); // assuming order.getPrice() returns a String
                                            }
                                            tvTotalPrice.setText("Tổng tiền: " + convertToVND(String.valueOf(totalPrice)));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Xử lý lỗi
                                        }
                                    });
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.show();
    }

    public String convertToVND(String priceString) {
        String cleanPriceString = priceString.replaceAll("[^\\d]", "");
        long priceLong = Long.parseLong(cleanPriceString);
        return String.format("%,d VND", priceLong);
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
