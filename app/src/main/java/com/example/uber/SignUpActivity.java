package com.example.uber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtName,edtPassword;
    private RadioButton rd_Driver,rd_Passenger;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName=findViewById(R.id.edt_Signup_Name);
        edtPassword=findViewById(R.id.edt_Signup_password);
        rd_Driver=findViewById(R.id.driver_Signup_check);
        rd_Passenger=findViewById(R.id.passeger_SignUp_check);
        btnSignUp=findViewById(R.id.signUP);

        btnSignUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (rd_Driver.isChecked()==false  && rd_Passenger.isChecked()==false){
            FancyToast.makeText(SignUpActivity.this,"You Must Check Driver or Passenger !", FancyToast.LENGTH_LONG,FancyToast.INFO,true).show();
            return;
        }
        ParseUser appUser=new ParseUser();
        appUser.setUsername(edtName.getText().toString());
        appUser.setPassword(edtPassword.getText().toString());
        if (rd_Driver.isChecked()){
            appUser.put("as","Driver");
        }
        else if (rd_Passenger.isChecked()){
            appUser.put("as","Passenger");
        }
        appUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null){
                    FancyToast.makeText(SignUpActivity.this,"Sign Up Successfully !", FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true).show();
                    Intent intent =new Intent(SignUpActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }
}
