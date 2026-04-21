package com.example.studezy;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studezy.api.AddSemesterRequest;
import com.example.studezy.api.RegisterResponse;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.ScheduleModel;
import com.example.studezy.api.SemesterModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.homeFragment)
            );
        }
        View btnAddNew = view.findViewById(R.id.btn_add_new);
        if (btnAddNew != null) {
            btnAddNew.setOnClickListener(v -> {
                if (token.isEmpty()) {
                    Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddClassBottomSheet bottomSheet = AddClassBottomSheet.newInstance(token, null, this::loadSchedules);
                bottomSheet.show(getParentFragmentManager(), "AddClassBottomSheet");
            });
        }

        // 2. KHỞI TẠO RECYCLERVIEW VÀ GẮN SỰ KIỆN CLICK SỬA/XÓA
        rvSchedules = view.findViewById(R.id.rv_schedules);
        if (rvSchedules != null) {
            rvSchedules.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter = new ScheduleAdapter(new ScheduleAdapter.OnItemClickListener() {
                @Override
                public void onEditClick(ScheduleModel schedule) {
                    AddClassBottomSheet bottomSheet = AddClassBottomSheet.newInstance(token, schedule, () -> loadSchedules());
                    bottomSheet.show(getParentFragmentManager(), "EditClassBottomSheet");
                }

                @Override
                public void onDeleteClick(ScheduleModel schedule) {
                    showDeleteDialog(schedule);
                }
            });

            rvSchedules.setAdapter(adapter);
        }

        setupTabClickListeners(view);

        // Menu chuyển trang
        view.findViewById(R.id.menu_home).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.homeFragment));
        view.findViewById(R.id.menu_task).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.taskFragment));
        view.findViewById(R.id.menu_settings).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.settingsFragment));

        // Tải dữ liệu
        if (!token.isEmpty()) {
            loadSchedules();
        }
    }

    private void updateSemesterUI(String semesterName, String startDateStr, String endDateStr, int subjectCount) {
        View view = getView();
        if (view == null) return;

        View groupDetails = view.findViewById(R.id.group_semester_details);
        Button btnAddSemester = view.findViewById(R.id.btn_add_new_semester);
        TextView tvSubtitle = view.findViewById(R.id.tv_subtitle);

        if (startDateStr == null || endDateStr == null) {
            if (groupDetails != null) groupDetails.setVisibility(View.GONE);
            if (btnAddSemester != null) {
                btnAddSemester.setVisibility(View.VISIBLE);
                btnAddSemester.setOnClickListener(v -> showAddSemesterDialog());
            }
            if (tvSubtitle != null) tvSubtitle.setText("Chưa có thông tin học kỳ");
            return;
        }

        if (groupDetails != null) groupDetails.setVisibility(View.VISIBLE);
        if (btnAddSemester != null) btnAddSemester.setVisibility(View.GONE);

        if (tvSubtitle != null) tvSubtitle.setText(semesterName);

        TextView tvStartDate = view.findViewById(R.id.tv_val_start_date);
        TextView tvEndDate = view.findViewById(R.id.tv_val_end_date);
        TextView tvTotalWeeks = view.findViewById(R.id.tv_val_total_weeks);
        TextView tvSubjects = view.findViewById(R.id.tv_val_subjects);
        TextView tvProgressText = view.findViewById(R.id.tv_val_progress);
        ProgressBar progressBar = view.findViewById(R.id.progress_semester);

        if (tvStartDate != null) tvStartDate.setText(startDateStr);
        if (tvEndDate != null) tvEndDate.setText(endDateStr);
        if (tvSubjects != null) tvSubjects.setText(subjectCount + " môn");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);
            Date today = new Date();

            if (startDate != null && endDate != null) {
                long diffInMillies = endDate.getTime() - startDate.getTime();
                long totalDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                int totalWeeks = (int) Math.ceil(totalDays / 7.0);
                if (tvTotalWeeks != null) tvTotalWeeks.setText(totalWeeks + " tuần");

                long elapsedMillies = today.getTime() - startDate.getTime();
                if (elapsedMillies < 0) elapsedMillies = 0;
                if (elapsedMillies > diffInMillies) elapsedMillies = diffInMillies;

                float progressPercentage = (float) elapsedMillies / diffInMillies * 100;

                if (tvProgressText != null) tvProgressText.setText(String.format(Locale.getDefault(), "%.1f%% đã hoàn thành", progressPercentage));
                if (progressBar != null) progressBar.setProgress((int) progressPercentage);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void showAddSemesterDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_semester);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText edtName = dialog.findViewById(R.id.edt_semester_name);
        TextView tvStartDate = dialog.findViewById(R.id.tv_select_start_date);
        TextView tvEndDate = dialog.findViewById(R.id.tv_select_end_date);

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();

        tvStartDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                startCalendar.set(year, month, dayOfMonth);
                String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                tvStartDate.setText(date);
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                endCalendar.set(year, month, dayOfMonth);
                String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                tvEndDate.setText(date);
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
            datePickerDialog.show();
        });

        dialog.findViewById(R.id.btn_cancel_semester).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.btn_save_semester).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String startDate = tvStartDate.getText().toString();
            String endDate = tvEndDate.getText().toString();

            if (name.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API Lưu học kỳ mới
            AddSemesterRequest requestObj = new AddSemesterRequest(name, startDate, endDate);
            RetrofitClient.getInstance().getApi().createSemester("Token " + token, requestObj)
                    .enqueue(new Callback<SemesterModel>() {
                        @Override
                        public void onResponse(Call<SemesterModel> call, Response<SemesterModel> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Đã thêm học kỳ mới", Toast.LENGTH_SHORT).show();
                                SemesterModel newSem = response.body();
                                updateSemesterUI(newSem.getName(), newSem.getStartDate(), newSem.getEndDate(), fullScheduleList.size());
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi tạo học kỳ", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SemesterModel> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialog.show(); // Dòng này bị thiếu đã được thêm lại
    }

    private void loadSchedules() {
        if (rvSchedules == null) return;

        // BƯỚC 1: Gọi API lấy danh sách Môn học
        RetrofitClient.getInstance().getApi().getAllClassSchedules("Token " + token)
                .enqueue(new Callback<List<ScheduleModel>>() {
                    @Override
                    public void onResponse(Call<List<ScheduleModel>> call, Response<List<ScheduleModel>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            fullScheduleList = response.body();
                            updateDayTabsCount(fullScheduleList);
                            filterSchedulesByDay(currentSelectedDay);

                            // BƯỚC 2: Gọi API lấy Thông tin Học kỳ
                            RetrofitClient.getInstance().getApi().getCurrentSemester("Token " + token)
                                    .enqueue(new Callback<SemesterModel>() {
                                        @Override
                                        public void onResponse(Call<SemesterModel> call, Response<SemesterModel> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                SemesterModel sem = response.body();
                                                if ("success".equals(sem.getStatus())) {
                                                    updateSemesterUI(sem.getName(), sem.getStartDate(), sem.getEndDate(), fullScheduleList.size());
                                                } else {
                                                    // Trường hợp status = "not_found" (Chưa có học kỳ)
                                                    updateSemesterUI(null, null, null, 0);
                                                }
                                            } else {
                                                // NẾU API TRẢ VỀ LỖI (Ví dụ 404, 500)
                                                Toast.makeText(getContext(), "Lỗi API Semester: " + response.code(), Toast.LENGTH_LONG).show();
                                                updateSemesterUI(null, null, null, 0); // Vẫn gọi hàm này để ẩn giao diện fix cứng đi
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<SemesterModel> call, Throwable t) {
                                            // NẾU LỖI MẠNG HOẶC SAI ĐỊNH DẠNG JSON
                                            Toast.makeText(getContext(), "Lỗi kết nối Semester: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                            updateSemesterUI(null, null, null, 0);
                                        }
                                    });
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
            RetrofitClient.getInstance().getApi().deleteClassSchedule("Token " + token, schedule.getId())
                    .enqueue(new Callback<RegisterResponse>() {
                        @Override
                        public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa môn học", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                loadSchedules();
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