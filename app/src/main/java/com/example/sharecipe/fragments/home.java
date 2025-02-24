package com.example.sharecipe.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharecipe.Adapter.RecipeAdapter;
import com.example.sharecipe.R;
import com.example.sharecipe.models.Recipe;
import com.example.sharecipe.services.DataServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class home extends Fragment {
    private RecyclerView recyclerView;
    private RecyclerView recyclerViewUserRecipes;
    private ArrayList<Recipe> apiRecipes;
    private ArrayList<Recipe> userRecipes;
    private DatabaseReference databaseReference;

    private RecipeAdapter apiRecipeAdapter;
    private RecipeAdapter userRecipeAdapter;

    private ArrayList<Recipe> filteredApiRecipes;
    private ArrayList<Recipe> filteredUserRecipes;
    private EditText searchEditText;
    private Button searchButton, filterFavoritesButton, searchInInternet;
    private boolean showingFavorites = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);
        filterFavoritesButton = view.findViewById(R.id.filterfavorites);
        searchInInternet = view.findViewById(R.id.searchInInternetButton);
        apiRecipes = new ArrayList<>();
        userRecipes = new ArrayList<>();
        filteredApiRecipes = new ArrayList<>();
        filteredUserRecipes = new ArrayList<>();

        Button btnAddRecipe = view.findViewById(R.id.btnAddRecipe);
        btnAddRecipe.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_addRecipe)
        );

        searchInInternet.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("search_query", query);
                bundle.putBoolean("from_home", true);
                Navigation.findNavController(v).navigate(R.id.action_home_to_searchInInternet, bundle);
            }
        });


        recyclerView = view.findViewById(R.id.recipes_recycler_view);
        LinearLayoutManager layoutManagerApi = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManagerApi);
        DataServices dataServices = new DataServices();
        apiRecipes = dataServices.getAllRecipes();
        filteredApiRecipes.addAll(apiRecipes);
        apiRecipeAdapter = new RecipeAdapter(filteredApiRecipes, requireContext());
        recyclerView.setAdapter(apiRecipeAdapter);

        recyclerViewUserRecipes = view.findViewById(R.id.your_recipes_recycler_view);
        LinearLayoutManager layoutManagerUser = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewUserRecipes.setLayoutManager(layoutManagerUser);
        databaseReference = FirebaseDatabase.getInstance().getReference("user_recipe");
        FragmentActivity activity = getActivity();
        String username = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("username", null);
        String safeUsername = username.replace(".", ",");
        databaseReference.child(safeUsername).child("recipes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userRecipes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        userRecipes.add(recipe);
                    }
                }
                filteredUserRecipes.clear();
                filteredUserRecipes.addAll(userRecipes);

                if (userRecipeAdapter == null) {
                    userRecipeAdapter = new RecipeAdapter(filteredUserRecipes, requireContext());
                    recyclerViewUserRecipes.setAdapter(userRecipeAdapter);
                } else {
                    userRecipeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String errorMessage = databaseError.getMessage();
                Log.e("FirebaseError", "Error: " + errorMessage);
                Toast.makeText(requireContext(), "Failed to load recipes: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        searchButton.setOnClickListener(v -> performSearch());
        filterFavoritesButton.setOnClickListener(v -> {
            showingFavorites = !showingFavorites;

            if (showingFavorites) {
                filterFavoritesButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled));
                showFavoritesOnly();
            } else {
                filterFavoritesButton.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart));
                showAllRecipes();
            }
        });

        return view;
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        filteredApiRecipes.clear();
        for (Recipe recipe : apiRecipes) {
            if (recipe.getStrMeal().toLowerCase().contains(query))
            {
                filteredApiRecipes.add(recipe);
            }
        }
        filteredUserRecipes.clear();
        for (Recipe recipe : userRecipes) {
            if (recipe.getStrMeal().toLowerCase().contains(query))
            {
                filteredUserRecipes.add(recipe);
            }
        }
        apiRecipeAdapter.notifyDataSetChanged();
        userRecipeAdapter.notifyDataSetChanged();
        if (filteredApiRecipes.isEmpty() && filteredUserRecipes.isEmpty()) {
            Toast.makeText(requireContext(), "No recipes found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFavoritesOnly() {
        FragmentActivity activity = getActivity();
        String username = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("username", null);
        String safeUsername = username != null ? username.replace(".", ",") : "";

        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(safeUsername);

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                filteredUserRecipes.clear();
                filteredApiRecipes.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String recipeId = snapshot.getKey();
                    for (Recipe recipe : userRecipes) {
                        if (recipe.getIdMeal().equals(recipeId)) {
                            filteredUserRecipes.add(recipe);
                            break;
                        }
                    }
                    for (Recipe recipe : apiRecipes) {
                        if (recipe.getIdMeal().equals(recipeId)) {
                            filteredApiRecipes.add(recipe);
                            break;
                        }
                    }
                }
                userRecipeAdapter.notifyDataSetChanged();
                apiRecipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Favorites", "Error loading favorites: " + databaseError.getMessage());
                Toast.makeText(requireContext(), "Error loading favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAllRecipes() {
        filteredUserRecipes.clear();
        filteredUserRecipes.addAll(userRecipes);
        userRecipeAdapter.notifyDataSetChanged();

        filteredApiRecipes.clear();
        filteredApiRecipes.addAll(apiRecipes);
        apiRecipeAdapter.notifyDataSetChanged();
    }
}
