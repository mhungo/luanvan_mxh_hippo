package vn.edu.stu.luanvanmxhhippo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
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

import vn.edu.stu.Adapter.PostAdapter;
import vn.edu.stu.Adapter.RequestFriendAdapter;
import vn.edu.stu.Adapter.SuggestionFriendAdapter;
import vn.edu.stu.Adapter.UserAdapter;
import vn.edu.stu.Adapter.UserBlockAdapter;
import vn.edu.stu.Model.Hobby;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;

public class FollowersActivity extends AppCompatActivity {

    private String id;
    private String title;

    private TextView txt_empty_load;

    private List<String> idList;

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;

    private RequestFriendAdapter requestFriendAdapter;
    private List<String> stringRequestList;
    private List<User> requestList;

    private List<String> listIdUserHasSimilarHobby;
    private List<String> listIdFriend;
    private List<User> suggestionFriendList;

    private String city = "";
    private String favorite = "";

    private List<String> postListSaved;

    private List<String> userListIdBlockByUser;

    private List<String> userListIdBlocked;
    private List<User> userListBlocked;
    private List<String> idUserDifferent;

    private List<String> mySaves;
    private List<Post> postList_saves;

    private List<Post> postListSuggestion;

    private List<Hobby> hobbies;

    private SuggestionFriendAdapter suggestionFriendAdapter;

