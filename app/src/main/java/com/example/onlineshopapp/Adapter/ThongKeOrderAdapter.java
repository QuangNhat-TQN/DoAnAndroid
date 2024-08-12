package com.example.onlineshopapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.R;

import java.util.List;

public class ThongKeOrderAdapter extends RecyclerView.Adapter<ThongKeOrderAdapter.OrderViewHolder> {

    private List<Order> orderList;

    public ThongKeOrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_thongke, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdTextView.setText("Mã đơn hàng: " + order.getId());
        holder.customerNameTextView.setText("Tên khách hàng: " + order.getCustomer_name());
        holder.totalPriceTextView.setText("Tổng tiền: " + order.getTotal_price() + " VND");
        holder.orderDateTextView.setText("Ngày đặt hàng: " + order.getOrder_date());
        holder.completionDateTextView.setText("Ngày hoàn thành: " + order.getCompletion_date());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTextView, customerNameTextView, totalPriceTextView, orderDateTextView, completionDateTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            customerNameTextView = itemView.findViewById(R.id.customerNameTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            completionDateTextView = itemView.findViewById(R.id.completionDateTextView);
        }
    }
}

