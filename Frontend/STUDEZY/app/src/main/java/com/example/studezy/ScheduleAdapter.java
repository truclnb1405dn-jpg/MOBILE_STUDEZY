package com.example.studezy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.ScheduleModel;
import java.util.ArrayList;
import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleModel> scheduleList = new ArrayList<>();

    // 1. THÊM INTERFACE ĐỂ LÀM CẦU NỐI RA FRAGMENT
    public interface OnItemClickListener {
        void onEditClick(ScheduleModel schedule);
        void onDeleteClick(ScheduleModel schedule);
    }

    private OnItemClickListener listener;

    // 2. SỬA LẠI CONSTRUCTOR ĐỂ NHẬN LISTENER
    public ScheduleAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_class, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleModel item = scheduleList.get(position);

        holder.tvCourseName.setText(item.getSubjectName());
        holder.tvRoom.setText(item.getRoom());
        holder.tvTime.setText(item.getTimeString());

        // 3. GẮN SỰ KIỆN CLICK CHO 2 NÚT SỬA VÀ XÓA Ở ĐÂY
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    // Hàm cập nhật dữ liệu tốt nhất
    public void updateData(List<ScheduleModel> newList) {
        this.scheduleList.clear();
        if (newList != null) {
            this.scheduleList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    // Thêm hàm xóa một item nếu cần sau này
    public void removeItem(int position) {
        if (position >= 0 && position < scheduleList.size()) {
            scheduleList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvRoom, tvTime;
        ImageButton btnDelete, btnEdit;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvCourseName = view.findViewById(R.id.tv_subject_name);
            tvRoom = view.findViewById(R.id.tv_room);
            tvTime = view.findViewById(R.id.tv_time);
            btnDelete = view.findViewById(R.id.btn_delete);
            btnEdit = view.findViewById(R.id.btn_edit);
        }
    }
}