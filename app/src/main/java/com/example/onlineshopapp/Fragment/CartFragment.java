package com.example.onlineshopapp.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshopapp.Activity.AddAddressActivity;
import com.example.onlineshopapp.Activity.LoginActivity;
import com.example.onlineshopapp.Activity.PaymentSuccessActivity;
import com.example.onlineshopapp.Activity.Product_detailsActivity;
import com.example.onlineshopapp.Adapter.AddressAdapter;
import com.example.onlineshopapp.Adapter.CartAdapter;
import com.example.onlineshopapp.Model.Address;
import com.example.onlineshopapp.Model.CartItem;
import com.example.onlineshopapp.Model.Order;
import com.example.onlineshopapp.R;
import com.example.onlineshopapp.Utils.CartUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemChangeListener, AddressAdapter.OnAddressSelectedListener {
    private RecyclerView recCart;
    private TextView tvTongTien, tvPaymentMethod, tvChangePM, tvChangeAddress, tvFullName, tvAddress, tvPhone, tvEmptyCart;
    private Button btnCheckout;
    LinearLayout linearLayoutPaymentMethod, linearLayoutBottom;
    private CartAdapter adapter;
    private List<String> productIds;
    private Map<String, CartItem> cartItems;
    private DatabaseReference cartRef;
    private String userId;
    FirebaseUser user;
    AddressAdapter addressAdapter;
    List<Address> addressList;
    ValueEventListener eventListener;
    BottomSheetDialog bottomSheetDialog;
    int totalPrice;
    private Address selectedAddress;

    public CartFragment() {
    }

    public static CartFragment newInstance(String param1, String param2) {
        CartFragment fragment = new CartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cartRef = FirebaseDatabase.getInstance().getReference("Carts").child(userId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        recCart = view.findViewById(R.id.recCart);
        tvTongTien = view.findViewById(R.id.tvTongTien);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvChangePM = view.findViewById(R.id.tvChangePM);
        tvChangeAddress = view.findViewById(R.id.tvChangeAddress);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvEmptyCart = view.findViewById(R.id.tvEmptyCart);
        btnCheckout = view.findViewById(R.id.btnCheckOut);
        linearLayoutPaymentMethod = view.findViewById(R.id.linearLayoutPaymentMethod);
        linearLayoutBottom = view.findViewById(R.id.linearLayoutBottom);
        productIds = new ArrayList<>();
        cartItems = new HashMap<>();

        adapter = new CartAdapter(cartItems, productIds, getContext(), this);
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(getContext(), addressList, this);

        recCart.setLayoutManager(new LinearLayoutManager(getContext()));
        recCart.setAdapter(adapter);


        if (user != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            cartRef = FirebaseDatabase.getInstance().getReference("Carts").child(userId);
            loadCartItems();
            displayUserAddresses();
        }

        tvChangePM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentMethodDialog();
            }
        });

        tvChangeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayUserAddresses();
                showAddressListDialog();
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog();
            }
        });

        return view;
    }

    // Hiển thị danh sách địa chỉ
    private void displayUserAddresses() {
        DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("address");
        eventListener = addressRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                        Address address = addressSnapshot.getValue(Address.class);
                        if (address != null) {
                            address.setId(addressSnapshot.getKey());
                            addressList.add(address);
                            if (selectedAddress == null) {
                                selectedAddress = address;
                            }
                        }
                    }
                    displaySelectedAddress();
                }

                addressAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Hiển thị địa chỉ đã chọn
    private void displaySelectedAddress() {
        if (selectedAddress != null) {
            tvFullName.setText(selectedAddress.getFull_name());
            tvAddress.setText(selectedAddress.getAddress_detail());
            tvPhone.setText(selectedAddress.getPhone());
        }
    }

    // Hiển thị thị dialog để chọn địa chỉ
    private void showAddressListDialog() {
        bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.address_list_bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        Button btnAddAddress = dialogView.findViewById(R.id.btnAddAddress);
        RecyclerView recyclerAddressList = dialogView.findViewById(R.id.recyclerAddressList);
        recyclerAddressList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAddressList.setAdapter(addressAdapter);

        btnAddAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddAddressActivity.class);
                startActivity(intent);
            }
        });

        bottomSheetDialog.show();
    }

    // Hiển thị dialog để chọn phương thức thanh toán
    private void showPaymentMethodDialog() {
        BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.bottomsheetlayout, null);
        bottomSheetDialog1.setContentView(dialogView);

        TextView tvCashOnDelivery = dialogView.findViewById(R.id.tvCashOnDelivery);
        TextView tvOnlinePayment = dialogView.findViewById(R.id.tvOnlinePayment);

        tvCashOnDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPaymentMethod.setText("Thanh toán khi nhận hàng");
                bottomSheetDialog1.dismiss();
            }
        });

        tvOnlinePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvPaymentMethod.setText("Thanh toán online");
                bottomSheetDialog1.dismiss();
            }
        });

        bottomSheetDialog1.show();
    }

    // Hiển thị dialog xác nhận trước khi thanh toán
    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận thanh toán");
        builder.setMessage("Bạn có chắc muốn thanh toán đơn hàng này?");
        builder.setPositiveButton("Thanh toán", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                payment();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // Thanh toán
    private void payment() {
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "Giỏ hàng của bạn rỗng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String orderId = FirebaseDatabase.getInstance().getReference("Orders").push().getKey();

        String address = selectedAddress.getAddress_detail();
        String paymentMethod = tvPaymentMethod.getText().toString();
        String fullName = selectedAddress.getFull_name();
        String phone = selectedAddress.getPhone();

        for (Map.Entry<String, CartItem> entry : cartItems.entrySet()) {
            String productId = entry.getKey();
            int orderedQuantity = entry.getValue().getQuantity();

            DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products").child(productId);
            productRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer currentStock = snapshot.child("quantity").getValue(Integer.class);
                        if (currentStock != null) {
                            if (currentStock >= orderedQuantity) {
                                int newStock = currentStock - orderedQuantity;
                                productRef.child("quantity").setValue(newStock);
                                Order order = new Order(address, "", "", "", fullName, "",
                                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()), paymentMethod, phone,
                                        new HashMap<>(cartItems), totalPrice, userId
                                );

                                DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
                                assert orderId != null;
                                ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getContext(), PaymentSuccessActivity.class);
                                        startActivity(intent);

                                        clearCart();
                                    } else {
                                        Toast.makeText(getContext(), "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (currentStock <= 0) {
                                Toast.makeText(getContext(), "Vui lòng xóa những sản phẩm hết hàng trước khi thanh toán", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Số lượng sản phẩm không đủ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }


    }


    // Xóa giỏ hàng sau khi thanh toán
    private void clearCart() {
        cartRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                cartItems.clear();
                productIds.clear();
                adapter.notifyDataSetChanged();
                tvTongTien.setText(convertToVND("0"));
            } else {
                Toast.makeText(getContext(), "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Giảm số lượng sản phẩm khi thanh toán thành công
    private void reduceProductQuantities(Map<String, CartItem> products) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");

        for (Map.Entry<String, CartItem> entry : products.entrySet()) {
            String productId = entry.getKey();
            int orderedQuantity = entry.getValue().getQuantity();

            productsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Integer currentStock = snapshot.child("quantity").getValue(Integer.class);
                        if (currentStock != null) {
                            if (currentStock >= orderedQuantity) {
                                int newStock = currentStock - orderedQuantity;
                                productsRef.child(productId).child("quantity").setValue(newStock);
                            } else if (currentStock <= 0){
                                Toast.makeText(getContext(), "Vui lòng xóa những sản phẩm hết hàng trước khi thanh toán", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Số lượng sản phẩm không đủ", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    // Kiểm tra số lượng sản phẩm trước khi thanh toán
    private void checkProductQuantities(Map<String, CartItem> products, OnQuantityCheckCompleteListener listener) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");
        List<Task<DataSnapshot>> tasks = new ArrayList<>();

        for (Map.Entry<String, CartItem> entry : products.entrySet()) {
            String productId = entry.getKey();
            Task<DataSnapshot> productTask = productsRef.child(productId).get();
            tasks.add(productTask);
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            boolean allQuantitiesSufficient = true;
            boolean hasOutOfStockItems = false;
            String insufficientProductName = null;

            for (Task<DataSnapshot> productTask : tasks) {
                if (productTask.isSuccessful()) {
                    DataSnapshot productSnapshot = productTask.getResult();
                    Integer currentStock = productSnapshot.child("quantity").getValue(Integer.class);
                    String productId = productSnapshot.getKey();
                    String productName = productSnapshot.child("name").getValue(String.class); // Assuming you have product name stored in database
                    int orderedQuantity = products.get(productId).getQuantity();

                    if (currentStock == null || currentStock < orderedQuantity) {
                        allQuantitiesSufficient = false;
                        if (currentStock == 0) {
                            hasOutOfStockItems = true;
                        }
                        insufficientProductName = productName != null ? productName : "Some product"; // Use product name if available
                        break;
                    }
                }
            }

            if (!allQuantitiesSufficient) {
                if (hasOutOfStockItems) {
                    Toast.makeText(getContext(), "Vui lòng xóa sản phẩm hết hàng trước khi thanh toán", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Số lượng sản phẩm " + insufficientProductName + " không đủ", Toast.LENGTH_SHORT).show();
                }
            }

            listener.onComplete(allQuantitiesSufficient);
        });
    }


    private interface OnQuantityCheckCompleteListener {
        void onComplete(boolean allQuantitiesSufficient);
    }

    // Load danh sách sản phẩm có trong giỏ hàng
    private void loadCartItems() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                productIds.clear();
                totalPrice = 0;
                DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products");
                List<Task<DataSnapshot>> tasks = new ArrayList<>();

                for (DataSnapshot itemSnapshot : snapshot.child("products").getChildren()) {
                    String productId = itemSnapshot.getKey();
                    CartItem item = itemSnapshot.getValue(CartItem.class);
                    cartItems.put(productId, item);
                    productIds.add(productId);

                    Task<DataSnapshot> productTask = proRef.child(productId).get();
                    tasks.add(productTask);
                }

                if (cartItems.isEmpty()) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                    recCart.setVisibility(View.GONE);
                    linearLayoutBottom.setVisibility(View.GONE);
                    linearLayoutPaymentMethod.setVisibility(View.GONE);
                } else {
                    tvEmptyCart.setVisibility(View.GONE);
                    recCart.setVisibility(View.VISIBLE);
                    linearLayoutBottom.setVisibility(View.VISIBLE);
                    linearLayoutPaymentMethod.setVisibility(View.VISIBLE);
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
                    for (Task<DataSnapshot> productTask : tasks) {
                        if (productTask.isSuccessful()) {
                            DataSnapshot productSnapshot = productTask.getResult();
                            Integer price = productSnapshot.child("price").getValue(Integer.class);
                            String productId = productSnapshot.getKey();
                            if (price != null && cartItems.containsKey(productId)) {
                                CartItem item = cartItems.get(productId);
                                totalPrice += price * item.getQuantity();
                            }
                        } else {
                            // Handle error if needed
                        }
                    }

                    tvTongTien.setText(convertToVND(String.valueOf(totalPrice)));
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    @Override
    public void onQuantityChanged(String productId, int newQuantity) {
        CartUtil.updateCartItemQuantity(productId, newQuantity);
    }

    @Override
    public void onItemDeleted(String productId) {
        CartUtil.removeCartItem(productId);
    }

    public String convertToVND(String priceString) {
        String cleanPriceString = priceString.replaceAll("[^\\d]", "");
        long priceLong = Long.parseLong(cleanPriceString);
        String vndString = String.format("%,d", priceLong) + " VND";

        return vndString;
    }

    @Override
    public void onAddressSelected(Address address) {
        selectedAddress = address;
        displaySelectedAddress();
        bottomSheetDialog.dismiss();
    }
}
