package com.example.switchorditch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity{

    EditText textEmail, textPassword;
    Button btnLogin;
    ImageButton btnBack;
    JSONArray namePasswordArray;
    String fName, sName;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.login);

        //initialising UI components
        textEmail = findViewById(R.id.edtUsername);
        textPassword=  findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        //does the login things
        btnLogin.setOnClickListener(v -> login());
        btnBack.setOnClickListener(v -> finish());
    }

    private void login(){
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();


        if(validation(email, password).equals("Continue")){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/login.php")).newBuilder();

            //creates a requestbody th@ will be used to do a POST request
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", email)
                    .addFormDataPart("pword", password)
                    .build();

            String url = urlBuilder.build().toString();

            //builds a request th@ will do a POST request, sending the info in body to the URL named "url"
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //tells user to check Internet connection if the request fails
                    runOnUiThread(() -> Toast.makeText(Login.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = Objects.requireNonNull(response.body()).string();
                    //if the username is not in the database
                    if (result.equals("[]")){
                        runOnUiThread(() -> Toast.makeText(Login.this, "Incorrect email or password.", Toast.LENGTH_SHORT).show());
                    }
                    else {
                        try {
                            namePasswordArray = new JSONArray(result);
                            JSONObject namePasswordObject = namePasswordArray.getJSONObject(0);
                            if (isPasswordCorrect(password, namePasswordObject)) {
                                runOnUiThread(() -> Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show());

                                SessionManager sessionManager = new SessionManager(Login.this);
                                sessionManager.createLoginSession(namePasswordObject.getString("USER_ID"));
                                Intent teleport = new Intent(Login.this, ProductListPage.class);
                                teleport.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(teleport);
                            }
                            else{
                                runOnUiThread(() -> Toast.makeText(Login.this, "The username or password is incorrect.", Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
        //if something needs to be filled
        else{
            Toast.makeText(this, validation(email, password), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPasswordCorrect(String password, JSONObject namePasswordObject) throws JSONException {
        fName = namePasswordObject.getString("USER_FNAME");
        sName = namePasswordObject.getString("USER_LNAME");
        return namePasswordObject.getString("USER_PASSWD").equals(password);
    }


    private String validation(String email, String password){
        if (email.equals("")){
            return "Please enter your email address.";
        }
        else if(password.equals("")){
            return "Please enter your password.";
        }
        else if (textPassword.length() < 8){
            return "Password must be a minimum of 8 characters.";
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return "Invalid email address.";
        }
        else{
            return "Continue";
        }
    }
}
