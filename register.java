package com.example.codevectorlabs.Screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.codevectorlabs.Database.model.personDetails;
import com.example.codevectorlabs.R;
import com.example.codevectorlabs.ViewModel.LoginViewModel;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class register extends AppCompatActivity {
    LoginViewModel viewModel;

    ImageView profileImageView;
    EditText emailEditText , nameEditText , userNameEditText , passwordEditText;
    TextView signInTextView;
    Button registerButton;
    ScrollView scrollView;
    static File fileF;
    Uri profileUri = null;


    //Alert Dialog for attach photos
    AlertDialog alertDialog = null;
    private Boolean checkPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions( (Activity) context,new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA ,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);
        } else {
            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==123
                && grantResults[0]==PackageManager.PERMISSION_GRANTED
                && grantResults[1]==PackageManager.PERMISSION_GRANTED
                && grantResults[2]==PackageManager.PERMISSION_GRANTED ){
            showAlertDialog();
        }
        else{
            Toast.makeText(this,"Grant Permissions to continue!",Toast.LENGTH_SHORT).show();
        }
    }
    private void showAlertDialog() {
        alertDialog.show();
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        profileImageView = findViewById(R.id.profileImageView);
        emailEditText = findViewById(R.id.emailEditText);
        nameEditText = findViewById(R.id.nameEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInTextView = findViewById(R.id.signInTextView);
        registerButton = findViewById(R.id.registerButton);
        scrollView = findViewById(R.id.scrollView);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.initialize(getApplication());


        profileImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                attachPhotos();
            }
        });
        signInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Register
                if(emailEditText.getText().toString().isEmpty()
                        || nameEditText.getText().toString().isEmpty()
                        || userNameEditText.getText().toString().isEmpty()
                        || passwordEditText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Fill all details!",Toast.LENGTH_SHORT).show();
                }
                else{
                    if(viewModel.isUniqueUsername(userNameEditText.getText().toString())==false){
                        Toast.makeText(getApplicationContext(),"Username already taken! Choose another",Toast.LENGTH_SHORT).show();
                    }
                    else if(viewModel.isUnqiueEmail(emailEditText.getText().toString())==false){
                        Toast.makeText(getApplicationContext(),"Email already taken! Choose another",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        try {
                            String uri = (profileUri==null)?"":profileUri.toString();
                            viewModel.insertUser(new personDetails(
                                    userNameEditText.getText().toString()
                                    ,passwordEditText.getText().toString()
                                    ,nameEditText.getText().toString()
                                    ,uri
                                    ,emailEditText.getText().toString()));

                            //Save data in sharedPreferences too now
                            saveDataInSharedPreferences(uri);
                            Toast.makeText(getApplicationContext(),"Registered Successfully!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),postActivity.class));
                            finish();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });
    }


    // ATTACH PHOTOS
    public void attachPhotos(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate( R.layout.attach_photos, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);

        TextView txTakePicture = dialogView.findViewById(R.id.tx_take_picture);
        TextView txOpenGallery = dialogView.findViewById(R.id.tx_open_gallery);

        alertDialog = dialog.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        txTakePicture.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(),AndoidX.class),111);
        }});

        txOpenGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,12);
            }
        });

        if(checkPermission(this)){
            showAlertDialog();
        }
    }

    public void saveDataInSharedPreferences(String uri){
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPref",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",nameEditText.getText().toString());
        editor.putString("username",userNameEditText.getText().toString());
        editor.putString("email",emailEditText.getText().toString());
        editor.putString("profileUri",uri);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==111){
            profileUri = Uri.parse(data.getStringExtra("path"));
            Picasso.get().load(profileUri).noPlaceholder().centerCrop().fit().into(profileImageView);
            alertDialog.dismiss();
        }
        else{
            if(resultCode==RESULT_OK){
                if(data!=null){
                    profileUri = data.getData();
                    Picasso.get().load(profileUri).noPlaceholder().centerCrop().fit().into(profileImageView);
                    alertDialog.dismiss();
                }
            }
        }
    }
}