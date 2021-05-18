package vn.edu.stu.luanvanmxhhippo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

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

}