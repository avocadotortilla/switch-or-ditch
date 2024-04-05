package com.example.switchorditch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
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

public class SignUp extends AppCompatActivity {

    EditText textFName, textSName, textEmail, textPassword;
    Button btnSignUp;
    ImageButton btnBack;
    SearchView srchViewFindItems;
    SharedPreferences preferences;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.signup);

        textFName = findViewById(R.id.edtFirstName);
        textSName = findViewById(R.id.edtLastName);
        textEmail = findViewById(R.id.edtUsername);
        textPassword=  findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btn);
        btnBack = findViewById(R.id.btnBack);
        srchViewFindItems = findViewById(R.id.srchViewFindItems);

        preferences = getSharedPreferences("UserInfo", 0);
        btnSignUp.setOnClickListener(v -> signUp());
        btnBack.setOnClickListener(v -> finish());


    }

    private void signUp(){

        String fName = textFName.getText().toString();
        String sName = textSName.getText().toString();
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();

        if(validation(email, password, fName, sName).equals("Continue")){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/sign_up.php")).newBuilder();

            //creates a requestbody th@ will be used to do a POST request
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email", email)
                    .addFormDataPart("pword", password)
                    .addFormDataPart("fname", fName)
                    .addFormDataPart("sname", sName)
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
                    runOnUiThread(() -> Toast.makeText(SignUp.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    /**/
                    String result = Objects.requireNonNull(response.body()).string();

                    if (!result.equals("This account already exists. Please use the login page.")){
                        try {
                            JSONArray jsonArray = new JSONArray(result);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            SessionManager sessionManager = new SessionManager(SignUp.this);
                            sessionManager.createLoginSession(jsonObject.getString("USER_ID"));
                            runOnUiThread(() -> Toast.makeText(SignUp.this, "Sign up successful!", Toast.LENGTH_SHORT).show());
                            Intent intent = new Intent(SignUp.this, ProductListPage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        runOnUiThread(() -> Toast.makeText(SignUp.this, result, Toast.LENGTH_SHORT).show());
                    }
                }
            });
        }
        else{
            Toast.makeText(this, validation(email, password, fName, sName), Toast.LENGTH_SHORT).show();
        }
    }
    private String validation(String email, String password, String fName, String sName){
        if(fName.equals("")){
            return "Please enter your first name.";
        }
        else if(sName.equals("")){
            return "Please enter your surname.";
        }
        else if (email.equals("")){
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
