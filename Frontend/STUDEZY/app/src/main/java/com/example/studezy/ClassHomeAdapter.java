package com.example.studezy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.ClassModel;
import java.util.List;

public class ClassHomeAdapter extends RecyclerView.Adapter<ClassHomeAdapter.ViewHolder> {

    private List<ClassModel> classList;
    private OnItemClickListener listener; // BỔ SUNG 1: Khai báo biến listener

    // BỔ SUNG 2: Tạo một Interface để định nghĩa sự kiện click
    public interface OnItemClickListener {
        void onEditClick(ClassModel classModel);
    }

    // BỔ SUNG 3: Sửa lại Constructor để nhận thêm listener từ Fragment truyền vào
    public ClassHomeAdapter(List<ClassModel> classList, OnItemClickListener listener) {
        this.classList = classList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_class_home.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassModel model = classList.get(position);

        holder.tvSubjectName.setText(model.getSubjectName());
        holder.tvRoom.setText(model.getRoom());

        // Sử dụng getTimeString() theo cấu trúc ClassModel của bạn
        String timeDisplay = "Giờ  •  " + model.getTimeString();
        holder.tvTime.setText(timeDisplay);

        // BỔ SUNG 4: Bắt sự kiện click vào nút chỉnh sửa (cây bút)
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                // Trả về model của môn học được click cho Fragment xử lý
                listener.onEditClick(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return classList == null ? 0 : classList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvRoom, tvTime;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ đúng ID trong file item_class_home.xml
            tvSubjectName = itemView.findViewById(R.id.tv_subject_name);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }
}