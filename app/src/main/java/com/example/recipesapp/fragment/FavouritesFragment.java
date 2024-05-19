package com.example.recipesapp.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import com.example.recipesapp.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.example.recipesapp.room.RecipeRepository;
import com.google.firebase.database.FirebaseDatabase;
import com.example.recipesapp.adapters.RecipeAdapter;
import com.example.recipesapp.models.FavouriteRecipe;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.example.recipesapp.databinding.FragmentFavouritesBinding;

import java.util.List;
import java.util.ArrayList;

public class FavouritesFragment extends Fragment {
    FragmentFavouritesBinding binding;
    RecipeRepository recipeRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavouritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Требуется войти в систему")
                    .setMessage("К сожалению, эта функция вам недоступна")
                    .show();
        } else {
            loadFavorites();
        }
    }

    private void loadFavorites() {
        recipeRepository = new RecipeRepository(requireActivity().getApplication());
        List<FavouriteRecipe> favouriteRecipes = recipeRepository.getAllFavourites();
        if (favouriteRecipes.isEmpty()) {
            Toast.makeText(requireContext(), "Вы еще не добавили ни одного рецепта", Toast.LENGTH_SHORT).show();
            binding.rvFavourites.setVisibility(View.GONE);
            binding.noFavourites.setVisibility(View.VISIBLE);
        } else {
            binding.rvFavourites.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            binding.rvFavourites.setAdapter(new RecipeAdapter());
            List<Recipe> recipes = new ArrayList<>();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Recipes");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            for (FavouriteRecipe favouriteRecipe : favouriteRecipes) {
                                if (dataSnapshot.getKey().equals(favouriteRecipe.getRecipeId())) {
                                    recipes.add(dataSnapshot.getValue(Recipe.class));
                                }
                            }
                        }
                        binding.rvFavourites.setVisibility(View.VISIBLE);
                        binding.noFavourites.setVisibility(View.GONE);
                        RecipeAdapter adapter = (RecipeAdapter) binding.rvFavourites.getAdapter();
                        if (adapter != null) {
                            adapter.setRecipeList(recipes);
                        }

                    } else {
                        binding.noFavourites.setVisibility(View.VISIBLE);
                        binding.rvFavourites.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FavouritesFragment", "onCancelled: " + error.getMessage());
                }
            });
        }
    }
}