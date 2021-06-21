package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Adapter.ImageMessageAdapter;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class MessageInfoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView name_user_chat;
    private CircleImageView image_user_chat;
    private MaterialButton btn_go_info, btn_block_info;

    private RecyclerView recycler_view_image;

    private List<Messages> messagesList;
    private ImageMessageAdapter imageMessageAdapter;

    private String user_chat;

    private boolean isBlock = false;

    private FirebaseUser firebaseUser;
    private String current_user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_info);

        addControls();
        getIntentData();
        addEvents();

        checkIsBlock();

        loadInfoUserChat();
        loadImageMessages();

    }

    private void checkIsBlock() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(user_chat)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if (dataSnapshot.exists()){
                                btn_block_info.setEnabled(false);
                                btn_go_info.setEnabled(false);
                                return;
                            }
                        }
                        btn_block_info.setEnabled(true);
                        btn_go_info.setEnabled(true);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(user_chat)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            if (dataSnapshot.exists()){
                                isBlock = true;
                                btn_block_info.setText("UnBlock");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void loadImageMessages() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
        reference.child(firebaseUser.getUid())
                .child(user_chat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        messagesList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            if (messages.getMessage_type().equals("image")) {
                                messagesList.add(messages);
                            }
                        }
                        Log.i("MMM", "MessageDataChange: " + messagesList);
                        imageMessageAdapter = new ImageMessageAdapter(MessageInfoActivity.this, messagesList);
                        recycler_view_image.setAdapter(imageMessageAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void addEvents() {
        //Go to info profile friend
        btn_go_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", user_chat);
                editor.apply();

                Intent intent = new Intent(MessageInfoActivity.this, InfoProfileFriendActivity.class);
                startActivity(intent);
            }
        });

        //Block
        btn_block_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get current user_id
                current_user_id = firebaseUser.getUid();
                //check state
                if (isBlock == true) {
                    UnBlockUser(current_user_id, user_chat);
                } else {
                    BlockUser(current_user_id, user_chat);
                }
            }
        });
    }

    //inblock user
    private void UnBlockUser(String current_user_id, String user_chat) {
        DatabaseReference ref_block = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        ref_block.child(current_user_id)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(user_chat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                dataSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Unblock successfull
                                                isBlock = false;
                                                btn_block_info.setText("Block");
                                                Snackbar.make(btn_block_info, "UnBlocked successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                //Unlock failed
                                                Snackbar.make(btn_block_info, "UnBlocked failed !!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //Block user
    //User is blocked doesn't Add friend, follow, like, comment, share, chat, with post.
    private void BlockUser(String current_user_id, String user_chat) {

        String timestamp = System.currentTimeMillis() + "";

        //create hashmap clockuser
        HashMap<String, Object> hashMapBlockUser = new HashMap<>();
        hashMapBlockUser.put(Constant.BLOCK_USER_ID, user_chat);
        hashMapBlockUser.put(Constant.BLOCK_USER_TIMESTAMP, timestamp);

        //Upload to db
        DatabaseReference ref_block = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        ref_block.child(current_user_id)
                .child(Constant.COLLECTION_BLOCKUSER)
                .child(user_chat)
                .setValue(hashMapBlockUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        isBlock = true;
                        Snackbar.make(btn_block_info, "Blocked successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Snackbar.make(btn_block_info, "Block failed !!", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadInfoUserChat() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(user_chat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            try {
                                Glide.with(MessageInfoActivity.this).load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(image_user_chat);
                            } catch (Exception e) {
                                image_user_chat.setImageResource(R.drawable.placeholder);
                            }

                            name_user_chat.setText(user.getUser_fullname());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void getIntentData() {
        user_chat = getIntent().getStringExtra("user_id");
    }

    private void addControls() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        image_user_chat = findViewById(R.id.image_user_chat);
        btn_go_info = findViewById(R.id.btn_go_info);
        btn_block_info = findViewById(R.id.btn_block_info);
        name_user_chat = findViewById(R.id.name_user_chat);


        messagesList = new ArrayList<>();
        recycler_view_image = findViewById(R.id.recycler_view_image);
        recycler_view_image.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(MessageInfoActivity.this, 3);
        recycler_view_image.setLayoutManager(linearLayoutManager);


    }
}