    private LinearProgressIndicator progress_circular;

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
                loadRequest();
                break;
            case "suggestionfriend":
                loadHobbyCityUser();
                loadIdFriend();
                readIdBlockUser();
                readIdBlockByUser();
                getIdUserHasSimilarHobby();
                //loadSuggestionFriend();
                break;
            case "userblocked":
                readIdBlockUser();
                //loadUserBlock();
                break;
            case "postsaved":
                readIdPostSaved();
                readSaves();
                break;
            case "friends":
                readFriends();
                break;
            case "suggestionpost":
                loadHobbyCityUser();
                loadIdFriend();
                readIdBlockUser();
                readIdBlockByUser();
                getIdUserHasSimilarHobbyPost();
                //loadSuggestionPost();
                break;
        }

    }

    private void readIdFriendListOfFriend() {
        idUserDifferent.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        for (String id : listIdFriend) {
            reference.child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (!dataSnapshot.getKey().equals(firebaseUser.getUid()) && !listIdFriend.contains(dataSnapshot.getKey())) {
                                    idUserDifferent.add(dataSnapshot.getKey());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }
    }

    //load Friends
    private void readFriends() {
        idList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
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

    //load id user
    //load id user blocked current user
    private void readIdBlockUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userListIdBlocked.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userListIdBlocked.add(dataSnapshot.getKey());
                        }
                        loadUserBlock();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load id users block
    private void readIdBlockByUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                userListIdBlockByUser.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.hasChild(Constant.COLLECTION_BLOCKUSER)) {
                        if (dataSnapshot.child(Constant.COLLECTION_BLOCKUSER).child(firebaseUser.getUid()).exists()) {
                            userListIdBlockByUser.add(dataSnapshot.getKey());
                        }

                    } else {
                        //not collection blockuser
                    }
                }
                Log.i("YYYYY", "Block by user: " + userListIdBlockByUser);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //load user blocked
    private void loadUserBlock() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                userListBlocked = new ArrayList<>();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userListBlocked.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            if (userListIdBlocked.contains(user.getUser_id())) {
                                userListBlocked.add(user);
                            }
                        }
                        if (userListBlocked.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }
                        UserBlockAdapter userAdapter = new UserBlockAdapter(userListBlocked, FollowersActivity.this);
                        recyclerView.setAdapter(userAdapter);
                        progress_circular.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //load user suggestion
    private void loadSuggestionPost() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                postListSuggestion = new ArrayList<>();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        postListSuggestion.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);

                            if (!post.getPost_publisher().equals(firebaseUser.getUid())) {
                                if ((listIdUserHasSimilarHobby.contains(post.getPost_publisher())
                                        && !listIdFriend.contains(post.getPost_publisher())
                                        && !userListIdBlocked.contains(post.getPost_publisher())
                                        && !userListIdBlockByUser.contains(post.getPost_publisher()))
                                        || (idUserDifferent.contains(post.getPost_publisher())
                                        && !listIdFriend.contains(post.getPost_publisher())
                                        && !userListIdBlocked.contains(post.getPost_publisher())
                                        && !userListIdBlockByUser.contains(post.getPost_publisher()))) {
                                    postListSuggestion.add(post);
                                }
                            }
                        }

                        Log.i("HOBBYY", "HOBBY: " + listIdUserHasSimilarHobby);
                        Log.i("HOBBYY", "HOBBYFIFFERNT: " + idUserDifferent);
                        Log.i("HOBBYY", "HOBBYFRIEND: " + listIdFriend);
                        Log.i("HOBBYY", "HOBBYBLOCK: " + userListIdBlocked);
                        Log.i("HOBBYY", "HOBBYBLOCKBY: " + userListIdBlockByUser);
                        Log.i("HOBBYY", "HOBBYSUGGESTION: " + postListSuggestion);

                        if (postListSuggestion.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }

                        PostAdapter postAdapter = new PostAdapter(FollowersActivity.this, postListSuggestion);
                        recyclerView.setAdapter(postAdapter);
                        progress_circular.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //load id post saved
    private void readIdPostSaved() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_SAVE)
                .child(firebaseUser.getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mySaves.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    mySaves.add(dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readSaves() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                postList_saves = new ArrayList<>();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        postList_saves.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Post post = dataSnapshot.getValue(Post.class);

                            for (String id : mySaves) {
                                if (post.getPost_id().equals(id)) {
                                    postList_saves.add(post);
                                }
                            }
                        }
                        /*Collections.reverse(postList_saves);*/
                        if (postList_saves.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }
                        PostAdapter userAdapter = new PostAdapter(FollowersActivity.this, postList_saves);
                        recyclerView.setAdapter(userAdapter);
                        progress_circular.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);

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
        listIdFriend.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            listIdFriend.add(dataSnapshot.getKey());
                        }
                        readIdFriendListOfFriend();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load friendHobbyFriend
    private void getIdUserHasSimilarHobby() {
        //Get data live in, hobby of user
        List<Hobby> hobbyListTemp = new ArrayList<>();

        DatabaseReference refInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        refInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals(firebaseUser.getUid())) {
                        refInfo.child(dataSnapshot.getKey())
                                .child(Constant.COLLECTION_INFO_HOBBY)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        /*Log.i("TTTT", "onDataChange: " + snapshot);*/
                                        hobbyListTemp.clear();
                                        for (DataSnapshot dataInfoHobby : snapshot.getChildren()) {
                                            String category = dataInfoHobby.child("category").getValue().toString();
                                            String sub_category = dataInfoHobby.child("subCategory").getValue().toString();
                                            String title = dataInfoHobby.child("title").getValue().toString();

                                            /*Log.i("PPPP", "onDataChange: " + title);
                                            Log.i("PPYY", "onDataChange: " + hobbies);*/

                                            Hobby hobby = new Hobby(category, sub_category, title);

                                            hobbyListTemp.add(hobby);

                                        }

                                        for (Hobby hobby : hobbies) {
                                            if (hobbyListTemp.contains(hobby)) {
                                                listIdUserHasSimilarHobby.add(dataSnapshot.getKey());
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                    }
                }

                loadSuggestionFriend();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getIdUserHasSimilarHobbyPost() {
        //Get data live in, hobby of user
        List<Hobby> hobbyListTemp = new ArrayList<>();

        DatabaseReference refInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        refInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals(firebaseUser.getUid())) {
                        refInfo.child(dataSnapshot.getKey())
                                .child(Constant.COLLECTION_INFO_HOBBY)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        /*Log.i("TTTT", "onDataChange: " + snapshot);*/
                                        hobbyListTemp.clear();
                                        for (DataSnapshot dataInfoHobby : snapshot.getChildren()) {
                                            String category = dataInfoHobby.child("category").getValue().toString();
                                            String sub_category = dataInfoHobby.child("subCategory").getValue().toString();
                                            String title = dataInfoHobby.child("title").getValue().toString();

                                            /*Log.i("PPPP", "onDataChange: " + title);
                                            Log.i("PPYY", "onDataChange: " + hobbies);*/

                                            Hobby hobby = new Hobby(category, sub_category, title);

                                            hobbyListTemp.add(hobby);

                                        }

                                        for (Hobby hobby : hobbies) {
                                            if (hobbyListTemp.contains(hobby)) {
                                                listIdUserHasSimilarHobby.add(dataSnapshot.getKey());
                                                break;
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                    }
                }

                loadSuggestionPost();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadSuggestionFriend() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                suggestionFriendList = new ArrayList<>();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        suggestionFriendList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);

                            if (!user.getUser_id().equals(firebaseUser.getUid())) {
                                if ((listIdUserHasSimilarHobby.contains(user.getUser_id())
                                        && !listIdFriend.contains(user.getUser_id())
                                        && !userListIdBlocked.contains(user.getUser_id())
                                        && !userListIdBlockByUser.contains(user.getUser_id()))
                                        || (idUserDifferent.contains(user.getUser_id())
                                        && !listIdFriend.contains(user.getUser_id())
                                        && !userListIdBlocked.contains(user.getUser_id())
                                        && !userListIdBlockByUser.contains(user.getUser_id()))) {
                                    suggestionFriendList.add(user);
                                }
                            }
                        }

                        //check list = 0 hiden recylerview

                        if (suggestionFriendList.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }
                        UserAdapter userAdapter = new UserAdapter(FollowersActivity.this, suggestionFriendList, true);
                        recyclerView.setAdapter(userAdapter);
                        progress_circular.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //get Hobby of current user
    private void loadHobbyCityUser() {
        //Get data live in, hobby of user
        //Get hobby of user
        DatabaseReference referenceInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        referenceInfo.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(Constant.COLLECTION_INFO_HOBBY)) {
                            referenceInfo.child(firebaseUser.getUid())
                                    .child(Constant.COLLECTION_INFO_HOBBY)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                String category = dataSnapshot.child("category").getValue().toString();
                                                String sub_category = dataSnapshot.child("subCategory").getValue().toString();
                                                String title = dataSnapshot.child("title").getValue().toString();

                                                Hobby hobby = new Hobby(category, sub_category, title);
                                                //selectedHobby.add(hobby);
                                                hobbies.add(hobby);
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

    //load list id user received add friend
    private void loadStringIdUserReceived() {
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
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load list request add friend
    private void loadRequest() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                        if (requestList.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }
                        requestFriendAdapter = new RequestFriendAdapter(FollowersActivity.this, requestList);
                        recyclerView.setAdapter(requestFriendAdapter);
                        progress_circular.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //get list following
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

    //get list follower
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

    //get like posts
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

    //show list user
    private void showUsers() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                        if (userList.size() == 0) {
                            txt_empty_load.setVisibility(View.VISIBLE);
                        } else {
                            txt_empty_load.setVisibility(View.GONE);
                        }
                        userAdapter = new UserAdapter(FollowersActivity.this, userList, true);
                        recyclerView.setAdapter(userAdapter);
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

        stringRequestList = new ArrayList<>();

        listIdUserHasSimilarHobby = new ArrayList<>();
        listIdFriend = new ArrayList<>();

        hobbies = new ArrayList<>();

        userListIdBlocked = new ArrayList<>();
        userListIdBlockByUser = new ArrayList<>();

        idUserDifferent = new ArrayList<>();

        mySaves = new ArrayList<>();

        progress_circular = findViewById(R.id.progress_circular);
        txt_empty_load = findViewById(R.id.txt_empty_load);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();


        idList = new ArrayList<>();

    }
}