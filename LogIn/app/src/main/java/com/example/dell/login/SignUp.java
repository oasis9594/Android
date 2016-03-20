package com.example.dell.login;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUp extends AppCompatActivity {

    EditText myName;
    EditText myUser;
    EditText myPassword;
    Button register;
    TextView myError;
    MyDBHandler dbHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        myName=(EditText)findViewById(R.id.myName);
        myUser=(EditText)findViewById(R.id.myUser);
        myPassword=(EditText)findViewById(R.id.myPassword);
        register=(Button)findViewById(R.id.register);
        myError=(TextView)findViewById(R.id.myError);
        dbHandler=new MyDBHandler(this, null, null, 1);
    }
    public void onRegister(View v)
    {
        String name, password, username;
        name=myName.getText().toString();
        password=myPassword.getText().toString();
        username=myUser.getText().toString();
        if(password.length()<=6)
        {
            myError.setText("Password length must be greater than 6");
            return;
        }
        if(password.length()>16)
        {
            myError.setText("Password length must be less than 17");
            return;
        }
        //Check whether username already exists in database
        boolean b=dbHandler.userExists(username);//true if database does contains that username
        if(b)
        {
            myError.setText("Username already exists!!Try a different username");
            return;
        }
        else
            dbHandler.addUser(new UserDetails(username, name, password));
        finish();
    }
}
