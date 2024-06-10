package com.example.recipesapp.fragment;

import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.recipesapp.adapters.CategoryAdapter;
import com.example.recipesapp.databinding.FragmentCategoryBinding;
import com.example.recipesapp.models.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// Фрагмент для отображения категорий рецептов
public class CategoriesFragment extends Fragment {
    private FragmentCategoryBinding binding; // Привязка фрагмента

    @Override
    // Создание представления фрагмента
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false); // Привязка макета фрагмента
        return binding.getRoot(); // Возвращает корневое представление фрагмента
    }

    @Override
    // Вызывается после создания представления фрагмента
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCategories(); // Загрузка категорий
    }

    // Метод для загрузки категорий из базы данных Firebase
    private void loadCategories() {
        binding.rvCategories.setAdapter(new CategoryAdapter()); // Установка пустого адаптера

        // Получение ссылки на базу данных Firebase для категорий
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            // Вызывается при изменении данных в базе данных Firebase
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Category> categories = new ArrayList<>();
                // Перебор данных о категориях из снимка данных Firebase
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Category category = dataSnapshot.getValue(Category.class); // Преобразование данных в объекты класса Category
                    categories.add(category); // Добавление категории в список
                }
                // Получение адаптера и установка списка категорий
                CategoryAdapter adapter = (CategoryAdapter) binding.rvCategories.getAdapter();
                if (adapter != null) {
                    adapter.setCategoryList(categories);
                }
            }

            @Override
            // Вызывается при отмене операции чтения из базы данных Firebase
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Error", error.getMessage()); // Вывод ошибки в журнал
            }
        });
    }

    @Override
    // Вызывается при уничтожении представления фрагмента
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Освобождение ссылки на привязку
    }
}
