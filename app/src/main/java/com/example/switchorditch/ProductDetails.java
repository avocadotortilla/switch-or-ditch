package com.example.switchorditch;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProductDetails extends AppCompatActivity {
    TextView productName, productPrice, sellerName, AvgRating, productDescription;
    Button toRate;
    ImageView image;
    RatingBar stars;
    boolean Rated;
    String Result, productID, txtAvgRating;
    JSONObject jo;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_product_details);

        toRate = findViewById(R.id.btToRate);
        image = findViewById(R.id.photo);
        stars = findViewById(R.id.ratingBar);
        productName = findViewById(R.id.tvProductName);
        sellerName = findViewById(R.id.tvSellerName);
        productPrice = findViewById(R.id.tvProductPrice);
        AvgRating = findViewById(R.id.tvAvgRating);
        productDescription = findViewById(R.id.tvProductDescription);

        SessionManager sessionManager = new SessionManager(this);
        HashMap<String, String> Username = sessionManager.getUsernameFromSession();
        String username = Username.get(SessionManager.KEY_USERNAME);

        productName.setText(getIntent().getStringExtra("productName"));
        productPrice.setText(getIntent().getStringExtra("price"));
        sellerName.setText(getIntent().getStringExtra("sellerName"));

        try {
            jo = new JSONObject(getIntent().getStringExtra("jsonObject"));
            txtAvgRating = jo.getString("PROD_AVG_RATING");
            productDescription.setText(jo.getString("PROD_DESC"));
            productID = jo.getString("PROD_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (txtAvgRating.equals("0.00")) {
            AvgRating.setText("Not yet Rated");
        } else {
            AvgRating.setText("Avg Rating: \n" + txtAvgRating);
        }

        hasRated(username, productID);

        byte[] byteArrayExtra = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length);
        image.setImageBitmap(bmp);

        if (Rated) {
            stars.setFocusable(false);
            stars.setOnTouchListener((v1, event) -> true);
            toRate.setText("Rated!");
        } else {

            toRate.setOnClickListener(v -> {
                double rating = stars.getRating();

                OkHttpClient client = new OkHttpClient();
                HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/rate_product.php")).newBuilder();

                assert username != null;
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uname", username)
                        .addFormDataPart("prodid", productID)
                        .addFormDataPart("rating", String.valueOf(rating))
                        .build();

                String url = urlBuilder.build().toString();

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //tells user to check Internet connection if the request fails
                        runOnUiThread(() -> Toast.makeText(ProductDetails.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        /**/
                        String result = Objects.requireNonNull(response.body()).string();
                        runOnUiThread(() -> Toast.makeText(ProductDetails.this, result, Toast.LENGTH_SHORT).show());

                        System.out.println(result);
                        Rated = result.equals("You have already rated this product.");
                        System.out.println(Rated);
                    }
                });
            });
        }
    }

        public void hasRated (String username, String productID){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/verify_rating.php")).newBuilder();


            assert username != null;
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uname", username)
                    .addFormDataPart("prodid", productID)
                    .build();

            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    //tells user to check Internet connection if the request fails
                    runOnUiThread(() -> Toast.makeText(ProductDetails.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    /**/
                    String result = Objects.requireNonNull(Objects.requireNonNull(response.body()).string());

                    // runOnUiThread(() -> Toast.makeText(ProductDetails.this, result, Toast.LENGTH_SHORT).show());
                    System.out.println("The result was: " + result);
                    Rated = result.equals("true");
                    Result = result;
                }
            });
            System.out.println("Result[0] was: " + Result);
            System.out.println("This user has rated: " + Rated);
        }
}