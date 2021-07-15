package vn.edu.stu.luanvanmxhhippo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.edu.stu.Adapter.RolePostAdapter;
import vn.edu.stu.Model.GroupChatList;
import vn.edu.stu.Model.RolePost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.DataRolePost;

public class PostImageActivity extends AppCompatActivity {

    private MaterialButton btnPost, btnClean, btnNext, btnPrevious, btnPickImage;
    private ImageView imageViewBack;
    private EditText txtDecription;
    private ImageSwitcher imageSwitcher;

    private Spinner selectRolePost;

    private ArrayList<Uri> mArrayUri;
    private Uri imageUri = null;
    private String myUrl = "";

    private List<String> listNameGroup;
    private List<GroupChatList> groupChatLists;
    //permistion
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALARY_REQUEST_CODE = 400;

    //image pick constant
    private static final int IMAGE_PICK_GALARY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    //permission to be requested
    private String[] cameraPermission;
    private String[] galaryPermission;

    private int position = 0;
    private StorageTask uploadTask;
    private StorageReference storageReference;

    private ArrayList<RolePost> arrayListRole;

    private String TYPE_POST;

    private String rolePost = "";

    private GroupChatList groupChatListSelected;

    private FirebaseUser firebaseUser;

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        addControls();
        loadRoleSelect();
        checkPermission();

        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                return imageView;
            }
        });

        addEvents();

    }

    private void checkPermission() {
        cameraPermission = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        galaryPermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    private void loadRoleSelect() {
        arrayListRole = DataRolePost.getRolePostArrayList();
        RolePostAdapter rolePostAdapter = new RolePostAdapter(PostImageActivity.this, R.layout.role_post_item, arrayListRole);
        rolePostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectRolePost.setAdapter(rolePostAdapter);
    }

    private void checkTypeTextOrImage() {
        String decriptionn = txtDecription.getText().toString();
        if (decriptionn.isEmpty() && mArrayUri.isEmpty()) {
            Toast.makeText(PostImageActivity.this, R.string.please_pick_img_write_decrip, Toast.LENGTH_SHORT).show();
        } else {
            if (mArrayUri.isEmpty()) {
                TYPE_POST = "text";
            } else {
                TYPE_POST = "image";
            }
        }
    }
    
    private void addEvents() {
        selectRolePost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RolePost rolePoss = (RolePost) parent.getItemAtPosition(position);
                if (rolePoss.getIdRolePost().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {
                    showSelectedGroup();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String decription = txtDecription.getText().toString();
                if (decription.isEmpty() && mArrayUri.isEmpty()) {
                    Toast.makeText(PostImageActivity.this, R.string.please_pick_img_write_decrip, Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setTitle(R.string.upload_img);
                    progressDialog.setMessage(getString(R.string.please_wait_minute_dont_exit_app));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    if (!decription.isEmpty() && mArrayUri.isEmpty()) {
                        uploadText();
                    } else {
                        uploadImage();
                    }
                }
            }
        });

        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage();
            }
        });

        btnClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayUri.clear();
                imageSwitcher.setImageResource(R.drawable.noimage);
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    // decrease the position by 1
                    position--;
                    imageSwitcher.setImageURI(mArrayUri.get(position));
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < mArrayUri.size() - 1) {
                    // increase the position by 1
                    position++;
                    imageSwitcher.setImageURI(mArrayUri.get(position));
                } else {
                    //Toast.makeText(PostImageActivity.this, "Last Image Already Shown", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showSelectedGroup() {
        listNameGroup = new ArrayList<>();
        groupChatLists = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listNameGroup.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //if current user exists in participants of group then show group
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseUser.getUid()).exists()) {
                        GroupChatList model = dataSnapshot.getValue(GroupChatList.class);
                        groupChatLists.add(model);
                        listNameGroup.add(model.getGroudchatlist_grouptitle());
                    }
                }
                CharSequence[] charSequences = listNameGroup.toArray(new CharSequence[listNameGroup.size()]);
                ArrayAdapter<GroupChatList> adapter = new ArrayAdapter<>(PostImageActivity.this, android.R.layout.simple_list_item_1, groupChatLists);
                ListView listView = new ListView(PostImageActivity.this);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(PostImageActivity.this);
                builder.setTitle(R.string.choose_group);
                builder.setMessage(getString(R.string.participant_group_seen_post));
                listView.setAdapter(adapter);
                builder.setView(listView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        groupChatListSelected = groupChatLists.get(position);
                        alertDialog.dismiss();
                        Toast.makeText(PostImageActivity.this, groupChatLists.get(position).getGroudchatlist_grouptitle(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALARY_CODE) {
                if (data.getClipData() != null) {
                    int totalImage = data.getClipData().getItemCount();

                    for (int i = 0; i < totalImage; i++) {
                        Uri imgUri = data.getClipData().getItemAt(i).getUri();
                        mArrayUri.add(imgUri);
                    }
                    imageSwitcher.setImageURI(mArrayUri.get(0));
                    position = 0;
                } else {
                    mArrayUri.add(data.getData());
                    imageSwitcher.setImageURI(mArrayUri.get(0));
                    position = 0;
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                mArrayUri.add(imageUri);

                imageSwitcher.setImageURI(mArrayUri.get(0));
                position = 0;
            }
        }
    }

    private void addControls() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        btnPost = findViewById(R.id.btn_post);
        btnClean = findViewById(R.id.btn_clean);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        btnPickImage = findViewById(R.id.btn_chonanh);
        imageViewBack = findViewById(R.id.image_view_back);
        txtDecription = findViewById(R.id.txt_decription);
        imageSwitcher = findViewById(R.id.slider);

        selectRolePost = findViewById(R.id.selectRolePost);

        mArrayUri = new ArrayList<>();

        progressDialog = new ProgressDialog(PostImageActivity.this);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    Thread threadUploadImage = new Thread(new Runnable() {
        @Override
        public void run() {
            uploadImage();
        }
    });

    //upload text
    private void uploadText() {
        String decription = txtDecription.getText().toString();
        RolePost rolePost = (RolePost) selectRolePost.getSelectedItem();

        if (rolePost.getIdRolePost().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {
            if (groupChatListSelected != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                String postid = reference.push().getKey();
                HashMap<String, Object> hashMapImage = new HashMap<>();
                hashMapImage.put(Constant.POST_ID, postid);
                hashMapImage.put(Constant.POST_VIDEO, "");
                hashMapImage.put(Constant.POST_IMAGE, "");
                hashMapImage.put(Constant.POST_MEMBER, groupChatListSelected.getGroudchatlist_groupid() + "");
                hashMapImage.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_TEXT);
                hashMapImage.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
                hashMapImage.put(Constant.POST_RULES, rolePost.getIdRolePost());
                hashMapImage.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
                hashMapImage.put(Constant.POST_DESCRIPTION, decription);
                hashMapImage.put(Constant.POST_CATEGORY, "");
                hashMapImage.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                reference.child(postid).setValue(hashMapImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(PostImageActivity.this, R.string.post_susccessfull, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });

            } else {
                progressDialog.dismiss();
                Toast.makeText(this, R.string.please_choose_group, Toast.LENGTH_SHORT).show();
            }
        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
            String postid = reference.push().getKey();
            HashMap<String, Object> hashMapImage = new HashMap<>();
            hashMapImage.put(Constant.POST_ID, postid);
            hashMapImage.put(Constant.POST_VIDEO, "");
            hashMapImage.put(Constant.POST_IMAGE, "");
            hashMapImage.put(Constant.POST_MEMBER, "");
            hashMapImage.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_TEXT);
            hashMapImage.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
            hashMapImage.put(Constant.POST_RULES, rolePost.getIdRolePost());
            hashMapImage.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
            hashMapImage.put(Constant.POST_DESCRIPTION, decription);
            hashMapImage.put(Constant.POST_CATEGORY, "");
            hashMapImage.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.child(postid).setValue(hashMapImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Toast.makeText(PostImageActivity.this, R.string.post_susccessfull, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });

        }

    }

    //Upload image
    private void uploadImage() {
        //check type post
        checkTypeTextOrImage();

        String decription = txtDecription.getText().toString();
        RolePost rolePost = (RolePost) selectRolePost.getSelectedItem();
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        if (rolePost.getIdRolePost().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {
            if (groupChatListSelected != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                String postid = reference.push().getKey();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(Constant.POST_ID, postid);
                hashMap.put(Constant.POST_VIDEO, "");
                hashMap.put(Constant.POST_MEMBER, groupChatListSelected.getGroudchatlist_groupid() + "");
                hashMap.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
                hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_IMAGE);
                hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
                hashMap.put(Constant.POST_RULES, rolePost.getIdRolePost());
                hashMap.put(Constant.POST_DESCRIPTION, decription);
                hashMap.put(Constant.POST_CATEGORY, "");
                hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                reference.child(postid).setValue(hashMap);
                for (int uploadcount = 0; uploadcount < mArrayUri.size(); uploadcount++) {
                    StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mArrayUri.get(uploadcount)));
                    uploadTask = imageReference.putFile(mArrayUri.get(uploadcount));

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull @NotNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return imageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                myUrl = downloadUri.toString();

                                HashMap<String, String> imgList = new HashMap<>();
                                imgList.put(Constant.POST_POST_IMAGE, myUrl);

                                DatabaseReference imgReference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                                        .child(postid).child(Constant.POST_IMAGE);
                                imgReference1.push().setValue(imgList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            Toast.makeText(PostImageActivity.this, R.string.post_susccessfull, Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(PostImageActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                finish();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            progressDialog.dismiss();
                            finish();
                        }
                    });
                }
            }
            //not null
            else {
                progressDialog.dismiss();
                Toast.makeText(this, R.string.please_choose_group, Toast.LENGTH_SHORT).show();
            }
        }
        //type private, public
        else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
            String postid = reference.push().getKey();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constant.POST_ID, postid);
            hashMap.put(Constant.POST_VIDEO, "");
            hashMap.put(Constant.POST_MEMBER, "");
            hashMap.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
            hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_IMAGE);
            hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
            hashMap.put(Constant.POST_RULES, rolePost.getIdRolePost());
            hashMap.put(Constant.POST_DESCRIPTION, decription);
            hashMap.put(Constant.POST_CATEGORY, "");
            hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

            reference.child(postid).setValue(hashMap);
            for (int uploadcount = 0; uploadcount < mArrayUri.size(); uploadcount++) {
                StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mArrayUri.get(uploadcount)));
                uploadTask = imageReference.putFile(mArrayUri.get(uploadcount));

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull @NotNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return imageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            myUrl = downloadUri.toString();

                            HashMap<String, String> imgList = new HashMap<>();
                            imgList.put(Constant.POST_POST_IMAGE, myUrl);

                            DatabaseReference imgReference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                                    .child(postid).child(Constant.POST_IMAGE);
                            imgReference1.push().setValue(imgList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(PostImageActivity.this, R.string.post_susccessfull, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(PostImageActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        progressDialog.dismiss();
                        finish();
                    }
                });
            }
        }

    }

    private void showImage() {
        //option pick camera or gallery
        String[] option = {getString(R.string.camera), getString(R.string.gallary)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.pick_img))
                .setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //hanlde clicks
                        if (which == 0) {
                            //camera clicked
                            if (!checkCameraPermission()) {
                                //not granted, request
                                requestCameraPermission();
                            } else {
                                pickCamera();
                            }

                        } else {
                            //galary
                            if (!checkStoragePermission()) {
                                requestGalleryPermission();

                            } else {
                                pickGallery();
                            }
                        }
                    }
                }).show();
    }

    private void pickGallery() {
        //intent pick image from gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_GALARY_CODE);

       /* Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALARY_CODE);*/
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "PostImageTitile");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "PostImageDecription");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, galaryPermission, GALARY_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

}