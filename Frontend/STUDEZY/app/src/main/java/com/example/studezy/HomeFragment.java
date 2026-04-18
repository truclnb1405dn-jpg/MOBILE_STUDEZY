package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studezy.api.ClassModel;
import com.example.studezy.api.DeadlineModel;
import com.example.studezy.api.HomeSummaryResponse;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.UpdateStatusRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvAvatar, tvGreeting, tvSummary;
    private RecyclerView rvClasses, rvDeadlines;
    private String userToken; // Lưu token ở cấp class để dùng lại khi click chọn ngày

    // Khai báo mảng để quản lý giao diện Lịch
    private LinearLayout[] layoutDays;
    private TextView[] tvLabels;
    private TextView[] tvDates;

    // Mảng lưu chuỗi ngày thực tế (VD: "2026-04-18") để gửi lên API khi click
    private String[] apiDateStrings = new String[7];

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
        rvClasses = view.findViewById(R.id.rv_classes_today);
        rvDeadlines = view.findViewById(R.id.rv_deadlines);

        rvClasses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDeadlines.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Lấy Full Name và Token đã lưu lúc Đăng nhập
        SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        String fullName = prefs.getString("USER_FULL_NAME", "Sinh Viên");
        userToken = prefs.getString("USER_TOKEN", ""); // Gán vào biến class

        // 3. Xử lý chuỗi tên để hiển thị lên Header
        processNameAndDisplay(fullName);

        // 4. Thiết lập lịch và sự kiện click
        setupCalendarCard(view);

        // 5. Load dữ liệu ban đầu (Tóm tắt, Lịch học, Deadline của hôm nay)
        if (!userToken.isEmpty()) {
            fetchHomeSummary(userToken);
            fetchClassesToday(userToken);
            fetchTopDeadlines(userToken);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy Token", Toast.LENGTH_SHORT).show();
        }

        // 6. Xử lý điều hướng thanh menu
        setupBottomNavigation(view);
    }

    private void setupCalendarCard(View view) {
        // Ánh xạ View vào mảng
        layoutDays = new LinearLayout[]{
                view.findViewById(R.id.layout_t2), view.findViewById(R.id.layout_t3),
                view.findViewById(R.id.layout_t4), view.findViewById(R.id.layout_t5),
                view.findViewById(R.id.layout_t6), view.findViewById(R.id.layout_t7),
                view.findViewById(R.id.layout_cn)
        };

        tvLabels = new TextView[]{
                view.findViewById(R.id.tv_label_t2), view.findViewById(R.id.tv_label_t3),
                view.findViewById(R.id.tv_label_t4), view.findViewById(R.id.tv_label_t5),
                view.findViewById(R.id.tv_label_t6), view.findViewById(R.id.tv_label_t7),
                view.findViewById(R.id.tv_label_cn)
        };

        tvDates = new TextView[]{
                view.findViewById(R.id.tv_date_t2), view.findViewById(R.id.tv_date_t3),
                view.findViewById(R.id.tv_date_t4), view.findViewById(R.id.tv_date_t5),
                view.findViewById(R.id.tv_date_t6), view.findViewById(R.id.tv_date_t7),
                view.findViewById(R.id.tv_date_cn)
        };

        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        if (todayIndex < 0) {
            todayIndex = 6;
        }

        // Tua lịch về ngày Thứ 2 của tuần hiện tại
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat sdfUI = new SimpleDateFormat("dd", Locale.getDefault()); // Định dạng hiển thị "18"
        SimpleDateFormat sdfAPI = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Định dạng gửi API "2026-04-18"

        // Gán dữ liệu cho 7 ngày và thiết lập sự kiện click
        for (int i = 0; i < 7; i++) {
            tvDates[i].setText(sdfUI.format(calendar.getTime()));
            apiDateStrings[i] = sdfAPI.format(calendar.getTime()); // Lưu chuỗi ngày để gọi API

            final int currentIndex = i; // Biến hằng để dùng trong onClick

            // Xử lý sự kiện khi người dùng click vào một ngày
            layoutDays[i].setOnClickListener(v -> {
                // 1. Cập nhật lại màu sắc giao diện
                updateCalendarUI(currentIndex);

                // 2. Lấy chuỗi ngày tương ứng vừa click
                String selectedDate = apiDateStrings[currentIndex];

                // 3. Gọi hàm tải dữ liệu theo ngày đã chọn
                fetchDataForSelectedDate(selectedDate);
            });

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Mặc định khi mới vào màn hình, chọn ngày hôm nay
        updateCalendarUI(todayIndex);
    }

    // Hàm cập nhật màu sắc giao diện khi chọn ngày
    private void updateCalendarUI(int selectedIndex) {
        for (int i = 0; i < 7; i++) {
            if (i == selectedIndex) {
                // Ngày được chọn: Nền xanh, chữ trắng
                layoutDays[i].setBackgroundResource(R.drawable.bg_tab_selected);
                tvLabels[i].setTextColor(Color.parseColor("#FFFFFF"));
                tvDates[i].setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                // Ngày không được chọn: Nền xám nhạt, chữ đen
                layoutDays[i].setBackgroundResource(R.drawable.bg_tab_unselected);
                tvLabels[i].setTextColor(Color.parseColor("#222222"));
                tvDates[i].setTextColor(Color.parseColor("#111111"));
            }
        }
    }

    // Hàm gọi dữ liệu khi click vào một ngày cụ thể trên Lịch
    private void fetchDataForSelectedDate(String dateStr) {
        if (userToken == null || userToken.isEmpty()) return;
        String authHeader = "Token " + userToken;

        // 1. Load Lịch Học
        RetrofitClient.getInstance().getApi().getClassesByDate(authHeader, dateStr).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClassAdapter adapter = new ClassAdapter(response.body());
                    rvClasses.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                // Xử lý lỗi
            }
        });

        // 2. Load Deadline (Mọi logic ưu tiên đã được Django xử lý)
        RetrofitClient.getInstance().getApi().getDeadlinesByDate(authHeader, dateStr).enqueue(new Callback<List<DeadlineModel>>() {
            @Override
            public void onResponse(Call<List<DeadlineModel>> call, Response<List<DeadlineModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeadlineAdapter adapter = new DeadlineAdapter(response.body(), (deadlineId, isCompleted) -> {
                        updateDeadlineStatusOnServer(deadlineId, isCompleted);
                    });
                    rvDeadlines.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<DeadlineModel>> call, Throwable t) {
                // Xử lý lỗi
            }
        });
    }

    // Thuật toán tách Tên lót và Tên
    private void processNameAndDisplay(String fullName) {
        String[] words = fullName.trim().split("\\s+");
        String initials = "";
        String firstName = "";

        if (words.length >= 2) {
            String middleName = words[words.length - 2];
            String lastWord = words[words.length - 1];
            initials = String.valueOf(middleName.charAt(0)) + String.valueOf(lastWord.charAt(0));
            firstName = lastWord;
        } else if (words.length == 1) {
            initials = String.valueOf(words[0].charAt(0));
            firstName = words[0];
        }

        tvAvatar.setText(initials.toUpperCase());
        tvGreeting.setText("Chào " + firstName + "!");
    }

    // --- CÁC HÀM GỌI API GIỮ NGUYÊN NHƯ CỦA BẠN ---
    private void fetchHomeSummary(String token) {
        String authHeader = "Token " + token;
        RetrofitClient.getInstance().getApi().getHomeSummary(authHeader).enqueue(new Callback<HomeSummaryResponse>() {
            public void onResponse(Call<HomeSummaryResponse> call, Response<HomeSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int classes = response.body().getClassesToday();
                    int deadlines = response.body().getDeadlinesToday();
                    String summaryText = "Hôm nay bạn có " + classes + " tiết học và " + deadlines + " deadline cần xử lý";
                    tvSummary.setText(summaryText);
                }
            }
            @Override
            public void onFailure(Call<HomeSummaryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Không thể tải dữ liệu tóm tắt", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchClassesToday(String token) {
        String authHeader = "Token " + token;
        RetrofitClient.getInstance().getApi().getClassesToday(authHeader).enqueue(new Callback<List<ClassModel>>() {
            @Override
            public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ClassAdapter adapter = new ClassAdapter(response.body());
                    rvClasses.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<ClassModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải lịch học", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTopDeadlines(String token) {
        String authHeader = "Token " + token;
        RetrofitClient.getInstance().getApi().getTopDeadlines(authHeader).enqueue(new Callback<List<DeadlineModel>>() {
            @Override
            public void onResponse(Call<List<DeadlineModel>> call, Response<List<DeadlineModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeadlineAdapter adapter = new DeadlineAdapter(response.body(), (deadlineId, isCompleted) -> {
                        // Gọi hàm cập nhật lên Server khi tick vào deadline của ngày bất kỳ
                        updateDeadlineStatusOnServer(deadlineId, isCompleted);
                    });
                    rvDeadlines.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<DeadlineModel>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải deadline", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDeadlineStatusOnServer(int deadlineId, boolean isCompleted) {
        if (userToken == null || userToken.isEmpty()) return;

        String authHeader = "Token " + userToken;
        UpdateStatusRequest request = new UpdateStatusRequest(isCompleted);

        RetrofitClient.getInstance().getApi().toggleDeadline(authHeader, deadlineId, request)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(), "Lỗi khi lưu vào CSDL", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getContext(), "Mất kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNavigation(View view) {
        NavController navController = Navigation.findNavController(view);
        view.findViewById(R.id.menu_schedule).setOnClickListener(v -> navController.navigate(R.id.scheduleFragment));
        view.findViewById(R.id.menu_task).setOnClickListener(v -> navController.navigate(R.id.taskFragment));
        view.findViewById(R.id.menu_settings).setOnClickListener(v -> navController.navigate(R.id.settingsFragment));

        View tvScheduleMore = view.findViewById(R.id.tv_schedule_more);
        if (tvScheduleMore != null) {
            tvScheduleMore.setOnClickListener(v -> {
                navController.navigate(R.id.scheduleFragment);
            });
        }

        // 2. Nhấn "Xem tất cả" ở phần Deadline -> Chuyển sang trang Nhiệm vụ (Task)
        View layoutDeadlineMore = view.findViewById(R.id.layout_deadline_more);
        if (layoutDeadlineMore != null) {
            layoutDeadlineMore.setOnClickListener(v -> {
                navController.navigate(R.id.taskFragment);
            });
        }

        ScrollView scrollView = view.findViewById(R.id.rje1iynje4di); // Lưu ý: Đảm bảo ID này tồn tại trong file XML của bạn, nếu không hãy xóa dòng này.
        if (scrollView != null) {
            scrollView.smoothScrollTo(0, 0);
        }
    }
}