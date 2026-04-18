package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- BẮT ĐẦU LOGIC ĐIỀU HƯỚNG THEO LƯỢT MỞ APP ---

        // 1. Kiểm tra biến APP_FIRST_OPEN trong bộ nhớ (mặc định là true nếu chưa từng lưu)
        SharedPreferences prefs = getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        boolean isFirstOpen = prefs.getBoolean("APP_FIRST_OPEN", true);

        // 2. Lấy NavController để điều khiển luồng màn hình
        // LƯU Ý: ID "nav_host_fragment" bên dưới phải khớp với ID trong file activity_main.xml của bạn.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavInflater navInflater = navController.getNavInflater();
            NavGraph navGraph = navInflater.inflate(R.navigation.nav_graph);

            // 3. Xử lý logic chuyển màn hình
            if (isFirstOpen) {
                // Nếu mở app lần đầu -> Đặt trang bắt đầu là Welcome
                navGraph.setStartDestination(R.id.welcomeFragment);

                // Đánh dấu là đã mở app rồi để lần sau không vào đây nữa
                prefs.edit().putBoolean("APP_FIRST_OPEN", false).apply();
            } else {
                // Nếu đã mở app từ lần thứ 2 trở đi -> Vào thẳng màn Đăng nhập
                navGraph.setStartDestination(R.id.loginFragment);
            }

            // Áp dụng biểu đồ điều hướng (graph) mới này cho NavController
            navController.setGraph(navGraph);
        }
        // --- KẾT THÚC LOGIC ĐIỀU HƯỚNG ---
    }
}