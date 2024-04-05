package com.example.switchorditch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class ItemLayout extends LinearLayout {

    TextView prodName;
    TextView prodPrice;
    TextView prodSeller;
    ImageView prodImage;
    JSONObject jObject;

    public ItemLayout(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        prodImage = new ImageView(context);
        prodImage.setPadding(30, 30, 30, 30);
        prodImage.setImageResource(R.drawable.camera);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(350, 350);
        prodImage.setLayoutParams(layoutParams);

        prodName = new TextView(context);
        prodName.setPadding(0, 30, 0, 0);
        prodName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        prodPrice = new TextView(context);
        prodSeller = new TextView(context);

        addView(prodImage);

        LinearLayout rightLayout = new LinearLayout(context);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        rightLayout.addView(prodName);
        rightLayout.addView(prodSeller);
        rightLayout.addView(prodPrice);
        addView(rightLayout);
    }

    @SuppressLint("SetTextI18n")
    public void populateFromJson(JSONObject jo) throws JSONException {
        jObject = jo;
        prodName.setText(jo.getString("PROD_NAME"));
        String sellerName = "Seller: " + jo.getString("USER_FNAME") + " "+ jo.getString("USER_LNAME");
        prodSeller.setText(sellerName);
        prodPrice.setText("R" + jo.getString("PROD_PRICE"));
    }
}
