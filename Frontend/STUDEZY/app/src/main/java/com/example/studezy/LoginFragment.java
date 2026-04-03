package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.studezy.api.LoginRequest;
import com.example.studezy.api.LoginResponse;
import com.example.studezy.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View (Nhớ sửa R.id... cho khớp với file XML của bạn)
        edtUsername = view.findViewById(R.id.et_username);
        edtPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);

        // 2. Bắt sự kiện click nút Đăng nhập
        btnLogin.setOnClickListener(v -> performLogin(view));
    }

    private void performLogin(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo cục dữ liệu để gửi đi
        LoginRequest request = new LoginRequest(username, password);

        // Gọi API qua Retrofit
        RetrofitClient.getInstance().getApi().loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Lấy token và tên từ server trả về
                    String token = loginResponse.getToken();
                    String fullName = loginResponse.getFullName();

                    // Lưu Token vào SharedPreferences (để dùng cho các màn hình sau)
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("USER_TOKEN", token);
                    editor.putString("USER_FULL_NAME", fullName);
                    editor.apply();

                    Toast.makeText(getContext(), "Xin chào " + fullName, Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình Home bằng Navigation (Nhớ tạo action trong nav_graph.xml trước)
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);

                } else {
                    Toast.makeText(getContext(), "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}