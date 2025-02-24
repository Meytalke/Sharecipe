package com.example.sharecipe.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.sharecipe.R;
import com.example.sharecipe.models.Recipe;

import java.io.Serializable;
import java.util.List;

public class recipeDetails extends Fragment {

    private static final String ARG_RECIPE = "recipe";
    private Recipe recipe;
    private Button btnIngredients, btnSteps, btnGeneral;
    private ImageButton btnBack;
    private LinearLayout ingredientsLayout, stepsLayout, generalInfoLayout;

    public static recipeDetails newInstance(Recipe recipe) {
        recipeDetails fragment = new recipeDetails();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPE, (Serializable) recipe);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
        }
    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_details, container, false);

        ImageView recipeImage = view.findViewById(R.id.details_recipe_image);
        TextView recipeName = view.findViewById(R.id.details_recipe_name);
        TextView category = view.findViewById(R.id.details_recipe_category);
        TextView area = view.findViewById(R.id.details_recipe_area);
        TextView source = view.findViewById(R.id.details_recipe_source);
        source.setText(recipe.getStrSource());
        TextView instructions = view.findViewById(R.id.details_recipe_instructions);

        btnIngredients = view.findViewById(R.id.btn_ingredients);
        btnSteps = view.findViewById(R.id.btn_steps);
        btnGeneral = view.findViewById(R.id.btn_general);
        btnBack = view.findViewById(R.id.btn_back);
        ingredientsLayout = view.findViewById(R.id.layout_ingredients);
        stepsLayout = view.findViewById(R.id.layout_steps);
        generalInfoLayout = view.findViewById(R.id.layout_general_info);
        if (recipe != null) {
            Glide.with(this)
                    .load(recipe.getStrMealThumb())
                    .into(recipeImage);
            //Log.d("IMAGE_URI", recipe.getStrMealThumb());
            recipeName.setText(recipe.getStrMeal());
            category.setText("Category: " + recipe.getStrCategory());
            area.setText("Area: " + recipe.getStrArea());
            if(recipe.getStrSource().contains("User") || recipe.getStrSource().isEmpty())
            {
                source.setText("Source: " + recipe.getStrSource());
            }
            else {
                source.setClickable(true);
                source.setMovementMethod(LinkMovementMethod.getInstance());

                source.setOnClickListener(v -> {
                    String url = recipe.getStrSource();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                });
            }

            instructions.setText(recipe.getStrInstructions());

            List<String> ingredients = recipe.getIngredients();
            List<String> measurements = recipe.getMeasurements();

            if(ingredients != null)
            {
                for (int i = 0; i < ingredients.size(); i++) {
                    TextView ingredientText = new TextView(getContext());
                    ingredientText.setText(measurements.get(i) + " " + ingredients.get(i));
                    ingredientText.setTextSize(16);
                    ingredientText.setPadding(8, 8, 8, 8);

                    ingredientsLayout.addView(ingredientText);
                }
            }
        }

        btnIngredients.setOnClickListener(v -> {
            ingredientsLayout.setVisibility(View.VISIBLE);
            stepsLayout.setVisibility(View.GONE);
            generalInfoLayout.setVisibility(View.GONE);

            btnIngredients.setBackgroundResource(R.drawable.button_background);
            btnSteps.setBackground(null);
            btnGeneral.setBackground(null);
            btnIngredients.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));
            btnSteps.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
            btnGeneral.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
        });

        btnSteps.setOnClickListener(v -> {
            ingredientsLayout.setVisibility(View.GONE);
            stepsLayout.setVisibility(View.VISIBLE);
            generalInfoLayout.setVisibility(View.GONE);

            btnIngredients.setBackground(null);
            btnSteps.setBackgroundResource(R.drawable.button_background);
            btnGeneral.setBackground(null);
            btnIngredients.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
            btnSteps.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));
            btnGeneral.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
        });

        btnGeneral.setOnClickListener(v -> {
            ingredientsLayout.setVisibility(View.GONE);
            stepsLayout.setVisibility(View.GONE);
            generalInfoLayout.setVisibility(View.VISIBLE);

            btnIngredients.setBackground(null);
            btnSteps.setBackground(null);
            btnGeneral.setBackgroundResource(R.drawable.button_background);
            btnIngredients.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
            btnSteps.setTextColor(ContextCompat.getColor(v.getContext(), R.color.black));
            btnGeneral.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));
        });

        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            Bundle args = getArguments();

            if (args != null) {
                String previousDestination = args.getString("previousDestination");

                if ("home".equals(previousDestination)) {
                    navController.navigate(R.id.action_recipeDetails_to_home);
                } else if ("searchInInternet".equals(previousDestination)) {
                    navController.navigate(R.id.action_recipeDetails_to_searchInInternet);
                }
            }
        });

        return view;
    }
}
