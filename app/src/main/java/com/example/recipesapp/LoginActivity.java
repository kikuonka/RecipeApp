package com.example.recipesapp;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Patterns;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.example.recipesapp.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnLogin.setOnClickListener(view -> login());
        binding.tvGuest.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
        binding.tvSignup.setOnClickListener(view -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void login() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите свои электронную почту и пароль", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Пожалуйста, введите действительную электронную почту", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать не менее 6 символов", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseApp.initializeApp(this);
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Вы вошли в систему", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}