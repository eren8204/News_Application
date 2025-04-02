package com.example.news_application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryRVAdapter extends RecyclerView.Adapter<CategoryRVAdapter.ViewHolder> {
    public ArrayList<CategoryRVModal> categoryRVModals;
    private Context context;
    private int selectedPosition = -1;
    private CategoryClickInterface categoryClickInterface;

    public CategoryRVAdapter(ArrayList<CategoryRVModal> categoryRVModals, Context context, CategoryClickInterface categoryClickInterface) {
        this.categoryRVModals = (categoryRVModals != null) ? categoryRVModals : new ArrayList<>();
        this.context = context;
        this.categoryClickInterface = categoryClickInterface;
    }

    @NonNull
    @Override
    public CategoryRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_rv_item, parent, false);
        return new ViewHolder(view); // Corrected instantiation
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryRVModal categoryRVModal = categoryRVModals.get(position);
        holder.categoryTV.setText(categoryRVModal.getCategory());
        Log.d("CategoryRVAdapter", "Image URL: " + categoryRVModal.getCategoryImageUrl());

        // Load the image using Picasso
        Picasso.get().load(categoryRVModal.getCategoryImageUrl()).into(holder.categoryIV);

        // Highlight the selected category
        if (position == selectedPosition) {
            holder.categoryCV.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.Dark_pink));
            holder.categoryTV.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            holder.categoryCV.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.categoryTV.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.Dark_pink));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPosition;
                notifyDataSetChanged();  // Refresh the entire list to update the UI
                categoryClickInterface.onCategoryClick(adapterPosition);
            }
        });
    }



    @Override
    public int getItemCount() {
        return (categoryRVModals != null) ? categoryRVModals.size() : 0;
    }

    public interface CategoryClickInterface {
        void onCategoryClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryTV;
        private ImageView categoryIV;
        private CardView categoryCV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryCV = itemView.findViewById(R.id.idCVCategory);
            categoryTV = itemView.findViewById(R.id.idTVCategory);
            categoryIV = itemView.findViewById(R.id.idIVCategory);
        }
    }
}
