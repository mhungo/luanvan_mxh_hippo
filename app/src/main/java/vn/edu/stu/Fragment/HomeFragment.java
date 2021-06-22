package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import vn.edu.stu.Adapter.StoryAdapter;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.Story;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.ChatManagerActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class HomeFragment extends Fragment {

    private ImageView logo, imageInbox;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

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

    private RelativeLayout layout_post_suggestion;
    private RecyclerView recycler_view_friend_suggestion;
    private List<Post> postListSuggestion;
    private PostAdapter postSuggestionAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        addEvents(view);
        addControls(view);

        //loadListIdFriend
        loadIdGroup();

        //load id block
        readIdBlockUser();

        //Goi ham check following and load story, post
        checkFollowing();
        //lazyLoadFollowing();

        //backgroundCheckFolowing.start();

        return view;
    }

    private void addControls(View view) {
        //Icon click inbox
        imageInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatManagerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void addEvents(View view) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        stringListIdGroup = new ArrayList<>();

        followingList = new ArrayList<>();
        stringListBlockId = new ArrayList<>();
        imageInbox = view.findViewById(R.id.image_chat);
        logo = view.findViewById(R.id.logo);
        progress_circular = view.findViewById(R.id.progress_circular);

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
        postListSuggestion = new ArrayList<>();
        postSuggestionAdapter = new PostAdapter(getContext(), postListSuggestion);
        recycler_view_friend_suggestion.setAdapter(postAdapter);

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

    private void readPostSuggestion() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postListSuggestion.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);

                    for (String id : followingList) {
                        if (!post.getPost_rules().equals(id)) {
                            postListSuggestion.add(post);
                        }
                    }
                }
                postSuggestionAdapter.notifyDataSetChanged();

                Log.i("TTT", "onDataChange: " + postListSuggestion);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

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
                Log.d("CCCC", "readPOst: " + stringListBlockId);
                checkRolePost();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

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
        postAdapter.notifyDataSetChanged();
        progress_circular.setVisibility(View.GONE);

    }

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
}