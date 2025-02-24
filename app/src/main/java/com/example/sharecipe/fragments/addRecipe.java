package com.example.sharecipe.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sharecipe.Adapter.IngredientAdapter;
import com.example.sharecipe.R;
import com.example.sharecipe.models.Ingredient;
import com.example.sharecipe.models.MyData;
import com.example.sharecipe.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class addRecipe extends Fragment {

    private Recipe recipe;
    private boolean isEditing = false;
    private EditText etRecipeName, etRecipeArea, etInstructions;
    private Spinner spinnerCategory;
    private Button btnAddIngredient;
    private RecyclerView recyclerView;
    private ImageButton ibRecipeImage;
    private Button btnSaveRecipe;
    private IngredientAdapter ingredientAdapter;
    private List<Ingredient> ingredientList = new ArrayList<>();
    private Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference databaseReference;

    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    ibRecipeImage.setImageURI(imageUri);
                }
            }
    );

    public addRecipe() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);


        etRecipeName = view.findViewById(R.id.etRecipeName);
        etRecipeArea = view.findViewById(R.id.etRecipeArea);
        etInstructions = view.findViewById(R.id.etInstructions);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnAddIngredient = view.findViewById(R.id.btnAddIngredient);
        recyclerView = view.findViewById(R.id.rvIngredients);
        ibRecipeImage = view.findViewById(R.id.ibRecipeImage);
        btnSaveRecipe = view.findViewById(R.id.btnSaveRecipe);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        setupCategorySpinner();
        setupIngredientList();
        setupAddIngredientButton();
        setupImagePicker();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                        }
                    }
                }
        );
        if (getArguments() != null && getArguments().containsKey("recipe")) {
            recipe = (Recipe) getArguments().getSerializable("recipe");
            isEditing = true;
            if (recipe != null) {
                preFillForm(recipe);
            }
        }
        btnSaveRecipe.setOnClickListener(v -> {
            String recipeName = etRecipeName.getText().toString().trim();
            String area = etRecipeArea.getText().toString().trim();
            String instructions = etInstructions.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            databaseReference = FirebaseDatabase.getInstance().getReference("user_recipe");
            boolean isValid = true;

            if (TextUtils.isEmpty(recipeName)) {
                etRecipeName.setError("Recipe name is required");
                isValid = false;
            }

            if (TextUtils.isEmpty(area)) {
                etRecipeArea.setError("Area is required");
                isValid = false;
            }

            if (TextUtils.isEmpty(instructions)) {
                etInstructions.setError("Instructions are required");
                isValid = false;
            }

            if (TextUtils.isEmpty(category)) {
                spinnerCategory.setTop(0);
                isValid = false;
            }

            if (ingredientList == null || ingredientList.isEmpty()) {
                showToast("Ingredients are required");
                isValid = false;
            }

            if (imageUri == null) {
                showToast("Image is required");
                isValid = false;
            }

            if(isValid)
            {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    String username = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            .getString("username", null);

                    if (username != null) {
                        if (isEditing) {
                            // Update existing recipe
                            recipe.setStrMeal(recipeName);
                            recipe.setStrArea(area);
                            recipe.setStrInstructions(instructions);
                            recipe.setStrCategory(category);
                            List<String> ingredients = new ArrayList<>();
                            List<String> measurements = new ArrayList<>();

                            for (Ingredient ingredient : ingredientList) {
                                ingredients.add(ingredient.getName());
                                double quantity = ingredient.getQuantity();
                                String measurement = ingredient.getMeasurement();
                                measurements.add(quantity + measurement);
                            }

                            recipe.setIngredients(ingredients);
                            recipe.setMeasurements(measurements);
                            recipe.setStrMealThumb((imageUri != null) ? imageUri.toString() : null);
                            updateRecipeDetails(username, recipe);
                        } else {
                            saveRecipeToFirebase(username, recipeName, area, instructions, category, ingredientList,imageUri);
                        }

                    } else {
                        showToast("User is not logged in");
                    }
                }
            }
        });

        return view;
    }

    private void preFillForm(Recipe recipe) {
        etRecipeName.setText(recipe.getStrMeal());
        etRecipeArea.setText(recipe.getStrArea());
        etInstructions.setText(recipe.getStrInstructions());

        ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
        if (categoryAdapter != null) {
            int categoryPosition = categoryAdapter.getPosition(recipe.getStrCategory());
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition);
            }
        }

        ingredientList.clear();
        List<String> recipeIngredients = recipe.getIngredients();
        List<String> recipeMeasurements = recipe.getMeasurements();

        if (recipeIngredients != null && recipeMeasurements != null && recipeIngredients.size() == recipeMeasurements.size()) {
            for (int i = 0; i < recipeIngredients.size(); i++) {
                String ingredientName = recipeIngredients.get(i);
                String measurementString = recipe.getMeasurements().get(i);
                float quantity = 0;
                String measurement = "";

                try {
                    String[] parts = measurementString.split(" ");
                    if (parts.length > 0) {
                        quantity = Float.parseFloat(parts[0]);
                        if (parts.length > 1) {
                            measurement = parts[1];
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e("Ingredient Parsing", "Error parsing quantity: " + measurementString, e);
                }

                ingredientList.add(new Ingredient(ingredientName, quantity, measurement));
            }
            ingredientAdapter.notifyDataSetChanged();
        } else {
            Log.w("Ingredients", "Ingredients or measurements are null or have different sizes.");
        }

        String imageUrl = recipe.getStrMealThumb();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .into(ibRecipeImage);
            } catch (Exception e) {
                Log.e("Image Loading", "Error loading image: " + imageUrl, e);
            }
        }
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                MyData.recipeCategories
        );
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupIngredientList() {
        ingredientAdapter = new IngredientAdapter(getContext(), ingredientList, position -> {

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(ingredientAdapter);
    }


    private void setupAddIngredientButton() {
        btnAddIngredient.setOnClickListener(v -> showIngredientDialog());
    }

    private void setupImagePicker() {
        ibRecipeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    private void showIngredientDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_ingredient);

        Spinner spinnerIngredients = dialog.findViewById(R.id.spinnerIngredients);
        EditText etQuantity = dialog.findViewById(R.id.etQuantity);
        Spinner spinnerMeasurement = dialog.findViewById(R.id.spinnerMeasurement);
        Button btnAddFromList = dialog.findViewById(R.id.btnAddFromList);
        Button btnAddCustomIngredient = dialog.findViewById(R.id.btnAddCustomIngredient);

        List<String> ingredientList = new ArrayList<>(Arrays.asList(MyData.commonIngredients));

        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                ingredientList
        );
        spinnerIngredients.setAdapter(ingredientAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            FragmentActivity activity = getActivity();
            String username = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("username", null);

            if (username != null) {
                String safeUsername = username.replace(".", ",");
                DatabaseReference userIngredientsRef = FirebaseDatabase.getInstance()
                        .getReference("user_ingredient")
                        .child(safeUsername)
                        .child("ingredient");

                Log.d("Firebase", "Fetching ingredients from: " + userIngredientsRef.toString());

                userIngredientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.d("Firebase", "No ingredients found for user.");
                            return;
                        }

                        boolean isDataUpdated = false;
                        for (DataSnapshot ingredientSnapshot : snapshot.getChildren()) {
                            String ingredient = ingredientSnapshot.getValue(String.class);
                            Log.d("Firebase", "Loaded Ingredient: " + ingredient);

                            if (ingredient != null && !ingredientList.contains(ingredient)) {
                                ingredientList.add(ingredient);
                                isDataUpdated = true;
                            }
                        }

                        if (isDataUpdated) {
                            Log.d("Firebase", "Updated Ingredient List: " + ingredientList.toString());
                            ingredientAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error loading user ingredients", error.toException());
                    }
                });
            } else {
                Log.e("Firebase", "Username is null! Data path might be incorrect.");
            }
        } else {
            Log.e("Firebase", "User is not logged in.");
        }

        ArrayAdapter<String> measurementAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"ml", "L", "tsp", "cup", "g"}
        );
        spinnerMeasurement.setAdapter(measurementAdapter);

        btnAddFromList.setOnClickListener(v -> {
            String ingredient = spinnerIngredients.getSelectedItem().toString();
            String quantity = etQuantity.getText().toString();
            String measurement = spinnerMeasurement.getSelectedItem().toString();

            if (!quantity.isEmpty()) {
                addIngredientToList(ingredient, quantity, measurement);
                dialog.dismiss();
            } else {
                etQuantity.setError("Please Enter Quantity");
            }
        });

        btnAddCustomIngredient.setOnClickListener(v -> showCustomIngredientDialog(dialog));

        dialog.show();
    }




    private void showCustomIngredientDialog(Dialog parentDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add new Ingredient");

        View customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_new_ingredient, null);
        EditText etCustomIngredient = customView.findViewById(R.id.etNewIngredient);
        EditText etCustomQuantity = customView.findViewById(R.id.etQuantityNewIngredient);
        Spinner spinnerCustomMeasurement = customView.findViewById(R.id.spinnerCustomMeasurement);

        ArrayAdapter<String> measurementAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"ml", "L", "tsp", "cup", "g"}
        );
        spinnerCustomMeasurement.setAdapter(measurementAdapter);

        builder.setView(customView);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String customIngredient = etCustomIngredient.getText().toString();
            String customQuantity = etCustomQuantity.getText().toString();
            String customMeasurement = spinnerCustomMeasurement.getSelectedItem().toString();
            addIngredientToList(customIngredient, customQuantity, customMeasurement);
            FragmentActivity activity = getActivity();
            String username = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("username", null);
            saveingredientToFirebase(username,customIngredient);
            parentDialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void addIngredientToList(String ingredient, String quantity, String measurement) {
        ingredientList.add(new Ingredient(ingredient, (float) Double.parseDouble(quantity), measurement));
        ingredientAdapter.notifyDataSetChanged();
    }

    private void saveingredientToFirebase(String username, String ingredientName)
    {
        String safeUsername = username.replace(".", ",");
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().
                getReference("user_ingredient")
                .child(safeUsername)
                .child("ingredient")
                .child(ingredientName);

        recipeRef.setValue(ingredientName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "ingredient saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save ingredient.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecipeDetails(String username, Recipe recipe) {
        String safeUsername = username.replace(".", ",");
        DatabaseReference recipeRef = databaseReference.child(safeUsername).child("recipes").child(recipe.getIdMeal());

        recipeRef.setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Recipe updated successfully!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_addRecipe_to_home); // Or wherever you navigate after update
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseDatabase", "Failed to update recipe", e);
                    Toast.makeText(getContext(), "Failed to update recipe.", Toast.LENGTH_SHORT).show();
                });
    }
    private void saveRecipeToFirebase(String username, String recipeName, String area, String instructions, String category, List<Ingredient> ingredientList, Uri imageUri) {
        String idMeal = generateUniqueId();
        String strMeal = recipeName;
        String strCategory = category;
        String strArea = area;
        String strInstructions = instructions;
        String strMealThumb = (imageUri != null) ? imageUri.toString() : null;
        String strSource = "User";
        List<String> ingredients = new ArrayList<>();
        List<String> measurements = new ArrayList<>();

        for (Ingredient ingredient : ingredientList) {
            ingredients.add(ingredient.getName());
            double quantity = ingredient.getQuantity();
            String measurement = ingredient.getMeasurement();
            measurements.add(quantity + measurement);
        }


        Recipe recipe = new Recipe(idMeal, strMeal, strCategory, strArea, strInstructions, strMealThumb, strSource, ingredients, measurements);

        String safeUsername = username.replace(".", ",");
        DatabaseReference recipeRef = databaseReference.child(safeUsername).child("recipes").child(idMeal);

        recipeRef.setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_addRecipe_to_home);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save recipe.", Toast.LENGTH_SHORT).show();
                });
    }

    public String generateUniqueId() {
        UUID uniqueKey = UUID.randomUUID();
        return uniqueKey.toString();
    }

}
