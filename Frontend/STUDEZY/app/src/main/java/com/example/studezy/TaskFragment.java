package com.example.studezy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class TaskFragment extends Fragment {

    public TaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo NavController
        NavController navController = Navigation.findNavController(view);

        // 2. Xử lý nút Back (Nếu có ID btn_back trong fragment_task.xml)
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navController.navigateUp());
        }

        // 3. Logic chuyển trang cho thanh Bottom Menu

        // Về Trang chủ
        view.findViewById(R.id.menu_home).setOnClickListener(v -> {
            navController.navigate(R.id.homeFragment);
        });

        // Sang trang Lịch
        view.findViewById(R.id.menu_schedule).setOnClickListener(v -> {
            navController.navigate(R.id.scheduleFragment);
        });

        // Sang trang Cài đặt
        view.findViewById(R.id.menu_settings).setOnClickListener(v -> {
            navController.navigate(R.id.settingsFragment);
        });

        // Nút Nhiệm vụ (Đang ở trang này)
        view.findViewById(R.id.menu_task).setOnClickListener(v -> {
            // Có thể xử lý cuộn lên đầu danh sách nhiệm vụ nếu cần
        });
    }
}