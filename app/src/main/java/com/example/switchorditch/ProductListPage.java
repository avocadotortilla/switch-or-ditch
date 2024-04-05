package com.example.switchorditch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProductListPage extends AppCompatActivity {

    FloatingActionButton fabUploadProduct;
    LinearLayout layoutProducts;
    ItemLayout itemLayout;
    String result = "";
    JSONArray jsonArray;
    SearchView srchFindProducts;
    SessionManager sessionManager;

    // add the following to Module gradle: implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    SwipeRefreshLayout srlProducts;
    boolean searched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_product_list_page);
        sessionManager = new SessionManager(ProductListPage.this);

        itemLayout = new ItemLayout(ProductListPage.this);

        //some toolbar stuff, useful for the logout feature
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        srlProducts = findViewById(R.id.srlProducts);
        fabUploadProduct = findViewById(R.id.fabUploadButton);
        //listOfProducts = findViewById(R.id.layoutProducts);
        layoutProducts = findViewById(R.id.layoutProducts);
        srchFindProducts = findViewById(R.id.srchViewFindItems);

        try {
            addProductListViews(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fabUploadProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductListPage.this, Upload.class);
            startActivity(intent);
        });

        srchFindProducts.clearFocus();
        srchFindProducts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                try {
                    layoutProducts.removeAllViews();
                    query = query.toLowerCase();
                    addProductListViews(query);
                    searched = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        srlProducts.setOnRefreshListener(() -> {
            try {
                layoutProducts.removeAllViews();
                addProductListViews("");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            srlProducts.setRefreshing(false);
        });
    }

    //the following two functions are for the Log Out feature
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_for_logout, menu);
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoutMenuItem) {
            sessionManager.logoutUserSession();
            Intent intent = new Intent(ProductListPage.this, Entrance.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(searched){
            searched = false;
            Intent intent = new Intent(ProductListPage.this, ProductListPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void addProductListViews(String query) throws JSONException {
        OkHttpClient clientGetText = new OkHttpClient();

        HttpUrl.Builder urlBuilderGetText = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/retrieve_product.php")).newBuilder();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("searchedstring", query)
                .build();

        String urlGetText = urlBuilderGetText.build().toString();
        Request requestGetText = new Request.Builder()
                .url(urlGetText)
                .post(body)
                .build();

        clientGetText.newCall(requestGetText).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ProductListPage.this, "Please check your Internet connection", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                result = Objects.requireNonNull(response.body()).string();
                ProductListPage.this.runOnUiThread(() -> {
                    try {
                        processJSON(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void processJSON(String json) throws JSONException {

        jsonArray = new JSONArray(json);
        for (int i = jsonArray.length()-1; i > -1; i--){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ItemLayout itemLayout = new ItemLayout(this);
            itemLayout.populateFromJson(jsonObject);
            String urlGetImage = "https://lamp.ms.wits.ac.za/~s2496879/uploads/" + jsonObject.getString("PROD_PIC_NAME");
            Request requestGetImage = new Request.Builder().url(urlGetImage).get().build();
            OkHttpClient client = new OkHttpClient();

            client.newCall(requestGetImage).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.i("Tag","error"+e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) {
                    Thread thread = new Thread(() -> {
                        ResponseBody in = response.body();
                        assert in != null;
                        InputStream inputStream = in.byteStream();
                        // convert inputstram to bufferinoutstream
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                        Bitmap bitmap= BitmapFactory.decodeStream(bufferedInputStream);
                        Bitmap resized = ThumbnailUtils.extractThumbnail(bitmap, 350, 350);
                        ProductListPage.this.runOnUiThread(() -> itemLayout.prodImage.setImageBitmap(resized));
                    });
                    thread.start();
                }
            });

            if(i%2 == 1){
                itemLayout.setBackgroundColor(Color.parseColor("#559ededd"));
            }

            itemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(ProductListPage.this, ProductDetails.class);

                intent.putExtra("productName", itemLayout.prodName.getText());
                intent.putExtra("price", itemLayout.prodPrice.getText());
                intent.putExtra("sellerName", itemLayout.prodSeller.getText());
                Bitmap bitmap = ((BitmapDrawable) itemLayout.prodImage.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* Ignored for PNGs */, stream);
                byte[] bitmapdata = stream.toByteArray();

                intent.putExtra("jsonObject", itemLayout.jObject.toString());

                intent.putExtra("image", bitmapdata);
                startActivity(intent);
            });

            layoutProducts.addView(itemLayout);
        }
    }
}