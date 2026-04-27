package com.example.studezy;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
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

    // Biến cho ẩn/hiện mật khẩu
    private ImageView ivTogglePass, ivToggleConfirmPass;
    private boolean isPassVisible = false;
    private boolean isConfirmPassVisible = false;

    public RegisterFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ ID
        edtRegFullName = view.findViewById(R.id.et_fullname);
        edtRegEmail = view.findViewById(R.id.et_email);
        edtRegUsername = view.findViewById(R.id.et_username_reg);
        edtRegPassword = view.findViewById(R.id.et_password_reg);
        edtRegConfirmPassword = view.findViewById(R.id.et_confirm_password);
        cbTerms = view.findViewById(R.id.cb_terms);
        btnRegisterSubmit = view.findViewById(R.id.btn_do_register);
        cbTerms = view.findViewById(R.id.cb_terms);
        btnRegisterSubmit = view.findViewById(R.id.btn_do_register);

        ivTogglePass = view.findViewById(R.id.iv_toggle_pass_reg);
        ivToggleConfirmPass = view.findViewById(R.id.iv_toggle_confirm_pass_reg);

        // Nút lùi về trang trước
        view.findViewById(R.id.btn_back_reg).setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        TextView tvTerms = view.findViewById(R.id.tv_terms);
        String termsHtml = "Tôi đồng ý với <font color='#376BE5'><u>Điều khoản dịch vụ</u></font> và <font color='#376BE5'><u>Chính sách bảo mật</u></font>";
        tvTerms.setText(android.text.Html.fromHtml(termsHtml, android.text.Html.FROM_HTML_MODE_COMPACT));
        // Chuyển sang trang đăng nhập nếu đã có tài khoản
        TextView tvBackToLogin = view.findViewById(R.id.tv_back_to_login);
        tvBackToLogin.setOnClickListener(v -> {
            // Thay vì popBackStack, ta dùng navigate để sang thẳng Login
            // và dùng NavOptions để xóa màn hình Register hiện tại khỏi lịch sử
            Navigation.findNavController(v).navigate(R.id.loginFragment, null,
                    new NavOptions.Builder().setPopUpTo(R.id.registerFragment, true).build());
        });

        // 2. Logic ẩn/hiện Mật khẩu
        ivTogglePass.setOnClickListener(v -> {
            if (isPassVisible) {
                // Đang hiện -> Chuyển sang ẨN (hiện dấu sao)
                edtRegPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePass.setImageResource(R.drawable.ic_mat);
                isPassVisible = false;
            } else {
                // Đang ẩn -> Chuyển sang HIỆN (thấy text)
                edtRegPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePass.setImageResource(R.drawable.ic_momk);
                isPassVisible = true;
            }
            edtRegPassword.setSelection(edtRegPassword.getText().length());
        });

        // 3. Logic ẩn/hiện Xác nhận mật khẩu
        ivToggleConfirmPass.setOnClickListener(v -> {
            if (isConfirmPassVisible) {
                edtRegConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggleConfirmPass.setImageResource(R.drawable.ic_mat);
                isConfirmPassVisible = false;
            } else {
                edtRegConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggleConfirmPass.setImageResource(R.drawable.ic_momk);
                isConfirmPassVisible = true;
            }
            edtRegConfirmPassword.setSelection(edtRegConfirmPassword.getText().length());
        });

        // Submit form
        btnRegisterSubmit.setOnClickListener(v -> performRegistration(view));
    }

    private void performRegistration(View view) {
        String fullName = edtRegFullName.getText().toString().trim();
        String email = edtRegEmail.getText().toString().trim();
        String username = edtRegUsername.getText().toString().trim();
        String password = edtRegPassword.getText().toString().trim();
        String confirmPassword = edtRegConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ các thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(getContext(), "Mật khẩu phải có tối thiểu 8 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(getContext(), "Bạn cần đồng ý với Điều khoản dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(username, password, fullName, email);

        RetrofitClient.getInstance().getApi().registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    // SỬA TẠI ĐÂY: Chuyển sang trang Đăng nhập sau khi đăng ký thành công
                    Navigation.findNavController(view).navigate(R.id.loginFragment, null,
                            new NavOptions.Builder().setPopUpTo(R.id.registerFragment, true).build());
                } else {
                    Toast.makeText(getContext(), "Tên đăng nhập đã tồn tại", Toast.LENGTH_SHORT).show();
                }
            }
            // ... (các phần khác giữ nguyên)

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
