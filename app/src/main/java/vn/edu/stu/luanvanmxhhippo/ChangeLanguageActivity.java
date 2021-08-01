package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class ChangeLanguageActivity extends AppCompatActivity {

    private TextView vi, en;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);

        addControls();
        addEvents();
    }

    private void addEvents() {
        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("current_language", "vi");
                editor.apply();

                changeLanguage(ChangeLanguageActivity.this, "vi");
            }
        });

        en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("current_language", "en");
                editor.apply();
                changeLanguage(ChangeLanguageActivity.this, "en");
            }
        });
    }

    private void changeLanguage(Context context, String language) {
        Locale locale = new Locale(language);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        context.getResources().updateConfiguration(configuration,
                context.getResources().getDisplayMetrics());

        Toast.makeText(this, getString(R.string.sucessfull_update), Toast.LENGTH_SHORT).show();

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.option);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vi = findViewById(R.id.vi);
        en = findViewById(R.id.en);
    }
}
