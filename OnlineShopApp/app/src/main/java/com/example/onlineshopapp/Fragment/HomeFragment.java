package com.example.onlineshopapp.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.onlineshopapp.Adapter.Categories_Adapter;
import com.example.onlineshopapp.Adapter.Products_Adapter;
import com.example.onlineshopapp.Adapter.ViewPagerAdapter;
import com.example.onlineshopapp.Model.Categories;
import com.example.onlineshopapp.Model.Product;
import com.example.onlineshopapp.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    RecyclerView recyclerView_categories;
    RecyclerView recyclerView_products;
    ViewPager2 viewPager2;
    ProgressBar progressBar1, progressBar2;
    ArrayList<Categories> categoriesArrayList = new ArrayList<>();
    Categories_Adapter categoriesArrayAdapter;
    ArrayList<Product> productArrayList = new ArrayList<>();
    Products_Adapter productsAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        addControls(view);
        FirebaseApp.initializeApp(getActivity());
        getDataFromFirebase_Categories();
        getFeaturedProducts();
        setupViewPager2();
        addEvents();
        return view;
    }
    private void addControls(View view) {
        recyclerView_categories = view.findViewById(R.id.recycler_categories);
        recyclerView_products = view.findViewById(R.id.recyclerView_products);
        viewPager2 = view.findViewById(R.id.viewPager2);
        progressBar1 = view.findViewById(R.id.progressBar1);
        progressBar2 = view.findViewById(R.id.progressBar2);
    }
    private void addEvents() {
    }
    private void setupViewPager2() {
        List<Integer> imageList = Arrays.asList(
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3
        );
        ViewPagerAdapter adapter = new ViewPagerAdapter(imageList, getContext());
        viewPager2.setAdapter(adapter);
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));

        viewPager2.setPageTransformer(compositePageTransformer);
        autoSlideImages();
    }

    private void autoSlideImages() {
        final int delay = 3000; // milliseconds
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(slideRunnable);
                sliderHandler.postDelayed(slideRunnable, delay);
            }
        });
    }

    private Runnable slideRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPager2.getCurrentItem();
            int nextItem = currentItem + 1;
            if (nextItem >= viewPager2.getAdapter().getItemCount()) {
                nextItem = 0;
            }
            viewPager2.setCurrentItem(nextItem, true);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(slideRunnable);
    }
    public void getDataFromFirebase_Categories()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Categories");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar1.setVisibility(View.GONE);
                categoriesArrayList.clear();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Categories user = userSnapshot.getValue(Categories.class);
                    user.setId(userSnapshot.getKey());
                    categoriesArrayList.add(user);
                }
                categoriesArrayAdapter = new Categories_Adapter(categoriesArrayList, getContext());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView_categories.setLayoutManager(layoutManager);
                recyclerView_categories.setItemAnimator(new DefaultItemAnimator());
                recyclerView_categories.setAdapter(categoriesArrayAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void getFeaturedProducts()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Products");

        Query query = myRef.orderByChild("featured").equalTo(true).limitToFirst(10);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar2.setVisibility(View.GONE);
                productArrayList.clear();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    Product user = userSnapshot.getValue(Product.class);
                    user.setId(userSnapshot.getKey());
                    productArrayList.add(user);
                }

                productArrayList.sort((o1, o2) -> o2.getCreated_at().compareTo(o1.getCreated_at()));

                productsAdapter = new Products_Adapter(productArrayList, getContext());
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
                recyclerView_products.setLayoutManager(layoutManager);
                recyclerView_products.setItemAnimator(new DefaultItemAnimator());
                recyclerView_products.setAdapter(productsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}