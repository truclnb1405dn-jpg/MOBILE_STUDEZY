package com.example.studezy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ScheduleFragment extends Fragment {

    public ScheduleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo NavController để điều hướng
        NavController navController = Navigation.findNavController(view);

        // 2. Xử lý nút Back (Nếu màn hình của bạn có nút mũi tên quay lại)
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navController.navigateUp());
        }

        // 3. Logic cho thanh Menu bên dưới

        // Chuyển sang Trang chủ
        view.findViewById(R.id.menu_home).setOnClickListener(v -> {
            navController.navigate(R.id.homeFragment);
        });

        // Chuyển sang trang Nhiệm vụ
        view.findViewById(R.id.menu_task).setOnClickListener(v -> {
            navController.navigate(R.id.taskFragment);
        });

        // Chuyển sang trang Cài đặt
        view.findViewById(R.id.menu_settings).setOnClickListener(v -> {
            navController.navigate(R.id.settingsFragment);
        });

        // Vì đang ở trang Lịch (Schedule), nút Lịch thường không cần navigate nữa
        // Bạn có thể để trống hoặc xử lý cuộn lên đầu trang
        view.findViewById(R.id.menu_schedule).setOnClickListener(v -> {
            // Có thể thêm hiệu ứng gì đó để báo hiệu đang ở trang này
        });
    }
}