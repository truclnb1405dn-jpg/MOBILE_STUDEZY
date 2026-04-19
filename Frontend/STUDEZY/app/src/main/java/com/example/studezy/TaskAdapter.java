package com.example.studezy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.TaskModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> taskList;

    public interface OnTaskClickListener {
        void onCheckClick(int position, TaskModel task);
        void onEditClick(int position, TaskModel task);
        void onDeleteClick(int position, TaskModel task); // BỔ SUNG: Xóa task
    }

    private OnTaskClickListener listener;

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    public TaskAdapter(List<TaskModel> taskList) {
        this.taskList = taskList;
    }

    public void setTaskList(List<TaskModel> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDesc.setText(task.getDescription());
        holder.tvDate.setText(task.getDeadlineDate());

        if (task.getStatus() == 1) {
            holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_green);
            holder.tvTimeLeft.setText("Đã hoàn thành");
            holder.tvTimeLeft.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            if (holder.ivCheck != null) holder.ivCheck.setImageResource(R.drawable.ic_hoanthanh1);
        } else {
            holder.tvTimeLeft.setText(task.getTimeLeft());
            if (holder.ivCheck != null) holder.ivCheck.setImageResource(R.drawable.ic_tron);

            String dateStr = task.getDeadlineDate();
            if (dateStr != null && dateStr.length() >= 10) {
                try {
                    String justDate = dateStr.substring(0, 10);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date deadlineDate = sdf.parse(justDate);

                    if (deadlineDate != null) {
                        long diffInMillis = deadlineDate.getTime() - System.currentTimeMillis();
                        long diffInHours = diffInMillis / (1000 * 60 * 60);
                        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

                        if (diffInHours < 24) {
                            holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_pink);
                            holder.tvTimeLeft.setTextColor(android.graphics.Color.parseColor("#E53935"));
                        } else if (diffInDays < 7) {
                            holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_yellow);
                            holder.tvTimeLeft.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                        } else {
                            holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_blue);
                            holder.tvTimeLeft.setTextColor(android.graphics.Color.parseColor("#1E88E5"));
                        }
                    }
                } catch (Exception e) {
                    holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_blue);
                }
            } else {
                holder.layoutContainer.setBackgroundResource(R.drawable.bg_task_blue);
            }
        }

        if (holder.ivCheck != null) {
            holder.ivCheck.setOnClickListener(v -> {
                if (listener != null) listener.onCheckClick(position, task);
            });
        }

        if (holder.ivEdit != null) {
            holder.ivEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEditClick(position, task);
            });
        }

        // BỔ SUNG: Bắt sự kiện click vào thùng rác
        if (holder.ivDelete != null) {
            holder.ivDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(position, task);
            });
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDate, tvTimeLeft;
        View layoutContainer;
        ImageView ivCheck;
        ImageView ivEdit;
        ImageView ivDelete; // BỔ SUNG

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDesc = itemView.findViewById(R.id.tvTaskDesc);
            tvDate = itemView.findViewById(R.id.tvDeadlineDate);
            tvTimeLeft = itemView.findViewById(R.id.tvTimeLeft);
            layoutContainer = itemView.findViewById(R.id.layoutTaskContainer);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete); // BỔ SUNG
        }
    }
}