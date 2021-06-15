package vn.edu.stu.luanvanmxhhippo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import vn.edu.stu.Adapter.RolePostAdapter;
import vn.edu.stu.Model.RolePost;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.DataRolePost;

public class PostVideoActivity extends AppCompatActivity {

    private Uri uriVideo;
    public static final int VIDEO_PICK = 1000;
    public static final int VIDEO_CAMERA_PICK = 2000;

    //permistion
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALARY_REQUEST_CODE = 400;

    //permission to be requested
    private String[] cameraPermission;
    private String[] galaryPermission;

    private MaterialButton btnPost, btnPickVideo;
    private ImageView imageViewBack;
    private EditText txtDecription;

    private String TYPE_POST;
    private String myUrl = "";

    private StorageTask uploadTask;
    private StorageReference storageReference;

    private Spinner selectRolePost;

    ArrayList<RolePost> arrayListRole;

    private VideoView videoView;

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        addControls();
        loadRoleSelect();
        addEvents();
    }

    private void addEvents() {
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String decription = txtDecription.getText().toString();

                if (decription.isEmpty() && uriVideo == null) {
                    Toast.makeText(PostVideoActivity.this, "Please pick video or write decription", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setTitle("Upload video");
                    progressDialog.setMessage("Please wait a minutes, don't exit app");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    if (!decription.isEmpty() && uriVideo == null) {
                        uploadText();
                    } else {
                        uploadVideo();
                    }

                }
            }
        });

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPickVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVideo();

            }
        });
    }

    private void showVideo() {
        //option pick camera or gallery
        String[] option = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(PostVideoActivity.this);
        builder.setTitle("Pick Video")
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
                                //camera clicked
                                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                startActivityForResult(intent, VIDEO_CAMERA_PICK);
                            }

                        } else {
                            //galary
                            if (!checkStoragePermission()) {
                                requestGalleryPermission();

                            } else {
                                //galary
                                Intent intent = new Intent();
                                intent.setType("video/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK);
                            }
                        }

                    }
                }).show();
    }

    Thread threadUploadVideo = new Thread(new Runnable() {
        @Override
        public void run() {
            uploadVideo();
        }
    });

    private void loadRoleSelect() {
        arrayListRole = DataRolePost.getRolePostArrayList();
        RolePostAdapter rolePostAdapter = new RolePostAdapter(PostVideoActivity.this, R.layout.role_post_item, arrayListRole);
        rolePostAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectRolePost.setAdapter(rolePostAdapter);
    }

    //check type text or video
    private void checkTypeTextOrVideo() {
        String decriptionn = txtDecription.getText().toString();
        if (decriptionn.isEmpty() && uriVideo != null) {
            Toast.makeText(PostVideoActivity.this, "Please pick video or write decription", Toast.LENGTH_SHORT).show();
        } else {
            if (uriVideo == null) {
                TYPE_POST = "text";
            } else {
                TYPE_POST = "video";
            }
        }
    }

    //upload text
    private void uploadText() {
        //check type post
        checkTypeTextOrVideo();
        String decription = txtDecription.getText().toString();
        RolePost rolePost = (RolePost) selectRolePost.getSelectedItem();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        String postid = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.POST_ID, postid);
        hashMap.put(Constant.POST_VIDEO, "");
        hashMap.put(Constant.POST_IMAGE, "");
        hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_TEXT);
        hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
        hashMap.put(Constant.POST_RULES, rolePost.getIdRolePost());
        hashMap.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
        hashMap.put(Constant.POST_DESCRIPTION, decription);
        hashMap.put(Constant.POST_CATEGORY, "");
        hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                //dismit progress
                progressDialog.dismiss();
                Toast.makeText(PostVideoActivity.this, "Post successfull", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    //Upload video
    private void uploadVideo() {

        //check type post
        checkTypeTextOrVideo();
        RolePost rolePost = (RolePost) selectRolePost.getSelectedItem();

        //upload video to firebase storage
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        StorageReference videoReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uriVideo));
        uploadTask = videoReference.putFile(uriVideo);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull @NotNull Task task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return videoReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    myUrl = downloadUri.toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                    String postid = reference.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(Constant.POST_ID, postid);
                    hashMap.put(Constant.POST_VIDEO, myUrl);
                    hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_VIDEO);
                    hashMap.put(Constant.POST_IMAGE, "");
                    hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
                    hashMap.put(Constant.POST_RULES, rolePost.getIdRolePost());
                    hashMap.put(Constant.POST_TIMESTAMP, System.currentTimeMillis() + "");
                    hashMap.put(Constant.POST_DESCRIPTION, txtDecription.getText().toString());
                    hashMap.put(Constant.POST_CATEGORY, "");
                    hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                    reference.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(PostVideoActivity.this, "Post successfull", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });

                } else {
                    Toast.makeText(PostVideoActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK) {
                uriVideo = data.getData();
                setVideoToVideoView();
            }
            if (requestCode == VIDEO_CAMERA_PICK) {
                uriVideo = data.getData();
                setVideoToVideoView();
            }
        }
    }

    //set video clicked to video view
    private void setVideoToVideoView() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        //set media controller to video view
        videoView.setMediaController(mediaController);
        //set video uri
        videoView.setVideoURI(uriVideo);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.pause();
            }
        });

    }

    //get file extension image or video
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result & result1;
    }

    private void requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, galaryPermission, GALARY_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
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

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private void addControls() {

        selectRolePost = findViewById(R.id.selectRolePost);

        btnPickVideo = findViewById(R.id.btn_pickvidedo);
        btnPost = findViewById(R.id.btn_post);
        imageViewBack = findViewById(R.id.image_view_back);
        txtDecription = findViewById(R.id.txt_decription);

        videoView = findViewById(R.id.videoView);

        progressDialog = new ProgressDialog(PostVideoActivity.this);
    }

}