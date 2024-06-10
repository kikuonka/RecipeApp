package com.example.recipesapp.fragment;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.recipesapp.R;

import com.example.recipesapp.models.User;
import com.example.recipesapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.recipesapp.adapters.RecipeAdapter;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.FragmentProfileBinding;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

public class ProfileFragment extends Fragment {
    // Привязка разметки фрагмента к классу
    private FragmentProfileBinding binding;
    // Экземпляр класса User для хранения информации о пользователе
    private User user;

    // Метод для создания представления фрагмента
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Метод, вызываемый после создания представления фрагмента
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Проверка, авторизован ли пользователь
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Если пользователь не авторизован, показать диалоговое окно
            new AlertDialog.Builder(getContext())
                    .setTitle("Требуется войти в систему")
                    .setMessage("Вы не можете редактировать профиль")
                    .show();
        } else {
            // Если пользователь авторизован, загрузить профиль и рецепты пользователя
            loadProfile();
            loadUserRecipes();
            // Инициализировать элементы управления
            init();
        }
    }

    // Метод, вызываемый при возобновлении фрагмента
    @Override
    public void onResume() {
        super.onResume();
        // Загрузить рецепты пользователя
        loadUserRecipes();
    }

    // Метод для инициализации элементов управления
    private void init() {
        // Обработчик нажатия для кнопки редактирования профиля
        binding.imgEditProfile.setOnClickListener(v -> {
            // Отобразить диалог выбора изображения для профиля
            PickImageDialog.build(new PickSetup()).show(requireActivity()).setOnPickResult(r -> {
                Log.e("ProfileFragment", "onPickResult: " + r.getUri());
                // Установить выбранное изображение в ImageView
                binding.imgProfile.setImageBitmap(r.getBitmap());
                binding.imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Загрузить изображение на сервер
                uploadImage(r.getBitmap());
            }).setOnPickCancel(() -> Toast.makeText(requireContext(), "Отменено", Toast.LENGTH_SHORT).show());
        });

        // Обработчик нажатия для кнопки редактирования обложки профиля
        binding.imgEditCover.setOnClickListener(view ->
                PickImageDialog.build(new PickSetup()).show(requireActivity()).setOnPickResult(r -> {
                    Log.e("ProfileFragment", "onPickResult: " + r.getUri());
                    // Установить выбранное изображение в ImageView
                    binding.imgCover.setImageBitmap(r.getBitmap());
                    binding.imgCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    // Загрузить изображение на сервер
                    uploadCoverImage(r.getBitmap());
                }).setOnPickCancel(() -> Toast.makeText(requireContext(), "Отменено", Toast.LENGTH_SHORT).show()));
    }

    // Метод для загрузки изображения обложки на сервер Firebase Storage
    private void uploadCoverImage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getUid() + "cover.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Toast.makeText(requireContext(), "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
                // Обновить URL обложки пользователя в базе данных Firebase
                user.setCover(Objects.requireNonNull(downloadUri).toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user);
            } else {
                Log.e("ProfileFragment", "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    // Метод для загрузки изображения профиля на сервер Firebase Storage
    private void uploadImage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getUid() + "image.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                Toast.makeText(requireContext(), "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
                // Обновить URL изображения профиля пользователя в базе данных Firebase
                user.setImage(Objects.requireNonNull(downloadUri).toString());
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user);
            } else {
                Log.e("ProfileFragment", "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    // Метод для загрузки рецептов пользователя
    private void loadUserRecipes() {
        // Установить менеджер компоновки для RecyclerView
        binding.rvProfile.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProfile.setAdapter(new RecipeAdapter());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        // Получить рецепты пользователя по ID автора
        reference.child("Recipes").orderByChild("authorId").equalTo(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                // Перебрать все рецепты и добавить их в список
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                // Установить список рецептов в адаптер RecyclerView
                ((RecipeAdapter) Objects.requireNonNull(binding.rvProfile.getAdapter())).setRecipeList(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "onCancelled: " + error.getMessage());
            }
        });
    }

    // Метод для загрузки профиля пользователя
    private void loadProfile() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    // Установить данные пользователя в соответствующие элементы управления
                    binding.tvUserName.setText(user.getName());
                    binding.tvEmail.setText(user.getEmail());
                    Glide
                            .with(requireContext())
                            .load(user.getImage())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(binding.imgProfile);

                    Glide
                            .with(requireContext())
                            .load(user.getCover())
                            .centerCrop()
                            .placeholder(R.drawable.bg_default_recipe)
                            .into(binding.imgCover);
                } else {
                    Log.e("ProfileFragment", "onDataChange: User is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибки при загрузке профиля пользователя
                Log.e("ProfileFragment", "onCancelled: " + error.getMessage());
            }
        });

        // Создание временного пользователя с жестко закодированными данными (для тестирования)
        User user = new User();
        user.setName("Polina");
        user.setEmail("efr-polina2004@yandex.ru");
        // Установить тестовые данные пользователя в соответствующие элементы управления
        binding.tvUserName.setText(user.getName());
        binding.tvEmail.setText(user.getEmail());
    }

    // Метод, вызываемый при уничтожении представления фрагмента
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Очистка привязки для предотвращения утечек памяти
        binding = null;
    }
}
