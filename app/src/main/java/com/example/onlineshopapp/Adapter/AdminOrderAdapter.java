package com.example.onlineshopapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Activity.OrderDetailActivity;
import com.example.onlineshopapp.Model.CartItem;
import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private Context mContext;
    private List<Order> mOrderList;
    private DatabaseReference ordersRef;
    private DatabaseReference productsRef;
    private LayoutInflater inflater;

    public AdminOrderAdapter(Context context, List<Order> orderList) {
        mContext = context;
        mOrderList = orderList;
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        productsRef = FirebaseDatabase.getInstance().getReference("Products");
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_order_admin, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = mOrderList.get(position);
        holder.bind(order);
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, OrderDetailActivity.class);
                intent.putExtra("order", order);
                mContext.startActivity(intent);
            }
        });
        holder.btnUpdateStatus.setOnClickListener(v -> updateOrderStatus(order, holder));
        holder.btnCancelOrder.setOnClickListener(v -> cancelOrder(order));
    }

    private void updateOrderStatus(Order order, OrderViewHolder holder) {
        String status1 = order.getCompletion_date();
        String status2 = order.getCancellation_date();
        if (!status1.isEmpty() || !status2.isEmpty()) {
            Toast.makeText(mContext, "Đơn hàng này không thể cập nhật trạng thái được nữa", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn cập nhật trạng thái đơn hàng không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                    String status = holder.tvOrderStatus.getText().toString();

                    DatabaseReference orderRef = ordersRef.child(order.getId());

                    switch (status) {
                        case "Chờ duyệt":
                            orderRef.child("approval_date").setValue(currentDate);
                            order.setApproval_date(currentDate);
                            break;
                        case "Đã duyệt":
                            orderRef.child("delivery_date").setValue(currentDate);
                            order.setDelivery_date(currentDate);
                            break;
                        case "Đang giao hàng":
                            orderRef.child("completion_date").setValue(currentDate);
                            order.setCompletion_date(currentDate);
                            break;
                    }

                    notifyDataSetChanged();
                    Toast.makeText(mContext, "Đơn hàng đã được cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null)
                .show();
    }
    private void cancelOrder(Order order) {
        String status1 = order.getDelivery_date();
        String status2 = order.getCancellation_date();
        if (!status1.isEmpty() || !status2.isEmpty()) {
            Toast.makeText(mContext, "Đơn hàng này không thể hủy được nữa", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                        DatabaseReference orderRef = ordersRef.child(order.getId());
                        orderRef.child("cancellation_date").setValue(currentDate);
                        order.setCancellation_date(currentDate);

                        for (Map.Entry<String, CartItem> entry : order.getProducts().entrySet()) {
                            String productId = entry.getKey();
                            CartItem cartItem = entry.getValue();
                            int orderQuantity = cartItem.getQuantity();

                            productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Product product = snapshot.getValue(Product.class);
                                    if (product != null) {
                                        int updatedQuantity = product.getQuantity() + orderQuantity;
                                        productsRef.child(productId).child("quantity").setValue(updatedQuantity);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(mContext, "Lỗi cập nhật số lượng sản phẩm", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        notifyDataSetChanged();
                        Toast.makeText(mContext, "Đơn hàng đã được hủy thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return mOrderList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        private TextView tvOrderId;
        private TextView tvOrderDate;
        private TextView tvOrderStatus;
        private CardView recCard;
        Button btnUpdateStatus, btnCancelOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            recCard = itemView.findViewById(R.id.recCard);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
        }

        @SuppressLint("ResourceAsColor")
        public void bind(Order order) {
            tvOrderId.setText("Mã đơn hàng:" +"\n" + order.getId());
            tvOrderDate.setText("Thời gian đặt hàng: " + order.getOrder_date());

            String status;
            int bgColor, textColor;
            if (!order.getCancellation_date().isEmpty()) {
                status = "Đã hủy";
                textColor = R.color.red;
                bgColor = R.color.bgRed;
                btnCancelOrder.setText("Đã hủy");
            } else if (!order.getCompletion_date().isEmpty()) {
                status = "Đã hoàn thành";
                textColor = R.color.green;
                bgColor = R.color.bgGreen;
                btnUpdateStatus.setText("Đã hoàn thành");
            } else if (!order.getDelivery_date().isEmpty()) {
                status = "Đang giao hàng";
                textColor = R.color.bgOrange;
                bgColor = R.color.bgOrange;
                btnUpdateStatus.setText("Hoàn thành");
            } else if (!order.getApproval_date().isEmpty()) {
                status = "Đã duyệt";
                textColor = R.color.yellow;
                bgColor = R.color.bgYellow;
                btnUpdateStatus.setText("Giao hàng");
            } else {
                status = "Chờ duyệt";
                textColor = R.color.darkGrey;
                bgColor = R.color.grey;
                btnUpdateStatus.setText("Duyệt đơn hàng");
            }
            tvOrderStatus.setText(status);
            tvOrderStatus.setBackgroundTintList(mContext.getResources().getColorStateList(bgColor, mContext.getTheme()));
//            tvOrderStatus.setTextColor(mContext.getResources().getColorStateList(textColor, mContext.getTheme()));
        }
    }
}
