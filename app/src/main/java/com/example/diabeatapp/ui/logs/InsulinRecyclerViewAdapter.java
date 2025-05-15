package com.example.diabeatapp.ui.logs;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.diabeatapp.databinding.FragmentInsulinItemBinding;
import com.example.diabeatapp.databinding.RecyclerviewInsulinTitleBinding;
import com.example.diabeatapp.models.InsulinItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InsulinRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TITLE = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private final List<InsulinItem> insulinList;

    public InsulinRecyclerViewAdapter(List<InsulinItem> items) {
        this.insulinList = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_TITLE : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_TITLE) {
            RecyclerviewInsulinTitleBinding binding = RecyclerviewInsulinTitleBinding.inflate(inflater, parent, false);
            return new InsulinTitleViewHolder(binding);
        } else {
            FragmentInsulinItemBinding binding = FragmentInsulinItemBinding.inflate(inflater, parent, false);
            return new InsulinItemViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof InsulinTitleViewHolder) {
            ((InsulinTitleViewHolder) holder).bind();
        } else if (holder instanceof InsulinItemViewHolder) {
            ((InsulinItemViewHolder) holder).bind(insulinList.get(position - 1)); // subtract title
        }
    }

    @Override
    public int getItemCount() {
        return insulinList.size() + 1;
    }

    public static class InsulinTitleViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;

        public InsulinTitleViewHolder(RecyclerviewInsulinTitleBinding binding) {
            super(binding.getRoot());
            titleTextView = binding.tvName;
        }

        public void bind() {
            titleTextView.setText("Insulin Dosage Summary");
        }
    }

    public static class InsulinItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView insulinDose;
        private final TextView timestamp;

        public InsulinItemViewHolder(FragmentInsulinItemBinding binding) {
            super(binding.getRoot());
            insulinDose = binding.tvInsulinDose;
            timestamp = binding.tvTimestamp;
        }

        public void bind(InsulinItem item) {
            insulinDose.setText(item.insulinDose);
            timestamp.setText(item.timestamp + "");
        }
    }
}