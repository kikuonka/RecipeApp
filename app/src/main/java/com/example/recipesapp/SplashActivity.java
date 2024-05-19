package com.example.recipesapp;

import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.recipesapp.databinding.ActivitySplashBinding;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private int splashScreenTime = 2000;
    private int timeInterval = 100;
    private int progress = 0;
    private Runnable runnable;
    private Handler handler;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ImageView imageView = binding.tvTitle;
        Animation pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        imageView.startAnimation(pulseAnimation);
        binding.progressBar.setMax(splashScreenTime);
        binding.progressBar.setProgress(progress);
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            if (progress < splashScreenTime) {
                progress += timeInterval;
                binding.progressBar.setProgress(progress);
                handler.postDelayed(runnable, timeInterval);
            } else {
                FirebaseApp.initializeApp(this);
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startActivity(user != null ? new Intent(SplashActivity.this, MainActivity.class) : new Intent(SplashActivity.this, LoginActivity.class));
                //startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        };
        handler.postDelayed(runnable, timeInterval);
    }
}