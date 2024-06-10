package com.example.recipesapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipesapp.AllRecipesActivity;
import com.example.recipesapp.R;
import com.example.recipesapp.databinding.ItemCategoryBinding;
import com.example.recipesapp.models.Category;

import java.util.ArrayList;
import java.util.List;

// Класс адаптера для отображения категорий в RecyclerView (коллекция элементов в списке )
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {

    // Список категорий
    private List<Category> categoryList = new ArrayList<>();

    // Метод для обновления списка категорий и уведомления об изменениях
    public void setCategoryList(List<Category> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged(); // Обновляет данные в адаптере
    }

    @NonNull
    @Override
    // Метод для создания ViewHolder (объект, который определяет, как именно должен выглядеть элемент списка на экране)
    public CategoryAdapter.CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создание нового ViewHolder с использованием привязки макета
        return new CategoryHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    // Метод для привязки данных к ViewHolder
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryHolder holder, int position) {
        // Получение текущей категории по позиции
        Category category = categoryList.get(position);
        // Привязка данных категории к ViewHolder
        holder.onBind(category);
    }

    @Override
    // Метод для получения количества элементов в списке
    public int getItemCount() {
        return categoryList.size();
    }

    // Вложенный класс ViewHolder для управления представлением элемента
    public static class CategoryHolder extends RecyclerView.ViewHolder {

        // Переменная для привязки представления элемента
        private final ItemCategoryBinding binding;

        // Конструктор ViewHolder
        public CategoryHolder(@NonNull ItemCategoryBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        // Метод для привязки данных категории к элементу представления
        public void onBind(Category category) {
            // Установка названия категории
            binding.tvName.setText(category.getName());
            // Загрузка изображения категории с помощью Glide
            Glide.with(binding.getRoot().getContext())
                    .load(category.getImage())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher) // Изображение-заполнитель
                    .into(binding.imgBgCategory);

            // Установка обработчика клика для элемента
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Создание намерения для перехода к AllRecipesActivity
                    Intent intent = new Intent(binding.getRoot().getContext(), AllRecipesActivity.class);
                    // Добавление дополнительных данных в намерение
                    intent.putExtra("type", "category");
                    intent.putExtra("category", category.getName());
                    // Запуск активности
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
        }
    }
}
