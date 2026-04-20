package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studezy.api.RegisterResponse;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.ScheduleModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleFragment extends Fragment {

    private RecyclerView rvSchedules;
    private String token;

    // Thêm các biến để xử lý lọc
    private ScheduleAdapter adapter;
    private List<ScheduleModel> fullScheduleList = new ArrayList<>();
    private TextView currentSelectedTab;
    private String currentSelectedDay = "2"; // Mặc định khi mở lên là xem Thứ 2

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("USER_TOKEN", "");

        // 1. NÚT THÊM MỚI
        View btnAddNew = view.findViewById(R.id.btn_add_new);
        if (btnAddNew != null) {
            btnAddNew.setOnClickListener(v -> {
                if (token.isEmpty()) {
                    Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                // CHÚ Ý: Truyền 'null' vào giữa vì đây là Thêm Mới
                AddClassBottomSheet bottomSheet = AddClassBottomSheet.newInstance(token, null, this::loadSchedules);
                bottomSheet.show(getParentFragmentManager(), "AddClassBottomSheet");
            });
        }

        // 2. KHỞI TẠO RECYCLERVIEW VÀ GẮN SỰ KIỆN CLICK SỬA/XÓA
        rvSchedules = view.findViewById(R.id.rv_schedules);
        if (rvSchedules != null) {
            rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));

            // Khởi tạo Adapter với Interface lắng nghe sự kiện
            adapter = new ScheduleAdapter(new ScheduleAdapter.OnItemClickListener() {
                @Override
                public void onEditClick(ScheduleModel schedule) {
                    // Mở BottomSheet và truyền dữ liệu môn học vào để SỬA
                    AddClassBottomSheet bottomSheet = AddClassBottomSheet.newInstance(token, schedule, () -> loadSchedules());
                    bottomSheet.show(getParentFragmentManager(), "EditClassBottomSheet");
                }

                @Override
                public void onDeleteClick(ScheduleModel schedule) {
                    // Hiển thị hộp thoại xác nhận Xóa
                    showDeleteDialog(schedule);
                }
            });

            rvSchedules.setAdapter(adapter);
        }

        // Khởi tạo sự kiện click cho các tab Thứ
        setupTabClickListeners(view);

        // Menu chuyển trang
        view.findViewById(R.id.menu_home).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.homeFragment));
        view.findViewById(R.id.menu_task).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.taskFragment));
        view.findViewById(R.id.menu_settings).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.settingsFragment));

        // Tải dữ liệu lịch học
        if (!token.isEmpty()) {
            loadSchedules();
        }
    }

    private void loadSchedules() {
        if (rvSchedules == null) return;

        RetrofitClient.getInstance().getApi().getAllClassSchedules("Token " + token)
                .enqueue(new Callback<List<ScheduleModel>>() {
                    @Override
                    public void onResponse(Call<List<ScheduleModel>> call, Response<List<ScheduleModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Lưu toàn bộ danh sách vào biến tổng
                            fullScheduleList = response.body();

                            // Cập nhật số đếm trên các Tab
                            updateDayTabsCount(fullScheduleList);

                            // Lọc danh sách theo ngày đang được chọn (mặc định là Thứ 2)
                            filterSchedulesByDay(currentSelectedDay);
                        } else {
                            Toast.makeText(getContext(), "Không tải được danh sách", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ScheduleModel>> call, Throwable t) {
                        Toast.makeText(getContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // === HÀM THIẾT LẬP SỰ KIỆN CLICK CHUYỂN TAB ===
    private void setupTabClickListeners(View view) {
        TextView[] tabs = {
                view.findViewById(R.id.tab_monday),
                view.findViewById(R.id.tab_tuesday),
                view.findViewById(R.id.tab_wednesday),
                view.findViewById(R.id.tab_thursday),
                view.findViewById(R.id.tab_friday),
                view.findViewById(R.id.tab_saturday),
                view.findViewById(R.id.tab_sunday)
        };
        String[] dayValues = {"2", "3", "4", "5", "6", "7", "8"};

        currentSelectedTab = tabs[0];

        for (int i = 0; i < tabs.length; i++) {
            final TextView tab = tabs[i];
            final String dayValue = dayValues[i];

            if (tab == null) continue;

            tab.setOnClickListener(v -> {
                if (currentSelectedTab != null) {
                    currentSelectedTab.setBackgroundResource(R.drawable.bg_tab_unselected);
                    currentSelectedTab.setTextColor(Color.parseColor("#94989B"));
                }

                tab.setBackgroundResource(R.drawable.bg_tab_selected);
                tab.setTextColor(Color.WHITE);

                currentSelectedTab = tab;
                currentSelectedDay = dayValue;

                filterSchedulesByDay(dayValue);
            });
        }
    }

    // === HÀM LỌC DANH SÁCH THEO NGÀY ===
    private void filterSchedulesByDay(String dayValue) {
        List<ScheduleModel> filteredList = new ArrayList<>();

        for (ScheduleModel item : fullScheduleList) {
            if (dayValue.equals(item.getDayOfWeek())) {
                filteredList.add(item);
            }
        }

        if (adapter != null) {
            adapter.updateData(filteredList);
        }
    }

    // === HÀM ĐẾM SỐ LƯỢNG MÔN TRÊN TAB ===
    private void updateDayTabsCount(List<ScheduleModel> schedules) {
        View view = getView();
        if (view == null) return;

        int[] counts = new int[7];

        for (ScheduleModel item : schedules) {
            String day = item.getDayOfWeek();
            if (day != null) {
                switch (day) {
                    case "2": counts[0]++; break;
                    case "3": counts[1]++; break;
                    case "4": counts[2]++; break;
                    case "5": counts[3]++; break;
                    case "6": counts[4]++; break;
                    case "7": counts[5]++; break;
                    case "8": counts[6]++; break;
                }
            }
        }

        TextView[] tabs = {
                view.findViewById(R.id.tab_monday),
                view.findViewById(R.id.tab_tuesday),
                view.findViewById(R.id.tab_wednesday),
                view.findViewById(R.id.tab_thursday),
                view.findViewById(R.id.tab_friday),
                view.findViewById(R.id.tab_saturday),
                view.findViewById(R.id.tab_sunday)
        };

        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};

        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i] != null) {
                String text = counts[i] > 0 ? dayNames[i] + " (" + counts[i] + ")" : dayNames[i];
                tabs[i].setText(text);
            }
        }
    }

    // 3. HÀM HIỂN THỊ DIALOG XÓA VÀ GỌI API XÓA MÔN HỌC
    private void showDeleteDialog(ScheduleModel schedule) {
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        dialog.setContentView(R.layout.dialog_delete_class);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvMessage = dialog.findViewById(R.id.tv_delete_message);
        String message = "Lịch học <b>" + schedule.getSubjectName() + "</b> sẽ bị xoá vĩnh viễn khỏi thời khoá biểu. Hành động này không thể hoàn tác.";
        tvMessage.setText(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_COMPACT));

        dialog.findViewById(R.id.btn_cancel_delete).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_confirm_delete).setOnClickListener(v -> {
            // Gọi API Xóa
            RetrofitClient.getInstance().getApi().deleteClassSchedule("Token " + token, schedule.getId())
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadSchedules(); // Tải lại danh sách sau khi xóa thành công
                            } else {
                                Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<RegisterResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show();
    }
}
