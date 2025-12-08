package com.example.padel;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.Holder> {

    public interface OnDateClickListener {
        void onDateSelected(String date);
    }

    private List<String> dates;
    private int selected = 0;
    private OnDateClickListener listener;

    public DateAdapter(List<String> dates, OnDateClickListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        h.tvDate.setText(dates.get(pos));
        h.itemView.setBackgroundResource(
                pos == selected ? R.drawable.date_selected : R.drawable.date_unselected
        );

        h.itemView.setOnClickListener(v -> {
            selected = pos;
            listener.onDateSelected(dates.get(pos));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() { return dates.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvDate;
        Holder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
 }
}
}
