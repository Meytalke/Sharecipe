package com.example.sharecipe.Adapter;

import static com.google.android.material.internal.ContextUtils.getActivity;

import com.bumptech.glide.Glide;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharecipe.R;
import com.example.sharecipe.models.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.MyViewHolder> {

    private ArrayList<Recipe> recipes;
    private Context context;
    private DatabaseReference databaseReference;
    String username;
    public RecipeAdapter(ArrayList<Recipe> recipes, Context context) {
        this.recipes = recipes;
        this.context = context;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textName;
        ImageView image;
        ImageButton btnLike, btnDetails,btnEdit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.recipe_name);
            image = itemView.findViewById(R.id.recipe_image);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnDetails = itemView.findViewById(R.id.btn_details);
            btnEdit = itemView.findViewById(R.id.btn_edit);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_card, parent, false);
        databaseReference = FirebaseDatabase.getInstance().getReference("user_recipe");
        return new MyViewHolder(view);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        String recipeId = recipe.getIdMeal(); // Assuming you have an ID field in your Recipe object
        username = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("username", null);

        String safeUsername = username != null ? username.replace(".", ",") : ""; // Handle null case
        if (isRecipeOwner(recipe)) {
            holder.btnEdit.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
        }

        holder.textName.setText(recipe.getStrMeal());

        Glide.with(context)
                .load(recipe.getStrMealThumb())
                .into(holder.image);

        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(safeUsername);

        if (recipeId != null) {
            favoritesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                        holder.btnLike.setSelected(true); // Set the selected state as well
                    } else {
                        holder.btnLike.setImageResource(R.drawable.ic_heart);
                        holder.btnLike.setSelected(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Favorites", "Error checking favorites: " + databaseError.getMessage());
                }
            });
        }
        holder.btnLike.setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                if (recipeId != null) {
                    favoritesRef.child(recipeId).setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to add to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                v.setSelected(false);
                                holder.btnLike.setImageResource(R.drawable.ic_heart);
                            });

                    NavController navController = Navigation.findNavController(v);
                    int currentDestinationId = navController.getCurrentDestination().getId();

                    if (currentDestinationId == R.id.searchInInternet)
                    {
                        String safeUsername1 = username.replace(".", ",");
                        DatabaseReference recipeRef = databaseReference.child(safeUsername1).child("recipes").child(recipe.getIdMeal());

                        recipeRef.setValue(recipe)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Recipe saved successfully to your list!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to save recipe.", Toast.LENGTH_SHORT).show();
                                });
                    }
                }

            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart);
                if (recipeId != null) {
                    favoritesRef.child(recipeId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to remove from favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                v.setSelected(true);
                                holder.btnLike.setImageResource(R.drawable.ic_heart_filled);
                            });
                }
            }
        });


        holder.btnDetails.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("recipe", recipe);

            NavController navController = Navigation.findNavController(v);
            int currentDestinationId = navController.getCurrentDestination().getId();

            if (currentDestinationId == R.id.home) {
                bundle.putString("previousDestination", "home");
                bundle.putBoolean("from_home", true);
                navController.navigate(R.id.action_home_to_recipeDetails, bundle);
            } else if (currentDestinationId == R.id.searchInInternet) {
                bundle.putString("previousDestination", "searchInInternet");
                bundle.putBoolean("from_home", false);
                navController.navigate(R.id.action_searchInInternet_to_recipeDetails, bundle);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("recipe", recipe);

            Navigation.findNavController(v).navigate(R.id.action_home_to_addRecipe, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    private boolean isRecipeOwner(Recipe recipe) {
        if(recipe.getStrSource().equals("User"))
        {
            return true;
        }
        return false;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        this.recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

}
