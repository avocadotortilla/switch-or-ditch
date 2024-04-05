package com.example.switchorditch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class LoginActivity extends AppCompatActivity {

    EditText textEmail, textPassword;
    Button login;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        //initialising UI components
        textEmail = findViewById(R.id.edtUsername);
        textPassword=  findViewById(R.id.edtPassword);
        login = findViewById(R.id.btnLogin);


        //does the login things
        login.setOnClickListener(v -> login());

    }

    private void login(){
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();

        if(!(email.equals("")) && !(password.equals(""))){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/login.php").newBuilder();

            //creates a request body th@ will be used to do a POST request
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uname", email)
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
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        /*Lets the user know the request was successful
                        Should this be done by moving to a new activity with a text in this current Toast, and a button th@ will take them
                        back to the home page? Something to think about.*/
                    String result = Objects.requireNonNull(response.body()).string();
                    //if the username is not in the database
                    if (result.equals("[]")){
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "The username or password is incorrect.", Toast.LENGTH_SHORT).show());
                    }
                    else {
                        try {
                            if (isPasswordCorrect(result, password)) {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show());
                                SessionManager sessionManager = new SessionManager(LoginActivity.this);
                                sessionManager.createLoginSession(email);
                                Intent teleport = new Intent(LoginActivity.this, ProductDetails.class);
                                teleport.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(teleport);
                            }
                            else{
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "The username or password is incorrect.", Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
        //if something needs to be filled
        else if(email.equals("")){
            Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPasswordCorrect(String result, String password) throws JSONException {
        JSONArray namePasswordArray = new JSONArray(result);
        JSONObject namePasswordObject = namePasswordArray.getJSONObject(0);
        return namePasswordObject.getString("USER_PASSWD").equals(password);
    }
}