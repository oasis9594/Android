package com.example.dell.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button logIn;
    Button signUp;
    ProgressBar myProgressBar;
    MyDBHandler dbHandler;
    static private final String NotExists="notexists&&isempty";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if user has logged in before...i.e. shared preferences file is not empty
        SharedPreferences shared=getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String first, second;
        first=shared.getString("username", NotExists);
        second=shared.getString("password", NotExists);
        if(!(first.equals(NotExists)||second.equals(NotExists)))
        {
            Intent i= new Intent(this, LogOut.class);
            i.putExtra("user", first);
            i.putExtra("pass", second);
            String name="Hello";
            i.putExtra("person", name);
            startActivity(i);
        }
        //Reference ids
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        logIn=(Button)findViewById(R.id.logIn);
        signUp=(Button)findViewById(R.id.signUp);
        dbHandler=new MyDBHandler(this, null, null, 1);
        myProgressBar=(ProgressBar)findViewById(R.id.myProgressBar);
    }
    public void onLogIn(View v)
    {
        myProgressBar.setVisibility(View.VISIBLE);
        String text=username.getText().toString();
        String pass=password.getText().toString();

        //Check if username and password is valid
        boolean b=dbHandler.validate(text, pass);//true if valid

        if(b)
        {
            SharedPreferences shared=getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=shared.edit();
            editor.putString("username", text);
            editor.putString("password", pass);
            editor.apply();
            Intent i= new Intent(this, LogOut.class);
            i.putExtra("user", text);
            i.putExtra("pass", pass);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
        else
        {
            myProgressBar.setVisibility(View.GONE);
            TextView errorText=(TextView)findViewById(R.id.errorText);
            errorText.setText("Invalid username or password");
        }
        myProgressBar.setVisibility(View.GONE);
        password.setText("");
    }
    public void onSignUp(View v)
    {
        password.setText("");
        Intent i=new Intent(this, SignUp.class);
        startActivity(i);
    }

    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit!!!")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
