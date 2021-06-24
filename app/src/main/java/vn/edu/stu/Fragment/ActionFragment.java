package vn.edu.stu.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import vn.edu.stu.luanvanmxhhippo.FollowersActivity;
import vn.edu.stu.luanvanmxhhippo.R;


public class ActionFragment extends Fragment {

    private NotificationAdapter notificationAdapter;
    private List<Action> notificationList;

    private List<String> stringRequestList;
    private List<User> requestList;
    private List<User> suggestionFriendList;

    private List<String> listIdUserHasSimilarHobby;
    private List<String> listIdFriend;
    private List<String> userListIdBlocked;
    private List<String> userListIdBlockByUser;
    private List<String> idUserDifferent;

    private RequestFriendAdapter requestFriendAdapter;
    private SuggestionFriendAdapter suggestionFriendAdapter;

    private FirebaseAuth firebaseAuth;

    private CircularProgressIndicator progressBar;

    private DatabaseReference reference;

    private FirebaseUser firebaseUser;

    private String city = "";
    private String favorite = "";

    private RecyclerView recyclerView, recycler_view_requestfriend, recycler_view_friend_suggestion;
    private RelativeLayout layout_request_add_friend, layout_friend_suggestion;
    private TextView text_more_request_add_friend, text_more_friend_suggestion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action, container, false);

        addControls(view);
        addEvent(view);

        //load request add friend
        loadStringIdUserReceived();

        //load friend suggestion
        loadHobbyCityUser();
        loadIdFriend();
        readIdBlockUser();
        readIdBlockByUser();
        getIdUserHasSimilarHobby();
        loadSuggestionFriend();

        //load notification
        readnotifications();

        return view;
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
        DatabaseReference refInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        refInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String favoriteUser = dataSnapshot.child(Constant.INFO_HOBBY).getValue().toString().toLowerCase();
                    if (favoriteUser.contains(favorite)) {
                        listIdUserHasSimilarHobby.add(dataSnapshot.getKey());
                    }
                }
                loadSuggestionFriend();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //load user suggestion
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
                                    if (suggestionFriendList.size() == 5)
                                        break;
                                }
                            }
                        }

                        //check list = 0 hiden recylerview
                        if (suggestionFriendList.size() == 0) {
                            layout_friend_suggestion.setVisibility(View.GONE);
                        } else {
                            layout_friend_suggestion.setVisibility(View.VISIBLE);
                        }

                        //set adapter
                        suggestionFriendAdapter = new SuggestionFriendAdapter(getContext(), suggestionFriendList);
                        recycler_view_friend_suggestion.setAdapter(suggestionFriendAdapter);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        }, 1000);
    }

    //load id user
    //load id user blocked current user
    private void readIdBlockUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_BLOCKUSER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        userListIdBlocked.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userListIdBlocked.add(dataSnapshot.getKey());
                        }
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

    //get hobby current user
    private void loadHobbyCityUser() {
        //Get data live in, hobby of user
        DatabaseReference refInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        refInfo.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            favorite = snapshot.child(Constant.INFO_HOBBY).getValue().toString().toLowerCase();
                            city = snapshot.child(Constant.INFO_LIVEIN).getValue().toString();
                        }
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
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("title", "requestfriend");
                getContext().startActivity(intent);
            }
        });

        text_more_friend_suggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", "");
                intent.putExtra("title", "suggestionfriend");
                getContext().startActivity(intent);
            }
        });
    }

    private void addControls(View view) {
        progressBar = view.findViewById(R.id.progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();
        listIdUserHasSimilarHobby = new ArrayList<>();
        listIdFriend = new ArrayList<>();
        userListIdBlocked = new ArrayList<>();
        userListIdBlockByUser = new ArrayList<>();
        idUserDifferent = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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

    //load request add friend
    private void loadRequest() {
        requestList = new ArrayList<>();
        requestList.clear();
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        Query query = reference.limitToLast(5);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    for (String id : stringRequestList) {
                        if (user.getUser_id().equals(id)) {
                            requestList.add(user);
                            if (requestList.size() == 5)
                                break;
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

    //load notification action fragment
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