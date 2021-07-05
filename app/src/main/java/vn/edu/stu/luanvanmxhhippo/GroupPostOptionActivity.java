package vn.edu.stu.luanvanmxhhippo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import vn.edu.stu.Util.Constant;

public class GroupPostOptionActivity extends AppCompatActivity {

    private TextView member, post_approval, request_join, delete_group_post;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;
    private boolean isExist = false;

    private String groupPostId = "";
    private String myGroupRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_option);

        addControls();
        getDataIntent();
        addEvents();

        //check status is exist user of group
        //checkIsExistUserOfGroup();

        //load my group role
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
                            if (ds.hasChild(Constant.ROLE_ROLE)) {
                                myGroupRole = "" + ds.child(Constant.ROLE_ROLE).getValue();

                                if (myGroupRole.equals("participant")) {
                                    post_approval.setVisibility(View.GONE);
                                    request_join.setVisibility(View.GONE);
                                    delete_group_post.setVisibility(View.GONE);
                                    member.setVisibility(View.VISIBLE);

                                } else if (myGroupRole.equals("admin")) {
                                    post_approval.setVisibility(View.VISIBLE);
                                    request_join.setVisibility(View.VISIBLE);
                                    delete_group_post.setVisibility(View.GONE);
                                    member.setVisibility(View.VISIBLE);

                                } else if (myGroupRole.equals("creator")) {
                                    post_approval.setVisibility(View.VISIBLE);
                                    request_join.setVisibility(View.VISIBLE);
                                    delete_group_post.setVisibility(View.VISIBLE);
                                    member.setVisibility(View.VISIBLE);
                                }
                            }

                        }
                        // check status is exist user of group
                        checkIsExistUserOfGroup();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //check status is exist user of group
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
                if (isExist == true) {
                    Intent intent = new Intent(GroupPostOptionActivity.this, FollowersActivity.class);
                    intent.putExtra("id", groupPostId);
                    intent.putExtra("title", "memberGroup");
                    startActivity(intent);
                } else {
                    Snackbar.make(member, "You are not a member of this group", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        //show list request join user
        request_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    Intent intent = new Intent(GroupPostOptionActivity.this, FollowersActivity.class);
                    intent.putExtra("id", groupPostId);
                    intent.putExtra("title", "requestJoin");
                    startActivity(intent);
                } else {
                    Snackbar.make(member, "You are not a member of this group", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        //approval post
        post_approval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    Intent intent = new Intent(GroupPostOptionActivity.this, FollowersActivity.class);
                    intent.putExtra("id", groupPostId);
                    intent.putExtra("title", "approvalPost");
                    startActivity(intent);

                } else {
                    Snackbar.make(member, "You are not a member of this group", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

        //delete group => role: creator
        delete_group_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExist == true) {
                    //init dialog custom
                    Dialog dialog = new Dialog(GroupPostOptionActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.custom_dialog_layout);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                    dialog.setCancelable(true);

                    //add controls dialog custom
                    MaterialButton btn_confirm_dialog, btn_cancel_dialog;
                    TextView textviewtitile;

                    btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
                    btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
                    textviewtitile = dialog.findViewById(R.id.textviewtitile);
                    textviewtitile.setText("Are you sure want delete group ?");

                    //button confirm delete
                    btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteGroup(groupPostId, firebaseUser.getUid());
                            dialog.dismiss();
                        }
                    });

                    //button cancel delete
                    btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    //show dialog
                    dialog.show();

                } else {
                    Snackbar.make(member, "You are not a member of this group", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void deleteGroup(String groupPostId, String uid) {
        if (myGroupRole.equals(Constant.ROLE_CREATOR)) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
            reference.child(groupPostId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(GroupPostOptionActivity.this, "Group successfully delete", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GroupPostOptionActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            //failed delete group
                            Toast.makeText(GroupPostOptionActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void addControls() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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