package com.example.recipesapp;

import android.os.Bundle;
import android.widget.Toast;
import android.content.Intent;

import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.example.recipesapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityMainBinding binding;
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        binding.floatingActionButton.setOnClickListener(view -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null)
                Toast.makeText(this, "Для добавления рецепта необходимо зарегистрироваться", Toast.LENGTH_SHORT).show();
            else
                startActivity(new Intent(MainActivity.this, AddRecipeActivity.class));
        });
    }
}