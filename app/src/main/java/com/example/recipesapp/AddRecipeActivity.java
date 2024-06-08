package com.example.recipesapp;

import static java.lang.System.currentTimeMillis;

import android.net.Uri;
import android.util.Log;
import android.os.Bundle;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.app.ProgressDialog;
import android.widget.ArrayAdapter;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recipesapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.UploadTask;
import com.example.recipesapp.models.Category;
import com.vansuita.pickimage.bundle.PickSetup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.ActivityAddRecipeBinding;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;

public class AddRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;
    private boolean isImageSelected = false;
    private ProgressDialog dialog;
    boolean isEdit;
    String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadCategories();
        binding.btnAddRecipe.setOnClickListener(view -> {
            getData();
        });
        binding.imgRecipe.setOnClickListener(view -> {
            pickImage();
        });

        isEdit = getIntent().getBooleanExtra("isEdit", false);
        if (isEdit) {
            editRecipe();
        }
    }

    private void editRecipe() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        recipeId = recipe.getId();
        isImageSelected = true;
        binding.etRecipeName.setText(recipe.getName());
        binding.etDescription.setText(recipe.getDescription());
        binding.etCookingTime.setText(recipe.getTime());
        binding.etCategory.setText(recipe.getCategory());
        binding.etCalories.setText(recipe.getCalories());
        Glide
                .with(binding.getRoot().getContext())
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.drawable.image_placeholder)
                .into(binding.imgRecipe);

        binding.btnAddRecipe.setText("Обновить");
    }

    private void loadCategories() {
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        binding.etCategory.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categories.add(dataSnapshot.getValue(Category.class).getName());
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void pickImage() {
        PickImageDialog.build(new PickSetup()).show(AddRecipeActivity.this).setOnPickResult(r -> {
            Log.e("ProfileFragment", "onPickResult: " + r.getUri());
            binding.imgRecipe.setImageBitmap(r.getBitmap());
            binding.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_CROP);
            isImageSelected = true;
        }).setOnPickCancel(() -> Toast.makeText(AddRecipeActivity.this, "Отменено", Toast.LENGTH_SHORT).show());
    }

    private void getData() {
        String recipeName = Objects.requireNonNull(binding.etRecipeName.getText()).toString();
        String recipeDescription = Objects.requireNonNull(binding.etDescription.getText()).toString();
        String cookingTime = Objects.requireNonNull(binding.etCookingTime.getText()).toString();
        String recipeCategory = binding.etCategory.getText().toString();
        String calories = Objects.requireNonNull(binding.etCalories.getText()).toString();

        if (recipeName.isEmpty()) {
            binding.etRecipeName.setError("Пожалуйста, введите название рецепта");
        } else if (recipeDescription.isEmpty()) {
            binding.etDescription.setError("Пожалуйста, введите описание рецепта");
        } else if (cookingTime.isEmpty()) {
            binding.etCookingTime.setError("Пожалуйста, укажите время приготовления");
        } else if (recipeCategory.isEmpty()) {
            binding.etCategory.setError("Пожалуйста, введите категорию рецептов");
        } else if (calories.isEmpty()) {
            binding.etCalories.setError("Пожалуйста, введите количество калорий");
        } else if (!isImageSelected) {
            Toast.makeText(this, "Пожалуйста, выберите изображение", Toast.LENGTH_SHORT).show();
        } else {
            dialog = new ProgressDialog(this);
            dialog.setMessage("Загружаем рецепт...");
            dialog.setCancelable(false);
            dialog.show();
            Recipe recipe = new Recipe(recipeName, recipeDescription, cookingTime, recipeCategory, calories, "", FirebaseAuth.getInstance().getUid());
            uploadImage(recipe);
        }
    }

    private String uploadImage(Recipe recipe) {
        final String[] url = {""};
        binding.imgRecipe.setDrawingCacheEnabled(true);
        Bitmap bitmap = ((BitmapDrawable) binding.imgRecipe.getDrawable()).getBitmap();
        binding.imgRecipe.setDrawingCacheEnabled(false);
        String id = isEdit ? recipe.getId() : currentTimeMillis() + "";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + id + "_recipe.jpg");
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
                url[0] = downloadUri.toString();
                Toast.makeText(AddRecipeActivity.this, "Изображение успешно загружено", Toast.LENGTH_SHORT).show();
                saveDataInDataBase(recipe, url[0]);
            } else {
                Toast.makeText(AddRecipeActivity.this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Log.e("ProfileFragment", "onComplete: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
        return url[0];
    }

    private void saveDataInDataBase(Recipe recipe, String url) {
        recipe.setImage(url);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Recipes");
        if (isEdit) {
            recipe.setId(recipeId);
            reference.child(recipe.getId()).setValue(recipe).addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(AddRecipeActivity.this, "Рецепт успешно обновлен", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Ошибка при обновлении рецепта", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String id = reference.push().getKey();
            recipe.setId(id);
            if (id != null) {
                reference.child(id).setValue(recipe).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(AddRecipeActivity.this, "Рецепт успешно добавлен", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddRecipeActivity.this, "Ошибка при добавлении рецепта", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}