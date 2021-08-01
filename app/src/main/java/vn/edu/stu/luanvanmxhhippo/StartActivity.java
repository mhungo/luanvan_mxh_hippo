package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class StartActivity extends AppCompatActivity {

    private MaterialButton btn_start_login, btn_start_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        addControls();
        addEvent();

    }

    private void addEvent() {
        //click button login
        btn_start_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        //click button register
        btn_start_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        btn_start_login = findViewById(R.id.btn_start_login);
        btn_start_register = findViewById(R.id.btn_start_register);
    }

    private void getLanguage() {
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        String language = prefs.getString("current_language", "none");

        Log.d("LLLLLL", "getLanguage: " + language);
        if (language != null) {
            changeLanguage(StartActivity.this, language);
        }


    }

    private void changeLanguage(Context context, String language) {
        Locale locale = new Locale(language);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,
                context.getResources().getDisplayMetrics());

        Toast.makeText(this, getString(R.string.sucessfull_update), Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}