package com.example.recipesapp;

import android.app.ProgressDialog;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recipesapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.recipesapp.room.RecipeRepository;
import com.example.recipesapp.models.FavouriteRecipe;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.ActivityRecipeDetailsBinding;

public class RecipeDetailsActivity extends AppCompatActivity {
    // Биндинг для связывания элементов макета с кодом
    ActivityRecipeDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Назначение обработчика нажатия на кнопку "Назад"
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Инициализация элементов и данных
        init();
    }

    private void init() {
        // Получение данных рецепта из Intent
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        // Установка значений в элементы интерфейса
        binding.tvName.setText(recipe.getName());
        binding.tcCategory.setText(recipe.getCategory());
        binding.tvDescription.setText(recipe.getDescription());
        binding.tvCalories.setText(String.format("%s Калорий", recipe.getCalories()));

        // Загрузка изображения рецепта с помощью Glide
        Glide
                .with(RecipeDetailsActivity.this)
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(binding.imgRecipe);

        // Проверка, является ли текущий пользователь автором рецепта
        if (recipe.getAuthorId().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            binding.imgEdit.setVisibility(View.VISIBLE);
            binding.btnDelete.setVisibility(View.VISIBLE);
        } else {
            binding.imgEdit.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);
        }

        // Назначение обработчика нажатия на кнопку редактирования рецепта
        binding.imgEdit.setOnClickListener(view -> {
            Intent intent = new Intent(binding.getRoot().getContext(), AddRecipeActivity.class);
            intent.putExtra("recipe", recipe);
            intent.putExtra("isEdit", true);
            binding.getRoot().getContext().startActivity(intent);
        });

        // Проверка, авторизован ли пользователь и добавление рецепта в избранное
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            checkFavourite(recipe);
            binding.imgFvrt.setOnClickListener(view -> favouriteRecipe(recipe));
        }

        // Назначение обработчика нажатия на кнопку удаления рецепта
        binding.btnDelete.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Удалить")
                    .setMessage("Вы уверены, что хотите удалить рецепт?")
                    .setPositiveButton("Да", (dialogInterface, i) -> {
                        // Показ диалога прогресса
                        ProgressDialog dialog = new ProgressDialog(this);
                        dialog.setMessage("Удаление...");
                        dialog.setCancelable(false);
                        dialog.show();

                        // Удаление рецепта из базы данных
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
                        reference.child(recipe.getId()).removeValue().addOnCompleteListener(task -> {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Рецепт успешно удален", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Ошибка при удалении рецепта", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });

        // Обновление данных рецепта из Firebase
        updateDataWithFireBase(recipe.getId());
    }

    // Метод для проверки, добавлен ли рецепт в избранное
    private void checkFavourite(Recipe recipe) {
        RecipeRepository repository = new RecipeRepository(getApplication());
        boolean isFavourite = repository.isFavourite(recipe.getId());
        if (isFavourite) {
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.accent));
        } else {
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.black));
        }
    }

    // Метод для добавления или удаления рецепта из избранного
    private void favouriteRecipe(Recipe recipe) {
        RecipeRepository repository = new RecipeRepository(getApplication());
        boolean isFavourite = repository.isFavourite(recipe.getId());
        if (isFavourite) {
            repository.delete(new FavouriteRecipe(recipe.getId()));
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.black));
        } else {
            repository.insert(new FavouriteRecipe(recipe.getId()));
            binding.imgFvrt.setColorFilter(getResources().getColor(R.color.accent));
        }
    }

    // Метод для обновления данных рецепта из Firebase
    private void updateDataWithFireBase(String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    binding.tvName.setText(recipe.getName());
                    binding.tcCategory.setText(recipe.getCategory());
                    binding.tvDescription.setText(recipe.getDescription());
                    binding.tvCalories.setText(String.format("%s Калорий", recipe.getCalories()));
                    Glide
                            .with(RecipeDetailsActivity.this)
                            .load(recipe.getImage())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(binding.imgRecipe);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }
}
