package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studezy.api.ClassModel;
import com.example.studezy.api.DeadlineModel;
import com.example.studezy.api.HomeSummaryResponse;
import com.example.studezy.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class HomeFragment extends Fragment {

    private TextView tvAvatar, tvGreeting, tvSummary;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các thành phần giao diện
        tvAvatar = view.findViewById(R.id.tv_avatar);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvSummary = view.findViewById(R.id.tv_summary);

        // 2. Lấy Full Name và Token đã lưu lúc Đăng nhập
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        String fullName = prefs.getString("USER_FULL_NAME", "Sinh Viên");
        String token = prefs.getString("USER_TOKEN", "");

        // 3. Xử lý chuỗi tên để hiển thị lên Header
        processNameAndDisplay(fullName);

        android.util.Log.d("API_TEST", "Token hiện tại là: " + token);

        if (!token.isEmpty()) {
            fetchHomeSummary(token);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy Token", Toast.LENGTH_SHORT).show();
        }

        setupCalendarCard(view);

        RecyclerView rvClasses = view.findViewById(R.id.rv_classes_today);
        // Cài đặt hiển thị danh sách theo chiều dọc
        rvClasses.setLayoutManager(new LinearLayoutManager(getContext()));

        if (!token.isEmpty()) {
            fetchHomeSummary(token);
            fetchClassesToday(token, rvClasses); // Gọi hàm tải danh sách
        }
        RecyclerView rvDeadlines = view.findViewById(R.id.rv_deadlines);
        rvDeadlines.setLayoutManager(new LinearLayoutManager(getContext()));
        if (!token.isEmpty()) {
            fetchTopDeadlines(token, rvDeadlines); // Thêm hàm này
        }
    }

    private void setupCalendarCard(View view) {
        // 1. Gom các View vào mảng để dễ dàng dùng vòng lặp xử lý
        LinearLayout[] layouts = {
                view.findViewById(R.id.layout_t2), view.findViewById(R.id.layout_t3),
                view.findViewById(R.id.layout_t4), view.findViewById(R.id.layout_t5),
                view.findViewById(R.id.layout_t6), view.findViewById(R.id.layout_t7),
                view.findViewById(R.id.layout_cn)
        };

        TextView[] tvLabels = {
                view.findViewById(R.id.tv_label_t2), view.findViewById(R.id.tv_label_t3),
                view.findViewById(R.id.tv_label_t4), view.findViewById(R.id.tv_label_t5),
                view.findViewById(R.id.tv_label_t6), view.findViewById(R.id.tv_label_t7),
                view.findViewById(R.id.tv_label_cn)
        };

        TextView[] tvDates = {
                view.findViewById(R.id.tv_date_t2), view.findViewById(R.id.tv_date_t3),
                view.findViewById(R.id.tv_date_t4), view.findViewById(R.id.tv_date_t5),
                view.findViewById(R.id.tv_date_t6), view.findViewById(R.id.tv_date_t7),
                view.findViewById(R.id.tv_date_cn)
        };

        // 2. Lấy thời gian thực tế của hệ thống
        Calendar calendar = Calendar.getInstance();

        // Tìm vị trí của ngày hôm nay (0: Thứ 2, ..., 6: Chủ nhật)
        // Trong Java Calendar, Chủ nhật là 1, Thứ 2 là 2... nên ta phải tính toán một chút
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0) {
            todayIndex = 6; // Nếu là Chủ nhật (1 - 2 = -1) thì gán vào vị trí cuối cùng của mảng
        }

        // 3. Tua lịch về ngày Thứ 2 của tuần hiện tại
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Định dạng lấy 2 chữ số của ngày (VD: 01, 09, 15)
        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());

        // 4. Vòng lặp gán dữ liệu cho 7 ngày
        for (int i = 0; i < 7; i++) {
            // Cập nhật ngày
            tvDates[i].setText(sdf.format(calendar.getTime()));

            // Cập nhật màu sắc cho ngày hôm nay
            if (i == todayIndex) {
                layouts[i].setBackgroundResource(R.drawable.bg_tab_selected);
                tvLabels[i].setTextColor(Color.parseColor("#FFFFFF"));
                tvDates[i].setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                layouts[i].setBackgroundResource(R.drawable.bg_tab_unselected);
                tvLabels[i].setTextColor(Color.parseColor("#222222"));
                tvDates[i].setTextColor(Color.parseColor("#111111"));
            }

            // Tiến lên 1 ngày để xử lý cho ô tiếp theo
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
    // Thuật toán tách Tên lót và Tên
    private void processNameAndDisplay(String fullName) {
        String[] words = fullName.trim().split("\\s+");
        String initials = "";
        String firstName = "";

        if (words.length >= 2) {
            // Lấy 2 từ cuối cùng (VD: "Bảo" và "Trúc")
            String middleName = words[words.length - 2];
            String lastWord = words[words.length - 1];

            // Cắt chữ cái đầu: B + T -> BT
            initials = String.valueOf(middleName.charAt(0)) + String.valueOf(lastWord.charAt(0));
            firstName = lastWord;
            // Nếu bạn thích chào bằng 2 chữ "Chào Bảo Trúc!", hãy đổi thành: firstName = middleName + " " + lastWord;

        } else if (words.length == 1) {
            initials = String.valueOf(words[0].charAt(0));
            firstName = words[0];
        }

        // Đổ dữ liệu lên giao diện
        tvAvatar.setText(initials.toUpperCase());
        tvGreeting.setText("Chào " + firstName + "!");
    }

    // Gọi API lấy dữ liệu tóm tắt hôm nay
    private void fetchHomeSummary(String token) {
        // Lưu ý: Cú pháp chuẩn của Token Authentication trong Django là phải có chữ "Token " ở trước
        String authHeader = "Token " + token;

        RetrofitClient.getInstance().getApi().getHomeSummary(authHeader).enqueue(new Callback<HomeSummaryResponse>() {
            public void onResponse(Call<HomeSummaryResponse> call, Response<HomeSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int classes = response.body().getClassesToday();
                    int deadlines = response.body().getDeadlinesToday();
                    String summaryText = "Hôm nay bạn có " + classes + " tiết học và " + deadlines + " deadline cần xử lý";
                    tvSummary.setText(summaryText);
                } else {
                    // THÊM ĐOẠN NÀY ĐỂ XEM LỖI TỪ DJANGO
                    try {
                        Toast.makeText(getContext(), "Lỗi API: " + response.errorBody().string(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<HomeSummaryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể tải dữ liệu hôm nay", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchClassesToday(String token, RecyclerView rvClasses) {
        String authHeader = "Token " + token;

        RetrofitClient.getInstance().getApi().getClassesToday(authHeader).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ClassModel> classList = response.body();

                    // Đưa danh sách dữ liệu vào Adapter, và gắn Adapter vào RecyclerView
                    ClassAdapter adapter = new ClassAdapter(classList);
                    rvClasses.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải lịch học", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchTopDeadlines(String token, RecyclerView rvDeadlines) {
        String authHeader = "Token " + token;
        RetrofitClient.getInstance().getApi().getTopDeadlines(authHeader).enqueue(new Callback<List<DeadlineModel>>() {
            @Override
            public void onResponse(Call<List<DeadlineModel>> call, Response<List<DeadlineModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeadlineAdapter adapter = new DeadlineAdapter(response.body());
                    rvDeadlines.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<DeadlineModel>> call, Throwable t) {
                // Xử lý lỗi
            }
        });
    }
}
