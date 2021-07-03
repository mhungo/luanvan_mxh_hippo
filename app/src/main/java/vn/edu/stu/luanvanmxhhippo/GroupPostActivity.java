package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Adapter.GroupPostItemAdapter;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.GroupPostPosts;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class GroupPostActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;

    private Toolbar toolbar;

    private String groupPostId = "";
    private ImageView image_group, icon_status, back, more_toolbar;
    private TextView txt_title_group, txt_title_status_group, total_member_group, txt_input_post;
    private MaterialButton btn_status_group, btn_add_participant;

    private String myGroupRole = "";

    private CircleImageView img_user_current;

    private RecyclerView recycler_view_group_post;

    private GroupPostItemAdapter groupPostItemAdapter;
    private List<GroupPostPosts> groupPostPosts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post);


        addControls();
        getDataIntent();
        addEvents();

        loadInfoGroup();
        loadImageUser();

        loadPostOfGroup();

        loadMyGroupRole();

    }

    //load group post role
    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        ref.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .orderByChild(Constant.ROLE_UID)
                .equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child(Constant.ROLE_ROLE).getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadPostOfGroup() {
        groupPostPosts.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            GroupPostPosts groupPo = dataSnapshot.getValue(GroupPostPosts.class);
                            groupPostPosts.add(groupPo);
                        }
                        groupPostItemAdapter = new GroupPostItemAdapter(GroupPostActivity.this, groupPostPosts, groupPostId);
                        recycler_view_group_post.setAdapter(groupPostItemAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load Image user currents
    private void loadImageUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            try {
                                Glide.with(GroupPostActivity.this).load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(img_user_current);
                            } catch (Exception e) {
                                img_user_current.setImageResource(R.drawable.placeholder);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load info group post
    private void loadInfoGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        GroupPost groupPost = snapshot.getValue(GroupPost.class);
                        if (groupPost != null) {
                            try {
                                Glide.with(GroupPostActivity.this).load(groupPost.getGrouppost_icon())
                                        .placeholder(R.drawable.placeholder)
                                        .into(image_group);

                            } catch (Exception e) {
                                image_group.setImageResource(R.drawable.placeholder);
                            }

                            txt_title_group.setText(groupPost.getGrouppost_title());

                            if (groupPost.getGrouppost_role().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                                icon_status.setImageResource(R.drawable.ic_role_public);
                            } else {
                                icon_status.setImageResource(R.drawable.ic_role_private);
                            }

                            txt_title_status_group.setText("");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        total_member_group.setText(snapshot.getChildrenCount() + " members");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void getDataIntent() {
        groupPostId = getIntent().getStringExtra("group_post_id");
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txt_input_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("groupPostId", groupPostId);
                editor.apply();

                startActivity(new Intent(GroupPostActivity.this, GroupPostPostsActivity.class));
            }
        });

        btn_add_participant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupPostActivity.this, AddParticipantGroupPostActivity.class);
                intent.putExtra("groupPostId", groupPostId);
                startActivity(intent);
            }
        });

    }


    private void addControls() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Group Posts");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        image_group = findViewById(R.id.image_group);
        icon_status = findViewById(R.id.icon_status);
        back = findViewById(R.id.back);
        more_toolbar = findViewById(R.id.more_toolbar);
        txt_title_group = findViewById(R.id.txt_title_group);
        txt_title_status_group = findViewById(R.id.txt_title_status_group);
        total_member_group = findViewById(R.id.total_member_group);
        txt_input_post = findViewById(R.id.txt_input_post);
        total_member_group = findViewById(R.id.total_member_group);
        btn_status_group = findViewById(R.id.btn_status_group);
        btn_add_participant = findViewById(R.id.btn_add_participant);
        img_user_current = findViewById(R.id.img_user_current);

        recycler_view_group_post = findViewById(R.id.recycler_view_group_post);
        recycler_view_group_post.setHasFixedSize(true);
        recycler_view_group_post.setLayoutManager(new LinearLayoutManager(this));
        groupPostPosts = new ArrayList<>();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_post_menu, menu);
        if (myGroupRole.equals(Constant.ROLE_CREATOR) || myGroupRole.equals(Constant.ROLE_ADMIN)) {
            menu.findItem(R.id.mnu_edit_group_post).setVisible(true);
        } else {
            menu.findItem(R.id.mnu_edit_group_post).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnu_edit_group_post) {
            Intent intent = new Intent(GroupPostActivity.this, GroupPostEditActivity.class);
            intent.putExtra("groupPostId", groupPostId);
            startActivity(intent);

        } else if (id == R.id.mnu_more_group_post) {
            Intent intent = new Intent(GroupPostActivity.this, GroupPostOptionActivity.class);
            intent.putExtra("groupPostId", groupPostId);
            startActivity(intent);
        } else {

        }

        return super.onOptionsItemSelected(item);
    }
}