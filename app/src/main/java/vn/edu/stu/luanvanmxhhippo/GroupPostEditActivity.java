package vn.edu.stu.luanvanmxhhippo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import vn.edu.stu.Adapter.RoleGroupPostAdapter;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.RoleGroupPost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.DataRoleGroupPost;

public class GroupPostEditActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView img_group_post;
    private MaterialButton btn_change_photo, btn_save;
    private TextInputEditText txt_title_group_post, txt_title_decription_group_post;
    private Spinner role_selected_group_post;

    private String groupPostId = "";

    private ArrayList<RoleGroupPost> roleGroupPosts;
    private RoleGroupPost roleGroupPostSelected = null;

    //permistion constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALARY_CODE = 400;

    //picked image uri
    private Uri image_uri = null;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_post_edit);

        addControls();
        getDataIntent();

        addEvents();

        loadRoleGroupPost();
        getInfoGroupPost();
    }

    private void addEvents() {
        btn_change_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfoGroupPost();
            }
        });
    }

    private void saveInfoGroupPost() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Group...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //input title, decription
        String groupTitle = txt_title_group_post.getText().toString().trim();
        String groupDecription = txt_title_decription_group_post.getText().toString().trim();
        roleGroupPostSelected = (RoleGroupPost) role_selected_group_post.getSelectedItem();

        //check null or empty
        if (TextUtils.isEmpty(groupTitle) || roleGroupPostSelected == null || TextUtils.isEmpty(groupDecription)) {
            progressDialog.dismiss();
            Toast.makeText(this, "Group Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (image_uri == null) {
            //update group image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constant.GROUP_POST_TITLE, groupTitle);
            hashMap.put(Constant.GROUP_POST_DECRIPTION, groupDecription);
            hashMap.put(Constant.GROUP_POST_ROLE, roleGroupPostSelected.getIdRoleGroupPost());

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
            reference.child(groupPostId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updating successfully
                            progressDialog.dismiss();
                            Toast.makeText(GroupPostEditActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(GroupPostEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //update group with image
            String timestamp = "" + System.currentTimeMillis();

            //upload image
            //image name and path
            String fileNameAndPath = "Group_Images/" + "image" + timestamp;

            //upload image to fireStorage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get Url
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadUrl = uriTask.getResult();
                            if (uriTask.isSuccessful()) {

                                //update group image
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(Constant.GROUP_POST_TITLE, groupTitle);
                                hashMap.put(Constant.GROUP_POST_DECRIPTION, groupDecription);
                                hashMap.put(Constant.GROUP_POST_ROLE, roleGroupPostSelected.getIdRoleGroupPost());
                                hashMap.put(Constant.GROUP_POST_ICON, "" + downloadUrl);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
                                reference.child(groupPostId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updating successfully
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupPostEditActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                //update failed
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupPostEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            //image upload failed
                            progressDialog.dismiss();
                            Toast.makeText(GroupPostEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void showImagePickDialog() {
        //option to pick image from
        String[] option = {"Camera", "Galary"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image:")
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle click
                        if (which == 0) {
                            //camera clicked
                            if (!checkCameraPermissions()) {
                                requestCameraPermissions();
                            } else {
                                pickFromGCamera();
                            }

                        } else {
                            //galary pick
                            if (!checkStoragePermission()) {
                                requestStoragePermisstion();
                            } else {
                                pickFromGallary();
                            }
                        }
                    }
                }).show();
    }

    private void pickFromGallary() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALARY_CODE);
    }

    private void pickFromGCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Image Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Image Icon Decription");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermisstion() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        //handle permission result
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromGCamera();
                    } else {
                        //both or one is denied
                        Toast.makeText(this, R.string.txt_camera_storage_required, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallary();
                    } else {
                        //permission denied
                        Toast.makeText(this, R.string.txt_storage_permission_requierd, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        //handle image pick result
        if (requestCode == IMAGE_PICK_GALARY_CODE) {
            //was pick from gallary
            if (data != null) {
                image_uri = data.getData();
                //set to imageview
                img_group_post.setImageURI(image_uri);
            }

        } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
            //was pick from camera
            //set to imageview
            img_group_post.setImageURI(image_uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getInfoGroupPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        GroupPost groupPost = snapshot.getValue(GroupPost.class);
                        if (groupPost != null) {
                            try {
                                Glide.with(GroupPostEditActivity.this).load(groupPost.getGrouppost_icon())
                                        .placeholder(R.drawable.placeholder)
                                        .into(img_group_post);
                            } catch (Exception e) {
                                img_group_post.setImageResource(R.drawable.placeholder);
                            }

                            txt_title_group_post.setText(groupPost.getGrouppost_title());
                            txt_title_decription_group_post.setText(groupPost.getGrouppost_decription());

                            for (RoleGroupPost roleGroupPost : roleGroupPosts) {
                                if (roleGroupPost.getIdRoleGroupPost().equals(groupPost.getGrouppost_role())) {
                                    role_selected_group_post.setSelection(roleGroupPosts.indexOf(roleGroupPost));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadRoleGroupPost() {
        roleGroupPosts.clear();
        roleGroupPosts = DataRoleGroupPost.getRoleGroupPostArrayList();

        RoleGroupPostAdapter roleGroupPostAdapter = new RoleGroupPostAdapter(GroupPostEditActivity.this, R.layout.role_post_item, roleGroupPosts);
        roleGroupPostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role_selected_group_post.setAdapter(roleGroupPostAdapter);
    }

    private void getDataIntent() {
        groupPostId = getIntent().getStringExtra("groupPostId");
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Edit group");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        img_group_post = findViewById(R.id.img_group_post);
        btn_change_photo = findViewById(R.id.btn_change_photo);
        btn_save = findViewById(R.id.btn_save);
        txt_title_group_post = findViewById(R.id.txt_title_group_post);
        txt_title_decription_group_post = findViewById(R.id.txt_title_decription_group_post);
        role_selected_group_post = findViewById(R.id.role_selected_group_post);

        roleGroupPosts = new ArrayList<>();

    }
}