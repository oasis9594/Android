package com.example.dell.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class LogOut extends AppCompatActivity {

    MyDBHandler dbHandler;
    static private final int changeCode=853891;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_out);
        Log.w("MyApp", "Log Out");
        Bundle LogInDetails=getIntent().getExtras();
        if(LogInDetails==null)
            return;
        String username=LogInDetails.getString("user");

        //fetch name from database using username
        try
        {
            dbHandler=new MyDBHandler(this, null, null, 1);
            String person=dbHandler.userN(username);

            TextView name=(TextView)findViewById(R.id.myText);
            String text="Hi "+person+"!!!\n"+"Your user id is "+username;
            name.setText(text);
        }
        catch (Exception e)
        {
            Log.w("MyApp", "Exception");
        }

    }
    public void onLogOut(View v)
    {
        SharedPreferences shared=getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=shared.edit();
        editor.clear();
        editor.apply();
        Intent intent=new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public void onChangePass(View v)
    {
        Intent i= new Intent(this, ChangePass.class);
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
