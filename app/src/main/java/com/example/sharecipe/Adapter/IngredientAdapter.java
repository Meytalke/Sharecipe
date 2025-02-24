package com.example.sharecipe.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharecipe.R;
import com.example.sharecipe.models.Ingredient;

import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
    private Context context;
    private List<Ingredient> ingredientList;
    private OnIngredientDeleteListener deleteListener;

    public interface OnIngredientDeleteListener {
        void onDelete(int position);
    }

    public IngredientAdapter(Context context, List<Ingredient> ingredientList, OnIngredientDeleteListener deleteListener) {
        this.context = context;
        this.ingredientList = ingredientList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);

        holder.tvIngredientName.setText(ingredient.getName());
        holder.tvQuantity.setText(String.valueOf(ingredient.getQuantity()));
        holder.tvMeasurement.setText(ingredient.getMeasurement());

        holder.btnDeleteIngredient.setOnClickListener(v -> {
            if (deleteListener != null && position >= 0 && position < ingredientList.size()) {
                deleteListener.onDelete(position);
                ingredientList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, ingredientList.size());
            }
        });
    }


    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredientName, tvQuantity, tvMeasurement;
        ImageButton btnDeleteIngredient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvMeasurement = itemView.findViewById(R.id.tvMeasurement);
            btnDeleteIngredient = itemView.findViewById(R.id.btnDeleteIngredient);
        }
    }

    public void addIngredient(Ingredient ingredient) {
        ingredientList.add(ingredient);
        notifyItemInserted(ingredientList.size() - 1);
    }
}

