package vn.edu.stu.luanvanmxhhippo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
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
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Adapter.MessageAdapter;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;

public class MessageActivity extends AppCompatActivity {

    private String user_chat;
    private String user_current;

    private String tempMessage;
    private String tempNameCurrentUser;

    private APIService apiService;

    private Toolbar toolbar;

    private TextView username;
    private CircleImageView mProfileImage;

    private ImageButton btnCamera;
    private ImageButton btnSend;
    private ImageButton btnCallVideo, btnCallAudio, btnInfomation;

    private EditText txtSendMessage;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout mRefreshLayout;

    private List<Messages> messagesList;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter messageAdapter;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;

    private int itemPos = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    private String keyTemp = "";

    //permistion
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int GALARY_REQUEST_CODE = 400;

    //image pick constant
    private static final int IMAGE_PICK_GALARY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 2000;

    //permission to be requested
    private String[] cameraPermission;
    private String[] galaryPermission;

    private static final int GALLERY_PICK = 1;
    private StorageReference storageReference;

    private StorageTask uploadTask;

    private String checker = "", myurl = "";
    private Uri mImageUri;

    private TextView txt_block_user;

    private boolean check_current_user_block = false;
    private boolean check_friend_user_block = false;

    private FirebaseUser firebaseUser;

    private RelativeLayout bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        addControls();
        addEvents();

        //set seen message
        setSeenMessages();

        //load messages
        loadUserChatInfo();
        loadCurrentUser();

