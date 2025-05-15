package com.example.diabeatapp.ui.logs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.diabeatapp.databinding.RecyclerviewFoodTitleBinding;
import com.example.diabeatapp.databinding.FragmentFoodItemBinding;
import com.example.diabeatapp.models.FoodItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final List<FoodItem> foodList;

    public FoodRecyclerViewAdapter(List<FoodItem> items) {
        this.foodList = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TITLE : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_TITLE) {
            RecyclerviewFoodTitleBinding binding = RecyclerviewFoodTitleBinding.inflate(inflater, parent, false);
            return new FoodTitleViewHolder(binding);
        } else {
            FragmentFoodItemBinding binding = FragmentFoodItemBinding.inflate(inflater, parent, false);
            return new FoodItemViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FoodTitleViewHolder) {
            ((FoodTitleViewHolder) holder).bind();
        } else if (holder instanceof FoodItemViewHolder) {
            ((FoodItemViewHolder) holder).bind(foodList.get(position - 1)); // subtract title
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size() + 1;
    }

    public static class FoodTitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;

        public FoodTitleViewHolder(RecyclerviewFoodTitleBinding binding) {
            super(binding.getRoot());
            titleTextView = binding.tvName;
        }

        public void bind() {
            titleTextView.setText("Food Summary");
        }
    }

    public static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView mealNameText;
        private final TextView caloriesText;

        public FoodItemViewHolder(FragmentFoodItemBinding binding) {
            super(binding.getRoot());
            mealNameText = binding.tvMealName;
            caloriesText = binding.tvCalories;
        }

        public void bind(FoodItem item) {
            mealNameText.setText(item.mealName);
            caloriesText.setText(item.calories + " cal");
        }
    }
}