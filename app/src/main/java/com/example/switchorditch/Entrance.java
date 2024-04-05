package com.example.switchorditch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class Entrance extends AppCompatActivity {

    Button goToLogin, goToSignUp;
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        SessionManager sessionManager = new SessionManager(this);
        if(sessionManager.checkLogin()){
            Intent intent = new Intent(Entrance.this, ProductListPage.class);
            startActivity(intent);
            finish();
        }

        //all the following code will only run if preferences is empty
        setContentView(R.layout.entrance);

        goToLogin = findViewById(R.id.btnLoginScreen);
        goToSignUp = findViewById(R.id.btnSignUpScreen);

        goToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Entrance.this, Login.class);
            startActivity(intent);
        });

        goToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Entrance.this, SignUp.class);
            startActivity(intent);
        });
    }
}
