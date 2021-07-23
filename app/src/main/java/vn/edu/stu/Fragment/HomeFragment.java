package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import vn.edu.stu.Adapter.PostAdapter;
import vn.edu.stu.Adapter.RecylerviewHomeAdapter;
import vn.edu.stu.Adapter.StoryAdapter;
import vn.edu.stu.Model.Hobby;
import vn.edu.stu.Model.Item;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.Story;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.ChatManagerActivity;
import vn.edu.stu.luanvanmxhhippo.EditProfileActivity;
import vn.edu.stu.luanvanmxhhippo.FollowersActivity;
import vn.edu.stu.luanvanmxhhippo.PostActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class HomeFragment extends Fragment {

    private ImageView logo, imageInbox, btn_add_post;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private TextView text_more_friend_suggestion, btn_update_info_home;

    private List<Post> getPostListTemp;

    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;

    private LinearLayoutManager linearLayoutManager;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private String mLastKey = "";
    private String mPrevKey = "";
    private int itemPos = 0;

    private FirebaseUser firebaseUser;

    private List<String> followingList;
    private ProgressBar progress_circular;

    private List<String> stringListIdGroup;

    private List<String> stringListBlockId;

    private List<Hobby> hobbies;


    private List<User> suggestionFriendList;

    private List<String> listIdUserHasSimilarHobby;
    private List<String> listIdFriend;
    private List<String> userListIdBlocked;
    private List<String> userListIdBlockByUser;
    private List<String> idUserDifferent;

    private String favorite = "";

    private RelativeLayout layout_post_suggestion;
    private RecyclerView recycler_view_friend_suggestion;
    private List<Post> postListSuggestion;
    private PostAdapter postSuggestionAdapter;

    private SwipeRefreshLayout mRefreshLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        addControls(view);
        addEvents(view);

        //loadListIdFriend
        loadIdGroup();

        //load id block
        readIdBlockUser();
        //load friend suggestion
        loadHobbyCityUser();
        loadIdFriend();
        readIdBlockByUser();

        //Goi ham check following and load story, post
        checkFollowing();
        //lazyLoadFollowing();

        //readPostUser();

        return view;
    }

    private void readPostUser() {
        List<Item> items = new ArrayList<>();
        items.clear();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    items.add(new Item(0, post));
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    items.add(new Item(1, user));

                }

                Collections.reverse(items);
                RecylerviewHomeAdapter recylerviewHomeAdapter = new RecylerviewHomeAdapter(items, getContext());
                recyclerView.setAdapter(recylerviewHomeAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void addEvents(View view) {
        //Icon click inbox
        imageInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatManagerActivity.class);
                startActivity(intent);
            }
        });

        text_more_friend_suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("title", "suggestionpost");
                getContext().startActivity(intent);
            }
        });

        btn_add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PostActivity.class));
            }
        });

        btn_update_info_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //loadListIdFriend
                loadIdGroup();

                //load id block
                readIdBlockUser();
                //load friend suggestion
                loadHobbyCityUser();
                loadIdFriend();
                readIdBlockByUser();

                //Goi ham check following and load story, post
                checkFollowing();

                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void addControls(View view) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        stringListIdGroup = new ArrayList<>();

        listIdUserHasSimilarHobby = new ArrayList<>();
        listIdFriend = new ArrayList<>();
        userListIdBlocked = new ArrayList<>();
        userListIdBlockByUser = new ArrayList<>();
        idUserDifferent = new ArrayList<>();
        hobbies = new ArrayList<>();

        mRefreshLayout = view.findViewById(R.id.mRefreshLayout);

        followingList = new ArrayList<>();
        stringListBlockId = new ArrayList<>();
        imageInbox = view.findViewById(R.id.image_chat);
        logo = view.findViewById(R.id.logo);
        btn_add_post = view.findViewById(R.id.btn_add_post);

        btn_update_info_home = view.findViewById(R.id.btn_update_info_home);

        progress_circular = view.findViewById(R.id.progress_circular);
        layout_post_suggestion = view.findViewById(R.id.layout_post_suggestion);
        text_more_friend_suggestion = view.findViewById(R.id.text_more_friend_suggestion);

        //Post
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        getPostListTemp = new ArrayList<>();
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        //Post suggestion
        recycler_view_friend_suggestion = view.findViewById(R.id.recycler_view_friend_suggestion);
        recycler_view_friend_suggestion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_view_friend_suggestion.setLayoutManager(linearLayoutManager);


        //Story
        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_story = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearLayoutManager_story);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);
    }

    //load id groupChat
    private void loadIdGroup() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                stringListIdGroup.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(firebaseUser.getUid()).exists()) {
                        stringListIdGroup.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void lazyLoadFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constant.COLLECTION_FOLLOWING);

        Query query = reference.limitToLast(mCurrentPage + TOTAL_ITEM_TO_LOAD);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = snapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                followingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingList.add(dataSnapshot.getKey());
                }

                readPost();
                readStory();

                recyclerView.scrollToPosition(followingList.size() - 1);

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

    private void loadMorePost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constant.COLLECTION_FOLLOWING);

        Query query = reference.orderByKey().endAt(mLastKey).limitToLast(10);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                String messageKey = snapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    followingList.add(itemPos++, snapshot.getKey());

                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }
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

    //load id user blocked
    private void readIdBlockUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        stringListBlockId.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListBlockId.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //check following list
    private void checkFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constant.COLLECTION_FOLLOWING);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingList.add(dataSnapshot.getKey());
                }
                followingList.add(FirebaseAuth.getInstance().getUid());

                readPost();
                //readPostSuggestion();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //get list posts
    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                getPostListTemp.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    //duyet list id following
                    for (String id : followingList) {
                        if (post.getPost_publisher().equals(id)) {
                            getPostListTemp.add(post);
                        }
                    }
                }
                checkRolePost();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //check role posts
    private void checkRolePost() {
        postList.clear();

        for (Post post : getPostListTemp) {
            if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                postList.add(post);
            } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PRIVATE)) {
                if (post.getPost_publisher().equals(FirebaseAuth.getInstance().getUid())) {
                    postList.add(post);
                } else {

                }
            } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {
                if (stringListIdGroup.contains(post.getPost_member())) {
                    postList.add(post);
                }
            }
        }

        Collections.sort(postList, new Comparator<Post>() {
            @Override
            public int compare(Post o1, Post o2) {
                return Double.compare(Long.parseLong(o1.getPost_timestamp()), Long.parseLong(o2.getPost_timestamp()));
            }
        });

        if (postList.size() == 0) {
            getIdUserHasSimilarHobby();
        } else {
            layout_post_suggestion.setVisibility(View.GONE);
        }

        postAdapter.notifyDataSetChanged();
        progress_circular.setVisibility(View.GONE);

    }

    //get stories
    private void readStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_STORY);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "",
                        FirebaseAuth.getInstance().getCurrentUser().getUid()));

                for (String id : followingList) {
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot dataSnapshot : snapshot.child(id).getChildren()) {
                        story = dataSnapshot.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //load id friend of friend
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
                            Log.i("LISTID", "readIdFriendListOfFriend: " + idUserDifferent);


                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }
    }

    //load id friend list
    private void loadIdFriend() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        listIdFriend.clear();
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

    //load userid has similar hobby
    private void getIdUserHasSimilarHobby() {
        //Get data live in, hobby of user

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

                                        //check equal hobby
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

    //load user suggestion
    private void loadSuggestionPost() {
        progress_circular.setVisibility(View.VISIBLE);
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
                                    if (postListSuggestion.size() == 5)
                                        break;
                                }
                            }
                        }

                        //check list = 0 hiden recylerview
                        if (postListSuggestion.size() == 0) {
                            layout_post_suggestion.setVisibility(View.GONE);
                            btn_update_info_home.setVisibility(View.VISIBLE);
                        } else {
                            layout_post_suggestion.setVisibility(View.VISIBLE);
                            btn_update_info_home.setVisibility(View.GONE);
                        }

                        //set adapter
                        postSuggestionAdapter = new PostAdapter(getContext(), postListSuggestion);
                        recycler_view_friend_suggestion.setAdapter(postSuggestionAdapter);
                        progress_circular.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //load id user
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

    //get hobby current user
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
                                            hobbies.clear();
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
}