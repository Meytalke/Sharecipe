package com.example.sharecipe.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.sharecipe.activities.MainActivity;
import com.example.sharecipe.R;

public class registration extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ImageButton btnBack;

    private String mParam1;
    private String mParam2;

    public registration() {
    }

    public static registration newInstance(String param1, String param2) {
        registration fragment = new registration();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration, container, false);
        EditText userNameField= view.findViewById(R.id.TextUserNameRegistration);
        EditText passwordField= view.findViewById(R.id.TextPasswordRegistration);
        EditText rePasswordField= view.findViewById(R.id.TextRePasswordRegistration);
        EditText phoneField= view.findViewById(R.id.TextPhoneRegistration);
        Button registrationButton=view.findViewById(R.id.RegistrationButton);
        btnBack = view.findViewById(R.id.btn_back_reg);
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = true;

                String userName = userNameField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                String rePassword = rePasswordField.getText().toString().trim();
                String phone = phoneField.getText().toString().trim();

                if (userName.isEmpty()) {
                    userNameField.setError("Email is required");
                    isValid = false;
                }

                if (password.isEmpty()) {
                    passwordField.setError("Password is required");
                    isValid = false;
                }
                if (password.length()<6) {
                    passwordField.setError("Password should be minimum 6");
                    isValid = false;
                }
                if (rePassword.isEmpty()) {
                    rePasswordField.setError("Please re-enter your password");
                    isValid = false;
                }

                if (!password.equals(rePassword)) {
                    rePasswordField.setError("Passwords do not match");
                    isValid = false;
                }

                if (phone.isEmpty()) {
                    phoneField.setError("Phone number is required");
                    isValid = false;
                }

                if (isValid) {
                    MainActivity mainActivity= (MainActivity) getActivity();
                    mainActivity.Registration(userName,password,phone,view);
                }
            }
        });

        btnBack.setOnClickListener(v ->{
            Navigation.findNavController(v).navigate(R.id.action_registration_to_login);

        });
        return view;
    }
}
