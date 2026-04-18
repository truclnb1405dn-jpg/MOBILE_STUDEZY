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

        // 1. Reset toàn bộ UI về trạng thái mặc định
        holder.itemView.setAlpha(1.0f);
        holder.cbDeadline.setVisibility(View.VISIBLE);
        holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        holder.tvTitle.setTextColor(Color.parseColor("#1E3A8A"));

        // Luôn set trạng thái tick dựa vào dữ liệu từ database trước
        holder.cbDeadline.setChecked(item.isCompleted());

        // Biến kiểm tra quá hạn
        boolean isOverdue = item.getRemainingText() != null && item.getRemainingText().equals("Đã quá hạn");

        // 2. LOGIC ƯU TIÊN HIỂN THỊ MỚI
        if (item.isCompleted()) {
            // ƯU TIÊN 1: ĐÃ HOÀN THÀNH (Bất kể hạn nộp là ngày nào trong quá khứ hay tương lai)
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvTitle.setText(item.getTitle());

            holder.tvTime.setText("Đã hoàn thành");
            holder.tvTime.setTextColor(Color.parseColor("#9E9E9E"));
            holder.ivCalendar.setImageResource(R.drawable.ic_checkbox_tick);

            holder.cbDeadline.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));

        } else if (isOverdue) {
            // ƯU TIÊN 2: CHƯA HOÀN THÀNH VÀ ĐÃ QUÁ HẠN
            holder.itemView.setAlpha(0.5f); // Làm mờ
            holder.cbDeadline.setVisibility(View.INVISIBLE); // Ẩn nút tick

            holder.tvTitle.setText(item.getTitle());
            holder.tvTime.setText("Đã quá hạn");
            holder.tvTime.setTextColor(Color.parseColor("#FF4D3D"));
            holder.ivCalendar.setImageResource(R.drawable.ic_calendar);

        } else {
            // ƯU TIÊN 3: CÁC DEADLINE BÌNH THƯỜNG (Chưa hoàn thành, chưa tới hạn)
            holder.tvTitle.setText(item.getTitle());

            if (item.isUrgent()) {
                holder.tvTime.setText("⏰  " + item.getRemainingText());
                holder.tvTime.setTextColor(Color.parseColor("#FF4D3D"));
            } else {
                holder.tvTime.setText(item.getRemainingText());
                holder.tvTime.setTextColor(Color.parseColor("#8B8B8B"));
            }

            holder.ivCalendar.setImageResource(R.drawable.ic_calendar);
            holder.cbDeadline.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#C7C7C7")));
        }

        // 3. Xử lý sự kiện khi người dùng click vào nút Tick
        holder.cbDeadline.setOnClickListener(v -> {
            boolean isNowChecked = holder.cbDeadline.isChecked();
            item.setCompleted(isNowChecked);
            notifyItemChanged(position); // Refresh lại giao diện ngay lập tức

            if (listener != null) {
                listener.onToggled(item.getId(), isNowChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deadlineList != null ? deadlineList.size() : 0;
    }

    // Đã thêm cbDeadline và ánh xạ findViewById
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