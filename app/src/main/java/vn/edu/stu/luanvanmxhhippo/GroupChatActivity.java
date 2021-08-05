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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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
    private LinearLayoutManager linearLayoutManager;

    //lazy load message group
    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";
    private SwipeRefreshLayout mRefreshLayout;
    private String keyTemp = "";
    //-----------------------------------

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

        //getId group
        getDataIntent();

        addControls();

        //inti permisstion
        checkPermission();


        addEvents();

        //load info group
        loadGroupInfo();
        //loadGroupMessages();
        loadMessages();
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
        ref.child(groupId).child(Constant.COLLECTION_PARTICIPANTS)
                .orderByChild(Constant.ROLE_UID).equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            myGroupRole = "" + ds.child(Constant.ROLE_ROLE).getValue();
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
                        groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, groupChatArrayList, groupId);
                        //set to recyecler view
                        chatRv.setAdapter(groupChatAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadMessages() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                .child(groupId).child(Constant.COLLECTION_MESSAGES);

        Query query = reference.limitToLast(mCurrentPage + TOTAL_ITEM_TO_LOAD);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupChat groupChat = snapshot.getValue(GroupChat.class);
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = snapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                groupChatArrayList.add(groupChat);
                groupChatAdapter.notifyDataSetChanged();
                chatRv.scrollToPosition(groupChatArrayList.size() - 1);

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                GroupChat groupChat = snapshot.getValue(GroupChat.class);
                for (GroupChat groupChat1 : groupChatArrayList) {
                    if (groupChat.getGroudchat_id().equals(groupChat1.getGroudchat_id())) {
                        groupChatArrayList.remove(groupChat1);
                        Toast.makeText(GroupChatActivity.this, getString(R.string.mesages_has_been_withdraw), Toast.LENGTH_SHORT).show();
                        groupChatAdapter.notifyDataSetChanged();
                        break;
                    }
                }

                chatRv.scrollToPosition(groupChatArrayList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMoreMessages() {
        String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                .child(groupId).child(Constant.COLLECTION_MESSAGES);

        Query query = reference.orderByKey().endAt(mLastKey).limitToLast(10);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupChat groupChat = snapshot.getValue(GroupChat.class);
                String messageKey = snapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    groupChatArrayList.add(itemPos++, groupChat);

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }
                groupChatAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                linearLayoutManager.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Event load them message +10
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mLastKey == "") {
                    mRefreshLayout.setRefreshing(false);
                } else {
                    if (!mLastKey.equalsIgnoreCase(keyTemp)) {
                        mCurrentPage++;
                        itemPos = 0;
                        loadMoreMessages();
                        keyTemp = mLastKey;
                    } else {
                        mRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String message = messageEt.getText().toString().trim();
                //validation
                if (TextUtils.isEmpty(message)) {
                    //empty, don't send
                    Toast.makeText(GroupChatActivity.this, R.string.please_input_message, Toast.LENGTH_SHORT).show();
                } else {
                    //send message
                    sendMessage(message);
                }
            }
        });

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentInfoGroup = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intentInfoGroup.putExtra("groupId", groupId);
                startActivity(intentInfoGroup);

            }
        });

        groupTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentInfoGroup = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                intentInfoGroup.putExtra("groupId", groupId);
                startActivity(intentInfoGroup);
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
        String[] option = {getString(R.string.camera), getString(R.string.gallary)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_img)
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

        DatabaseReference ref_current = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                .child(groupId)
                .child(Constant.COLLECTION_MESSAGES);
        String id_groupchat = ref_current.push().getKey();

        //setup message data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.GROUPCHAT_SENDER, "" + firebaseAuth.getUid());
        hashMap.put(Constant.GROUPCHAT_MESSAGE, "" + message);
        hashMap.put(Constant.GROUPCHAT_TIMESTAP, "" + timestamp);
        hashMap.put(Constant.GROUPCHAT_IMAGE, "");
        hashMap.put(Constant.GROUPCHAT_VIDEO, "");
        hashMap.put(Constant.GROUPCHAT_FILE, "");
        hashMap.put(Constant.GROUPCHAT_TYPE, "text"); //text/image/file
        hashMap.put(Constant.GROUPCHAT_ID, id_groupchat);

        //add last message timestamp
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                .child(groupId);
        ref.child(Constant.GROUP_LASSMESSAGETIMESTAMP).setValue("" + timestamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });

        //add to databaseList()
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_MESSAGES)
                .child(id_groupchat)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //message sent
                        //clean message
                        messageEt.setText("");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
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
        pb.setTitle(getString(R.string.txt_please_wait));
        pb.setMessage(getString(R.string.upload_img));
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
                            DatabaseReference ref_current = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                                    .child(groupId)
                                    .child(Constant.COLLECTION_MESSAGES);
                            String id_groupchat = ref_current.push().getKey();

                            //setup message data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put(Constant.GROUPCHAT_SENDER, "" + firebaseAuth.getUid());
                            hashMap.put(Constant.GROUPCHAT_MESSAGE, "");
                            hashMap.put(Constant.GROUPCHAT_TIMESTAP, "" + timestamp);
                            hashMap.put(Constant.GROUPCHAT_IMAGE, "" + downloadUrl);
                            hashMap.put(Constant.GROUPCHAT_VIDEO, "");
                            hashMap.put(Constant.GROUPCHAT_FILE, "");
                            hashMap.put(Constant.GROUPCHAT_TYPE, "image"); //text/image/file

                            //add last message timestamp
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS)
                                    .child(groupId);
                            ref.child(Constant.GROUP_LASSMESSAGETIMESTAMP).setValue("" + timestamp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {

                                        }
                                    });

                            //add to databaseList()
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                            reference.child(groupId)
                                    .child(Constant.COLLECTION_MESSAGES)
                                    .child(id_groupchat)
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
        reference.orderByChild(Constant.GROUP_ID).equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String groupTitile = "" + ds.child(Constant.GROUP_TITLE).getValue();
                            String groupDecription = "" + ds.child(Constant.GROUP_DECRIPTION).getValue();
                            String groupIcon = "" + ds.child(Constant.GROUP_ICON).getValue();
                            String timestamp = "" + ds.child(Constant.GROUP_TIMESTAMP).getValue();
                            String createBy = "" + ds.child(Constant.GROUP_CREATEBY).getValue();

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
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        groupIconIv = findViewById(R.id.groupIconIv);
        groupTitleTv = findViewById(R.id.groupTitle);
        btnAttack = findViewById(R.id.attchBtn);
        btnSend = findViewById(R.id.sendBtn);
        messageEt = findViewById(R.id.messageEt);

        firebaseAuth = FirebaseAuth.getInstance();


        mRefreshLayout = findViewById(R.id.mRefreshLayout);

        chatRv = findViewById(R.id.chatRv);
        groupChatArrayList = new ArrayList<>();
        groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, groupChatArrayList, groupId);
        linearLayoutManager = new LinearLayoutManager(GroupChatActivity.this);
       /* linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);*/
        chatRv.setHasFixedSize(true);
        chatRv.setLayoutManager(linearLayoutManager);
        chatRv.setAdapter(groupChatAdapter);

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
        } else if (id == (R.id.info_group)) {
            Intent intentInfoGroup = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            intentInfoGroup.putExtra("groupId", groupId);
            startActivity(intentInfoGroup);
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
                        Toast.makeText(this, R.string.camera_is_required, Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case GALARY_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStoraeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStoraeAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, R.string.camera_is_required, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }
}