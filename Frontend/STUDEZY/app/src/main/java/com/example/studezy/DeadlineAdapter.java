package com.example.studezy;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.DeadlineModel;
import java.util.List;

public class DeadlineAdapter extends RecyclerView.Adapter<DeadlineAdapter.DeadlineViewHolder> {

    private List<DeadlineModel> deadlineList;
    private OnItemToggledListener listener;

    public interface OnItemToggledListener {
        void onToggled(int deadlineId, boolean isCompleted);
    }

    public DeadlineAdapter(List<DeadlineModel> deadlineList, OnItemToggledListener listener) {
        this.deadlineList = deadlineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeadlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deadline, parent, false);
        return new DeadlineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeadlineViewHolder holder, int position) {
        DeadlineModel item = deadlineList.get(position);

        // 1. Reset toàn bộ UI về trạng thái mặc định để tránh lỗi tái sử dụng View
        holder.itemView.setAlpha(1.0f);
        holder.cbDeadline.setVisibility(View.VISIBLE);
        holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.tvTitle.setTextColor(Color.parseColor("#1E3A8A"));

        // Cực kỳ quan trọng: Xóa bỏ mọi bộ lọc màu cũ để icon hiển thị đúng màu gốc
        holder.ivCalendar.clearColorFilter();

        // Set trạng thái tick
        holder.cbDeadline.setChecked(item.isCompleted());

        // Biến kiểm tra quá hạn
        boolean isOverdue = item.getRemainingText() != null && item.getRemainingText().equals("Đã quá hạn");

        // 2. LOGIC ƯU TIÊN HIỂN THỊ
        if (item.isCompleted()) {
            // ĐÃ HOÀN THÀNH
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvTitle.setText(item.getTitle());

            holder.tvTime.setText("Đã hoàn thành");
            holder.tvTime.setTextColor(Color.parseColor("#9E9E9E"));
            holder.ivCalendar.setImageResource(R.drawable.ic_checkbox_tick); // Dùng icon tick (nếu có) hoặc giữ ic_calendar

        } else if (isOverdue) {
            // QUÁ HẠN
            holder.itemView.setAlpha(0.5f);
            holder.cbDeadline.setVisibility(View.INVISIBLE);

            holder.tvTitle.setText(item.getTitle());
            holder.tvTime.setText("Đã quá hạn");
            holder.tvTime.setTextColor(Color.parseColor("#FF4D3D"));

            // Hiện icon đồng hồ đỏ
            holder.ivCalendar.setImageResource(R.drawable.ic_redclock);

        } else {
            // CÁC DEADLINE BÌNH THƯỜNG
            holder.tvTitle.setText(item.getTitle());
            // Reset Checkbox về XÁM
            holder.cbDeadline.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#C7C7C7")));

            if (item.isUrgent()) {
                // Hạn <= 1 ngày: Chữ đỏ, icon đồng hồ đỏ
                holder.tvTime.setText(item.getRemainingText());
                holder.tvTime.setTextColor(Color.parseColor("#FF4D3D"));
                holder.ivCalendar.setImageResource(R.drawable.ic_redclock);
            } else {
                // Hạn > 1 ngày: Chữ xám, icon lịch xám
                holder.tvTime.setText(item.getRemainingText());
                holder.tvTime.setTextColor(Color.parseColor("#8B8B8B"));
                holder.ivCalendar.setImageResource(R.drawable.ic_calendar);
            }
        }

        // 3. Xử lý sự kiện khi click CheckBox
        holder.cbDeadline.setOnClickListener(v -> {
            boolean isNowChecked = holder.cbDeadline.isChecked();
            item.setCompleted(isNowChecked);

            // Cập nhật lại màu sắc giao diện ngay lập tức
            notifyItemChanged(position);

            if (listener != null) {
                listener.onToggled(item.getId(), isNowChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deadlineList != null ? deadlineList.size() : 0;
    }

    static class DeadlineViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime;
        ImageView ivCalendar;
        CheckBox cbDeadline;

        public DeadlineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_deadline_title);
            tvTime = itemView.findViewById(R.id.tv_deadline_time);
            ivCalendar = itemView.findViewById(R.id.iv_calendar_icon);
            cbDeadline = itemView.findViewById(R.id.cb_deadline);
        }
    }
}