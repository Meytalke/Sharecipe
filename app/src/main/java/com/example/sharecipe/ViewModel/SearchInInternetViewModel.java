package com.example.sharecipe.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.example.sharecipe.models.Recipe;
import java.util.ArrayList;
import java.util.List;

public class SearchInInternetViewModel extends ViewModel {
    private SavedStateHandle savedStateHandle;

    public SearchInInternetViewModel(SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
    }

    public LiveData<String> getSearchQuery() {
        return savedStateHandle.getLiveData("searchQuery", "");
    }

    public void setSearchQuery(String query) {
        savedStateHandle.set("searchQuery", query);
    }

    public LiveData<List<Recipe>> getRecipeList() {
        return savedStateHandle.getLiveData("recipeList", new ArrayList<>());
    }

    public void setRecipeList(List<Recipe> recipes) {
        savedStateHandle.set("recipeList", recipes);
    }
}

