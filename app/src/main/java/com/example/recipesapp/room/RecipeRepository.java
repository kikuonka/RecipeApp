package com.example.recipesapp.room;

import android.app.Application;

import com.example.recipesapp.models.FavouriteRecipe;

import java.util.List;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class RecipeRepository {
    private RecipeDao recipeDao;

    // Конструктор, который инициализирует DAO для работы с базой данных
    public RecipeRepository(Application application) {
        // Получение экземпляра базы данных
        RecipeDatabase database = RecipeDatabase.getInstance(application);
        // Инициализация DAO для работы с таблицей любимых рецептов
        recipeDao = database.favouriteDao();
    }

    // Метод для вставки нового любимого рецепта в базу данных
    public long insert(FavouriteRecipe recipe) {
        // Запуск операции вставки в фоновом потоке
        Future<Long> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.insert(recipe));
        try {
            // Ожидание завершения операции и возврат результата
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // Обработка исключений и возврат -1 в случае ошибки
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    // Метод для удаления любимого рецепта из базы данных
    public void delete(FavouriteRecipe recipe) {
        // Запуск операции удаления в фоновом потоке
        RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.delete(recipe.getRecipeId()));
    }

    // Метод для проверки, является ли рецепт любимым
    public boolean isFavourite(String favouriteRecipe) {
        // Запуск операции проверки в фоновом потоке
        Future<Boolean> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.getFavourite(favouriteRecipe) != null);
        try {
            // Ожидание завершения операции и возврат результата
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // Обработка исключений и возврат false в случае ошибки
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // Метод для получения всех любимых рецептов из базы данных
    public List<FavouriteRecipe> getAllFavourites() {
        // Запуск операции получения списка в фоновом потоке
        Future<List<FavouriteRecipe>> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.getAllFavourites());
        try {
            // Ожидание завершения операции и возврат результата
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // Обработка исключений и возврат пустого списка в случае ошибки
            e.printStackTrace();
            Thread.currentThread().interrupt();
            //return null;
            return Collections.emptyList();
        }
    }
}
