package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import vn.edu.stu.Adapter.RoleGroupPostAdapter;
import vn.edu.stu.Model.RoleGroupPost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.DataRoleGroupPost;

public class GroupPostCreateActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout text_group_post_name;
    private Spinner role_selected_group_post;

    private ArrayList<RoleGroupPost> roleGroupPosts;

    private MaterialButton btn_create_group_post;

    private RoleGroupPost roleGroupPostSelected = null;

    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_create);

        addControls();
        addEvents();
        loadRoleGroupPost();
    }

    private void loadRoleGroupPost() {
        roleGroupPosts.clear();
        roleGroupPosts = DataRoleGroupPost.getRoleGroupPostArrayList();

        RoleGroupPostAdapter roleGroupPostAdapter = new RoleGroupPostAdapter(GroupPostCreateActivity.this, R.layout.role_post_item, roleGroupPosts);
        roleGroupPostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role_selected_group_post.setAdapter(roleGroupPostAdapter);
    }

    private void addEvents() {
        btn_create_group_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroupPost();
            }
        });
    }

    private void createGroupPost() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating group");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String nameGroup = text_group_post_name.getEditText().getText().toString().trim();
        roleGroupPostSelected = (RoleGroupPost) role_selected_group_post.getSelectedItem();

        String timeStamp = System.currentTimeMillis() + "";

        if (TextUtils.isEmpty(nameGroup) || roleGroupPostSelected == null) {
            progressDialog.dismiss();
            Snackbar.make(btn_create_group_post, "Please enter all the information", BaseTransientBottomBar.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> hashMapGroupPost = new HashMap<>();
            hashMapGroupPost.put(Constant.GROUP_POST_ID, timeStamp);
            hashMapGroupPost.put(Constant.GROUP_POST_TITLE, nameGroup);
            hashMapGroupPost.put(Constant.GROUP_POST_DECRIPTION, "");
            hashMapGroupPost.put(Constant.GROUP_POST_ICON, Constant.IMAGE_PROFILE);
            hashMapGroupPost.put(Constant.GROUP_POST_CREATEBY, firebaseUser.getUid());
            hashMapGroupPost.put(Constant.GROUP_POST_TIMESTAMP, timeStamp);
            hashMapGroupPost.put(Constant.GROUP_POST_ROLE, roleGroupPostSelected.getIdRoleGroupPost());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
            reference.child(timeStamp)
                    .setValue(hashMapGroupPost)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("uid", firebaseUser.getUid());
                            hashMap1.put("role", "creator");
                            hashMap1.put("timestamp", timeStamp);

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
                            reference1.child(timeStamp)
                                    .child(Constant.COLLECTION_PARTICIPANTS)
                                    .child(firebaseUser.getUid())
                                    .setValue(hashMap1)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //participant added
                                            progressDialog.dismiss();
                                            finish();
                                            Toast.makeText(GroupPostCreateActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    //failed adding participant
                                    progressDialog.dismiss();
                                    Toast.makeText(GroupPostCreateActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            progressDialog.dismiss();
                        }
                    });
        }

    }

    private void addControls() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        toolbar = findViewById(R.id.toolbar);
        text_group_post_name = findViewById(R.id.text_group_post_name);
        role_selected_group_post = findViewById(R.id.role_selected_group_post);
        btn_create_group_post = findViewById(R.id.btn_create_group_post);

        roleGroupPosts = new ArrayList<>();
    }
}