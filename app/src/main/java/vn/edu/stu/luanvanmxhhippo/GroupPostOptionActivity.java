package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class GroupPostOptionActivity extends AppCompatActivity {

    private TextView member, post_approval, request_join, delete_group_post;
    private Toolbar toolbar;

    private String groupPostId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_option);

        addControls();
        getDataIntent();
        addEvents();
    }

    private void getDataIntent() {
        groupPostId = getIntent().getStringExtra("groupPostId");
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //click member => list members
        member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupPostOptionActivity.this, FollowersActivity.class);
                intent.putExtra("id", groupPostId);
                intent.putExtra("title", "memberGroup");
                startActivity(intent);
            }
        });

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Members");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        member = findViewById(R.id.member);
        post_approval = findViewById(R.id.post_approval);
        request_join = findViewById(R.id.request_join);
        delete_group_post = findViewById(R.id.delete_group_post);

    }
}