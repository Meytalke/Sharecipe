package com.example.sharecipe.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.sharecipe.Adapter.RecipeAdapter;
import com.example.sharecipe.R;
import com.example.sharecipe.ViewModel.SearchInInternetViewModel;
import com.example.sharecipe.models.Recipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class searchInInternet extends Fragment {

    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private static final String API_URL = "https://www.themealdb.com/api/json/v1/1/search.php?s=";

    private SearchInInternetViewModel viewModel;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public searchInInternet() {
    }

    public static searchInInternet newInstance(String param1, String param2) {
        searchInInternet fragment = new searchInInternet();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_in_internet, container, false);
        btnBack = view.findViewById(R.id.btn_back_internet);
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_searchInInternet_to_home);
        });

        viewModel = new ViewModelProvider(requireActivity()).get(SearchInInternetViewModel.class);
        recyclerView = view.findViewById(R.id.Internet_search_results_recycler_view);
        LinearLayoutManager layoutManagerApi = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManagerApi);

        viewModel.getRecipeList().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                adapter = new RecipeAdapter(new ArrayList<>(recipes), requireContext());
                recyclerView.setAdapter(adapter);
            }
        });

        Bundle args = getArguments();
        if (args != null) {
            String query = args.getString("search_query", "");
            boolean fromHome = args.getBoolean("from_home", false);

            if (!query.isEmpty()) {
                viewModel.setSearchQuery(query);

                if (fromHome) {
                    viewModel.setRecipeList(new ArrayList<>());
                    getAllRecipes(query);
                } else if (viewModel.getRecipeList().getValue() == null || viewModel.getRecipeList().getValue().isEmpty()) {
                    getAllRecipes(query);
                }
            }
        }

        return view;
    }



    private void updateAdapter() {
        if (viewModel.getRecipeList().getValue() != null && !viewModel.getRecipeList().getValue().isEmpty()) {
            adapter = new RecipeAdapter(new ArrayList<>(viewModel.getRecipeList().getValue()), requireContext());
            recyclerView.setAdapter(adapter);
        }
    }

    private class FetchRecipesTask extends AsyncTask<String, Void, List<Recipe>> {
        @Override
        protected List<Recipe> doInBackground(String... params) {
            String query = params[0];
            return getRecipesFromApi(query);
        }

        @Override
        protected void onPostExecute(List<Recipe> recipes) {
            super.onPostExecute(recipes);
            if (recipes != null) {
                viewModel.setRecipeList(recipes);
            }
        }
    }

    public void getAllRecipes(String query) {
        new FetchRecipesTask().execute(query);
    }

    public List<Recipe> getRecipesFromApi(String query) {
        URL url;
        String sUrl = API_URL + query;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            url = new URL(sUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection request = null;
        ArrayList<Recipe> arrRecipes = new ArrayList<>();

        try {
            request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject rootObject = root.getAsJsonObject();
            JsonArray rootArray = rootObject.getAsJsonArray("meals");

            for (JsonElement je : rootArray) {
                JsonObject obj = je.getAsJsonObject();

                String idMeal = obj.has("idMeal") && !obj.get("idMeal").isJsonNull() ? obj.get("idMeal").getAsString() : "";
                String strMeal = obj.has("strMeal") && !obj.get("strMeal").isJsonNull() ? obj.get("strMeal").getAsString() : "";
                String strCategory = obj.has("strCategory") && !obj.get("strCategory").isJsonNull() ? obj.get("strCategory").getAsString() : "";
                String strArea = obj.has("strArea") && !obj.get("strArea").isJsonNull() ? obj.get("strArea").getAsString() : "";
                String strInstructions = obj.has("strInstructions") && !obj.get("strInstructions").isJsonNull() ? obj.get("strInstructions").getAsString() : "";
                String strMealThumb = obj.has("strMealThumb") && !obj.get("strMealThumb").isJsonNull() ? obj.get("strMealThumb").getAsString() : "";
                String strSource = obj.has("strSource") && !obj.get("strSource").isJsonNull() ? obj.get("strSource").getAsString() : "";

                List<String> ingredients = new ArrayList<>();
                List<String> measurements = new ArrayList<>();

                for (int i = 1; i <= 20; i++) {
                    String ingredientKey = "strIngredient" + i;
                    String measurementKey = "strMeasure" + i;

                    String ingredient = obj.has(ingredientKey) && !obj.get(ingredientKey).isJsonNull() ? obj.get(ingredientKey).getAsString().trim() : "";
                    String measurement = obj.has(measurementKey) && !obj.get(measurementKey).isJsonNull() ? obj.get(measurementKey).getAsString().trim() : "";

                    if (!ingredient.isEmpty()) {
                        ingredients.add(ingredient);
                        measurements.add(measurement);
                    }
                }

                arrRecipes.add(new Recipe(idMeal, strMeal, strCategory, strArea, strInstructions, strMealThumb, strSource, ingredients, measurements));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return arrRecipes;
    }
}