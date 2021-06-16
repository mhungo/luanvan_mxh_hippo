package vn.edu.stu.luanvanmxhhippo;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class OpenImagenActivity extends AppCompatActivity {

    private ImageView image_open;
    private String image_url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_imagen);

        addControls();
        getDataIntent();
        loadImage();

    }

    private void loadImage() {
        try {
            Glide.with(OpenImagenActivity.this).load(image_url)
                    .placeholder(R.drawable.placeholder)
                    .into(image_open);
        } catch (Exception e) {
            image_open.setImageResource(R.drawable.placeholder);
        }
    }

    private void getDataIntent() {
        image_url = getIntent().getStringExtra("image_url_open");
    }

    private void addControls() {
        image_open = findViewById(R.id.image_open);
    }
}