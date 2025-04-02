package com.example.news_application;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiWorker extends Worker {

    private static final String BASE_URL = "https://newsapi.org/";
    public ApiWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            makeApiCall();
            return Result.success();
        } catch (Exception e) {
            Log.e("ApiWorker", "Error making API call", e);
            return Result.failure();
        }
    }


    private void makeApiCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<NewsModal> call;
        String pageUrl = getString();

        Log.d("API Request", "URL: " + pageUrl);

        call = retrofitAPI.getNewsByCategory(pageUrl);

        call.enqueue(new Callback<NewsModal>() {
            @Override
            public void onResponse(@NonNull Call<NewsModal> call, @NonNull Response<NewsModal> response) {
                if (response.isSuccessful()) {
                    NewsModal newsModal = response.body();
                    if (newsModal != null) {
                        ArrayList<Articles> articles = newsModal.getArticles();
                        Log.d("API Response", "Articles size: " + (articles != null ? articles.size() : 0));
                        if (articles != null && !articles.isEmpty()) {
                            for (Articles article : articles) {
                                Log.d("Article", "Title: " + article.getTitle() + ", Description: " + article.getDescription());
                                showNotification(article.getTitle(), article.getDescription());
                            }
                        } else {
                            Log.d("API", "No articles found");
                        }
                    } else {
                        Log.d("API", "NewsModal is null");
                    }
                } else {
                    Log.d("API", "API response not successful. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsModal> call, @NonNull Throwable t) {
                Log.e("API Failure", "Error: " + t.getMessage());
            }
        });
    }

    @NonNull
    private static String getString() {
        String apiKey = "12ac8a8dced24f4abf216c1d3c7c066d";
        String[] categories = {"All", "Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology"};

        Random random = new Random();

        String category = categories[random.nextInt(categories.length)];

        int pageSize = random.nextInt(8) + 3;
        int pageNumber = 1;

        String pageUrl = "v2/top-headlines?country=us&page=" + pageNumber + "&pageSize=" + pageSize + "&apiKey=" + apiKey;

        if (!category.equals("All")) {
            pageUrl += "&category=" + category;
        }
        return pageUrl;
    }


    private void showNotification(String title, String content) {
        // Build and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
