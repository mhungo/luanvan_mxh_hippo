package vn.edu.stu.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.edu.stu.Adapter.NotificationAdapter;
import vn.edu.stu.Adapter.RequestFriendAdapter;
import vn.edu.stu.Adapter.SuggestionFriendAdapter;
import vn.edu.stu.Model.Action;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;


public class ActionFragment extends Fragment {

    private NotificationAdapter notificationAdapter;
    private List<Action> notificationList;

    private List<String> stringRequestList;
    private List<User> requestList;
    private List<User> suggestionFriendList;

    private List<String> stringListIdFriend;

    private RequestFriendAdapter requestFriendAdapter;
    private SuggestionFriendAdapter suggestionFriendAdapter;

    private FirebaseAuth firebaseAuth;

    private CircularProgressIndicator progressBar;

    private DatabaseReference reference;

    private RecyclerView recyclerView, recycler_view_requestfriend, recycler_view_friend_suggestion;
    private RelativeLayout layout_request_add_friend, layout_friend_suggestion;
    private TextView text_more_request_add_friend, text_more_friend_suggestion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action, container, false);


        addControls(view);
        addEvent(view);


        loadStringIdUserReceived();
        loadIdFriend();

        readnotifications();

        return view;
    }

    private void loadSuggestionFriend() {
        suggestionFriendList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        //Query query = reference.limitToLast(5);
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

                    /*for (String id : stringListIdFriend) {
                        if (!id.equals(user.getUser_id())) {
                            if (suggestionFriendList.size() == 0) {
                                suggestionFriendList.add(user);
                            } else if (suggestionFriendList.size() == 1) {
                                User user1 = suggestionFriendList.get(0);
                                if (user1.getUser_id().equals(user.getUser_id())) {

                                } else {
                                    suggestionFriendList.add(user);
                                }
                            } else {
                                for (User user2 : suggestionFriendList) {
                                    if (user.getUser_id().equals(user2.getUser_id())) {

                                    } else {
                                        suggestionFriendList.add(user);
                                    }
                                }
                            }
                        }
                    }*/
                }

                Log.i("YYY", "onDataChange: " + suggestionFriendList);


                if (suggestionFriendList.size() == 0) {
                    layout_friend_suggestion.setVisibility(View.GONE);
                } else {
                    layout_friend_suggestion.setVisibility(View.VISIBLE);
                }

                suggestionFriendAdapter = new SuggestionFriendAdapter(getContext(), suggestionFriendList);
                recycler_view_friend_suggestion.setAdapter(suggestionFriendAdapter);


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void loadIdFriend() {
        stringListIdFriend.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            stringListIdFriend.add(dataSnapshot.getKey());
                        }

                        stringListIdFriend.add(firebaseAuth.getUid());

                        loadSuggestionFriend();
                        /*---------------------------------------------------------------------*/

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void addEvent(View view) {
        text_more_request_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "click more", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void addControls(View view) {
        progressBar = view.findViewById(R.id.progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
        stringListIdFriend = new ArrayList<>();


        layout_request_add_friend = view.findViewById(R.id.layout_request_add_friend);
        text_more_request_add_friend = view.findViewById(R.id.text_more_request_add_friend);

        layout_friend_suggestion = view.findViewById(R.id.layout_friend_suggestion);
        text_more_friend_suggestion = view.findViewById(R.id.text_more_friend_suggestion);


        layout_request_add_friend.setVisibility(View.GONE);
        layout_friend_suggestion.setVisibility(View.GONE);


        /*----------Recyclerview--------------*/
        recycler_view_friend_suggestion = view.findViewById(R.id.recycler_view_friend_suggestion);
        recycler_view_friend_suggestion.setHasFixedSize(true);
        LinearLayoutManager linearLayout_suggestion = new LinearLayoutManager(getContext());
        recycler_view_friend_suggestion.setLayoutManager(linearLayout_suggestion);

        recycler_view_requestfriend = view.findViewById(R.id.recycler_view_requestfriend);
        recycler_view_requestfriend.setHasFixedSize(true);
        LinearLayoutManager linearLayout_request = new LinearLayoutManager(getContext());
        recycler_view_requestfriend.setLayoutManager(linearLayout_request);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayout);
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(notificationAdapter);
        /*----------Recyclerview--------------*/
    }

    private void loadRequest() {
        requestList = new ArrayList<>();
        requestList.clear();
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        Query query = reference.limitToLast(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    layout_request_add_friend.setVisibility(View.GONE);
                } else {
                    layout_request_add_friend.setVisibility(View.VISIBLE);
                }

                requestFriendAdapter = new RequestFriendAdapter(getContext(), requestList);
                recycler_view_requestfriend.setAdapter(requestFriendAdapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void loadStringIdUserReceived() {
        stringRequestList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(firebaseAuth.getUid())
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

    //Doc thong bao
    private void readnotifications() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_NOTIFICATION);
                reference.child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                notificationList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Action notification = dataSnapshot.getValue(Action.class);
                                    notificationList.add(notification);
                                }
                                Collections.reverse(notificationList);
                                notificationAdapter.notifyDataSetChanged();

                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });

            }
        }, 1000);
    }


}