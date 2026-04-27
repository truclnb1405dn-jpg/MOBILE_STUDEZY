package com.example.studezy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

public class WelcomeFragment extends Fragment {

    public WelcomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnLoginWelcome = view.findViewById(R.id.btn_welcome_login);
        Button btnRegisterWelcome = view.findViewById(R.id.btn_welcome_register);

        // THUẬT TOÁN CHỐNG LỖI NÚT BACK:
        // Lệnh này sẽ xóa WelcomeFragment khỏi lịch sử màn hình khi chuyển trang
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.welcomeFragment, true)
                .build();

        if (btnLoginWelcome != null) {
            btnLoginWelcome.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_loginFragment, null, navOptions)
            );
        }

        if (btnRegisterWelcome != null) {
            btnRegisterWelcome.setOnClickListener(v ->
                    Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_registerFragment, null, navOptions)
            );
        }
    }
}
