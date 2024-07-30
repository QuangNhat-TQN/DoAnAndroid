package com.example.onlineshopapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.onlineshopapp.Adapter.ProductDetailViewPagerAdapter;
import com.example.onlineshopapp.Adapter.Products_Adapter;
import com.example.onlineshopapp.Adapter.ViewPagerAdapter;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.example.onlineshopapp.Utils.CartUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product_detailsActivity extends AppCompatActivity {
    RecyclerView recyclerView_products;
    ArrayList<Product> productArrayList = new ArrayList<>();
    Products_Adapter productsAdapter;
    ImageView img_product;
    TextView txt_name, txt_price, txt_rating, txt_quantity, tvMinus, tvPlus, tvOutOfStock;
    EditText edtQuantity;
    Button btnAddToCart;
    TabLayout tabLayout;
    ViewPager viewPager;
    ProductDetailViewPagerAdapter viewPagerAdapter;
    int productQuantity;

    private String userId;
    private String productId;
    FirebaseUser user;
    ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ScrollView scrollView = findViewById(R.id.scrollView);

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        addControls();
        setData();
        getDataFromFirebase_Products();
        addEvents();
    }

    private void setData() {
        Intent intent = getIntent();
        String image = intent.getStringExtra("image_url");
        String name = intent.getStringExtra("name");
        int price = intent.getIntExtra("price", -1);
        double rating = intent.getDoubleExtra("rating", -1);
        productQuantity = intent.getIntExtra("quantity", -1);
        Glide.with(this).load(image).into(img_product);
        txt_name.setText(name);
        txt_price.setText(convertToVND(String.format("%d đ", price)));
        txt_rating.setText(String.valueOf(rating));
        txt_quantity.setText("Số lượng: " + productQuantity);

        if (productQuantity == 0) {
            tvOutOfStock.setVisibility(View.VISIBLE);
            tvMinus.setEnabled(false);
            tvPlus.setEnabled(false);
            edtQuantity.setEnabled(false);
        }
    }

    private void addControls() {
        img_product = findViewById(R.id.img_product_details);
        txt_name = findViewById(R.id.txt_name_product_details);
        txt_price = findViewById(R.id.txt_price_product_details);
        txt_quantity = findViewById(R.id.txt_soluog_product_details);
        txt_rating = findViewById(R.id.txt_danhgia_product_dtails);
        recyclerView_products = findViewById(R.id.recyclerView_product_details);
        tvMinus = findViewById(R.id.tvMinus);
        tvPlus = findViewById(R.id.tvPlus);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtQuantity.setText("1");
        tvOutOfStock = findViewById(R.id.tvOutOfStock);
        btnAddToCart = findViewById(R.id.btnAddToCart);

    }

    private void addEvents() {
        tvMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = getQuantityFromEditText();
                if (currentQuantity <= 1) {
                    return;
                }
                edtQuantity.setText(String.valueOf(currentQuantity - 1));
            }
        });

        tvPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQuantity = getQuantityFromEditText();
                if (currentQuantity >= productQuantity) {
                    return;
                }
                edtQuantity.setText(String.valueOf(currentQuantity + 1));
            }
        });

        edtQuantity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                int quantity = getQuantityFromEditText();
                if (quantity <= 0) {
                    edtQuantity.setText("1");
                } else if (quantity >= productQuantity) {
                    edtQuantity.setText(String.valueOf(productQuantity));
                } else {
                    edtQuantity.setText(String.valueOf(quantity));
                }

                edtQuantity.setSelection(edtQuantity.getText().length());
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        btnAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });
    }

    private int getQuantityFromEditText() {
        String quantityString = edtQuantity.getText().toString().trim();
        if (quantityString.isEmpty() || quantityString.equals("0")) {
            return 0;
        } else {
            return Integer.parseInt(quantityString);
        }
    }

    public String convertToVND(String priceString) {
        String cleanPriceString = priceString.replaceAll("[^\\d]", "");
        long priceLong = Long.parseLong(cleanPriceString);
        return String.format("%,d VND", priceLong);
    }

    public void getDataFromFirebase_Products() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Products");

        eventListener = myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productArrayList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Product product = userSnapshot.getValue(Product.class);
                    product.setId(userSnapshot.getKey());
                    productArrayList.add(product);
                }

                recyclerView_products.addItemDecoration(new DividerItemDecoration(Product_detailsActivity.this, LinearLayoutManager.HORIZONTAL));
                productsAdapter = new Products_Adapter(productArrayList, Product_detailsActivity.this);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(Product_detailsActivity.this, 2);
                recyclerView_products.setLayoutManager(layoutManager);
                recyclerView_products.setItemAnimator(new DefaultItemAnimator());
                recyclerView_products.setAdapter(productsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.product_detail_menu, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        Intent intent = getIntent();
        productId = intent.getStringExtra("id");
        kTDangNhap();
        getWishlist(userId, productId, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
        switch (item.getItemId()) {
            case R.id.navigation_addToCart:
                addToCart();
                break;
            case R.id.navigation_wishlist:
                Intent intent = getIntent();
                productId = intent.getStringExtra("id");
                kTDangNhap();
                if (userId == null) {
                    showAlertDialog();
                }

                getDataFromFirebase_Wishlist(userId, productId, item);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addToCart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(Product_detailsActivity.this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        Intent intent = getIntent();
        String productId = intent.getStringExtra("id");
        int price = intent.getIntExtra("price", -1);
        int quantity = getQuantityFromEditText();

        if (productQuantity == 0) {
            Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        } else if (quantity <= 0) {
            Toast.makeText(this, "Số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
        } else if (quantity > productQuantity) {
            Toast.makeText(this, "Số lượng vượt quá số lượng sản phẩm hiện có", Toast.LENGTH_SHORT).show();
        } else if (productId != null && price != -1) {
            CartUtil.addToCart(productId, quantity, price);
            Toast.makeText(this, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông báo")
                .setMessage("Bạn chưa đăng nhập. Chuyển đến trang đăng nhập")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getDataFromFirebase_Wishlist(String userId, String proId, MenuItem item) {
        if (userId == null)
            return;

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(userId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> favoriteProductIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.child("product_id").getValue(String.class);
                    if (productId != null) {
                        favoriteProductIds.add(productId.trim());
                    }
                }
                boolean kt = kTSanPham(favoriteProductIds, proId);
                if(!kt){
                    item.setIcon(R.drawable.baseline_favorite_24);
                    addProductToWishlist(userId, proId);
                }else {
                    item.setIcon(R.drawable.baseline_favorite_border_24);
                    removeProductFromWishlist(userId, productId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    public void addProductToWishlist(String userId, String productId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference wishlistRef = database.getReference("Wishlist").child(userId).child(productId);

        // Tạo một đối tượng chứa thông tin sản phẩm yêu thích
        Map<String, Object> productData = new HashMap<>();
        productData.put("product_id", productId);

        // Thêm sản phẩm vào wishlist của người dùng
        wishlistRef.setValue(productData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Thêm thành công
                    Toast.makeText(getApplicationContext(), "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                } else {
                    // Thêm thất bại
                    Toast.makeText(getApplicationContext(), "Không thể thêm sản phẩm vào wishlist: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void removeProductFromWishlist(String userId, String productId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference wishlistRef = database.getReference("Wishlist").child(userId).child(productId);

        // Xóa sản phẩm khỏi wishlist của người dùng
        wishlistRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Xóa thành công
                    Toast.makeText(getApplicationContext(), "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                } else {
                    // Xóa thất bại
                    Toast.makeText(getApplicationContext(), "Không thể xóa sản phẩm khỏi wishlist: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setMenuIconsWishlist(Menu menu, int icon) {
        // Lấy các mục menu theo ID và đặt biểu tượng
        MenuItem settingsItem = menu.findItem(R.id.navigation_wishlist);
        if (settingsItem != null) {
            settingsItem.setIcon(icon);
        }
    }
    private void kTDangNhap() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }
    }

    private void getWishlist(String userId, String proId, Menu item) {
        if (userId == null)
            return;

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(userId);
         myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> favoriteProductIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.child("product_id").getValue(String.class);
                    if (productId != null) {
                        favoriteProductIds.add(productId.trim());
                    }
                }
                boolean kt = kTSanPham(favoriteProductIds, proId);
                if(kt){
                    setMenuIconsWishlist(item, R.drawable.baseline_favorite_24);
                }else {
                    setMenuIconsWishlist(item, R.drawable.baseline_favorite_border_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }
    private boolean kTSanPham(List<String> productIds, String productId) {
        for (String s : productIds){
            if(s.equals(productId)){
                return true;
            }
        }
        return false;
    }
}
