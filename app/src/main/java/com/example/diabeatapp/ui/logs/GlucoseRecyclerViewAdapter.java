package com.example.diabeatapp.ui.logs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.diabeatapp.databinding.FragmentGlucoseItemBinding;
import com.example.diabeatapp.databinding.RecyclerviewGlucoseTitleBinding;
import com.example.diabeatapp.models.GlucoseItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GlucoseRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final List<GlucoseItem> glucoseList;

    public GlucoseRecyclerViewAdapter(List<GlucoseItem> items) {
        this.glucoseList = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TITLE : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_TITLE) {
            RecyclerviewGlucoseTitleBinding binding = RecyclerviewGlucoseTitleBinding.inflate(inflater, parent, false);
            return new GlucoseTitleViewHolder(binding);
        } else {
            FragmentGlucoseItemBinding binding = FragmentGlucoseItemBinding.inflate(inflater, parent, false);
            return new GlucoseItemViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GlucoseTitleViewHolder) {
            ((GlucoseTitleViewHolder) holder).bind();
        } else if (holder instanceof GlucoseItemViewHolder) {
            ((GlucoseItemViewHolder) holder).bind(glucoseList.get(position - 1)); // subtract title
        }
    }

    @Override
    public int getItemCount() {
        return glucoseList.size() + 1;
    }

    public static class GlucoseTitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;

        public GlucoseTitleViewHolder(RecyclerviewGlucoseTitleBinding binding) {
            super(binding.getRoot());
            titleTextView = binding.tvName;
        }

        public void bind() {
            titleTextView.setText("Blood Sugar Summary");
        }
    }

    public static class GlucoseItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView glucoseLevel;
        private final TextView timestamp;

        public GlucoseItemViewHolder(FragmentGlucoseItemBinding binding) {
            super(binding.getRoot());
            glucoseLevel = binding.tvGlucoseLevel;
            timestamp = binding.tvTimestamp;
        }

        public void bind(GlucoseItem item) {
            glucoseLevel.setText(item.glucoseLevel);
            timestamp.setText(item.timestamp + "");
        }
    }
}