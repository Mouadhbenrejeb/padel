package com.example.padel;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TimeAdapter extends RecyclerView.Adapter<TimeAdapter.Holder> {

    public interface OnTimeClickListener {
        void onTimeSelected(String time);
    }

    private List<String> times;
    private List<String> disabledTimes = new ArrayList<>();
    private int selected = -1;
    private OnTimeClickListener listener;

    public TimeAdapter(List<String> times, OnTimeClickListener listener) {
        this.times = times;
        this.listener = listener;
    }

    /**
     * Set the list of disabled (booked) time slots
     */
    public void setDisabledTimes(List<String> disabledTimes) {
        this.disabledTimes = disabledTimes != null ? disabledTimes : new ArrayList<>();
        // Reset selection if the selected time is now disabled
        if (selected >= 0 && selected < times.size() && this.disabledTimes.contains(times.get(selected))) {
            selected = -1;
        }
        notifyDataSetChanged();
    }

    /**
     * Clear all disabled times (make all slots available)
     */
    public void clearDisabledTimes() {
        this.disabledTimes.clear();
        notifyDataSetChanged();
    }

    /**
     * Reset selection
     */
    public void resetSelection() {
        selected = -1;
        notifyDataSetChanged();
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
        String time = times.get(pos);
        h.tvTime.setText(time);

        boolean isDisabled = disabledTimes.contains(time);

        if (isDisabled) {
            // Disabled state - slot is already booked
            h.tvTime.setBackgroundResource(R.drawable.time_disabled);
            h.tvTime.setTextColor(0xFF9E9E9E); // Gray color
            h.tvTime.setEnabled(false);
            h.tvTime.setOnClickListener(null);
        } else if (pos == selected) {
            // Selected state
            h.tvTime.setBackgroundResource(R.drawable.time_selected);
            h.tvTime.setTextColor(0xFFFFFFFF);
            h.tvTime.setEnabled(true);
            h.tvTime.setOnClickListener(v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    selected = adapterPos;
                    listener.onTimeSelected(times.get(adapterPos));
                    notifyDataSetChanged();
                }
            });
        } else {
            // Available state
            h.tvTime.setBackgroundResource(R.drawable.time_available);
            h.tvTime.setTextColor(0xFF1565C0);
            h.tvTime.setEnabled(true);
            h.tvTime.setOnClickListener(v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    selected = adapterPos;
                    listener.onTimeSelected(times.get(adapterPos));
                    notifyDataSetChanged();
                }
            });
        }
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