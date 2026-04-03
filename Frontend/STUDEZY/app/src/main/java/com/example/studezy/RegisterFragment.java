package com.example.studezy;

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

import com.example.studezy.api.RegisterRequest;
import com.example.studezy.api.RegisterResponse;
import com.example.studezy.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText edtRegUsername, edtRegPassword, edtRegConfirmPassword, edtRegFullName, edtRegEmail;
    private CheckBox cbTerms;
    private Button btnRegisterSubmit;

    public RegisterFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ ID (Nhớ kiểm tra lại cho khớp với thiết kế XML của bạn)
        edtRegFullName = view.findViewById(R.id.et_fullname);
        edtRegEmail = view.findViewById(R.id.et_email);
        edtRegUsername = view.findViewById(R.id.et_username_reg);
        edtRegPassword = view.findViewById(R.id.et_password_reg);
        edtRegConfirmPassword = view.findViewById(R.id.et_confirm_password); // Ô mới
        cbTerms = view.findViewById(R.id.cb_terms);                             // Ô mới
        btnRegisterSubmit = view.findViewById(R.id.btn_do_register);

        // Nút lùi về trang trước (Nút mũi tên trên cùng bên trái)
        view.findViewById(R.id.btn_back_reg).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnRegisterSubmit.setOnClickListener(v -> performRegistration(view));
    }

    private void performRegistration(View view) {
        String fullName = edtRegFullName.getText().toString().trim();
        String email = edtRegEmail.getText().toString().trim();
        String username = edtRegUsername.getText().toString().trim();
        String password = edtRegPassword.getText().toString().trim();
        String confirmPassword = edtRegConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra rỗng
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra xác nhận mật khẩu khớp nhau
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra độ dài mật khẩu (tối thiểu 8 ký tự như trên UI)
        if (password.length() < 8) {
            Toast.makeText(getContext(), "Mật khẩu phải có tối thiểu 8 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Kiểm tra xem đã tick đồng ý điều khoản chưa
        if (!cbTerms.isChecked()) {
            Toast.makeText(getContext(), "Bạn cần đồng ý với Điều khoản dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu qua hết các ải trên, đóng gói dữ liệu và gửi đi

        RegisterRequest request = new RegisterRequest(username, password, fullName, email);

        RetrofitClient.getInstance().getApi().registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // Thành công thì lùi về màn hình Đăng nhập
                    Navigation.findNavController(view).popBackStack();
                } else {
                    Toast.makeText(getContext(), "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}