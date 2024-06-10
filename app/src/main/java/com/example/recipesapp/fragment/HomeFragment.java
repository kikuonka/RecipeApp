package com.example.recipesapp.fragment;

import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipesapp.AllRecipesActivity;
import com.example.recipesapp.SettingActivity;
import com.example.recipesapp.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.FragmentHomeBinding;
import com.example.recipesapp.adapters.HorizontalRecipeAdapter;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

// Фрагмент для отображения домашнего экрана
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding; // Привязка фрагмента

    @Override
    // Создание представления фрагмента
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false); // Привязка макета фрагмента
        return binding.getRoot(); // Возвращает корневое представление фрагмента
    }

    @Override
    // Вызывается при возобновлении фрагмента
    public void onResume() {
        super.onResume();
        loadRecipes(); // Загрузка рецептов
    }

    @Override
    // Вызывается после создания представления фрагмента
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadRecipes(); // Загрузка рецептов

        // Установка слушателя действия для поля ввода поиска
        binding.etSearch.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(); // Выполнение поиска
                return true;
            }
            return false;
        });

        // Обработчик клика для кнопки просмотра всех избранных
        binding.tvSeeAllFavourite.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
            intent.putExtra("type", "favourite");
            startActivity(intent);
        });

        // Обработчик клика для кнопки просмотра всех популярных рецептов
        binding.tvSeeAllPopulars.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
            intent.putExtra("type", "popular");
            startActivity(intent);
        });

        // Обработчик клика для кнопки настроек
        binding.btnSetting.setOnClickListener(view1 -> startActivity(new Intent(requireContext(), SettingActivity.class)));
    }

    // Метод для выполнения поиска
    private void performSearch() {
        String query = Objects.requireNonNull(binding.etSearch.getText()).toString().trim();
        Intent intent = new Intent(requireContext(), AllRecipesActivity.class);
        intent.putExtra("type", "search");
        intent.putExtra("query", query);
        startActivity(intent);
    }

    // Метод для загрузки рецептов
    private void loadRecipes() {
        // Установка адаптеров для RecyclerView
        binding.rvPopulars.setAdapter(new HorizontalRecipeAdapter());
        binding.rvFavouriteMeal.setAdapter(new HorizontalRecipeAdapter());

        // Получение ссылки на базу данных Firebase для рецептов
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            // Вызывается при изменении данных в базе данных Firebase
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                // Перебор данных о рецептах из снимка данных Firebase
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe); // Добавление рецепта в список
                }
                // Загрузка популярных и избранных рецептов
                loadPopularRecipes(recipes);
                loadFavouriteRecipes(recipes);
            }

            @Override
            // Вызывается при отмене операции чтения из базы данных Firebase
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", error.getMessage());
            }
        });
    }

    // Метод для загрузки популярных рецептов
    private void loadPopularRecipes(List<Recipe> recipes) {
        List<Recipe> popularRecipes = new ArrayList<>();
        // Выбор случайных популярных рецептов
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * recipes.size());
            popularRecipes.add(recipes.get(random));
        }
        // Установка списка популярных рецептов в адаптер
        HorizontalRecipeAdapter adapter = (HorizontalRecipeAdapter) binding.rvPopulars.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(popularRecipes);
        }
    }

    // Метод для загрузки избранных рецептов
    private void loadFavouriteRecipes(List<Recipe> recipes) {
        List<Recipe> favouriteRecipes = new ArrayList<>();
        // Выбор случайных избранных рецептов
        for (int i = 0; i < 5; i++) {
            int random = (int) (Math.random() * recipes.size());
            favouriteRecipes.add(recipes.get(random));
        }
        // Установка списка избранных рецептов в адаптер
        HorizontalRecipeAdapter adapter = (HorizontalRecipeAdapter) binding.rvFavouriteMeal.getAdapter();
        if (adapter != null) {
            adapter.setRecipeList(favouriteRecipes);
        }
    }

    @Override
    // Вызывается при уничтожении представления фрагмента
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Освобождение ссылки на привязку
    }
}
