package com.example.recipesapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesapp.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.recipesapp.adapters.RecipeAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.ActivityAllRecipesBinding;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class AllRecipesActivity extends AppCompatActivity {
    // Биндинг для связывания элементов макета с кодом
    ActivityAllRecipesBinding binding;
    // Ссылка на базу данных Firebase
    DatabaseReference reference;
    // Тип фильтрации или поиска рецептов
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация ссылки на раздел "Recipes" в базе данных Firebase
        reference = FirebaseDatabase.getInstance().getReference("Recipes");

        // Установка макета RecyclerView с GridLayoutManager
        binding.rvRecipes.setLayoutManager(new GridLayoutManager(this, 2));

        // Установка адаптера для RecyclerView
        binding.rvRecipes.setAdapter(new RecipeAdapter());

        // Получение типа (фильтрации или поиска) из Intent
        type = getIntent().getStringExtra("type");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Проверка типа и загрузка соответствующих рецептов
        if (type.equalsIgnoreCase("category")) {
            filterByCategory();
        } else if (type.equalsIgnoreCase("search")) {
            loadByRecipes();
        } else {
            loadAllRecipes();
        }
    }

    // Метод для загрузки рецептов по поисковому запросу
    private void loadByRecipes() {
        String query = getIntent().getStringExtra("query");

        // Добавление слушателя для изменений в базе данных
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                // Итерация по всем рецептам в базе данных
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    // Проверка, содержит ли название рецепта поисковой запрос
                    if (recipe.getName().toLowerCase().contains(query.toLowerCase()))
                        recipes.add(recipe);
                }

                // Обновление данных адаптера
                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null) {
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Логирование ошибки
                Log.e("Error", error.getMessage());
            }
        });
    }

    // Метод для загрузки всех рецептов
    private void loadAllRecipes() {
        // Добавление слушателя для изменений в базе данных
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                // Итерация по всем рецептам в базе данных
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }

                // Перемешивание списка рецептов для случайного порядка
                Collections.shuffle(recipes);

                // Обновление данных адаптера
                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null) {
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Логирование ошибки
                Log.e("Error", error.getMessage());
            }
        });
    }

    // Метод для фильтрации рецептов по категории
    private void filterByCategory() {
        String category = getIntent().getStringExtra("category");

        // Добавление слушателя для изменений в базе данных с фильтрацией по категории
        reference.orderByChild("category").equalTo(category).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                // Итерация по всем рецептам в базе данных
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }

                // Обновление данных адаптера
                RecipeAdapter adapter = (RecipeAdapter) binding.rvRecipes.getAdapter();
                if (adapter != null) {
                    adapter.setRecipeList(recipes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Логирование ошибки
                Log.e("Error", error.getMessage());
            }
        });
    }
}
