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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import vn.edu.stu.Adapter.GroupChatAdapter;
import vn.edu.stu.Model.GroupChat;
import vn.edu.stu.Util.Constant;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;
    private ImageView groupIconIv;
    private TextView groupTitleTv;
    private ImageButton btnSend, btnAttack;
    private EditText messageEt;

    private String groupId, myGroupRole = "";

    private RecyclerView chatRv;

    private ArrayList<GroupChat> groupChatArrayList;
    private GroupChatAdapter groupChatAdapter;

    //permistion
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALARY_REQUEST_CODE = 400;

    //image pick constant
    private static final int IMAGE_PICK_GALARY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    //permission to be requested
    private String[] cameraPermission;
    private String[] galaryPermission;

    //uri pick
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        addControls();

        //inti permisstion
        checkPermission();

        //getId group
        getDataIntent();
        addEvents();

        //load info group
        loadGroupInfo();
        loadGroupMessages();
        loadMyGroupRole();
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

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.child(groupId).child("Participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child("role").getValue();

                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadGroupMessages() {
        //init list
        groupChatArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        groupChatArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            GroupChat model = ds.getValue(GroupChat.class);
                            groupChatArrayList.add(model);
                        }

                        //adapter
                        groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, groupChatArrayList);
                        //set to recyecler view
                        chatRv.setAdapter(groupChatAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void addEvents() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message = messageEt.getText().toString().trim();
                //validation
                if (TextUtils.isEmpty(message)) {
                    //empty, don't send
                    Toast.makeText(GroupChatActivity.this, "Please input message", Toast.LENGTH_SHORT).show();
                } else {
                    //send message
                    sendMessage(message);
                }
            }
        });

        //click attack btn
        btnAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick image from a
                showImage();
            }
        });
    }

    private void showImage() {
        //option pick camera or gallery
        String[] option = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALARY_CODE);
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitile");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDecription");

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

    private void sendMessage(String message) {

        //timestamp
        String timestamp = "" + System.currentTimeMillis();

        //setup message data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", "" + firebaseAuth.getUid());
        hashMap.put("message", "" + message);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("type", "" + "text"); //text/image/file

        //add to databaseList()
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //message sent
                        //clean message
                        messageEt.setText("");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                //message sending failed
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void sendImageMessage() {
        //progress dialog
        ProgressDialog pb = new ProgressDialog(this);
        pb.setTitle("Please wait");
        pb.setMessage("Sending Image...");
        pb.setCanceledOnTouchOutside(false);
        pb.show();

        //file name and path in firebase storage
        String urlPath = "GroupChatImage/" + "" + System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(urlPath);

        //upload image
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded, get url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUrl = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            //upload database
                            //timestamp
                            String timestamp = "" + System.currentTimeMillis();

                            //setup message data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", "" + firebaseAuth.getUid());
                            hashMap.put("message", "" + downloadUrl);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("type", "" + "image"); //text/image/file

                            //add to databaseList()
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                            reference.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //message sent
                                            //clean message
                                            messageEt.setText("");
                                            pb.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    //message sending failed
                                    pb.dismiss();
                                    Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //fail upload image
                        Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        pb.dismiss();
                    }
                });

    }

    private void getDataIntent() {
        //getId chat group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupTitile = "" + ds.child("groupTitle").getValue();
                            String groupDecription = "" + ds.child("groupDecription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createBy = "" + ds.child("createBy").getValue();

                            groupTitleTv.setText(groupTitile);
                            try {
                                Glide.with(GroupChatActivity.this).load(groupIcon)
                                        .placeholder(R.drawable.placeholder)
                                        .into(groupIconIv);
                            } catch (Exception e) {
                                groupIconIv.setImageResource(R.drawable.group);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitle);
        btnAttack = findViewById(R.id.attchBtn);
        btnSend = findViewById(R.id.sendBtn);
        messageEt = findViewById(R.id.messageEt);

        firebaseAuth = FirebaseAuth.getInstance();

        chatRv = findViewById(R.id.chatRv);

        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_participants_menu, menu);

        menu.findItem(R.id.add_group);

        if (myGroupRole.equals("creator") || myGroupRole.equals("admin")) {
            //im admin/creator, show add person option
            menu.findItem(R.id.add_group).setVisible(true);
        } else {
            menu.findItem(R.id.add_group).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == (R.id.add_group)) {
            Intent intent = new Intent(GroupChatActivity.this, GroupParticipantAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALARY_CODE) {
                // got image from gallery
                imageUri = data.getData();
                sendImageMessage();

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //pick from camera
                sendImageMessage();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStoraeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStoraeAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case GALARY_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStoraeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStoraeAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Camera & Storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;


        }


    }
}