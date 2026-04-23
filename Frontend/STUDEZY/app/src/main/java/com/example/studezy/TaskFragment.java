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

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.TaskModel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskFragment extends Fragment {

    private RecyclerView rcvTasks;
    private TaskAdapter taskAdapter;
    private EditText edtSearchTask;
    private ImageView ivSearchTask;
    private LinearLayout filterAll, filterToday, filterWeek, filterCompleted;
    private TextView tvTotalTasks, tvDoingTasks, tvDoneTasks;
    private TextView tvFilterAll, tvFilterToday, tvFilterWeek, tvFilterCompleted;
    private TextView tvEmptyState;

    private int countAll = 0, countToday = 0, countWeek = 0, countDone = 0;
    private String currentFilter = "all";
    private List<TaskModel> taskList = new ArrayList<>();
    private List<TaskModel> fullTaskList = new ArrayList<>();

    public TaskFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = Navigation.findNavController(view);

        initViews(view);
        setupNavigation(view, navController);

        rcvTasks.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        taskAdapter = new TaskAdapter(taskList);
        taskAdapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onCheckClick(int position, TaskModel task) {
                int newStatus = (task.getStatus() == 1) ? 0 : 1;
                if (task.getStatus() == 1 && newStatus == 0) {
                    if (isOverdue(task.getDate())) {
                        Toast.makeText(getContext(), "Deadline này đã quá hạn", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                task.setStatus(newStatus);
                for (TaskModel originalTask : fullTaskList) {
                    if (originalTask.getId() == task.getId()) {
                        originalTask.setStatus(newStatus);
                        break;
                    }
                }
                if (currentFilter.equals("all")) {
                    taskAdapter.notifyItemChanged(position);
                } else {
                    taskList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    taskAdapter.notifyItemRangeChanged(position, taskList.size());
                }
                updateStats(fullTaskList);
                checkEmptyState();
                syncTaskStatusWithBackend(task.getId(), newStatus);
            }

            @Override
            public void onEditClick(int position, TaskModel task) {
                showEditDeadlineDialog(position, task);
            }

            // ==========================================
            // BỔ SUNG: Bắt sự kiện Xóa
            // ==========================================
            @Override
            public void onDeleteClick(int position, TaskModel task) {
                showDeleteDeadlineDialog(position, task);
            }
        });

        rcvTasks.setAdapter(taskAdapter);
        setupSearch();
        setupFilters();
        fetchTasksFromDjango("", currentFilter);
    }

    private void initViews(View view) {
        rcvTasks = view.findViewById(R.id.rcvTasks);
        edtSearchTask = view.findViewById(R.id.edtSearchTask);
        ivSearchTask = view.findViewById(R.id.ivSearchTask);
        filterAll = view.findViewById(R.id.filterAll);
        filterToday = view.findViewById(R.id.filterToday);
        filterWeek = view.findViewById(R.id.filterWeek);
        filterCompleted = view.findViewById(R.id.filterCompleted);
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvDoingTasks = view.findViewById(R.id.tvDoingTasks);
        tvDoneTasks = view.findViewById(R.id.tvDoneTasks);
        tvFilterAll = view.findViewById(R.id.tvFilterAll);
        tvFilterToday = view.findViewById(R.id.tvFilterToday);
        tvFilterWeek = view.findViewById(R.id.tvFilterWeek);
        tvFilterCompleted = view.findViewById(R.id.tvFilterCompleted);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
    }

    private void setupNavigation(View view, NavController nav) {
        view.findViewById(R.id.btn_back).setOnClickListener(v -> nav.navigate(R.id.homeFragment));
        view.findViewById(R.id.menu_home).setOnClickListener(v -> nav.navigate(R.id.homeFragment));
        view.findViewById(R.id.menu_schedule).setOnClickListener(v -> nav.navigate(R.id.scheduleFragment));
        view.findViewById(R.id.menu_settings).setOnClickListener(v -> nav.navigate(R.id.settingsFragment));

        View btnAdd = view.findViewById(R.id.ruarg55nipw);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showCreateDeadlineDialog());
        }
    }

    private void syncTaskStatusWithBackend(int taskId, int newStatus) {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", android.content.Context.MODE_PRIVATE);
        String authHeader = "Token " + prefs.getString("USER_TOKEN", "");
        Map<String, Integer> body = new HashMap<>();
        body.put("status", newStatus);
        RetrofitClient.getInstance().getApi().updateTaskStatus(authHeader, taskId, body).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOverdue(String dateStr) {
        if (dateStr == null || dateStr.length() < 10) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date deadline = sdf.parse(dateStr.substring(0, 10));
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
            return deadline != null && deadline.before(cal.getTime());
        } catch (Exception e) { return false; }
    }

    private void checkEmptyState() {
        if (taskList == null || taskList.isEmpty()) {
            rcvTasks.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            if (currentFilter.equals("today")) tvEmptyState.setText("Bài tập hôm nay bạn đã hoàn thành tất cả 🎉");
            else if (currentFilter.equals("week")) tvEmptyState.setText("Tuần này bạn đã xử lý xong mọi nhiệm vụ 😊");
            else if (currentFilter.equals("completed")) tvEmptyState.setText("Bạn chưa hoàn thành nhiệm vụ nào.");
            else tvEmptyState.setText("Danh sách trống.");
        } else {
            rcvTasks.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void setupFilters() {
        resetFilters();
        filterAll.setBackgroundResource(R.drawable.s000000sw1cr8lr2701e3a8a1877f2);
        if (tvFilterAll != null) tvFilterAll.setTextColor(Color.WHITE);

        View.OnClickListener filterClick = v -> {
            resetFilters();
            v.setBackgroundResource(R.drawable.s000000sw1cr8lr2701e3a8a1877f2);
            if (v == filterAll) { currentFilter = "all"; tvFilterAll.setTextColor(Color.WHITE); }
            else if (v == filterToday) { currentFilter = "today"; tvFilterToday.setTextColor(Color.WHITE); }
            else if (v == filterWeek) { currentFilter = "week"; tvFilterWeek.setTextColor(Color.WHITE); }
            else if (v == filterCompleted) { currentFilter = "completed"; tvFilterCompleted.setTextColor(Color.WHITE); }
            fetchTasksFromDjango(edtSearchTask.getText().toString().trim(), currentFilter);
        };
        filterAll.setOnClickListener(filterClick);
        filterToday.setOnClickListener(filterClick);
        filterWeek.setOnClickListener(filterClick);
        filterCompleted.setOnClickListener(filterClick);
    }

    private void fetchTasksFromDjango(String searchKeyword, String filter) {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", android.content.Context.MODE_PRIVATE);
        String authHeader = "Token " + prefs.getString("USER_TOKEN", "");
        RetrofitClient.getInstance().getApi().getTasks(authHeader, searchKeyword, filter).enqueue(new Callback<List<TaskModel>>() {
            @Override
            public void onResponse(Call<List<TaskModel>> call, Response<List<TaskModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList = response.body();
                    taskAdapter.setTaskList(taskList);
                    if (filter.equals("all") && searchKeyword.isEmpty()) {
                        fullTaskList.clear();
                        fullTaskList.addAll(taskList);
                    }
                    updateStats(fullTaskList);
                    checkEmptyState();
                } else {
                    taskList.clear();
                    taskAdapter.notifyDataSetChanged();
                    checkEmptyState();
                }
            }
            @Override public void onFailure(Call<List<TaskModel>> call, Throwable t) {}
        });
    }

    private void updateStats(List<TaskModel> tasks) {
        int activeToday = 0, activeWeek = 0, doneCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);

        Calendar mondayCal = Calendar.getInstance();
        mondayCal.add(Calendar.DAY_OF_YEAR, -daysToSubtract);
        mondayCal.set(Calendar.HOUR_OF_DAY, 0); mondayCal.set(Calendar.MINUTE, 0);
        mondayCal.set(Calendar.SECOND, 0); mondayCal.set(Calendar.MILLISECOND, 0);
        Date monday = mondayCal.getTime();

        Calendar sundayCal = (Calendar) mondayCal.clone();
        sundayCal.add(Calendar.DAY_OF_YEAR, 6);
        sundayCal.set(Calendar.HOUR_OF_DAY, 23); sundayCal.set(Calendar.MINUTE, 59);
        Date sunday = sundayCal.getTime();

        for (TaskModel task : tasks) {
            if (task.getStatus() == 1) {
                doneCount++;
            } else {
                String dateStr = task.getDate();
                if (dateStr != null && dateStr.length() >= 10) {
                    String justDate = dateStr.substring(0, 10);
                    if (justDate.equals(todayStr)) activeToday++;
                    try {
                        Date d = sdf.parse(justDate);
                        if (d != null && !d.before(monday) && !d.after(sunday)) activeWeek++;
                    } catch (Exception ignored) {}
                }
            }
        }

        countAll = tasks.size();
        countToday = activeToday;
        countWeek = activeWeek;
        countDone = doneCount;

        tvTotalTasks.setText(String.valueOf(tasks.size()));
        tvDoingTasks.setText(String.valueOf(tasks.size() - doneCount));
        tvDoneTasks.setText(String.valueOf(doneCount));
        updateFilterTexts();
    }

    private void updateFilterTexts() {
        if (tvFilterAll != null) tvFilterAll.setText("Tất cả (" + countAll + ")");
        if (tvFilterToday != null) tvFilterToday.setText("Hôm nay (" + countToday + ")");
        if (tvFilterWeek != null) tvFilterWeek.setText("Tuần này (" + countWeek + ")");
        if (tvFilterCompleted != null) tvFilterCompleted.setText("Hoàn thành (" + countDone + ")");
    }

    private void resetFilters() {
        filterAll.setBackgroundResource(R.drawable.s94989bsw1cr8bffffff);
        filterToday.setBackgroundResource(R.drawable.s94989bsw1cr8bffffff);
        filterWeek.setBackgroundResource(R.drawable.s94989bsw1cr8bffffff);
        filterCompleted.setBackgroundResource(R.drawable.s94989bsw1cr8bffffff);
        int gray = Color.parseColor("#666666");
        tvFilterAll.setTextColor(gray); tvFilterToday.setTextColor(gray);
        tvFilterWeek.setTextColor(gray); tvFilterCompleted.setTextColor(gray);
    }

    private void showCreateDeadlineDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_create_deadline);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.5f);
        }

        EditText edtName = dialog.findViewById(R.id.edt_name);
        EditText edtDesc = dialog.findViewById(R.id.edt_desc);
        EditText edtDate = dialog.findViewById(R.id.edt_date);

        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                new android.app.TimePickerDialog(requireContext(), (view2, h, min) -> {
                    edtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d", d, m + 1, y, h, min));
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String title = edtName.getText().toString().trim();
            String dateStr = edtDate.getText().toString().trim();
            if (title.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date selectedDate = sdf.parse(dateStr);
                if (selectedDate != null && selectedDate.before(new Date())) {
                    Toast.makeText(getContext(), "Hạn deadline không thể nằm trong quá khứ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) { e.printStackTrace(); }

            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", android.content.Context.MODE_PRIVATE);
            String auth = "Token " + prefs.getString("USER_TOKEN", "");
            Map<String, String> body = new HashMap<>();
            body.put("title", title);
            body.put("description", edtDesc.getText().toString().trim());
            body.put("due_date", dateStr);

            RetrofitClient.getInstance().getApi().createTask(auth, body).enqueue(new Callback<TaskModel>() {
                @Override
                public void onResponse(Call<TaskModel> call, Response<TaskModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        fullTaskList.add(0, response.body());
                        fetchTasksFromDjango("", currentFilter);
                        dialog.dismiss();
                    } else { Toast.makeText(getContext(), "Lỗi từ server", Toast.LENGTH_SHORT).show(); }
                }
                @Override public void onFailure(Call<TaskModel> call, Throwable t) {}
            });
        });
        dialog.show();
    }

    private void showEditDeadlineDialog(int position, TaskModel task) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_edit_deadline);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.5f);
        }

        EditText edtName = dialog.findViewById(R.id.edt_name);
        EditText edtDesc = dialog.findViewById(R.id.edt_desc);
        EditText edtDate = dialog.findViewById(R.id.edt_date);

        edtName.setText(task.getTitle());
        edtDesc.setText(task.getDescription());
        edtDate.setText(task.getDeadlineDate());

        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                new android.app.TimePickerDialog(requireContext(), (view2, h, min) -> {
                    edtDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d %02d:%02d", d, m + 1, y, h, min));
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
            String title = edtName.getText().toString().trim();
            String dateStr = edtDate.getText().toString().trim();

            if (title.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat sdf;
                if (dateStr.length() > 10) sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                else sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Date selectedDate = sdf.parse(dateStr);
                if (selectedDate != null && selectedDate.before(new Date())) {
                    Toast.makeText(getContext(), "Hạn deadline không thể nằm trong quá khứ!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) { e.printStackTrace(); }

            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", android.content.Context.MODE_PRIVATE);
            String auth = "Token " + prefs.getString("USER_TOKEN", "");

            Map<String, String> body = new HashMap<>();
            body.put("title", title);
            body.put("description", edtDesc.getText().toString().trim());
            body.put("due_date", dateStr);

            RetrofitClient.getInstance().getApi().editTask(auth, task.getId(), body).enqueue(new Callback<TaskModel>() {
                @Override
                public void onResponse(Call<TaskModel> call, Response<TaskModel> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TaskModel updatedTask = response.body();
                        taskList.set(position, updatedTask);

                        for (int i = 0; i < fullTaskList.size(); i++) {
                            if (fullTaskList.get(i).getId() == task.getId()) {
                                fullTaskList.set(i, updatedTask);
                                break;
                            }
                        }
                        taskAdapter.notifyItemChanged(position);
                        updateStats(fullTaskList);
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Đã cập nhật nhiệm vụ!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi cập nhật từ server", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<TaskModel> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    // ==========================================
    // BỔ SUNG: Hàm hiển thị Form Xóa nhiệm vụ
    // ==========================================
    private void showDeleteDeadlineDialog(int position, TaskModel task) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_clean_deadline);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setDimAmount(0.5f);
        }

        // Đổ tên nhiệm vụ vào chữ thông báo xoá
        TextView tvDesc = dialog.findViewById(R.id.tv_description);
        if (tvDesc != null) {
            tvDesc.setText("Deadline " + task.getTitle() + " sẽ bị xoá vĩnh viễn.\nHành động này không thể hoàn tác.");
        }

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        // Nút Xoá deadline
        dialog.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences("StudezyPrefs", android.content.Context.MODE_PRIVATE);
            String auth = "Token " + prefs.getString("USER_TOKEN", "");

            // Gọi API Delete
            RetrofitClient.getInstance().getApi().deleteTask(auth, task.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Xoá khỏi danh sách đang hiển thị
                        taskList.remove(position);
                        taskAdapter.notifyItemRemoved(position);
                        taskAdapter.notifyItemRangeChanged(position, taskList.size());

                        // Tìm và xoá khỏi danh sách gốc
                        for (int i = 0; i < fullTaskList.size(); i++) {
                            if (fullTaskList.get(i).getId() == task.getId()) {
                                fullTaskList.remove(i);
                                break;
                            }
                        }

                        updateStats(fullTaskList);
                        checkEmptyState();
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Đã xoá nhiệm vụ!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi xoá từ server", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Lỗi mạng!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.show();
    }

    private void setupSearch() {
        ivSearchTask.setOnClickListener(v -> fetchTasksFromDjango(edtSearchTask.getText().toString().trim(), currentFilter));
        edtSearchTask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                fetchTasksFromDjango(edtSearchTask.getText().toString().trim(), currentFilter);
                return true;
            }
            return false;
        });
    }
}
