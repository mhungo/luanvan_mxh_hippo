package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.Adapter.RequestFriendAdapter;
import vn.edu.stu.Adapter.SuggestionFriendAdapter;
import vn.edu.stu.Adapter.UserAdapter;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class FollowersActivity extends AppCompatActivity {

    private String id;
    private String title;

    private List<String> idList;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private RequestFriendAdapter requestFriendAdapter;
    private List<String> stringRequestList;
    private List<User> requestList;

    private List<String> stringListIdFriend;
    private List<User> suggestionFriendList;

    private SuggestionFriendAdapter suggestionFriendAdapter;


    private Toolbar toolbar;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        addControls();
        getDataIntent();
        addEvents();
    }

    private void addEvents() {

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        switch (title) {
            case "likes":
                getLikes();
                break;
            case "following":
                getFollowing();
                break;
            case "followers":
                getFollowers();
                break;
            case "views":
                getViews();
                break;
            case "requestfriend":
                loadStringIdUserReceived();
                break;
            case "suggestionfriend":
                loadIdFriend();
                break;
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

    private void getViews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STORY)
                .child(id).child(getIntent().getStringExtra("storyid")).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadIdFriend() {
        stringListIdFriend = new ArrayList<>();
        stringListIdFriend.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListIdFriend.add(dataSnapshot.getKey());
                        }

                        stringListIdFriend.add(firebaseUser.getUid());

                        loadSuggestionFriend();
                        /*---------------------------------------------------------------------*/
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadSuggestionFriend() {
        suggestionFriendList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                suggestionFriendList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (stringListIdFriend.contains(user.getUser_id())) {

                    } else {
                        suggestionFriendList.add(user);
                    }
                }
                suggestionFriendAdapter = new SuggestionFriendAdapter(FollowersActivity.this, suggestionFriendList);
                recyclerView.setAdapter(suggestionFriendAdapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void loadStringIdUserReceived() {
        stringRequestList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        stringRequestList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String requesttype = dataSnapshot.child(Constant.REQUEST_TYPE).getValue().toString();
                            if (requesttype.equals(Constant.REQUEST_TYPE_RECEIVED))
                                stringRequestList.add(dataSnapshot.getKey());
                        }
                        loadRequest();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadRequest() {
        requestList = new ArrayList<>();
        requestList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    for (String id : stringRequestList) {
                        if (user.getUser_id().equals(id)) {
                            requestList.add(user);
                        }
                    }
                }

                requestFriendAdapter = new RequestFriendAdapter(FollowersActivity.this, requestList);
                recyclerView.setAdapter(requestFriendAdapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(id).child(Constant.COLLECTION_FOLLOWING);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(id).child(Constant.COLLECTION_FOLLOWER);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    idList.add(dataSnapshot.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(id)
                .child(Constant.COLLECTION_LIKES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        idList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            idList.add(dataSnapshot.getKey());
                        }
                        showUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void showUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    for (String id : idList) {
                        if (user.getUser_id().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(FollowersActivity.this, userList, false);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

    }

    private void addControls() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();


        idList = new ArrayList<>();

    }
}