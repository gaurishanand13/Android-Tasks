package com.example.codevectorlabs.Screens;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codevectorlabs.Database.model.post;
import com.example.codevectorlabs.R;
import com.example.codevectorlabs.Utils.dashboardRecyclerView;
import com.example.codevectorlabs.ViewModel.LoginViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class postActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;
    ImageView profileImageView;
    LoginViewModel viewModel;

    AlertDialog alertDialog;
    Uri profileUri;

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity();
            } else {
                super.onBackPressed();
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.initialize(getApplication());

        //Setting up the adapter
        ArrayList<post> postsList = new ArrayList<>();
        RecyclerView postRecyclerView = findViewById(R.id.postRecyclerView);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dashboardRecyclerView adapter = new dashboardRecyclerView(this,postsList);
        postRecyclerView.setAdapter(adapter);


        TextView noPostTextView = findViewById(R.id.noPostTextView);
        if(postsList.size()==0){
            noPostTextView.setVisibility(View.VISIBLE);
        }
        else{
            noPostTextView.setVisibility(View.GONE);
        }

        //Updating the adapter
        viewModel.getAllPosts().observe(this, new Observer<List<post>>() {
            @Override
            public void onChanged(List<post> posts) {
                postsList.clear();
                postsList.addAll(posts);
                adapter.notifyDataSetChanged();
                if(postsList.size()==0){
                    noPostTextView.setVisibility(View.VISIBLE);
                }
                else{
                    noPostTextView.setVisibility(View.GONE);
                }
            }
        });


        ImageButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            addPostAlertDialog();
        });

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        setUpHeaderView(navigationView);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setUpHeaderView(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        ImageView profileImageView = headerView.findViewById(R.id.profileImageView);
        TextView nameTextView , emailTextView;
        nameTextView = headerView.findViewById(R.id.nameTextView);
        emailTextView = headerView.findViewById(R.id.emailTextView);
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        nameTextView.setText(sharedPreferences.getString("name",""));
        emailTextView.setText(sharedPreferences.getString("email",""));

        try {
            Uri uri = Uri.parse(sharedPreferences.getString("profileUri",""));
            Log.i("uri",uri.toString());
            Picasso.get().load(uri).noPlaceholder().centerCrop().fit().into(profileImageView);
        }catch (Exception e){
            Log.i("error in uri",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout){
            //Logout from here
            SharedPreferences sharedPreferences = this.getSharedPreferences("myPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username","");
            editor.commit();
            startActivity(new Intent(this,login.class));
            finish();
            Toast.makeText(getApplicationContext(),"Logout Done!",Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



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
            attachPhotos();
        }
        else{
            Toast.makeText(this,"Grant Permissions to continue!",Toast.LENGTH_SHORT).show();
        }
    }
    public void addPostAlertDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate( R.layout.add_post_layout, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);

        profileImageView = dialogView.findViewById(R.id.captionPhotoEditText);
        EditText captionEditText = dialogView.findViewById(R.id.captionEditText);
        Button uploadButton = dialogView.findViewById(R.id.uploadPostButton);

        AlertDialog alertDialog = dialog.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show photos
                attachPhotos();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Date currentTime = Calendar.getInstance().getTime();
                String currentTimeString = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);
                currentTimeString = (new SimpleDateFormat("HH:mm").format(currentTime)) + " " + currentTimeString;
                SharedPreferences sharedPreferences = getSharedPreferences("myPref",Context.MODE_PRIVATE);

                viewModel.insertPost(new post(
                        currentTimeString
                        ,sharedPreferences.getString("email","")
                        ,sharedPreferences.getString("profileUri","")
                        ,profileUri.toString()
                        ,sharedPreferences.getString("name","")
                        ,captionEditText.getText().toString()
                ));

                Toast.makeText(getApplicationContext(),"Uploaded",Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
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
            alertDialog.show();
        }
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