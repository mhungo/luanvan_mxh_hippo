package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.PostAdapter;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Util.Constant;

public class PostDetailActivity extends AppCompatActivity {

    private String postid;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        addControls();
        readPost();
    }

    private void addControls() {
        /*SharedPreferences preferences = PostDetailActivity.this.getSharedPreferences("PREFS", Context.MODE_PRIVATE);*/
        /*postid = getIntent().getStringExtra("postid");*/
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postid = prefs.getString("postid", "none");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostDetailActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(PostDetailActivity.this, postList);
        recyclerView.setAdapter(postAdapter);
    }


    private void readPost() {
        if (postid == null) {

        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.hasChild(postid)) {
                        postList.clear();
                        Post post = snapshot.child(postid).getValue(Post.class);
                        postList.add(post);
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(PostDetailActivity.this, getString(R.string.postsisnotexist), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}