package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

    LinearLayoutManager linearLayoutManager;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private String mLastKey = "";
    private String mPrevKey = "";
    private int itemPos = 0;

    private List<String> followingList;
    ProgressBar progress_circular;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        followingList = new ArrayList<>();
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

        //Story
        recyclerView_story = view.findViewById(R.id.recycler_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_story = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearLayoutManager_story);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        //Goi ham check following and load story, post
        checkFollowing();
        //lazyLoadFollowing();

        //backgroundCheckFolowing.start();

        //Icon click inbox
        imageInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatManagerActivity.class);
                startActivity(intent);
            }
        });

        return view;
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

    private void checkFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Constant.COLLECTION_FOLLOWING);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    followingList.add(dataSnapshot.getKey());
                }
                followingList.add(FirebaseAuth.getInstance().getUid());

                readPost();
                readStory();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addValueEventListener(new ValueEventListener() {
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

    private void checkRolePost() {
        postList.clear();
        for (Post post : getPostListTemp) {
            if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                postList.add(post);
            } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PRIVATE)) {
                if (post.getPost_publisher().equals(FirebaseAuth.getInstance().getUid())) {
                    postList.add(post);
                } else {
                    continue;
                }
            } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {

            }
        }

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