package vn.edu.stu.luanvanmxhhippo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.GroupPostParticipantAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class AddParticipantGroupPostActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView recycler_view_user;

    private FirebaseAuth firebaseAuth;

    private String groupPostId = "";

    private String myGroupRole = "";

    private List<String> idParticipantOfGroup;

    private ArrayList<User> userList;
    private List<String> stringListIdFriend;
    private GroupPostParticipantAdapter participantAdapter;

    private LinearProgressIndicator progress_circular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participant_group_post);

        getDataIntent();
        addControls();
        addEvents();

        loadGroupInfo();
        loadParticipantOfGroup();
    }

    private void loadParticipantOfGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        idParticipantOfGroup.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            idParticipantOfGroup.add(dataSnapshot.getKey());
                        }

                        loadFriend();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void addEvents() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadIdFriendList() {
        //init list
        //load Id friend
        DatabaseReference referenceFriend = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        referenceFriend.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        stringListIdFriend.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListIdFriend.add(dataSnapshot.getKey());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void loadFriend() {
        //load users from database
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User modelUser = ds.getValue(User.class);

                            //get all user accept currently signed in
                            if (!firebaseAuth.getUid().equals(modelUser.getUser_id())) {
                                //not my uid

                                if (stringListIdFriend.contains(modelUser.getUser_id()) && !idParticipantOfGroup.contains(modelUser.getUser_id())) {
                                    userList.add(modelUser);
                                } else {
                                    //this current user
                                }
                            }
                        }
                        //set up adpater
                        participantAdapter = new GroupPostParticipantAdapter(
                                AddParticipantGroupPostActivity.this, userList, "" + groupPostId, "" + myGroupRole);

                        //set apdater to recylerview
                        recycler_view_user.setAdapter(participantAdapter);
                        progress_circular.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    private void getDataIntent() {
        groupPostId = getIntent().getStringExtra("groupPostId");
    }

    private void loadGroupInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        ref.orderByChild(Constant.GROUP_POST_ID)
                .equalTo(groupPostId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {

                            String groupId = "" + ds.child(Constant.GROUP_POST_ID).getValue();
                            reference.child(groupPostId)
                                    .child(Constant.COLLECTION_PARTICIPANTS)
                                    .child(firebaseAuth.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                myGroupRole = "" + snapshot.child("role").getValue();

                                                loadIdFriendList();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
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
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        userList = new ArrayList<>();
        stringListIdFriend = new ArrayList<>();
        idParticipantOfGroup = new ArrayList<>();

        recycler_view_user = findViewById(R.id.recycler_view_user);
        recycler_view_user.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddParticipantGroupPostActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_view_user.setLayoutManager(linearLayoutManager);

        progress_circular = findViewById(R.id.progress_circular);

        firebaseAuth = FirebaseAuth.getInstance();

    }
}