package com.example.studezy;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment; // Đổi từ BottomSheetDialogFragment sang DialogFragment
import com.example.studezy.api.ClassModel;
import com.google.android.material.textfield.TextInputEditText;

// Kế thừa DialogFragment để nó hiển thị nổi ở giữa màn hình
public class DetailClassBottomSheet extends DialogFragment {

    private ClassModel classModel;

    public static DetailClassBottomSheet newInstance(ClassModel model) {
        DetailClassBottomSheet fragment = new DetailClassBottomSheet();
        fragment.classModel = model;
        return fragment;
    }

    // Thiết lập cho Dialog hiển thị ở giữa, nền ngoài tối màu (dim), nền trong trong suốt để hiện góc bo tròn
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // Đặt nền trong suốt để viền bo tròn của LinearLayout/ScrollView trong XML được hiển thị
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // Thiết lập chiều rộng match_parent (có lề 2 bên từ XML) và chiều cao wrap_content
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_class_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        TextInputEditText etSubject = view.findViewById(R.id.et_subject_name);
        TextInputEditText etTime = view.findViewById(R.id.et_start_time);
        TextInputEditText etRoom = view.findViewById(R.id.et_room);
        TextInputEditText etNote = view.findViewById(R.id.et_note);
        View btnClose = view.findViewById(R.id.btn_close);

        // 2. Đổ dữ liệu
        if (classModel != null) {
            etSubject.setText(classModel.getSubjectName());
            etRoom.setText(classModel.getRoom());
            etTime.setText(classModel.getTimeString());

            // Xử lý Highlight ngày học
            highlightDays(view);
        }

        // 3. Xử lý nút Đóng
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void highlightDays(View view) {
        // LƯU Ý: Giả sử ClassModel của bạn có hàm getDayOfWeek() trả về "2", "3", "4"...
        // Nếu ClassModel chưa có, bạn cần thêm biến day_of_week và hàm getDayOfWeek() vào ClassModel.java nhé!
        String day = classModel.getDayOfWeek();
        if (day == null) return;

        TextView tvDayToHighlight = null;

        switch (day) {
            case "2": tvDayToHighlight = view.findViewById(R.id.tv_day_2); break;
            case "3": tvDayToHighlight = view.findViewById(R.id.tv_day_3); break;
            case "4": tvDayToHighlight = view.findViewById(R.id.tv_day_4); break;
            case "5": tvDayToHighlight = view.findViewById(R.id.tv_day_5); break;
            case "6": tvDayToHighlight = view.findViewById(R.id.tv_day_6); break;
            case "7": tvDayToHighlight = view.findViewById(R.id.tv_day_7); break;
            case "8": tvDayToHighlight = view.findViewById(R.id.tv_day_8); break;
        }

        if (tvDayToHighlight != null) {
            // Đổi background sang màu xanh (giống tab được chọn)
            tvDayToHighlight.setBackgroundResource(R.drawable.bg_tab_selected);
            // Đổi màu chữ thành trắng
            tvDayToHighlight.setTextColor(Color.WHITE);
        }
    }
}