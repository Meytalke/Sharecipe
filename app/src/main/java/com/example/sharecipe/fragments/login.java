package com.example.sharecipe.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import static com.example.sharecipe.R.*;

import com.example.sharecipe.activities.MainActivity;
import com.example.sharecipe.R;

public class login extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public login() {
    }

    public static login newInstance(String param1, String param2) {
        login fragment = new login();
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
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button registrationButtonInLogin = view.findViewById(R.id.registrationButtonInLoginPage);
        registrationButtonInLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_registration)
        );

        Button login = view.findViewById(R.id.LogInButton);
        login.setOnClickListener(v -> {
            EditText userNameField = view.findViewById(R.id.TextUserNameAddressLogin);
            EditText passwordField = view.findViewById(R.id.TextPasswordLogin);

            String userName = userNameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            boolean isValid = true;

            if (TextUtils.isEmpty(userName)) {
                userNameField.setError("Email is required");
                isValid = false;
            }

            if (TextUtils.isEmpty(password)) {
                passwordField.setError("Password is required");
                isValid = false;
            }

            FragmentActivity activity = getActivity();
            if (isValid && activity != null) {
                activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("username", userName)
                        .apply();

                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).login(userName, password, v);
                }
            }
        });

        return view;
    }
}
