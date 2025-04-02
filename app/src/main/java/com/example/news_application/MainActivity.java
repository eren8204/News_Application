package com.example.news_application;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements CategoryRVAdapter.CategoryClickInterface {

    private RecyclerView newsRV, categoryRV;
    private ProgressBar loadingPB;
    private ArrayList<Articles> articlesArrayList;
    private ArrayList<CategoryRVModal> categoryRVModalArrayList;
    private CategoryRVAdapter categoryRVAdapter;
    private NewsRVAdapter newsRVAdapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private int currentPage = 1, selectedPosition = 0;
    private int pageSize = 5;
    private String selectedCategory = "All";
    private Button btnPrev, btnNext;
    private TextView tvPageNumber;

    private static final String BASE_URL = "https://newsapi.org/";

    @SuppressLint({"SourceLockedOrientationActivity", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusBarColor();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        newsRV = findViewById(R.id.idRVNews);
        categoryRV = findViewById(R.id.idRVCategories);
        loadingPB = findViewById(R.id.idPBLoading);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvPageNumber = findViewById(R.id.tvPageNumber);

        articlesArrayList = new ArrayList<>();
        categoryRVModalArrayList = new ArrayList<>();

        newsRVAdapter = new NewsRVAdapter(articlesArrayList, this);
        categoryRVAdapter = new CategoryRVAdapter(categoryRVModalArrayList, this, this);

        newsRV.setLayoutManager(new LinearLayoutManager(this));
        newsRV.setAdapter(newsRVAdapter);
        categoryRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRV.setAdapter(categoryRVAdapter);
        selectedPosition = 0;
        categoryRVAdapter.notifyDataSetChanged();
        btnPrev.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                updatePageNumber();
                articlesArrayList.clear();
                getNews(selectedCategory, currentPage);
            }
        });

        btnNext.setOnClickListener(v -> {
            currentPage++;
            updatePageNumber();
            articlesArrayList.clear();
            getNews(selectedCategory, currentPage);
        });
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(this, "Permission Denied. Some features may not work.", Toast.LENGTH_LONG).show();
                    }
                });

        checkNotificationPermission();
        getCategories();
        getNews(selectedCategory, currentPage);
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission();
            }
        }
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS");
        }
    }

    private void updatePageNumber() {
        tvPageNumber.setText(String.valueOf(currentPage));
    }

    private void statusBarColor() {
        Window window = MainActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.action2));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getCategories() {
        categoryRVModalArrayList.add(new CategoryRVModal("All", "all"));
        categoryRVModalArrayList.add(new CategoryRVModal("Technology", "technology"));
        categoryRVModalArrayList.add(new CategoryRVModal("Science", "science"));
        categoryRVModalArrayList.add(new CategoryRVModal("General", "general"));
        categoryRVModalArrayList.add(new CategoryRVModal("Business", "business"));
        categoryRVModalArrayList.add(new CategoryRVModal("Entertainment", "entertainment"));
        categoryRVModalArrayList.add(new CategoryRVModal("Health", "health"));
        categoryRVAdapter.notifyDataSetChanged();
    }

    private void getNews(String category, int page) {
        loadingPB.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<NewsModal> call;
        String apiKey = getString(R.string.news_api_key);
        String pageUrl = "v2/top-headlines?country=us&page=" + page + "&pageSize=" + pageSize + "&apiKey=" + apiKey;

        if (!category.equals("All")) {
            pageUrl += "&category=" + category;
        }

        call = retrofitAPI.getNewsByCategory(pageUrl);

        call.enqueue(new Callback<NewsModal>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<NewsModal> call, @NonNull Response<NewsModal> response) {
                loadingPB.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    NewsModal newsModal = response.body();
                    if (newsModal != null) {
                        ArrayList<Articles> articles = newsModal.getArticles();
                        if (articles != null && !articles.isEmpty()) {
                            newsRV.animate().alpha(0.0f).setDuration(200).withEndAction(() -> {
                                articlesArrayList.clear();
                                articlesArrayList.addAll(articles);
                                newsRVAdapter.notifyDataSetChanged();
                                newsRV.animate().alpha(1.0f).setDuration(200);
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "No more articles available.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsModal> call, @NonNull Throwable t) {
                loadingPB.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Failed to load news", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCategoryClick(int position) {
        selectedCategory = categoryRVModalArrayList.get(position).getCategory();
        selectedPosition = position;
        currentPage = 1;
        updatePageNumber();
        articlesArrayList.clear();
        getNews(selectedCategory, currentPage);
        categoryRVAdapter.notifyDataSetChanged();
    }
}
