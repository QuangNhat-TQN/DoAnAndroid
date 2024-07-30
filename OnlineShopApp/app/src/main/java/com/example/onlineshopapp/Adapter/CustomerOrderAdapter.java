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

public class CustomerOrderAdapter extends RecyclerView.Adapter<CustomerOrderAdapter.OrderViewHolder> {

    private Context mContext;
    private List<Order> mOrderList;
    private DatabaseReference ordersRef;
    private DatabaseReference productsRef;
    private LayoutInflater inflater;

    public CustomerOrderAdapter(Context context, List<Order> orderList) {
        mContext = context;
        mOrderList = orderList;
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        productsRef = FirebaseDatabase.getInstance().getReference("Products");
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_item_order_customer, parent, false);
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

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            recCard = itemView.findViewById(R.id.recCard);
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
            } else if (!order.getCompletion_date().isEmpty()) {
                status = "Đã hoàn thành";
                textColor = R.color.green;
                bgColor = R.color.bgGreen;
            } else if (!order.getDelivery_date().isEmpty()) {
                status = "Đang giao hàng";
                textColor = R.color.bgOrange;
                bgColor = R.color.bgOrange;
            } else if (!order.getApproval_date().isEmpty()) {
                status = "Đã duyệt";
                textColor = R.color.yellow;
                bgColor = R.color.bgYellow;
            } else {
                status = "Chờ duyệt";
                textColor = R.color.darkGrey;
                bgColor = R.color.grey;
            }
            tvOrderStatus.setText(status);
            tvOrderStatus.setBackgroundTintList(mContext.getResources().getColorStateList(bgColor, mContext.getTheme()));
//            tvOrderStatus.setTextColor(mContext.getResources().getColorStateList(textColor, mContext.getTheme()));
        }
    }
}
