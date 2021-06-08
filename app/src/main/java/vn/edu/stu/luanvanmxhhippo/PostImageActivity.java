package vn.edu.stu.luanvanmxhhippo;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
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

import vn.edu.stu.Adapter.RolePostAdapter;
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
    private String myUrl = "";

    public static final int IMAGE_CODE = 1;

    private int position = 0;
    private StorageTask uploadTask;
    private StorageReference storageReference;

    ArrayList<RolePost> arrayListRole;

    private String TYPE_POST;

    private String rolePost = "";

    public ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        addControls();
        loadRoleSelect();
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                return imageView;
            }
        });

        addEvents();

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
            Toast.makeText(PostImageActivity.this, "Please pick image or write decription", Toast.LENGTH_SHORT).show();
        } else {
            if (mArrayUri.isEmpty()) {
                TYPE_POST = "text";
            } else {
                TYPE_POST = "image";
            }
        }
    }


    private void addEvents() {
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
                    Toast.makeText(PostImageActivity.this, "Please pick image or write decription", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setTitle("Upload image");
                    progressDialog.setMessage("Please wait a minutes, don't exit app");
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
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_CODE);
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
                    Toast.makeText(PostImageActivity.this, "Last Image Already Shown", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                int totalImage = data.getClipData().getItemCount();

                for (int i = 0; i < totalImage; i++) {
                    Uri imgUri = data.getClipData().getItemAt(i).getUri();
                    mArrayUri.add(imgUri);
                }
                imageSwitcher.setImageURI(mArrayUri.get(0));
                position = 0;

            } else if (data.getData() != null) {
                Uri imageurl = data.getData();
                mArrayUri.add(imageurl);
                imageSwitcher.setImageURI(mArrayUri.get(0));
                position = 0;
            }
        }
    }

    private void addControls() {
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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        String postid = reference.push().getKey();
        HashMap<String, Object> hashMapImage = new HashMap<>();
        hashMapImage.put(Constant.POST_ID, postid);
        hashMapImage.put(Constant.POST_VIDEO, "");
        hashMapImage.put(Constant.POST_IMAGE, "");
        hashMapImage.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_TEXT);
        hashMapImage.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
        hashMapImage.put(Constant.POST_RULES, rolePost.getIdRolePost());
        hashMapImage.put(Constant.POST_DESCRIPTION, decription);
        hashMapImage.put(Constant.POST_PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.child(postid).setValue(hashMapImage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    Toast.makeText(PostImageActivity.this, "Post successfull", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

    }

    //Upload image
    private void uploadImage() {
        //check type post
        checkTypeTextOrImage();

        String decription = txtDecription.getText().toString();
        RolePost rolePost = (RolePost) selectRolePost.getSelectedItem();

        storageReference = FirebaseStorage.getInstance().getReference("posts");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        String postid = reference.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.POST_ID, postid);
        hashMap.put(Constant.POST_VIDEO, "");
        hashMap.put(Constant.POST_TYPE, Constant.DEFAULT_POST_TYPE_IMAGE);
        hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);
        hashMap.put(Constant.POST_RULES, rolePost.getIdRolePost());
        hashMap.put(Constant.POST_DESCRIPTION, txtDecription.getText().toString());
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
                        imgReference1.push().setValue(imgList).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(PostImageActivity.this, "Post successfull", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(PostImageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
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