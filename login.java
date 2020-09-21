package com.example.codevectorlabs.Screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codevectorlabs.R;
import com.example.codevectorlabs.Database.model.personDetails;
import com.example.codevectorlabs.ViewModel.LoginViewModel;

import java.util.List;

public class login extends AppCompatActivity {

    LoginViewModel viewModel;

    EditText editTextEmail,editTextPassword;
    TextView signUpTextView;
    Button cirLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = this.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        if(sharedPreferences.getString("username","").equals("")){
            setContentView(R.layout.acitvity_login);

            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            signUpTextView = findViewById(R.id.signUpTextView);
            cirLoginButton = findViewById(R.id.cirLoginButton);

            viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
            viewModel.initialize(getApplication());

            signUpTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), register.class));
                }
            });

            cirLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editTextEmail.getText().toString().isEmpty() || editTextPassword.getText().toString().isEmpty()){
                        Toast.makeText(getApplicationContext(),"Please fill all the details",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<personDetails> list =  viewModel.isUserExists(editTextEmail.getText().toString(),editTextPassword.getText().toString());
                    if(list.size()==0){
                        Toast.makeText(getApplicationContext(),"Incorrect username or password",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //User exists
                        saveLoginInSharedPreferences(list.get(0));
                        startActivity(new Intent(getApplicationContext(),postActivity.class));
                        finish();
                    }
                }
            });

        }
        else{
            startActivity(new Intent(getApplicationContext(),postActivity.class));
            finish();
        }
    }

    public void saveLoginInSharedPreferences(personDetails p){
        SharedPreferences sharedPreferences = this.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name",p.getName());
        editor.putString("username",p.getUsername());
        editor.putString("email",p.getEmail());
        editor.putString("profileUri",p.getProfilePhoto());
        editor.commit();
    }
}