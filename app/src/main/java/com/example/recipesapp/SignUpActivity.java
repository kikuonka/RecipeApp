package com.example.recipesapp;

import android.os.Bundle;
import android.widget.Toast;
import android.util.Patterns;
import android.content.Intent;
import android.app.ProgressDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.android.gms.tasks.Task;
import com.example.recipesapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.example.recipesapp.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnSignup.setOnClickListener(view -> signup());
        binding.tvLogin.setOnClickListener(view -> finish());
    }

    private void signup() {
        String name = Objects.requireNonNull(binding.etName.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите свое имя, электронную почту и пароль", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Пожалуйста, введите действительную электронную почту", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать не менее 6 символов", Toast.LENGTH_SHORT).show();
        } else {
            createNewUser(name, email, password);
        }
    }

    private void createNewUser(String name, String email, String password) {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Создание пользователя...");
        dialog.setCancelable(false);
        dialog.show();

        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveName(name, email);
                    } else {
                        dialog.dismiss();
                        Toast.makeText(this, "Не удалось создать учетную запись", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void saveName(String name, String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        User user = new User(FirebaseAuth.getInstance().getUid(), name, email, "", "");
        reference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isComplete()) {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Пользователь успешно создан", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                    finishAffinity();
                } else {
                    dialog.dismiss();
                    Toast.makeText(SignUpActivity.this, "Ошибка в создании пользователя", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}