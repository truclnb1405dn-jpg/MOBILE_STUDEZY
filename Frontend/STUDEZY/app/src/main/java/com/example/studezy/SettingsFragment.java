package com.example.studezy;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Gravity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.example.studezy.api.ChangePasswordRequest;
import com.example.studezy.api.ChangePasswordResponse;
import com.example.studezy.api.ProfileResponse;
import com.example.studezy.api.RetrofitClient;
import com.example.studezy.api.UpdateProfileRequest;
import com.example.studezy.api.UpdateProfileResponse;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingsFragment extends Fragment {


   private TextView tvFullName, tvEmail, tvAvatar;
   private ProfileResponse currentProfile;


   public SettingsFragment() {
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
       return inflater.inflate(R.layout.fragment_settings, container, false);
   }


   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
       super.onViewCreated(view, savedInstanceState);


       // --- ĐOẠN CODE CHÈN MỚI ---
       NavController navController = Navigation.findNavController(view);


       view.findViewById(R.id.menu_home).setOnClickListener(v ->
               navController.navigate(R.id.homeFragment));


       view.findViewById(R.id.menu_schedule).setOnClickListener(v ->
               navController.navigate(R.id.scheduleFragment));


       view.findViewById(R.id.menu_task).setOnClickListener(v ->
               navController.navigate(R.id.taskFragment));
       // --------------------------


       tvFullName = view.findViewById(R.id.tv_full_name);
       tvEmail = view.findViewById(R.id.tv_email);
       tvAvatar = view.findViewById(R.id.tv_avatar);


       LinearLayout layoutEditProfile = view.findViewById(R.id.layout_edit_profile);
       LinearLayout layoutChangePassword = view.findViewById(R.id.layout_change_password);
       Button btnLogout = view.findViewById(R.id.btnLogout);
       ImageButton btnBack = view.findViewById(R.id.btn_back);


       btnBack.setOnClickListener(v -> {
           if (!navController.popBackStack()) {
               requireActivity().onBackPressed();
           }
       });


       layoutEditProfile.setOnClickListener(v -> showEditProfileDialog());
       layoutChangePassword.setOnClickListener(v -> showChangePasswordDialog());
       btnLogout.setOnClickListener(v -> showLogoutDialog());


       loadProfile();
   }


   private String getToken() {
       SharedPreferences sharedPreferences = requireActivity()
               .getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
       return sharedPreferences.getString("USER_TOKEN", null);
   }


   private void loadProfile() {
       String token = getToken();


       if (token == null || token.isEmpty()) {
           Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
           return;
       }


       RetrofitClient.getInstance().getApi().getProfile("Token " + token)
               .enqueue(new Callback<ProfileResponse>() {
                   @Override
                   public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                       if (response.isSuccessful() && response.body() != null) {
                           currentProfile = response.body();


                           tvFullName.setText(currentProfile.getFullName());
                           tvEmail.setText(currentProfile.getEmail());


                           String avatarText = getAvatarText(currentProfile.getFullName());
                           tvAvatar.setText(avatarText);


                           SharedPreferences sharedPreferences = requireActivity()
                                   .getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
                           sharedPreferences.edit()
                                   .putString("USER_FULL_NAME", currentProfile.getFullName())
                                   .apply();
                       } else {
                           Toast.makeText(getContext(), "Không tải được thông tin cá nhân", Toast.LENGTH_SHORT).show();
                       }
                   }


                   @Override
                   public void onFailure(Call<ProfileResponse> call, Throwable t) {
                       Toast.makeText(getContext(), "Lỗi tải hồ sơ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
   }


   private String getAvatarText(String fullName) {
       if (fullName == null || fullName.trim().isEmpty()) {
           return "SV";
       }


       String[] parts = fullName.trim().split("\\s+");
       if (parts.length == 1) {
           return parts[0].substring(0, 1).toUpperCase();
       }


       String first = parts[0].substring(0, 1).toUpperCase();
       String last = parts[parts.length - 1].substring(0, 1).toUpperCase();
       return first + last;
   }


   private void showEditProfileDialog() {
       if (currentProfile == null) {
           Toast.makeText(requireContext(), "Đang tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
           return;
       }


       Dialog dialog = new Dialog(requireContext());
       dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       dialog.setContentView(R.layout.dialog_edit_profile);
       dialog.setCancelable(true);


       if (dialog.getWindow() != null) {
           Window window = dialog.getWindow();
           window.getAttributes().windowAnimations = android.R.style.Animation_Dialog;
           window.setBackgroundDrawableResource(android.R.color.transparent);
           int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
           window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
       }


       EditText edtFullName = dialog.findViewById(R.id.edtFullName);
       EditText edtEmail = dialog.findViewById(R.id.edtEmail);
       EditText edtPhone = dialog.findViewById(R.id.edtPhone);
       Button btnCancel = dialog.findViewById(R.id.btnCancelEdit);
       Button btnSave = dialog.findViewById(R.id.btnSaveEdit);


       edtFullName.setText(currentProfile.getFullName());
       edtEmail.setText(currentProfile.getEmail());
       edtPhone.setText(currentProfile.getPhoneNumber());


       btnCancel.setOnClickListener(v -> dialog.dismiss());


       btnSave.setOnClickListener(v -> {
           String fullName = edtFullName.getText().toString().trim();
           String email = edtEmail.getText().toString().trim();
           String phone = edtPhone.getText().toString().trim();


           if (fullName.isEmpty()) {
               edtFullName.setError("Vui lòng nhập họ và tên");
               return;
           }


           updateProfile(fullName, email, phone, dialog);
       });


       dialog.show();
   }


   private void updateProfile(String fullName, String email, String phone, Dialog dialog) {
       String token = getToken();


       UpdateProfileRequest request = new UpdateProfileRequest(fullName, email, phone);


       RetrofitClient.getInstance().getApi()
               .updateProfile("Token " + token, request)
               .enqueue(new Callback<UpdateProfileResponse>() {
                   @Override
                   public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                       if (response.isSuccessful() && response.body() != null) {
                           UpdateProfileResponse body = response.body();


                           tvFullName.setText(body.getFullName());
                           tvEmail.setText(body.getEmail());
                           tvAvatar.setText(getAvatarText(body.getFullName()));


                           SharedPreferences sharedPreferences = requireActivity()
                                   .getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
                           sharedPreferences.edit()
                                   .putString("USER_FULL_NAME", body.getFullName())
                                   .apply();


                           loadProfile();


                           Toast.makeText(requireContext(), body.getMessage(), Toast.LENGTH_SHORT).show();
                           dialog.dismiss();
                       } else {
                           Toast.makeText(requireContext(), "Cập nhật hồ sơ thất bại", Toast.LENGTH_SHORT).show();
                       }
                   }


                   @Override
                   public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                       Toast.makeText(requireContext(), "Lỗi cập nhật: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
   }


   private void showChangePasswordDialog() {
       Dialog dialog = new Dialog(requireContext());
       dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       dialog.setContentView(R.layout.dialog_change_password);
       dialog.setCancelable(true);


       if (dialog.getWindow() != null) {
           Window window = dialog.getWindow();
           window.setBackgroundDrawableResource(android.R.color.transparent);
           int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
           window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
       }


       EditText edtCurrentPassword = dialog.findViewById(R.id.edtCurrentPassword);
       EditText edtNewPassword = dialog.findViewById(R.id.edtNewPassword);
       EditText edtConfirmPassword = dialog.findViewById(R.id.edtConfirmPassword);
       Button btnCancel = dialog.findViewById(R.id.btnCancelPassword);
       Button btnSave = dialog.findViewById(R.id.btnSavePassword);


       edtCurrentPassword.setTag(false);
       edtNewPassword.setTag(false);
       edtConfirmPassword.setTag(false);


       setupPasswordToggle(edtCurrentPassword);
       setupPasswordToggle(edtNewPassword);
       setupPasswordToggle(edtConfirmPassword);


       btnCancel.setOnClickListener(v -> dialog.dismiss());


       btnSave.setOnClickListener(v -> {
           String currentPassword = edtCurrentPassword.getText().toString().trim();
           String newPassword = edtNewPassword.getText().toString().trim();
           String confirmPassword = edtConfirmPassword.getText().toString().trim();


           if (currentPassword.isEmpty()) {
               edtCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
               return;
           }


           if (newPassword.length() < 8) {
               edtNewPassword.setError("Mật khẩu mới phải có ít nhất 8 ký tự");
               return;
           }


           if (!newPassword.equals(confirmPassword)) {
               edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
               return;
           }


           changePassword(currentPassword, newPassword, confirmPassword, dialog);
       });


       dialog.show();
   }


   private void changePassword(String currentPassword, String newPassword, String confirmPassword, Dialog dialog) {
       String token = getToken();


       if (token == null || token.isEmpty()) {
           Toast.makeText(requireContext(), "Phiên đăng nhập đã hết", Toast.LENGTH_SHORT).show();
           return;
       }


       ChangePasswordRequest request =
               new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);


       RetrofitClient.getInstance().getApi()
               .changePassword("Token " + token, request)
               .enqueue(new Callback<ChangePasswordResponse>() {
                   @Override
                   public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                       if (response.isSuccessful() && response.body() != null) {
                           ChangePasswordResponse body = response.body();


                           if (body.getToken() != null && !body.getToken().isEmpty()) {
                               SharedPreferences sharedPreferences = requireActivity()
                                       .getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
                               sharedPreferences.edit()
                                       .putString("USER_TOKEN", body.getToken())
                                       .apply();
                           }


                           String message = body.getMessage();
                           if (message == null || message.trim().isEmpty()) {
                               message = "Đổi mật khẩu thành công";
                           }


                           Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                           dialog.dismiss();


                       } else {
                           String userMessage = "Không thể đổi mật khẩu";


                           try {
                               if (response.errorBody() != null) {
                                   String errorText = response.errorBody().string();


                                   if (errorText.contains("too similar to the username")) {
                                       userMessage = "Mật khẩu mới quá giống tên đăng nhập";
                                   } else if (errorText.contains("too short")) {
                                       userMessage = "Mật khẩu mới quá ngắn";
                                   } else if (errorText.contains("too common")) {
                                       userMessage = "Mật khẩu mới quá đơn giản";
                                   } else if (errorText.contains("numeric")) {
                                       userMessage = "Mật khẩu mới không được chỉ gồm số";
                                   } else if (errorText.contains("Current password is incorrect")) {
                                       userMessage = "Mật khẩu hiện tại không đúng";
                                   } else if (errorText.contains("current_password")) {
                                       userMessage = "Vui lòng kiểm tra lại mật khẩu hiện tại";
                                   } else if (errorText.contains("new_password")) {
                                       userMessage = "Mật khẩu mới không hợp lệ";
                                   }
                               }
                           } catch (Exception e) {
                               e.printStackTrace();
                           }


                           Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show();
                       }
                   }


                   @Override
                   public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                       Toast.makeText(requireContext(), "Không thể kết nối để đổi mật khẩu", Toast.LENGTH_LONG).show();
                   }
               });
   }


   private void showLogoutDialog() {
       Dialog dialog = new Dialog(requireContext());
       dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       dialog.setContentView(R.layout.dialog_logout_confirm);
       dialog.setCancelable(true);


       if (dialog.getWindow() != null) {
           Window window = dialog.getWindow();
           window.setBackgroundDrawableResource(android.R.color.transparent);
           int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.82);
           window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
           window.setGravity(Gravity.CENTER);
       }


       Button btnCancelLogout = dialog.findViewById(R.id.btnCancelLogout);
       Button btnConfirmLogout = dialog.findViewById(R.id.btnConfirmLogout);


       btnCancelLogout.setOnClickListener(v -> dialog.dismiss());


       btnConfirmLogout.setOnClickListener(v -> {
           SharedPreferences sharedPreferences = requireActivity()
                   .getSharedPreferences("StudezyPrefs", Context.MODE_PRIVATE);
           sharedPreferences.edit().clear().apply();


           dialog.dismiss();


           NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
           navController.navigate(R.id.welcomeFragment);
       });


       dialog.show();
   }


   private void setupPasswordToggle(EditText editText) {
       editText.setOnTouchListener((v, event) -> {
           if (event.getAction() == MotionEvent.ACTION_UP) {
               if (editText.getCompoundDrawables()[2] != null) {
                   int drawableWidth = editText.getCompoundDrawables()[2].getBounds().width();


                   if (event.getRawX() >= (editText.getRight() - drawableWidth - editText.getPaddingEnd())) {
                       Object tag = editText.getTag();
                       boolean isVisible = tag != null && (boolean) tag;


                       // Tip: To avoid font reset when toggling, you can save and restore the typeface
                       android.graphics.Typeface existingTypeface = editText.getTypeface();


                       if (isVisible) {
                           editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                           editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_mkdong, 0);
                           editText.setTag(false);
                       } else {
                           editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                           editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_matmo, 0);
                           editText.setTag(true);
                       }


                       editText.setTypeface(existingTypeface);
                       editText.setSelection(editText.getText().length());
                       return true;
                   }
               }
           }
           return false;
       });
   }
}



