package vn.edu.stu.luanvanmxhhippo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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

import vn.edu.stu.Adapter.PhotoAdpater;
import vn.edu.stu.Util.Constant;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private int position = 0;
    private ArrayList<Uri> mArrayUri;
    private String myUrl = "";
    public static final int IMAGE_CODE = 1;
    private StorageTask uploadTask;
    private StorageReference storageReference;

    private ImageView close, image_added;
    private TextView post;
    private EditText decription;

    private ImageSwitcher imageView;
    private MaterialButton materialButton, previous, next;
    private RecyclerView recyclerViewPhoto;
    private PhotoAdpater photoAdpater;

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

        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int totalImage = data.getClipData().getItemCount();

                for (int i = 0; i < totalImage; i++) {
                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                    mArrayUri.add(imgUri);
                }
                imageView.setImageURI(mArrayUri.get(0));
                position = 0;

            } else if (data.getData() != null) {
                Uri imageurl = data.getData();
                mArrayUri.add(imageurl);
                imageView.setImageURI(mArrayUri.get(0));
                position = 0;
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    Thread threadUploadImage = new Thread(new Runnable() {
        @Override
        public void run() {
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
    });

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

            /*startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();*/
        }


        /*if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postid = reference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.POST_ID, postid);
                        hashMap.put(Constant.POST_IMAGE, myUrl);
                        hashMap.put(Constant.POST_DESCRIPTION, decription.getText().toString());
                        hashMap.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.child(postid).setValue(hashMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "No image selected!", Toast.LENGTH_SHORT).show();
                }
            });
        }*/
    }

    private void addEvents() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*startActivity(new Intent(PostActivity.this, MainActivity.class));*/
                finish();
            }
        });

        materialButton.setOnClickListener(new View.OnClickListener() {
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

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uploadImage();
                threadUploadImage.start();
            }
        });

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

    private void addControls() {
        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        decription = findViewById(R.id.description);

        materialButton = findViewById(R.id.btnMutilImage);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        imageView = findViewById(R.id.image);

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