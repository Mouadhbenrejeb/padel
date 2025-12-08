package com.example.padel;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.Holder> {

    public interface OnTimeClickListener {
        void onTimeSelected(String time);
    }

    private List<String> times;
    private int selected = -1;
    private OnTimeClickListener listener;

    public TimeAdapter(List<String> times, OnTimeClickListener listener) {
        this.times = times;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        h.tvTime.setText(times.get(pos));

        if (pos == selected) {
            h.tvTime.setBackgroundResource(R.drawable.time_selected);
            h.tvTime.setTextColor(0xFFFFFFFF);
        } else {
            h.tvTime.setBackgroundResource(R.drawable.time_available);
            h.tvTime.setTextColor(0xFF1565C0);
        }

        h.tvTime.setOnClickListener(v -> {
            selected = pos;
            listener.onTimeSelected(times.get(pos));
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() { return times.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTime;
        Holder(View v) {
            super(v);
            tvTime = v.findViewById(R.id.tvTime);
 }
}
}