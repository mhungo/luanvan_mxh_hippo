package vn.edu.stu.luanvanmxhhippo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
    private MaterialButton btn_status_group, btn_add_participant, btn_join;

    private LinearLayout layout_group_add, layout_group;
    private RelativeLayout layout_posts;

    private String myGroupRole = "";
    private boolean isJoin = false;

    private boolean isExist = false;

    private String nameGroupTemp = "";

    private SwipeRefreshLayout mRefreshLayout;

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

        checkUserExistGroup();

        /*loadInfoGroup();
        loadImageUser();

        loadPostOfGroup();

        loadMyGroupRole();*/

        //check status is exist user of group
        checkIsExistUserOfGroup();


    }

    private void checkIsExistUserOfGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.child(firebaseUser.getUid()).exists()) {
                            isExist = true;
                        } else {
                            isExist = false;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }


    //check user exist in group
    private void checkUserExistGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.child(firebaseUser.getUid()).exists()) {
                            layout_group.setVisibility(View.VISIBLE);
                            layout_posts.setVisibility(View.VISIBLE);
                            layout_group_add.setVisibility(View.GONE);

                            isJoin = true;
                            invalidateOptionsMenu();

                            loadInfoGroup();
                            loadImageUser();
                            loadPostOfGroup();
                            loadMyGroupRole();


                        } else {
                            layout_group.setVisibility(View.GONE);
                            layout_posts.setVisibility(View.GONE);
                            layout_group_add.setVisibility(View.VISIBLE);

                            isJoin = false;
                            invalidateOptionsMenu();

                            loadInfoGroup();
                            checkSentRequestJoin(firebaseUser.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void checkSentRequestJoin(String current_userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(current_userid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(groupPostId)) {
                            reference.child(current_userid)
                                    .child(groupPostId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            Log.i("UUU", "onDataChange: " + snapshot.child(Constant.REQUEST_TYPE));
                                            String type = snapshot.child(Constant.REQUEST_TYPE).getValue().toString();
                                            if (type.equals(Constant.REQUEST_TYPE_SENT)) {
                                                btn_join.setText(R.string.cancel_request);
                                                btn_join.setTag("sented");
                                            } else {
                                                btn_join.setText(R.string.join_group);
                                                btn_join.setTag("sent");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            btn_join.setText(R.string.join_group);
                            btn_join.setTag("sent");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

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
                            if (groupPo.getPost_status().equals(Constant.DEFAULT_STATUS_ENABLE)) {
                                groupPostPosts.add(groupPo);
                            }
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
                        total_member_group.setText(snapshot.getChildrenCount()+ " " + getString(R.string.members));
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

       /* mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkUserExistGroup();
                mRefreshLayout.setRefreshing(false);
            }
        });*/

        total_member_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    Intent intent = new Intent(GroupPostActivity.this, FollowersActivity.class);
                    intent.putExtra("id", groupPostId);
                    intent.putExtra("title", "memberGroup");
                    startActivity(intent);
                } else {
                    Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        txt_input_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("groupPostId", groupPostId);
                    editor.apply();

                    startActivity(new Intent(GroupPostActivity.this, GroupPostPostsActivity.class));
                } else {
                    Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        btn_status_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    if (myGroupRole.equals(Constant.ROLE_CREATOR)) {

                    } else {
                        leaveGroup();
                    }
                } else {
                    Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        btn_add_participant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    Intent intent = new Intent(GroupPostActivity.this, AddParticipantGroupPostActivity.class);
                    intent.putExtra("groupPostId", groupPostId);
                    startActivity(intent);
                } else {
                    Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    if (btn_join.getTag().equals("sent")) {
                        sentRequestJoinToGroup(firebaseUser.getUid());
                    } else {
                        cancelRequestJoinGroup(firebaseUser.getUid());
                    }
                }
            }
        });

    }

    private void cancelRequestJoinGroup(String current_userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(current_userid)
                .child(groupPostId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(groupPostId)
                                    .child(current_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_join.setTag("sent");
                                                btn_join.setText(R.string.join_group);

                                            } else {
                                                //failed
                                            }
                                        }
                                    });
                        } else {
                            //failed
                        }
                    }
                });

    }

    //sent request join group
    private void sentRequestJoinToGroup(String current_userid) {

        String timestamp = System.currentTimeMillis() + "";

        //create hashmap
        HashMap<String, Object> hashMapRequest = new HashMap<>();
        hashMapRequest.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_SENT);
        hashMapRequest.put(Constant.REQUEST_TIMESTAMP, timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(current_userid)
                .child(groupPostId)
                .setValue(hashMapRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //create hashmap request received
                            HashMap<String, Object> hashMapRequestReceived = new HashMap<>();
                            hashMapRequestReceived.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_RECEIVED);
                            hashMapRequestReceived.put(Constant.REQUEST_TIMESTAMP, timestamp);

                            reference.child(groupPostId)
                                    .child(current_userid)
                                    .setValue(hashMapRequestReceived)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btn_join.setTag("sented");
                                                btn_join.setText(R.string.cancel_request);
                                                Toast.makeText(GroupPostActivity.this, R.string.sent_reuqest_join_group, Toast.LENGTH_SHORT).show();

                                            } else {
                                                //failed
                                            }
                                        }
                                    });
                        } else {
                            //failed
                        }
                    }
                });
    }


    //leave group
    private void leaveGroup() {
        Dialog dialog = new Dialog(GroupPostActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);

        nameGroupTemp = txt_title_group.getText().toString();

        MaterialButton btn_confirm_dialog, btn_cancel_dialog;
        TextView textviewtitile;
        btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
        btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
        textviewtitile = dialog.findViewById(R.id.textviewtitile);
        textviewtitile.setText(getString(R.string.you_want_haven_out_group) + nameGroupTemp + " ?");

        //confirm unfollow
        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
                reference.child(groupPostId)
                        .child(Constant.COLLECTION_PARTICIPANTS)
                        .child(firebaseUser.getUid())
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupPostActivity.this, R.string.you_have_left_the_group, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });

        //ccancel
        btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addControls() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.Group_post);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        /* mRefreshLayout = findViewById(R.id.mRefreshLayout);*/

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        image_group = findViewById(R.id.image_group);
        icon_status = findViewById(R.id.icon_status);
        back = findViewById(R.id.back);
        more_toolbar = findViewById(R.id.more_toolbar);
        txt_title_group = findViewById(R.id.txt_title_group);
        txt_title_status_group = findViewById(R.id.txt_title_status_group);
        total_member_group = findViewById(R.id.total_member_group);
        txt_input_post = findViewById(R.id.txt_input_post);
        btn_status_group = findViewById(R.id.btn_status_group);
        btn_join = findViewById(R.id.btn_join);
        btn_add_participant = findViewById(R.id.btn_add_participant);
        img_user_current = findViewById(R.id.img_user_current);

        layout_group = findViewById(R.id.layout_group);
        layout_group_add = findViewById(R.id.layout_group_add);
        layout_posts = findViewById(R.id.layout_posts);

        recycler_view_group_post = findViewById(R.id.recycler_view_group_post);
        recycler_view_group_post.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recycler_view_group_post.setLayoutManager(linearLayoutManager);
        groupPostPosts = new ArrayList<>();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_post_menu, menu);
        if (isJoin == true) {
            menu.findItem(R.id.mnu_more_group_post).setVisible(true);
            if (myGroupRole.equals(Constant.ROLE_CREATOR) || myGroupRole.equals(Constant.ROLE_ADMIN)) {
                menu.findItem(R.id.mnu_edit_group_post).setVisible(true);
            } else {
                menu.findItem(R.id.mnu_edit_group_post).setVisible(false);
            }
        } else {
            menu.findItem(R.id.mnu_more_group_post).setVisible(false);
            menu.findItem(R.id.mnu_edit_group_post).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnu_edit_group_post) {
            if (isExist == true) {
                Intent intent = new Intent(GroupPostActivity.this, GroupPostEditActivity.class);
                intent.putExtra("groupPostId", groupPostId);
                startActivity(intent);
            } else {
                Snackbar.make(total_member_group, R.string.you_are_not_member_group, BaseTransientBottomBar.LENGTH_SHORT).show();
            }

        } else if (id == R.id.mnu_more_group_post) {
            Intent intent = new Intent(GroupPostActivity.this, GroupPostOptionActivity.class);
            intent.putExtra("groupPostId", groupPostId);
            startActivity(intent);
        } else {

        }

        return super.onOptionsItemSelected(item);
    }
}