package vn.edu.stu.luanvanmxhhippo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

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

    private static final int GALLERY_PICK = 1;
    private StorageReference storageReference;

    private StorageTask uploadTask;

    private String checker = "", myurl = "";
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        addControls();
        addEvents();

        loadMessages();
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
                Intent intent = new Intent(MessageActivity.this, OutgoingInvitationActivity.class);
                intent.putExtra("userid", user_chat);
                intent.putExtra("typeCall", "audio");
                startActivity(intent);
            }
        });

        //Click goi video
        btnCallVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageActivity.this, OutgoingInvitationActivity.class);
                intent.putExtra("userid", user_chat);
                intent.putExtra("typeCall", "video");
                startActivity(intent);
            }
        });

        //Click gui anh, file....
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence[] = new CharSequence[]{
                        "Images",
                        "PDF File",
                        "MS Word"
                };

                //Mo thong bao chon anh, file pdf, word
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Select type File");
                builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";
                            CropImage.activity()
                                    .setFixAspectRatio(true)
                                    .start(MessageActivity.this);

                        }
                        if (which == 1) {
                            checker = "pdf";
                        }
                        if (which == 2) {
                            checker = "docx";
                        }
                    }
                });
                builder.show();
            }
        });

        //Event load them message +10
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            //Gui anh
            sentMessageImage();
        } else {
            Toast.makeText(this, "Something gone worng", Toast.LENGTH_SHORT).show();
        }
    }

    //Get uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    //Ham gui anh
    private void sentMessageImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

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

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", myUrl);
                        hashMap.put("seen", false);
                        hashMap.put("type", "image");
                        hashMap.put("time", ServerValue.TIMESTAMP);
                        hashMap.put("from", current_user_id);

                        FirebaseDatabase.getInstance().getReference().child("Messages")
                                .child(current_user_id).child(user_chat).push().setValue(hashMap);
                        FirebaseDatabase.getInstance().getReference().child("Messages")
                                .child(user_chat).child(current_user_id).push().setValue(hashMap);


                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatList")
                                .child(user_chat).child(current_user_id);
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    reference.child("id").setValue(current_user_id);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        DatabaseReference referenceChatReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                                .child(current_user_id).child(user_chat);
                        referenceChatReceiver.child("id").setValue(user_chat);

                        //new SentNotificationInBackground().execute();

                        pd.dismiss();

                    } else {
                        Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        user_chat = getIntent().getStringExtra("user_id");
        user_current = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mProfileImage = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        btnSend = findViewById(R.id.chat_send_btn);
        btnCamera = findViewById(R.id.chat_add_btn);
        btnCallAudio = findViewById(R.id.btn_call_audio);
        btnCallVideo = findViewById(R.id.btn_call_video);
        btnInfomation = findViewById(R.id.btn_more_infomation);
        txtSendMessage = findViewById(R.id.chat_send_text);

        messagesList = new ArrayList<>();

        messageAdapter = new MessageAdapter(MessageActivity.this, messagesList);
        recyclerView = findViewById(R.id.messages_list);
        mRefreshLayout = findViewById(R.id.message_swipe_layout);
        mLinearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        recyclerView.setAdapter(messageAdapter);

        storageReference = FirebaseStorage.getInstance().getReference().child("Image File");

        //Lay anh user friend chat and text user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(user_chat);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getImageurl()).into(mProfileImage);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Lay full name user friend chat
        DatabaseReference referenceCurrentName = FirebaseDatabase.getInstance().getReference("Users")
                .child(user_current);
        referenceCurrentName.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tempNameCurrentUser = user.getFullname();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages")
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

    //Ham gui tin nhan text
    private void sendMessage() {
        String message = txtSendMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            tempMessage = message;

            final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("message", message);
            hashMap.put("seen", false);
            hashMap.put("type", "text");
            hashMap.put("time", ServerValue.TIMESTAMP);
            hashMap.put("from", current_user_id);

            FirebaseDatabase.getInstance().getReference().child("Messages")
                    .child(current_user_id).child(user_chat).push().setValue(hashMap);
            FirebaseDatabase.getInstance().getReference().child("Messages")
                    .child(user_chat).child(current_user_id).push().setValue(hashMap);

            txtSendMessage.setText("");

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(user_chat).child(current_user_id);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        reference.child("id").setValue(current_user_id);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            DatabaseReference referenceChatReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                    .child(current_user_id).child(user_chat);
            referenceChatReceiver.child("id").setValue(user_chat);

            new SentNotificationInBackground().execute();

        }
    }

    private void loadMoreMessages() {
        String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Messages")
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
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(user_current, R.drawable.hippoicon, username + ": " + message, getString(R.string.txt_message_newmessage), user_chat);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Error sent notification", Toast.LENGTH_SHORT).show();
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