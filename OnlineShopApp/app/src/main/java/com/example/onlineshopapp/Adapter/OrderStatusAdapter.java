package com.example.onlineshopapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Model.Status;
import com.example.onlineshopapp.R;

import java.util.List;

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.StatusViewHolder> {

    private Context mContext;
    private List<Status> mStatusList;

    public OrderStatusAdapter(Context context, List<Status> statusList) {
        mContext = context;
        mStatusList = statusList;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_order_status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        Status status = mStatusList.get(position);
        holder.bind(status, position == mStatusList.size() - 1);
    }

    @Override
    public int getItemCount() {
        return mStatusList.size();
    }

    public class StatusViewHolder extends RecyclerView.ViewHolder {

        private TextView tvStatus;
        private TextView tvDate;
        private TextView tvPoint;
        private View viewLine;

        public StatusViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPoint = itemView.findViewById(R.id.tvPoint);
            viewLine = itemView.findViewById(R.id.viewLine);
        }

        public void bind(Status status, boolean isLast) {
            tvStatus.setText(status.getStatus());
            tvDate.setText(status.getDate());

            int bgColor;
            if (status.getStatus().equals("Đã hủy") && !status.getDate().isEmpty()) {
                bgColor = R.color.red;
            } else if (status.getStatus().equals("Đã hoàn thành") && !status.getDate().isEmpty()) {
                bgColor = R.color.green;
            } else if (status.getStatus().equals("Đang giao hàng") && !status.getDate().isEmpty()) {
                bgColor = R.color.orange;
            } else if (status.getStatus().equals("Đã duyệt") && !status.getDate().isEmpty()) {
                bgColor = R.color.yellow;
            } else if (status.getStatus().equals("Chờ duyệt") && !status.getDate().isEmpty()) {
                bgColor = R.color.darkGrey;
            } else {
                bgColor = R.color.grey;
            }
            tvPoint.setBackgroundTintList(mContext.getResources().getColorStateList(bgColor, mContext.getTheme()));

            if (isLast) {
                viewLine.setVisibility(View.GONE);
            } else {
                viewLine.setVisibility(View.VISIBLE);
            }
        }
    }
}