        loadMessages();
        checkBlockFriend(user_current, user_chat);
        checkBlockUser(user_current, user_chat);

    }

    private void setSeenMessages() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                .child(firebaseUser.getUid()).child(user_chat);
        Query query = reference.limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.i("HAHA", "onDataChange: " + dataSnapshot);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(Constant.MESSAGE_SEEN, true);

                    reference.child(dataSnapshot.getKey())
                            .updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                .child(user_chat).child(firebaseUser.getUid());
        Query query1 = ref.limitToLast(1);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.i("HAHA", "onDataChange: " + dataSnapshot);
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(Constant.MESSAGE_SEEN, true);

                    ref.child(dataSnapshot.getKey())
                            .updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void checkBlockFriend(String user_current, String user_chat) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(user_chat)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(user_current)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                bottom.setVisibility(View.INVISIBLE);
                                btnCallAudio.setEnabled(false);
                                btnCallVideo.setEnabled(false);
                                btnInfomation.setEnabled(false);
                                /*txt_block_user.setVisibility(View.VISIBLE);*/
                                /*Toast.makeText(MessageActivity.this, "You're blocked by that user, can't send message", Toast.LENGTH_SHORT).show();*/
                                return;
                            }
                        }
                        /*txt_block_user.setVisibility(View.GONE);*/
                        bottom.setVisibility(View.VISIBLE);
                        btnCallAudio.setEnabled(true);
                        btnCallVideo.setEnabled(true);
                        btnInfomation.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void checkBlockUser(String user_current, String user_chat) {
        /*--------------------------------------------------*/
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(user_current)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(user_chat)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                bottom.setVisibility(View.INVISIBLE);
                                btnCallAudio.setEnabled(false);
                                btnCallVideo.setEnabled(false);
                                /*txt_block_user.setVisibility(View.VISIBLE);*/
                                /*Toast.makeText(MessageActivity.this, "You blocked this user", Toast.LENGTH_SHORT).show();*/
                                return;
                            }
                        }
                        /*txt_block_user.setVisibility(View.GONE);*/
                        bottom.setVisibility(View.VISIBLE);
                        btnCallAudio.setEnabled(true);
                        btnCallVideo.setEnabled(true);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void finish() {
        super.finish();
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Click nut gui tin nhan
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        //Click goi audio
        btnCallAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
                reference.child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(user_chat)) {
                                    Intent intent = new Intent(MessageActivity.this, OutgoingInvitationActivity.class);
                                    intent.putExtra("userid", user_chat);
                                    intent.putExtra("typeCall", "audio");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MessageActivity.this, R.string.you_must_friend_can_call, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
            }
        });

        //Click goi video
        btnCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
                reference.child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(user_chat)) {
                                    Intent intent = new Intent(MessageActivity.this, OutgoingInvitationActivity.class);
                                    intent.putExtra("userid", user_chat);
                                    intent.putExtra("typeCall", "video");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MessageActivity.this, R.string.you_must_friend_can_call, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
            }
        });

        //Click gui anh, file....
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence[] = new CharSequence[]{
                        getString(R.string.image),
                };

                //Mo thong bao chon anh, file pdf, word
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle(R.string.select_type_file);
                builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";
                            showImage();
                            /*CropImage.activity()
                                    .setFixAspectRatio(true)
                                    .start(MessageActivity.this);*/
                        }
                    }
                });
                builder.show();
            }
        });

        btnInfomation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MessageInfoActivity.class);
                intent.putExtra("user_id", user_chat);
                startActivity(intent);
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

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MessageInfoActivity.class);
                intent.putExtra("user_id", user_chat);
                startActivity(intent);
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, MessageInfoActivity.class);
                intent.putExtra("user_id", user_chat);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALARY_CODE) {
                // got image from gallery
                mImageUri = data.getData();
                sentMessageImage();

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //pick from camera
                sentMessageImage();
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
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALARY_CODE);
    }

    private void pickCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitile");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDecription");

        mImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
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

    //Get uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    //Ham gui tin nhan text
    private void sendMessage() {
        String message = txtSendMessage.getText().toString();
        String timestamp = System.currentTimeMillis() + "";
        if (!TextUtils.isEmpty(message)) {

            tempMessage = message;

            final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref_current = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                    .child(current_user_id)
                    .child(user_chat);
            String id_messages = ref_current.push().getKey();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(Constant.MESSAGE_IMAGE, "");
            hashMap.put(Constant.MESSAGE_SEEN, false);
            hashMap.put(Constant.MESSAGE_TYPE, "text");
            hashMap.put(Constant.MESSAGE_TIMESTAMP, timestamp);
            hashMap.put(Constant.MESSAGE_FROM, current_user_id);
            hashMap.put(Constant.MESSAGE_TEXT, message);
            hashMap.put(Constant.MESSAGE_VIDEO, "");
            hashMap.put(Constant.MESSAGE_FILE, "");
            hashMap.put(Constant.MESSAGE_ID, id_messages);

            //add message to collection message
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
            reference.child(current_user_id)
                    .child(user_chat)
                    .child(id_messages)
                    .setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reference.child(user_chat)
                                        .child(current_user_id)
                                        .child(id_messages)
                                        .setValue(hashMap);
                            } else {

                            }
                        }
                    });

            txtSendMessage.setText("");

            //create hashmap chatlist
            HashMap<String, Object> hashMapChatList = new HashMap<>();
            hashMapChatList.put(Constant.CHATLIST_ID, user_chat);
            hashMapChatList.put(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP, timestamp);

            //create hashmap chatlist
            HashMap<String, Object> hashMapChatListFriend = new HashMap<>();
            hashMapChatListFriend.put(Constant.CHATLIST_ID, current_user_id);
            hashMapChatListFriend.put(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP, timestamp);

            //add chatlist to collection chatlist
            DatabaseReference referenceChatReceiver = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_CHATLIST);
            referenceChatReceiver.child(current_user_id)
                    .child(user_chat)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                referenceChatReceiver.child(current_user_id)
                                        .child(user_chat)
                                        .setValue(hashMapChatList);
                            } else {
                                referenceChatReceiver.child(current_user_id)
                                        .child(user_chat)
                                        .updateChildren(hashMapChatList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
            referenceChatReceiver.child(user_chat)
                    .child(current_user_id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                referenceChatReceiver.child(user_chat)
                                        .child(current_user_id)
                                        .setValue(hashMapChatListFriend);
                            } else {
                                referenceChatReceiver.child(user_chat)
                                        .child(current_user_id)
                                        .updateChildren(hashMapChatListFriend);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

            /*//update timestamp last message
            referenceChatReceiver.child(current_user_id)
                    .child(user_chat)
                    .child(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP)
                    .setValue(timestamp)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                referenceChatReceiver.child(user_chat)
                                        .child(current_user_id)
                                        .child(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP)
                                        .setValue(timestamp);
                            }
                        }
                    });*/

            new SentNotificationInBackground().execute();

        }
    }

    //Ham gui anh
    private void sentMessageImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.uploading));
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        String timestamp = System.currentTimeMillis() + "";

        if (mImageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
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
                        String myUrl = downloadUri.toString();

                        final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref_current = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                                .child(current_user_id)
                                .child(user_chat);
                        String id_messages = ref_current.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.MESSAGE_IMAGE, myUrl);
                        hashMap.put(Constant.MESSAGE_SEEN, false);
                        hashMap.put(Constant.MESSAGE_TYPE, "image");
                        hashMap.put(Constant.MESSAGE_TIMESTAMP, timestamp);
                        hashMap.put(Constant.MESSAGE_FROM, current_user_id);
                        hashMap.put(Constant.MESSAGE_TEXT, "");
                        hashMap.put(Constant.MESSAGE_VIDEO, "");
                        hashMap.put(Constant.MESSAGE_FILE, "");
                        hashMap.put(Constant.MESSAGE_ID, id_messages);

                        //add message to collection message
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
                        reference.child(current_user_id)
                                .child(user_chat)
                                .child(id_messages)
                                .setValue(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            reference.child(user_chat)
                                                    .child(current_user_id)
                                                    .child(id_messages)
                                                    .setValue(hashMap);
                                        } else {

                                        }
                                    }
                                });

                        //create hashmap chatlist
                        HashMap<String, Object> hashMapChatList = new HashMap<>();
                        hashMapChatList.put(Constant.CHATLIST_ID, user_chat);
                        hashMapChatList.put(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP, timestamp);

                        //add chatlist to collection chatlist
                        DatabaseReference referenceChatReceiver = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_CHATLIST);
                        referenceChatReceiver.child(current_user_id)
                                .child(user_chat)
                                .setValue(hashMapChatList)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            referenceChatReceiver.child(user_chat)
                                                    .child(current_user_id)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                            if (!snapshot.exists()) {
                                                                //create hashmap chatlist
                                                                HashMap<String, Object> hashMapChatListFriend = new HashMap<>();
                                                                hashMapChatListFriend.put(Constant.CHATLIST_ID, current_user_id);
                                                                hashMapChatListFriend.put(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP, timestamp);

                                                                referenceChatReceiver.child(user_chat)
                                                                        .child(current_user_id)
                                                                        .setValue(hashMapChatListFriend);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                        }
                                                    });
                                        } else {

                                        }
                                    }
                                });

                        //update timestamp last message
                        referenceChatReceiver.child(current_user_id)
                                .child(user_chat)
                                .child(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP)
                                .setValue(timestamp)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            referenceChatReceiver.child(user_chat)
                                                    .child(current_user_id)
                                                    .child(Constant.CHATLIST_LASTMESSAGE_TIMESTAMP)
                                                    .setValue(timestamp);
                                        }
                                    }
                                });

                        //new SentNotificationInBackground().execute();

                        pd.dismiss();

                    } else {
                        Toast.makeText(MessageActivity.this, R.string.fail, Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.no_img_selected, Toast.LENGTH_SHORT).show();
        }
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        user_chat = getIntent().getStringExtra("user_id");
        user_current = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mProfileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        txt_block_user = findViewById(R.id.txt_block_user);

        btnSend = findViewById(R.id.chat_send_btn);
        btnCamera = findViewById(R.id.chat_add_btn);
        btnCallAudio = findViewById(R.id.btn_call_audio);
        btnCallVideo = findViewById(R.id.btn_call_video);
        btnInfomation = findViewById(R.id.btn_more_infomation);
        txtSendMessage = findViewById(R.id.chat_send_text);

        bottom = findViewById(R.id.bottom);

        messagesList = new ArrayList<>();

        messageAdapter = new MessageAdapter(MessageActivity.this, messagesList);
        recyclerView = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        storageReference = FirebaseStorage.getInstance().getReference().child("Image File");

    }

    private void loadCurrentUser() {
        //Lay full name user friend chat
        DatabaseReference referenceCurrentName = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        referenceCurrentName.child(user_current)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            tempNameCurrentUser = user.getUser_fullname();
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadUserChatInfo() {
        //Lay anh user friend chat and text user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(user_chat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            try {
                                Glide.with(getApplicationContext()).load(user.getUser_imageurl()).into(mProfileImage);
                            } catch (Exception e) {
                                mProfileImage.setImageResource(R.drawable.placeholder);
                            }
                            username.setText(user.getUser_username());
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    Thread loadMessageBackground = new Thread(new Runnable() {
        @Override
        public void run() {
            loadMessages();
        }
    });

    //Load cac message +11
    private void loadMessages() {
        String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES)
                .child(current_user_id).child(user_chat);

        Query query = reference.limitToLast(mCurrentPage + TOTAL_ITEM_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = snapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messagesList.size() - 1);

                mRefreshLayout.setRefreshing(false);
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

    private void loadMoreMessages() {
        String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constant.COLLECTION_MESSAGES)
                .child(current_user_id).child(user_chat);

        Query query = reference.orderByKey().endAt(mLastKey).limitToLast(10);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                String messageKey = snapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, messages);

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }
                messageAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayoutManager.scrollToPositionWithOffset(10, 0);
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

    //Gui thong bao Asyn
    private class SentNotificationInBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            sendNotification(user_chat, tempNameCurrentUser, tempMessage);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    //ham gui thong bao
    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(user_current, R.drawable.notify, username + ": " + message, "" + getString(R.string.txt_message_newmessage), user_chat, Constant.TYPE_NOTIFICATION_CHAT);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, R.string.error_sent_notification, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}