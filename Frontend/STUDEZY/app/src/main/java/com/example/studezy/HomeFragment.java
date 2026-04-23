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

import java.util.ArrayList;
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
import com.example.studezy.api.SemesterModel;
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

        // --- ĐOẠN CODE MỚI ĐÃ SỬA LỖI NHẢY TUẦN ---
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (currentDayOfWeek == Calendar.SUNDAY) {
            // Nếu hôm nay là Chủ Nhật, lùi lại 6 ngày để về Thứ 2 của tuần hiện tại
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            // Nếu là các ngày khác, set về Thứ 2 bình thường
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        // ------------------------------------------

        SimpleDateFormat sdfUI = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfAPI = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Gán dữ liệu cho 7 ngày và thiết lập sự kiện click
        for (int i = 0; i < 7; i++) {
            tvDates[i].setText(sdfUI.format(calendar.getTime()));
            apiDateStrings[i] = sdfAPI.format(calendar.getTime());

            final int currentIndex = i;

            layoutDays[i].setOnClickListener(v -> {
                updateCalendarUI(currentIndex);
                String selectedDate = apiDateStrings[currentIndex];
                fetchDataForSelectedDate(selectedDate);
            });

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

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
    // Hàm gọi dữ liệu khi click vào một ngày cụ thể trên Lịch
    private void fetchDataForSelectedDate(String dateStr) {
        if (userToken == null || userToken.isEmpty()) return;
        String authHeader = "Token " + userToken;

        // --- PHẦN LỊCH HỌC: Chỉ hiển thị nếu có học kỳ ---
        RetrofitClient.getInstance().getApi().getCurrentSemester(authHeader).enqueue(new Callback<SemesterModel>() {
            @Override
            public void onResponse(Call<SemesterModel> call, Response<SemesterModel> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {

                    RetrofitClient.getInstance().getApi().getClassesByDate(authHeader, dateStr).enqueue(new Callback<List<ClassModel>>() {
                        @Override
                        public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ClassHomeAdapter adapter = new ClassHomeAdapter(response.body(), classModel -> showDetailClassPopup(classModel));
                                rvClasses.setAdapter(adapter);
                            }
                        }
                        @Override
                        public void onFailure(Call<List<ClassModel>> call, Throwable t) { }
                    });

                } else {
                    rvClasses.setAdapter(new ClassHomeAdapter(new ArrayList<>(), null));
                }
            }

            @Override
            public void onFailure(Call<SemesterModel> call, Throwable t) {
                rvClasses.setAdapter(new ClassHomeAdapter(new ArrayList<>(), null));
            }
        });

        // --- PHẦN DEADLINE: GIỮ NGUYÊN TUYỆT ĐỐI ---
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
            public void onFailure(Call<List<DeadlineModel>> call, Throwable t) { }
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

        // Kiểm tra xem có học kỳ nào không trước khi tải lịch
        RetrofitClient.getInstance().getApi().getCurrentSemester(authHeader).enqueue(new Callback<SemesterModel>() {
            @Override
            public void onResponse(Call<SemesterModel> call, Response<SemesterModel> response) {
                // Chỉ cần Backend trả về success (nghĩa là có học kỳ) thì tải lịch học
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {

                    RetrofitClient.getInstance().getApi().getClassesToday(authHeader).enqueue(new Callback<List<ClassModel>>() {
                        @Override
                        public void onResponse(Call<List<ClassModel>> call, Response<List<ClassModel>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ClassHomeAdapter adapter = new ClassHomeAdapter(response.body(), classModel -> showDetailClassPopup(classModel));
                                rvClasses.setAdapter(adapter);
                            }
                        }
                        @Override
                        public void onFailure(Call<List<ClassModel>> call, Throwable t) { }
                    });

                } else {
                    // Nếu không có học kỳ, xóa trắng danh sách lịch học
                    rvClasses.setAdapter(new ClassHomeAdapter(new ArrayList<>(), null));
                }
            }

            @Override
            public void onFailure(Call<SemesterModel> call, Throwable t) {
                rvClasses.setAdapter(new ClassHomeAdapter(new ArrayList<>(), null));
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

        androidx.core.widget.NestedScrollView scrollView = view.findViewById(R.id.rje1iynje4di); // Lưu ý: Đảm bảo ID này tồn tại trong file XML của bạn, nếu không hãy xóa dòng này.
        if (scrollView != null) {
            scrollView.smoothScrollTo(0, 0);
        }
    }
    private void showDetailClassPopup(ClassModel classModel) {
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Khởi tạo popup xem chi tiết với dữ liệu môn học đã chọn
        DetailClassBottomSheet detailSheet = DetailClassBottomSheet.newInstance(classModel);

        // Hiển thị popup lên màn hình
        detailSheet.show(getParentFragmentManager(), "DetailClassBottomSheet");
    }
}