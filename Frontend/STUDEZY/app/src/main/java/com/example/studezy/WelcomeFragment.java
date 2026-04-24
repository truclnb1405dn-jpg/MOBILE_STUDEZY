package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("USER_TOKEN", "");
        long lastActivity = prefs.getLong("LAST_ACTIVITY_TIME", 0);

        // Cờ kiểm tra xem đây có phải lần đầu mở app không (Mặc định là true)
        boolean isFirstTime = prefs.getBoolean("IS_FIRST_TIME", true);

        long currentTime = System.currentTimeMillis();
        long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000; // 3 ngày ra miligiây

        // 1. KIỂM TRA ĐĂNG NHẬP
        if (!token.isEmpty() && (currentTime - lastActivity <= threeDaysInMillis)) {
            // Nếu đã đăng nhập VÀ chưa quá 3 ngày -> Chuyển thẳng vào Home
            // Gọi trực tiếp R.id.homeFragment để khắc phục lỗi báo đỏ
            Navigation.findNavController(view).navigate(R.id.homeFragment);

        } else {
            // 2. NẾU CHƯA ĐĂNG NHẬP HOẶC ĐÃ QUÁ 3 NGÀY
            if (!token.isEmpty() && (currentTime - lastActivity > threeDaysInMillis)) {
                // Xóa dữ liệu phiên bản cũ nếu đã hết hạn
                prefs.edit().remove("USER_TOKEN").remove("USER_FULL_NAME").remove("LAST_ACTIVITY_TIME").apply();
                Toast.makeText(getContext(), "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
            }

            // 3. XỬ LÝ LOGIC LẦN ĐẦU / LẦN 2
            if (isFirstTime) {
                // Lần đầu tiên mở app: Đánh dấu là đã mở và GIỮ NGUYÊN ở trang Welcome
                prefs.edit().putBoolean("IS_FIRST_TIME", false).apply();
            } else {
                // Lần thứ 2 trở đi: CHUYỂN THẲNG sang trang Login
                Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_loginFragment);
            }
        }

        // --- CÁC SỰ KIỆN NÚT BẤM CỦA TRANG WELCOME ---
        // (Chỉ chạy khi người dùng thực sự ở lại trang Welcome ở lần đầu tiên)
        Button btnLoginWelcome = view.findViewById(R.id.btn_welcome_login);
        Button btnRegisterWelcome = view.findViewById(R.id.btn_welcome_register);

        if (btnLoginWelcome != null) {
            btnLoginWelcome.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_loginFragment)
            );
        }

        if (btnRegisterWelcome != null) {
            btnRegisterWelcome.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_registerFragment)
            );
        }
    }
}