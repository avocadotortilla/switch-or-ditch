package com.example.switchorditch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Upload extends AppCompatActivity {

    public static final int CAMERA_PERMISSION_CODE = 101;
    public static final int PICK_IMAGE = 1;
    public static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int STORAGE_PERMISSION_CODE = 103;

    Button buttonPost, buttonSelectPhoto, buttonTakePhoto;
    EditText textName, textDescription, textPrice;
    TextView countChars;
    ImageView viewProductImage;
    MediaType imageType = MediaType.parse("image/*");
    File selectedImage;
    Bitmap bitmapSelectedImage = null;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.upload);

        //initialising UI components
        buttonPost = findViewById(R.id.buttonPost);
        buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto);
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto);
        textName = findViewById(R.id.textProdName);
        textDescription = findViewById(R.id.textDescription);
        textPrice = findViewById(R.id.textPrice);
        viewProductImage = findViewById(R.id.imgProduct);
        countChars = findViewById(R.id.textViewNumCharLeft);

        final TextWatcher textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //This sets a textview to the current length
                String charLimitWarningMessage = String.valueOf(255 - s.length());
                charLimitWarningMessage+= " characters left";
                countChars.setText(charLimitWarningMessage);
            }

            public void afterTextChanged(Editable s) {
            }
        };

        textDescription.addTextChangedListener(textWatcher);

        //preferences = getSharedPreferences("UserInfo", 0);

        buttonPost.setOnClickListener(v -> post());
        buttonSelectPhoto.setOnClickListener(v -> requestStoragePermission());
        buttonTakePhoto.setOnClickListener(v -> requestCameraPermission());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (selectedImage != null){
            selectedImage.delete();
        }
    }

    private void post(){
        SessionManager sessionManager = new SessionManager(this);

        //grabbing text from input
        String name = textName.getText().toString();
        String description = textDescription.getText().toString();
        String price = textPrice.getText().toString();
        String validationString = validation(name, description, price, bitmapSelectedImage);

        //runs if user has filled in required fields
        if(validationString.equals("Continue")){

            //OkHttp stuff I still don't fully understand
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse("https://lamp.ms.wits.ac.za/~s2496879/add_product.php")).newBuilder();

            //creates a requestbody th@ will be used to do a POST request
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("prod_name", name)
                    .addFormDataPart("prod_desc", description)
                    .addFormDataPart("price", price)
                    .addFormDataPart("prod_pic", selectedImage.getName(), RequestBody.create(selectedImage, imageType))
                    .addFormDataPart("user_id", Objects.requireNonNull(sessionManager.getUsernameFromSession().get(SessionManager.KEY_USERNAME)))
                    .build();

            //final url
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
                    runOnUiThread(() -> Toast.makeText(Upload.this, "An error has occurred. Please check your Internet connection.", Toast.LENGTH_SHORT).show());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    //Lets the user know the request was successful
                    runOnUiThread(() -> Toast.makeText(Upload.this, "Successfully uploaded", Toast.LENGTH_SHORT).show());
                    selectedImage.delete();
                    finish();
                }
            });
        }

        //messages telling user not to leave things out...
        else{
            Toast.makeText(this, validationString, Toast.LENGTH_SHORT).show();
        }
    }

    private String validation(String name, String desc, String price, Bitmap image){
        if (image == null){
            return "Please add a picture of your product.";
        }
        else if(name.equals("")){
            return "Please add the name of your product.";
        }
        else if(desc.equals("")){
            return "Please add the description of your product.";
        }
        else if (!isValidAmount(price)){
            return "Invalid price format.";
        }
        else if(price.equals("")){
            return "Please enter the price of your product.";
        }
        else if(desc.equals("")){
            return "Please add a product description.";
        }
        else{
            return "Continue";
        }
    }

    private boolean isValidAmount(String price){
        if(price.contains(".")){
            String[] arrOfNumbers = price.split("\\.");

            if(arrOfNumbers.length < 2){
                return false;
            }
            else if(arrOfNumbers[0].length() == 0) {
                return false;
            }
            else if(arrOfNumbers[1].length() > 2){
                return false;
            }
            else if(arrOfNumbers[1].length() == 0){
                return false;
            }
            return true;
        }
        return false;
    }

    //lets user select photo
    @SuppressLint("IntentReset")
    private void getImage(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        @SuppressLint("IntentReset") Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            bitmapSelectedImage = loadFromUri(imageUri);
            viewProductImage.setImageBitmap(bitmapSelectedImage);
            imageType = MediaType.parse(getFileType());
            selectedImage = bitmapToFile(bitmapSelectedImage);
        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE){
            bitmapSelectedImage = (Bitmap)data.getExtras().get("data");
            viewProductImage.setImageBitmap(bitmapSelectedImage);
            selectedImage = bitmapToFile(bitmapSelectedImage);
        }
    }

    private String getFileType(){
        return getContentResolver().getType(imageUri);
    }

    public static File bitmapToFile(Bitmap bitmap) { // File name like "image.png"
        //create a file to write bitmap data
        File file = null;
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        try {
            dir.mkdirs();
            file = File.createTempFile("image", ".jpg", dir);

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50 , bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        }catch (Exception e){
            e.printStackTrace();
            return file;
        }
    }

    private void requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else{
            openCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "You need to enable camera permissions to perform this action.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage();
            } else {
                Toast.makeText(this, "You need to enable storage permissions to perform this action", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
        catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }
    private void requestStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
        else{
            getImage();
        }
    }
}