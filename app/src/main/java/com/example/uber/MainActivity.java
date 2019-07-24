package com.example.uber;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edt_Name,edt_Password,edtDriverOrPasseger;
    private Button btn_Login,btnAnonymoususer;
    private RadioButton rd_Driver,rd_Passenger;
    private TextView signUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ParseUser.getCurrentUser() != null){
            transitionMapActivty();
            transitionDriverGetRequest();
        }

        edt_Name=findViewById(R.id.edt_name);
        edt_Password=findViewById(R.id.edt_password);
        rd_Driver=findViewById(R.id.driver_check);
        rd_Passenger=findViewById(R.id.passeger_check);
        signUpText=(TextView)findViewById(R.id.signUpText);
        edtDriverOrPasseger=findViewById(R.id.edtAnpnymous);
        btn_Login=findViewById(R.id.btn_Login);
        btnAnonymoususer=findViewById(R.id.anonymousLoginBtn);


        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

       btn_Login.setOnClickListener(this);
       btnAnonymoususer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btn_Login:
            ParseUser.logInInBackground(edt_Name.getText().toString(), edt_Password.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (user != null && e == null) {
                        FancyToast.makeText(MainActivity.this, "Login Successfully !", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                        transitionMapActivty();
                        transitionDriverGetRequest();
                    }
                }
            });
            break;
            case R.id.anonymousLoginBtn:
                if (edtDriverOrPasseger.getText().toString().equals("Driver") || edtDriverOrPasseger.getText().toString().equals("Passenger")){
                    if (ParseUser.getCurrentUser() !=null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (user!=null && e==null){
                                    FancyToast.makeText(MainActivity.this,"You are a AnonymousUser!", FancyToast.LENGTH_LONG,FancyToast.SUCCESS,true).show();
                                    user.put("as",edtDriverOrPasseger.getText().toString());
                                    user.signUpInBackground(new SignUpCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            transitionMapActivty();
                                            transitionDriverGetRequest();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                break;
        }
    }
    private void transitionMapActivty(){
        if (ParseUser.getCurrentUser() !=null){
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent=new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void transitionDriverGetRequest(){
        if (ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent=new Intent(MainActivity.this,DriverGetRequestActivity.class);
                startActivity(intent);
            }
        }
    }
}
