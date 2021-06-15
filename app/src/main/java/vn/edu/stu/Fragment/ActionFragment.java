package vn.edu.stu.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.edu.stu.Adapter.NotificationAdapter;
import vn.edu.stu.Adapter.RequestFriendAdapter;
import vn.edu.stu.Model.Action;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;


public class ActionFragment extends Fragment {

    private RecyclerView recyclerView, recycler_view_requestfriend;
    private NotificationAdapter notificationAdapter;
    private List<Action> notificationList;

    private List<String> stringRequestList;
    private List<User> requestList;

    private RequestFriendAdapter requestFriendAdapter;

    private FirebaseAuth firebaseAuth;

    private CircularProgressIndicator progressBar;

    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action, container, false);


        progressBar = view.findViewById(R.id.progress_bar);
        firebaseAuth = FirebaseAuth.getInstance();


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

        loadStringIdUserReceived();
        readnotifications();

        return view;
    }

    private void loadRequest() {
        requestList = new ArrayList<>();
        requestList.clear();
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
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