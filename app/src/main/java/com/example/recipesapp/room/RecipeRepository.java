package com.example.recipesapp.room;

import android.app.Application;

import com.example.recipesapp.models.FavouriteRecipe;

import java.util.List;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class RecipeRepository {
    private RecipeDao recipeDao;

    public RecipeRepository(Application application) {
        RecipeDatabase database = RecipeDatabase.getInstance(application);
        recipeDao = database.favouriteDao();
    }

    public long insert(FavouriteRecipe recipe) {
        Future<Long> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.insert(recipe));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public void delete(FavouriteRecipe recipe) {
        RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.delete(recipe.getRecipeId()));
    }

    public boolean isFavourite(String favouriteRecipe) {
        Future<Boolean> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.getFavourite(favouriteRecipe) != null);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public List<FavouriteRecipe> getAllFavourites() {
        Future<List<FavouriteRecipe>> future = RecipeDatabase.databaseWriteExecutor.submit(() -> recipeDao.getAllFavourites());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            //return null;
            return Collections.emptyList();
        }
    }
}