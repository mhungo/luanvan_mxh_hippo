package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;

import vn.edu.stu.Util.Constant;

public class PostVideoActivity extends AppCompatActivity {

    private Uri uriVideo;
    public static final int VIDEO_PICK = 100;

    private MaterialButton btnPost, btnPickVideo;
    private ImageView imageViewBack;
    private EditText txtDecription;

    private String TYPE_POST;
    private String myUrl = "";

    private StorageTask uploadTask;
    private StorageReference storageReference;

    private VideoView videoView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        addControls();
        addEvents();
    }

    private void addEvents() {
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Upload video");
                progressDialog.setMessage("Please wait a minutes, don't exit app");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                threadUploadVideo.start();
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
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK);
            }
        });
    }

    Thread threadUploadVideo = new Thread(new Runnable() {
        @Override
        public void run() {
            uploadVideo();
        }
    });

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

    //Upload video
    private void uploadVideo() {

        //check type post
        checkTypeTextOrVideo();

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        //check null urivideo
        if (uriVideo != null) {
            //upload video to firebase storage
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

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
                        String postid = reference.push().getKey();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.POST_ID, postid);
                        hashMap.put(Constant.POST_VIDEO, myUrl);
                        hashMap.put(Constant.POST_TYPE, TYPE_POST);
                        hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
                        hashMap.put(Constant.POST_RULES, Constant.DEFAULT_POST_RULES);
                        hashMap.put(Constant.POST_DESCRIPTION, txtDecription.getText().toString());
                        hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                        //dismit progress
                        progressDialog.dismiss();
                        finish();

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK) {
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

    private void addControls() {
        btnPickVideo = findViewById(R.id.btn_pickvidedo);
        btnPost = findViewById(R.id.btn_post);
        imageViewBack = findViewById(R.id.image_view_back);
        txtDecription = findViewById(R.id.txt_decription);

        videoView = findViewById(R.id.videoView);

        progressDialog = new ProgressDialog(PostVideoActivity.this);
    }

}