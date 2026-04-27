package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

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

        // --- BẮT ĐẦU LOGIC ĐIỀU HƯỚNG TẬP TRUNG ---
        SharedPreferences prefs = getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);

        // 1. Lấy tất cả thông tin cần thiết
        boolean isFirstOpen = prefs.getBoolean("APP_FIRST_OPEN", true);
        String token = prefs.getString("USER_TOKEN", "");
        long lastActivity = prefs.getLong("LAST_ACTIVITY_TIME", 0);
        long currentTime = System.currentTimeMillis();
        long threeDaysInMillis = 3L * 24 * 60 * 60 * 1000;

        // 2. Chuẩn bị Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavInflater navInflater = navController.getNavInflater();
            NavGraph navGraph = navInflater.inflate(R.navigation.nav_graph);

            // 3. KIỂM TRA VÀ CHỌN TRANG BẮT ĐẦU (Start Destination)
            if (isFirstOpen) {
                // Lần đầu mở app -> Vào trang Welcome
                navGraph.setStartDestination(R.id.welcomeFragment);
                // Đánh dấu là đã mở app
                prefs.edit().putBoolean("APP_FIRST_OPEN", false).apply();

            } else if (!token.isEmpty() && (currentTime - lastActivity <= threeDaysInMillis)) {
                // Đã đăng nhập và chưa quá 3 ngày -> Vào thẳng trang Home
                navGraph.setStartDestination(R.id.homeFragment);

            } else {
                // Hết hạn 3 ngày HOẶC chưa đăng nhập -> Vào trang Login
                if (!token.isEmpty() && (currentTime - lastActivity > threeDaysInMillis)) {
                    prefs.edit().remove("USER_TOKEN").remove("USER_FULL_NAME").remove("LAST_ACTIVITY_TIME").apply();
                    Toast.makeText(this, "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
                }
                navGraph.setStartDestination(R.id.loginFragment);
            }

            // Áp dụng graph đã chọn
            navController.setGraph(navGraph);
        }
        // --- KẾT THÚC LOGIC ĐIỀU HƯỚNG ---
    }
}
