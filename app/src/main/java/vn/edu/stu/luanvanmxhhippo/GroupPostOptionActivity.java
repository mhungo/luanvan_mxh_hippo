package vn.edu.stu.luanvanmxhhippo;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GroupPostOptionActivity extends AppCompatActivity {

    private TextView logout, settings, view_post_saved, view_block_user, view_friend_list;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_option);

        addControls();
        addEvents();
    }

    private void addEvents() {
    }

    private void addControls() {

    }
}