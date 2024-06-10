package com.example.recipesapp.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Insert;

import com.example.recipesapp.models.FavouriteRecipe;

import java.util.List;

// Интерфейс RecipeDao определяет методы для взаимодействия с базой данных Room.
@Dao
public interface RecipeDao {

    // Метод для вставки нового любимого рецепта в базу данных.
    // Возвращает ID вставленного рецепта.
    @Insert
    long insert(FavouriteRecipe recipe);

    // Метод для удаления любимого рецепта из базы данных по его ID.
    @Query("DELETE FROM favourite_recipes WHERE recipeId = :id")
    void delete(String id);

    // Метод для получения всех любимых рецептов из базы данных.
    @Query("SELECT * FROM favourite_recipes")
    List<FavouriteRecipe> getAll();

    // Метод для получения конкретного любимого рецепта по его ID.
    @Query("SELECT * FROM favourite_recipes WHERE recipeId = :favouriteName")
    FavouriteRecipe getFavourite(String favouriteName);

    // Метод для получения всех любимых рецептов из базы данных (дублирующий метод getAll).
    @Query("SELECT * FROM favourite_recipes")
    List<FavouriteRecipe> getAllFavourites();
}
