package com.example.studezy;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.studezy.api.AddScheduleRequest;
import com.example.studezy.api.RegisterResponse;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.ScheduleModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddClassBottomSheet extends BottomSheetDialogFragment {

    private String token;
    private int semesterId;
    private ScheduleModel scheduleToEdit; // Biến lưu môn học cần sửa
    private OnClassAddedListener listener;

    public interface OnClassAddedListener {
        void onClassAdded();
    }

    public static AddClassBottomSheet newInstance(String token, @Nullable ScheduleModel schedule, int semesterId, OnClassAddedListener listener) {
        AddClassBottomSheet fragment = new AddClassBottomSheet();
        fragment.token = token;
        fragment.scheduleToEdit = schedule;
        fragment.semesterId = semesterId; // Gán ID học kỳ
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_sheet_title);
        EditText etSubject = view.findViewById(R.id.et_subject_name);
        EditText etTime = view.findViewById(R.id.et_start_time);
        EditText etRoom = view.findViewById(R.id.et_room);
        EditText etNote = view.findViewById(R.id.et_note);

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        final String[] selectedDay = {null};

        int[] dayIds = {
                R.id.tv_day_2, R.id.tv_day_3, R.id.tv_day_4,
                R.id.tv_day_5, R.id.tv_day_6, R.id.tv_day_7, R.id.tv_day_8
        };
        String[] dayValues = {"2", "3", "4", "5", "6", "7", "8"};

        // Xử lý sự kiện bấm chọn Thứ
        for (int i = 0; i < dayIds.length; i++) {
            TextView tvDay = view.findViewById(dayIds[i]);
            final String value = dayValues[i];

            tvDay.setOnClickListener(v -> {
                selectedDay[0] = value;
                for (int id : dayIds) {
                    view.findViewById(id).setSelected(false);
                }
                tvDay.setSelected(true);
            });
        }

        // Bảng chọn Giờ
        etTime.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            new TimePickerDialog(getContext(), (picker, hour, minute) -> {
                etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show();
        });

        // === NẾU LÀ CHẾ ĐỘ SỬA: Điền sẵn dữ liệu cũ vào Form ===
        if (scheduleToEdit != null) {
            if (tvTitle != null) tvTitle.setText("Chỉnh sửa lịch học");
            etSubject.setText(scheduleToEdit.getSubjectName());
            etRoom.setText(scheduleToEdit.getRoom());
            if (scheduleToEdit.getNote() != null) etNote.setText(scheduleToEdit.getNote());

            // Tách giờ từ định dạng "07h00 - 08h00" thành "07:00" để hiển thị vào ô nhập
            String timeStr = scheduleToEdit.getTimeString();
            if (timeStr != null && timeStr.contains("-")) {
                etTime.setText(timeStr.split("-")[0].trim().replace("h", ":"));
            }

            // Kích hoạt màu Gradient cho nút Thứ cũ
            selectedDay[0] = scheduleToEdit.getDayOfWeek();
            for (int i = 0; i < dayIds.length; i++) {
                if (dayValues[i].equals(selectedDay[0])) {
                    View dayView = view.findViewById(dayIds[i]);
                    if (dayView != null) dayView.setSelected(true);
                }
            }
        }

        // === XỬ LÝ LƯU (THÊM MỚI HOẶC SỬA) ===
        btnSave.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String timeStr = etTime.getText().toString().trim();
            String room = etRoom.getText().toString().trim();
            String note = etNote != null ? etNote.getText().toString().trim() : "";

            if (subject.isEmpty() || timeStr.isEmpty() || room.isEmpty() || selectedDay[0] == null) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin (*)", Toast.LENGTH_SHORT).show();
                return;
            }

            AddScheduleRequest request = new AddScheduleRequest(semesterId, subject, selectedDay[0], timeStr, room, note);

            if (scheduleToEdit == null) {
                // CHẾ ĐỘ THÊM MỚI
                RetrofitClient.getInstance().getApi().addClassSchedule("Token " + token, request)
                        .enqueue(createCallback("Thêm lịch học thành công!"));
            } else {
                // CHẾ ĐỘ SỬA
                RetrofitClient.getInstance().getApi().updateClassSchedule("Token " + token, scheduleToEdit.getId(), request)
                        .enqueue(createCallback("Chỉnh sửa thành công!"));
            }
        });

        // Nút Hủy
        btnCancel.setOnClickListener(v -> dismiss());
    }

    // Hàm gọi chung để xử lý API trả về
    private Callback<RegisterResponse> createCallback(String successMessage) {
        return new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (listener != null) listener.onClassAdded(); // Yêu cầu tải lại list
                } else {
                    // === BẮT LỖI TỪ DJANGO VÀ HIỂN THỊ ===
                    try {
                        if (response.errorBody() != null) {
                            // Đọc chuỗi JSON lỗi từ Backend trả về
                            String errorString = response.errorBody().string();
                            // Chuyển thành Object để lấy trường "message"
                            org.json.JSONObject jsonObject = new org.json.JSONObject(errorString);
                            String errorMessage = jsonObject.getString("message");

                            // Hiển thị câu lỗi chính xác lên màn hình
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        };
    }
}
