package com.example.dell.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChangePass extends AppCompatActivity {

    EditText oldPass;
    EditText newPass;
    Button change;
    MyDBHandler dbHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        oldPass=(EditText)findViewById(R.id.oldPass);
        newPass=(EditText)findViewById(R.id.newPass);
        change=(Button)findViewById(R.id.change);
        dbHandler=new MyDBHandler(this, null, null, 1);
    }
    public void onChange(View v)
    {
        String old=oldPass.getText().toString();//Old Password
        String newpw=newPass.getText().toString();//New Password
        TextView errorChange=(TextView)findViewById(R.id.errorChange);
        SharedPreferences shared=getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String pw=shared.getString("password", "");
        if(newpw.length()<=6)
        {
            errorChange.setText("Password length must be greater than 6");
            return;
        }
        else if(newpw.length()>16)
        {
            errorChange.setText("Password length must be less than 17");
            return;
        }
        else if(old.equals(pw))
        {
            if(old.equals(newpw))
            {
                errorChange.setText("Old password and new password must be different");
                return;
            }
            //Make changes in Database
            dbHandler.changePassword(shared.getString("username", ""), newpw);

            //Make changes in shared preferences
            SharedPreferences.Editor editor=shared.edit();
            editor.remove("password");//Remove old Password
            editor.putString("password", newpw);//Add New Password
            editor.apply();

            finish();
        }
        else
        {
            errorChange.setText("Enter correct old password");
        }
    }
}
