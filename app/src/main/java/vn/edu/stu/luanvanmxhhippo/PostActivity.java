package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class PostActivity extends AppCompatActivity {

    private MaterialButton btnImage, btnVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        //add control
        addControls();

        //Event click
        addEvents();
    }


    private void addEvents() {
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostActivity.this, PostVideoActivity.class);
                startActivity(intent);

            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostActivity.this, PostImageActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        btnImage = findViewById(R.id.btn_post_image);
        btnVideo = findViewById(R.id.btn_post_video);
    }


    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}