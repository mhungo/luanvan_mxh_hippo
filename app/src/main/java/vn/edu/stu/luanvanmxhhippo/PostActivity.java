package vn.edu.stu.luanvanmxhhippo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

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

import java.util.ArrayList;
import java.util.HashMap;

import vn.edu.stu.Util.Constant;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private Uri uriVideo;
    private int position = 0;
    private ArrayList<Uri> mArrayUri;
    private String myUrl = "";
    public static final int IMAGE_CODE = 1;
    public static final int VIDEO_PICK = 100;
    private StorageTask uploadTask;
    private StorageReference storageReference;

    //flag = true: image, false: video
    private boolean flagTypePost;

    private VideoView videoView;

    private ImageView close, image_added;
    private TextView post;
    private EditText decription;

    private LinearLayout linearLayoutImage, linearLayoutVideo;

    private ImageSwitcher imageView;
    private MaterialButton btnImage, btnVideo, previous, next, cleanImage, cleanVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        addControls();
        imageView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView1 = new ImageView(getApplicationContext());
                return imageView1;
            }
        });
        addEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            image_added.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            *//*startActivity(new Intent(PostActivity.this, MainActivity.class));*//*
            finish();
        }*/

        //Image Pick
        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int totalImage = data.getClipData().getItemCount();

                for (int i = 0; i < totalImage; i++) {
                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                    mArrayUri.add(imgUri);
                }
                imageView.setImageURI(mArrayUri.get(0));
                position = 0;

                flagTypePost = true;

            } else if (data.getData() != null) {
                Uri imageurl = data.getData();
                mArrayUri.add(imageurl);
                imageView.setImageURI(mArrayUri.get(0));
                position = 0;
                flagTypePost = true;
            }
        }
        //Video Pick
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK) {
                uriVideo = data.getData();
                setVideoToVideoView();
                flagTypePost = false;
            }
        }

    }

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

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    Thread threadUploadImage = new Thread(new Runnable() {
        @Override
        public void run() {
            if (flagTypePost == true) {
                uploadImage();
            } else {
                uploadVideo();
            }

        }
    });

    //Upload video
    private void uploadVideo() {
        //Time stamp
        String timestamp = "" + System.currentTimeMillis();

        //file path
        String filePath = "posts/" + "video_" + timestamp;
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
                    return storageReference.getDownloadUrl();
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
                        hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_VIDEO);
                        hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
                        hashMap.put(Constant.POST_RULES, Constant.DEFAULT_POST_RULES);
                        hashMap.put(Constant.POST_DESCRIPTION, decription.getText().toString());
                        hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {

                }
            });
        }


    }

    //Upload image
    private void uploadImage() {
        storageReference = FirebaseStorage.getInstance().getReference("posts");
        if (mArrayUri != null) {

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
            String postid = reference.push().getKey();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constant.POST_ID, postid);
            hashMap.put(Constant.POST_VIDEO, "");
            hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_IMAGE);
            hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
            hashMap.put(Constant.POST_RULES, Constant.DEFAULT_POST_RULES);
            hashMap.put(Constant.POST_DESCRIPTION, decription.getText().toString());
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
                            imgList.put("image", myUrl);

                            DatabaseReference imgReference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                                    .child(postid).child(Constant.POST_IMAGE);
                            imgReference1.push().setValue(imgList);

                        } else {
                            Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
            }

            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }

    private void addEvents() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*startActivity(new Intent(PostActivity.this, MainActivity.class));*/
                finish();
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*CropImage.activity()
                        .setAspectRatio(1, 1)
                        .start(PostActivity.this);*/
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_CODE);
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK);
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uploadImage();
                threadUploadImage.start();
            }
        });

        //Click next image
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < mArrayUri.size() - 1) {
                    // increase the position by 1
                    position++;
                    imageView.setImageURI(mArrayUri.get(position));
                } else {
                    Toast.makeText(PostActivity.this, "Last Image Already Shown", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Click clean list image
        cleanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayUri.clear();
                imageView.setImageResource(R.drawable.noimage);
            }
        });

        // click here to view previous image
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position > 0) {
                    // decrease the position by 1
                    position--;
                    imageView.setImageURI(mArrayUri.get(position));
                }
            }
        });
    }

    private void checkTypePost() {
        String txtDecription = decription.getText().toString();
        if (mArrayUri.isEmpty() && txtDecription.isEmpty()) {

        }
    }

    private void addControls() {

        linearLayoutImage = findViewById(R.id.linear_layout_image);
        linearLayoutVideo = findViewById(R.id.linear_layout_video);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        decription = findViewById(R.id.description);

        btnImage = findViewById(R.id.btnMutilImage);
        btnVideo = findViewById(R.id.btnVideo);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        cleanImage = findViewById(R.id.clearimage);
        imageView = findViewById(R.id.image);

        videoView = findViewById(R.id.videoView);

        mArrayUri = new ArrayList<>();

    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}