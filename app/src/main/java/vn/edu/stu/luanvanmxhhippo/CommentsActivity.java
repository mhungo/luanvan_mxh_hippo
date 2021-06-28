package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Adapter.CommentAdapter;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Comment;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    private EditText addcomment;
    private ImageView image_profile;
    private TextView post;

    private LinearProgressIndicator progressBar;

    private String postid;
    private String publisherid;

    private APIService apiService;

    private List<String> stringListBlockId;
    private List<String> stringListBlockIdByUser;

    private FirebaseUser firebaseUser;

    private String usernameTemp = "";

    private boolean isBlock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        getDataIntent();
        addControls();
        addEvents();

        getImage();
        readIdBlockUser();
        //readIdBlockByUser();
        readcomments();
        checkBlockClickEvents();
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");
    }

    //load id user blocked
    private void checkBlockClickEvents() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(publisherid)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                isBlock = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load id user blocked
    private void readIdBlockUser() {
        stringListBlockId.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListBlockId.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void readIdBlockByUser() {
        stringListBlockIdByUser.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(publisherid)
                .child(Constant.COLLECTION_BLOCKUSER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListBlockIdByUser.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void addEvents() {
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isBlock == true) {
                    Snackbar.make(post, "You're blocked by that user!, can't send comment !", BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    if (addcomment.getText().toString().equals("")) {
                        Toast.makeText(CommentsActivity.this, "You can't send comment", Toast.LENGTH_SHORT).show();
                    } else {
                        addComment();
                        //backgroundAddComment.start();
                    }
                }
            }
        });
    }

    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(postid).child(Constant.COLLECTION_COMMENTS);

        String commentid = reference.push().getKey();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constant.COMMENT_COMMENT, addcomment.getText().toString());
        hashMap.put(Constant.COMMENT_PUBLISHER, firebaseUser.getUid());
        hashMap.put(Constant.COMMENT_ID, commentid);
        hashMap.put(Constant.COMMENT_IMAGE, "");
        hashMap.put(Constant.COMMENT_TIMESTAMP, System.currentTimeMillis() + "");

        reference.child(commentid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        sendNotification(publisherid, usernameTemp, "has commented your posts");
                        Snackbar.make(post, "Commented !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
        //backgroundAddNotification.start();
        //addNotifications();
        addcomment.setText("");

    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(publisherid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.ACTION_USERID, firebaseUser.getUid());
        hashMap.put(Constant.ACTION_TEXT, "commented: " + addcomment.getText().toString());
        hashMap.put(Constant.ACTION_POSTID, postid);
        hashMap.put(Constant.ACTION_TIMESTAMP, System.currentTimeMillis() + "");
        hashMap.put(Constant.ACTION_ISPOST, true);
        reference.push().setValue(hashMap);
        addcomment.setText("");
    }

    //ham gui thong bao
    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(postid, R.drawable.notify, username + ": " + message, "New comments", "" + firebaseUser.getUid(), Constant.TYPE_NOTIFICATION_COMMENT);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(CommentsActivity.this, "Error sent notification", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    try {
                        Glide.with(getApplicationContext()).load(user.getUser_imageurl())
                                .placeholder(R.drawable.placeholder).into(image_profile);
                    } catch (Exception e) {
                        image_profile.setImageResource(R.drawable.placeholder);
                    }
                    usernameTemp = user.getUser_username();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readcomments() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                        .child(postid).child(Constant.COLLECTION_COMMENTS);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            if (!stringListBlockId.contains(comment.getComment_publisher())) {
                                commentList.add(comment);
                            } else {

                            }

                        }
                        commentAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }, 1000);

    }

    private void addControls() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        stringListBlockId = new ArrayList<>();
        stringListBlockIdByUser = new ArrayList<>();

        progressBar = findViewById(R.id.progress_bar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid);
        recyclerView.setAdapter(commentAdapter);

        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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