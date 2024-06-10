package com.example.recipesapp.room;

import android.content.Context;

import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.recipesapp.models.FavouriteRecipe;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

// Аннотация @Database указывает, что это класс базы данных Room.
// Определяем сущности (FavouriteRecipe) и версию базы данных (1).
@Database(entities = {FavouriteRecipe.class}, version = 1, exportSchema = false)
public abstract class RecipeDatabase extends RoomDatabase {

    // Метод для получения текущего экземпляра базы данных.
    public static RecipeDatabase getInstance() {
        return instance;
    }

    // Метод для установки экземпляра базы данных.
    public static void setInstance(RecipeDatabase instance) {
        RecipeDatabase.instance = instance;
    }

    // Абстрактный метод для получения DAO объекта для работы с любимыми рецептами.
    public abstract RecipeDao favouriteDao();

    // Поле для хранения экземпляра базы данных.
    private static RecipeDatabase instance = null;

    // Определяем количество потоков для ExecutorService.
    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService для выполнения операций записи в базу данных в фоновом режиме.
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Метод для получения синхронизированного экземпляра базы данных.
    public static synchronized RecipeDatabase getInstance(Context context) {
        // Проверяем, существует ли уже экземпляр базы данных.
        if (getInstance() == null) {
            // Создаем новый экземпляр базы данных с использованием Room.databaseBuilder.
            setInstance(Room.databaseBuilder(context.getApplicationContext(), RecipeDatabase.class, "recipe_database")
                    // Разрешаем уничтожающую миграцию, которая удаляет старые данные при изменении схемы.
                    .fallbackToDestructiveMigration()
                    // Строим базу данных.
                    .build());
        }
        // Возвращаем текущий экземпляр базы данных.
        return getInstance();
    }
}
