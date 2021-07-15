package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.edu.stu.Adapter.ReplyCommentAdapter;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Comment;
import vn.edu.stu.Model.ReplyComment;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;

public class ReplyCommentsActivity extends AppCompatActivity {

    private RecyclerView recycler_view_reply_comments;
    private ReplyCommentAdapter replyCommentAdapter;
    private List<ReplyComment> replyCommentList;

    private EditText add_comment;
    private ImageView image_profile, image_use_comments_top;
    private TextView btn_post, top_username, comment;

    private LinearProgressIndicator progressBar;

    private String postid;
    private String publisherid;
    private String commentid;

    private APIService apiService;

    private List<String> stringListBlockId;
    private List<String> stringListBlockIdByUser;

    private FirebaseUser firebaseUser;

    private String usernameTemp = "";

    private boolean isBlock = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_comments);

        getDataIntent();
        addControls();

        addEvents();

        loadImageUserCommentTop();
        loadImageUserCurrent();
        loadCommentTop();
        readIdBlockUser();
        //readIdBlockByUser();
        loadCommentsReply();

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

    private void loadImageUserCurrent() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            try {
                                Glide.with(ReplyCommentsActivity.this).load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(image_profile);
                            } catch (Exception e) {
                                image_profile.setImageResource(R.drawable.placeholder);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadCommentsReply() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.child(postid)
                        .child(Constant.COLLECTION_COMMENTS)
                        .child(commentid)
                        .child(Constant.COLLECTION_REPLYCOMMENT)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                replyCommentList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    ReplyComment replyComment = dataSnapshot.getValue(ReplyComment.class);
                                    if (!stringListBlockId.contains(replyComment.getReplycomment_publisher())) {
                                        replyCommentList.add(replyComment);
                                    } else {

                                    }

                                }
                                progressBar.setVisibility(View.GONE);
                                replyCommentAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
            }
        }, 1000);
    }

    private void loadImageUserCommentTop() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(publisherid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            top_username.setText(user.getUser_username());
                            try {
                                Glide.with(ReplyCommentsActivity.this)
                                        .load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(image_use_comments_top);
                            } catch (Exception e) {
                                image_use_comments_top.setImageResource(R.drawable.placeholder);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadCommentTop() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(commentid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        Comment comment_top = snapshot.getValue(Comment.class);
                        if (comment_top != null) {
                            comment.setText(comment_top.getComment_comment());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void addEvents() {
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReplyComments();
            }
        });
    }

    private void addReplyComments() {
        String txt_replycomments = add_comment.getText().toString().trim();
        if (!TextUtils.isEmpty(txt_replycomments)) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                    .child(postid)
                    .child(Constant.COLLECTION_COMMENTS)
                    .child(commentid)
                    .child(Constant.COLLECTION_REPLYCOMMENT);

            String timestamp = System.currentTimeMillis() + "";
            String idreply = reference.push().getKey();

            HashMap<String, Object> hashMapReplyComment = new HashMap();
            hashMapReplyComment.put(Constant.REPLY_COMMENT, txt_replycomments);
            hashMapReplyComment.put(Constant.REPLY_PUBLISHER, firebaseUser.getUid());
            hashMapReplyComment.put(Constant.REPLY_REPLYUSERID, publisherid);
            hashMapReplyComment.put(Constant.REPLY_COMMENTID, idreply);
            hashMapReplyComment.put(Constant.REPLY_TIMESTAMP, timestamp);
            hashMapReplyComment.put(Constant.REPLY_IMAGE, "");

            reference.child(idreply).setValue(hashMapReplyComment)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            add_comment.setText("");
                            Snackbar.make(btn_post, R.string.commented, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Snackbar.make(btn_post, R.string.fail , BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, R.string.please_input_message, Toast.LENGTH_SHORT).show();
        }

    }

    private void getDataIntent() {
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisher");
        commentid = intent.getStringExtra("comment_id");
    }

    private void addControls() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.replies);
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

        recycler_view_reply_comments = findViewById(R.id.recycler_view_reply_comments);
        recycler_view_reply_comments.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_view_reply_comments.setLayoutManager(linearLayoutManager);
        replyCommentList = new ArrayList<>();
        replyCommentAdapter = new ReplyCommentAdapter(ReplyCommentsActivity.this, replyCommentList, postid, commentid);
        recycler_view_reply_comments.setAdapter(replyCommentAdapter);

        add_comment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        btn_post = findViewById(R.id.btn_post);
        image_use_comments_top = findViewById(R.id.image_use_comments_top);
        top_username = findViewById(R.id.top_username);
        comment = findViewById(R.id.comment);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }
}