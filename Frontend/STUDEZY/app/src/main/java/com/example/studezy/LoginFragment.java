package com.example.studezy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    private CheckBox cbRemember; // Bổ sung biến cho CheckBox

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

        // 1. Ánh xạ View
        edtUsername = view.findViewById(R.id.et_username);
        edtPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        cbRemember = view.findViewById(R.id.cb_remember); // Ánh xạ CheckBox

        // 2. Kiểm tra xem trước đó có lưu tên đăng nhập không
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("SAVED_USERNAME", "");

        if (!savedUsername.isEmpty()) {
            // Nếu có, điền sẵn tên đăng nhập và tự động tick vào ô CheckBox
            edtUsername.setText(savedUsername);
            cbRemember.setChecked(true);

            // Focus (đưa con trỏ chuột) thẳng vào ô Mật khẩu để người dùng tiện nhập luôn
            edtPassword.requestFocus();
        }

        // 3. Bắt sự kiện click nút Đăng nhập
        btnLogin.setOnClickListener(v -> performLogin(view));
    }

    private void performLogin(View view) {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getInstance().getApi().loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    String token = loginResponse.getToken();
                    String fullName = loginResponse.getFullName();

                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("USER_TOKEN", token);
                    editor.putString("USER_FULL_NAME", fullName);

                    // --- LOGIC GHI NHỚ ĐĂNG NHẬP ---
                    if (cbRemember.isChecked()) {
                        // Nếu user tick chọn -> Lưu lại tên đăng nhập
                        editor.putString("SAVED_USERNAME", username);
                    } else {
                        // Nếu user KHÔNG tick (hoặc bỏ tick) -> Xóa tên đăng nhập đã lưu đi
                        editor.remove("SAVED_USERNAME");
                    }

                    editor.apply(); // Áp dụng thay đổi

                    Toast.makeText(getContext(), "Xin chào " + fullName, Toast.LENGTH_SHORT).show();

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