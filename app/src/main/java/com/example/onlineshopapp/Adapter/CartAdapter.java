package com.example.onlineshopapp.Adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onlineshopapp.Model.CartItem;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Map<String, CartItem> cartItems;
    private List<String> productIds;
    private Context context;
    private OnCartItemChangeListener listener;

    public CartAdapter(Map<String, CartItem> cartItems, List<String> productIds, Context context, OnCartItemChangeListener listener) {
        this.cartItems = cartItems;
        this.productIds = productIds;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        String productId = productIds.get(position);
        CartItem item = cartItems.get(productId);

        DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products").child(productId);
        proRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                assert product != null;
                Glide.with(context).load(product.getImage_url()).into(holder.imgProductEachItem);
                holder.tvProductName.setText(product.getName());
                holder.tvPriceEachItem.setText(convertToVND(String.valueOf(product.getPrice() * item.getQuantity())));
                holder.edtQuantityEachItem.setText(String.valueOf(item.getQuantity()));
                holder.availableQuantity = product.getQuantity();

                if (product.getQuantity() == 0) {
                    holder.tvPlus.setEnabled(false);
                    holder.btnMinus.setEnabled(false);
                    holder.edtQuantityEachItem.setEnabled(false);
                    holder.tvOutOfStock.setVisibility(View.VISIBLE);
                } else {
                    holder.tvPlus.setEnabled(true);
                    holder.btnMinus.setEnabled(true);
                    holder.edtQuantityEachItem.setEnabled(true);
                    holder.tvOutOfStock.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.edtQuantityEachItem.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String text = holder.edtQuantityEachItem.getText().toString().trim();
                if (text.isEmpty() || Integer.parseInt(text) == 0) {
                    holder.edtQuantityEachItem.setText(String.valueOf(item.getQuantity()));
                } else {
                    try {
                        int newQuantity = Integer.parseInt(text);
                        if (newQuantity > holder.availableQuantity) {
                            Toast.makeText(context, "Số lượng vượt quá số lượng sản phẩm hiện có", Toast.LENGTH_SHORT).show();
                            holder.edtQuantityEachItem.setText(String.valueOf(item.getQuantity()));
                        } else {
                            item.setQuantity(newQuantity);
                            listener.onQuantityChanged(productId, newQuantity);
                        }

                    } catch (NumberFormatException e) {
                        holder.edtQuantityEachItem.setText(String.valueOf(item.getQuantity()));
                    }
                }
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
        holder.tvPlus.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            holder.edtQuantityEachItem.setText(String.valueOf(newQuantity));
            listener.onQuantityChanged(productId, newQuantity);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                holder.edtQuantityEachItem.setText(String.valueOf(newQuantity));
                listener.onQuantityChanged(productId, newQuantity);
            }
        });

        holder.imgDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")
                    .setPositiveButton("Xóa", (dialog, which) -> listener.onItemDeleted(productId))
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    public String convertToVND(String priceString) {
        String cleanPriceString = priceString.replaceAll("[^\\d]", "");
        long priceLong = Long.parseLong(cleanPriceString);
        String vndString = String.format("%,d", priceLong) + " VND";

        return vndString;
    }

    @Override
    public int getItemCount() {
        return productIds.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvPriceEachItem, edtQuantityEachItem;
        TextView tvPlus, btnMinus, tvOutOfStock;
        ImageView imgProductEachItem, imgDelete;
        int availableQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPriceEachItem = itemView.findViewById(R.id.tvPriceEachItem);
            edtQuantityEachItem = itemView.findViewById(R.id.edtQuantityEachItem);
            tvPlus = itemView.findViewById(R.id.tvPlus);
            btnMinus = itemView.findViewById(R.id.tvMinus);
            imgProductEachItem = itemView.findViewById(R.id.imgProductEachItem);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvOutOfStock = itemView.findViewById(R.id.tvOutOfStock);
        }
    }

    public interface OnCartItemChangeListener {
        void onQuantityChanged(String productId, int newQuantity);
        void onItemDeleted(String productId);
    }
}
