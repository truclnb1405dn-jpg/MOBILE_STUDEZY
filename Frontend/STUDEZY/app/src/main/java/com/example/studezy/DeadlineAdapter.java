package com.example.studezy;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.DeadlineModel;
import java.util.List;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.DeadlineViewHolder> {

    private List<DeadlineModel> deadlineList;

    public DeadlineAdapter(List<DeadlineModel> deadlineList) {
        this.deadlineList = deadlineList;
    }

    @NonNull
    @Override
    public DeadlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deadline, parent, false);
        return new DeadlineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewHolder holder, int position) {
        DeadlineModel deadline = deadlineList.get(position);
        holder.tvTitle.setText(deadline.getTitle());

        // Kiểm tra xem deadline có gấp không để đổi màu và Icon
        if (deadline.isUrgent()) {
            holder.ivCalendar.setVisibility(View.GONE); // Ẩn icon lịch
            holder.tvTime.setText("⏰  " + deadline.getRemainingText());
            holder.tvTime.setTextColor(Color.parseColor("#FF4D3D")); // Đỏ
        } else {
            holder.ivCalendar.setVisibility(View.VISIBLE); // Hiện icon lịch
            holder.tvTime.setText(deadline.getRemainingText());
            holder.tvTime.setTextColor(Color.parseColor("#8B8B8B")); // Xám
        }
    }

    @Override
    public int getItemCount() {
        return deadlineList != null ? deadlineList.size() : 0;
    }

    static class DeadlineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        ImageView ivCalendar;

        public DeadlineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_deadline_title);
            tvTime = itemView.findViewById(R.id.tv_deadline_time);
            ivCalendar = itemView.findViewById(R.id.iv_calendar_icon);
        }
    }
}