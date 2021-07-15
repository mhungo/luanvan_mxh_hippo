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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import vn.edu.stu.Util.Constant;

public class GroupEditActivity extends AppCompatActivity {

    //permistion constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALARY_CODE = 400;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;

    private FirebaseAuth firebaseAuth;

    private ImageView groupIconIv;
    private EditText groupTitleEt, groupDecriptionEt;
    private FloatingActionButton updateGroupBtn;

    private ProgressDialog progressDialog;

    //picked image uri
    private Uri image_uri = null;

    private Toolbar toolbar;
    private ActionBar actionBar;

    private String groupId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        addControls();
        getDataIntent();
        addEvents();
        checkUser();
        loadGroupInfo();
    }

    private void addEvents() {
        //pick image
        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        //handle click event
        updateGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdatingGroup();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.orderByChild(Constant.GROUP_ID).equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //get data
                    String groupId = "" + dataSnapshot.child(Constant.GROUP_ID).getValue();
                    String groupTitle = "" + dataSnapshot.child(Constant.GROUP_TITLE).getValue();
                    String groupIcon = "" + dataSnapshot.child(Constant.GROUP_ICON).getValue();
                    String groupDes = "" + dataSnapshot.child(Constant.GROUP_DECRIPTION).getValue();
                    String createBy = "" + dataSnapshot.child(Constant.GROUP_CREATEBY).getValue();
                    String timestamp = "" + dataSnapshot.child(Constant.GROUP_TIMESTAMP).getValue();

                    //convert time
                    //convert time stamp to dd/mm/yyy hh:mm am/pm
                    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    groupTitleEt.setText(groupTitle);
                    groupDecriptionEt.setText(groupDes);

                    try {
                        Glide.with(GroupEditActivity.this).load(groupIcon)
                                .placeholder(R.drawable.placeholder)
                                .into(groupIconIv);
                    } catch (Exception e) {
                        groupIconIv.setImageResource(R.drawable.placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getDataIntent() {
        groupId = getIntent().getStringExtra("groupId");
    }

    private void startUpdatingGroup() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.update_group));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //input title, decription
        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDecription = groupDecriptionEt.getText().toString().trim();

        //check null or empty
        if (TextUtils.isEmpty(groupTitle)) {
            Toast.makeText(this, R.string.group_title_is_require, Toast.LENGTH_SHORT).show();
            return;
        }

        if (image_uri == null) {
            //update group image
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constant.GROUP_TITLE, groupTitle);
            hashMap.put(Constant.GROUP_DECRIPTION, groupDecription);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
            reference.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //updating successfully
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, R.string.group_info_updated, Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                hashMap.put(Constant.GROUP_TITLE, groupTitle);
                                hashMap.put(Constant.GROUP_DECRIPTION, groupDecription);
                                hashMap.put(Constant.GROUP_ICON, "" + downloadUrl);

                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                                reference.child(groupId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //updating successfully
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, R.string.group_info_updated, Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                //update failed
                                                progressDialog.dismiss();
                                                Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void showImagePickDialog() {
        //option to pick image from
        String[] option = {getString(R.string.camera), getString(R.string.gallary)};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_img)
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

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            /*actionBar.setTitle(user.getEmail());*/
        }
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
                groupIconIv.setImageURI(image_uri);
            }

        } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
            //was pick from camera
            //set to imageview
            groupIconIv.setImageURI(image_uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_group);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //init UI view
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleEt = findViewById(R.id.groupTitile);
        groupDecriptionEt = findViewById(R.id.groupDecription);
        updateGroupBtn = findViewById(R.id.updateGroupBtn);

        //init permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
    }
}