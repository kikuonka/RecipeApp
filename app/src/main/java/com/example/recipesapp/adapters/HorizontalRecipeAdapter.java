package com.example.recipesapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipesapp.R;
import com.example.recipesapp.RecipeDetailsActivity;
import com.example.recipesapp.databinding.ItemRecipeHorizontalBinding;
import com.example.recipesapp.models.Recipe;

import java.util.ArrayList;
import java.util.List;

// Адаптер для горизонтального списка рецептов в RecyclerView
public class HorizontalRecipeAdapter extends RecyclerView.Adapter<HorizontalRecipeAdapter.RecipeHolder> {

    // Список рецептов
    private List<Recipe> recipeList = new ArrayList<>();

    // Метод для установки списка рецептов и уведомления об изменениях
    public void setRecipeList(List<Recipe> recipeList) {
        this.recipeList.clear(); // Очищаем предыдущий список
        this.recipeList.addAll(recipeList); // Добавляем новый список
        notifyDataSetChanged(); // Уведомляем RecyclerView об изменениях
    }

    @NonNull
    @Override
    // Метод для создания ViewHolder
    public RecipeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Создаем новый ViewHolder, используя привязку макета
        return new RecipeHolder(ItemRecipeHorizontalBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    // Метод для привязки данных к ViewHolder
    public void onBindViewHolder(@NonNull RecipeHolder holder, int position) {
        // Получаем текущий рецепт по позиции
        Recipe recipe = recipeList.get(position);
        // Привязываем данные рецепта к ViewHolder
        holder.onBind(recipe);
    }

    @Override
    // Метод для получения количества элементов в списке
    public int getItemCount() {
        return recipeList.size();
    }

    // Вложенный класс ViewHolder для управления представлением элемента
    public static class RecipeHolder extends RecyclerView.ViewHolder {

        // Переменная для привязки представления элемента
        private final ItemRecipeHorizontalBinding binding;

        // Конструктор ViewHolder
        public RecipeHolder(@NonNull ItemRecipeHorizontalBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        // Метод для привязки данных рецепта к элементу представления
        public void onBind(Recipe recipe) {
            // Загрузка изображения рецепта с помощью Glide
            Glide.with(binding.getRoot().getContext())
                    .load(recipe.getImage())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher) // Изображение-заполнитель
                    .into(binding.bgImgRecipe);
            // Установка названия рецепта
            binding.tvRecipeName.setText(recipe.getName());

            // Установка обработчика клика для элемента
            binding.getRoot().setOnClickListener(view -> {
                // Создание намерения для перехода к RecipeDetailsActivity
                Intent intent = new Intent(binding.getRoot().getContext(), RecipeDetailsActivity.class);
                // Добавление дополнительных данных в намерение (в данном случае, рецепта)
                intent.putExtra("recipe", recipe);
                // Запуск активности
                binding.getRoot().getContext().startActivity(intent);
            });
        }
    }
}
