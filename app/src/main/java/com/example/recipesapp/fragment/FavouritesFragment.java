package com.example.recipesapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.recipesapp.adapters.RecipeAdapter;
import com.example.recipesapp.databinding.FragmentFavouritesBinding;
import com.example.recipesapp.models.FavouriteRecipe;
import com.example.recipesapp.models.Recipe;
import com.example.recipesapp.room.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Фрагмент для отображения избранных рецептов
public class FavouritesFragment extends Fragment {
    private FragmentFavouritesBinding binding; // Привязка фрагмента
    private RecipeRepository recipeRepository; // Репозиторий рецептов

    @Override
    // Создание представления фрагмента
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false); // Привязка макета фрагмента
        return binding.getRoot(); // Возвращает корневое представление фрагмента
    }

    @Override
    // Вызывается при возобновлении фрагмента
    public void onResume() {
        super.onResume();
        // Проверка аутентификации пользователя
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Показ сообщения об ошибке, если пользователь не вошел в систему
            new AlertDialog.Builder(getContext())
                    .setTitle("Требуется войти в систему")
                    .setMessage("К сожалению, эта функция вам недоступна")
                    .show();
        } else {
            loadFavorites(); // Загрузка избранных рецептов
        }
    }

    // Метод для загрузки избранных рецептов
    private void loadFavorites() {
        recipeRepository = new RecipeRepository(requireActivity().getApplication()); // Инициализация репозитория рецептов
        List<FavouriteRecipe> favouriteRecipes = recipeRepository.getAllFavourites(); // Получение списка избранных рецептов из базы данных Room
        if (favouriteRecipes.isEmpty()) {
            // Показ сообщения, если список избранных рецептов пуст
            Toast.makeText(requireContext(), "Вы еще не добавили ни одного рецепта", Toast.LENGTH_SHORT).show();
            binding.rvFavourites.setVisibility(View.GONE);
            binding.noFavourites.setVisibility(View.VISIBLE);
        } else {
            // Установка менеджера компоновки для RecyclerView
            binding.rvFavourites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            binding.rvFavourites.setAdapter(new RecipeAdapter()); // Установка пустого адаптера

            List<Recipe> recipes = new ArrayList<>(); // Создание списка рецептов
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes"); // Получение ссылки на базу данных Firebase для рецептов
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                // Вызывается при изменении данных в базе данных Firebase
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            for (FavouriteRecipe favouriteRecipe : favouriteRecipes) {
                                // Проверка, является ли рецепт избранным
                                if (dataSnapshot.getKey().equals(favouriteRecipe.getRecipeId())) {
                                    recipes.add(dataSnapshot.getValue(Recipe.class)); // Добавление рецепта в список
                                }
                            }
                        }
                        // Показ списка избранных рецептов, если они есть
                        binding.rvFavourites.setVisibility(View.VISIBLE);
                        binding.noFavourites.setVisibility(View.GONE);
                        RecipeAdapter adapter = (RecipeAdapter) binding.rvFavourites.getAdapter(); // Получение адаптера RecyclerView
                        if (adapter != null) {
                            adapter.setRecipeList(recipes); // Установка списка рецептов в адаптер
                        }

                    } else {
                        // Показ сообщения, если список рецептов пуст
                        binding.noFavourites.setVisibility(View.VISIBLE);
                        binding.rvFavourites.setVisibility(View.GONE);
                    }
                }

                @Override
                // Вызывается при отмене операции чтения из базы данных Firebase
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FavouritesFragment", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    @Override
    // Вызывается при уничтожении представления фрагмента
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Освобождение ссылки на привязку
    }
}
