package com.example.sharecipe.services;

import android.os.StrictMode;

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

public class DataServices {
    ArrayList<Recipe> arrRecipes;

    public DataServices() {
        this.arrRecipes = new ArrayList<>();
    }

    public ArrayList<Recipe> getAllRecipes() {
        URL url;
        String sUrl = "https://www.themealdb.com/api/json/v1/1/search.php?s=";
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
