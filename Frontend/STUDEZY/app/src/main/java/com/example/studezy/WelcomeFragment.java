package com.example.studezy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Ánh xạ layout XML vào Fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    // Code xử lý sự kiện LUÔN LUÔN nên viết ở hàm onViewCreated này
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Tìm nút bấm thông qua ID
        Button btnLoginWelcome = view.findViewById(R.id.btn_welcome_login);
        Button btnRegisterWelcome = view.findViewById(R.id.btn_welcome_register);

        // 2. Bắt sự kiện khi bấm nút Đăng nhập
        btnLoginWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lệnh chuyển trang kỳ diệu của Navigation
                Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_loginFragment);
            }
        });

        // 3. (Tùy chọn) Bắt sự kiện khi bấm nút Tạo tài khoản mới
        btnRegisterWelcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_registerFragment);
            }
        });
    }
}