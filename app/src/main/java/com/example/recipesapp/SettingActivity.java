package com.example.recipesapp;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.recipesapp.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            binding.btnSignout.setVisibility(View.GONE);
        }
        binding.linearLayoutShare.setOnClickListener(view -> shareApp());
        binding.linearLayoutRate.setOnClickListener(view -> rateApp());
        binding.linearLayoutFeedback.setOnClickListener(view -> sendFeedback());
        binding.linearLayoutApps.setOnClickListener(view -> moreApps());
        binding.linearLayoutPrivacy.setOnClickListener(view -> privacyPolicy());
        binding.btnSignout.setOnClickListener(view -> signOut());
    }

    private void signOut() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Выход")
                .setMessage("Вы уверенны, что хотите выйти из аккаунт?")
                .setPositiveButton("Выйти", (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Отмена", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
    }

    private void privacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://vk.com/gremln"));
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Отзыв для " + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Привет " + getString(R.string.developer_name) + ",");
        startActivity(Intent.createChooser(intent, "Отправить отзыв"));
    }

    private void moreApps() {
        Intent intent = new Intent(this, InstructionsActivity.class);
        startActivity(intent);
    }

    private void rateApp() {
        Intent intent = new Intent(this, AboutProgramActivity.class);
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(this, AboutAuthorActivity.class);
        startActivity(intent);
    }
}