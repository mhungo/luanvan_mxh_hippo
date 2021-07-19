package vn.edu.stu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.R;

public class RequestInviteGroupAdapter extends RecyclerView.Adapter<RequestInviteGroupAdapter.ViewHolder> {
    private Context mContext;
    private List<GroupPost> groupPosts;

    private String usenameTemp = "";


    public RequestInviteGroupAdapter(Context mContext, List<GroupPost> groupPosts) {
        this.mContext = mContext;
        this.groupPosts = groupPosts;
    }

    @NonNull
    @NotNull
    @Override
    public RequestInviteGroupAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.request_friend_item, parent, false);
        return new RequestInviteGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RequestInviteGroupAdapter.ViewHolder holder, int position) {
        GroupPost groupPost = groupPosts.get(position);
        if (groupPost != null) {

            holder.text_username_request_friend.setText(groupPost.getGrouppost_title());

            try {
                Glide.with(mContext).load(groupPost.getGrouppost_icon())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.img_request_friend);
            } catch (Exception e) {
                holder.img_request_friend.setImageResource(R.drawable.placeholder);
            }

            holder.text_title_request_friend.setText(R.string.invite_join_to_group);
            loadTimeRequest(holder, groupPost);
            getUsernameCurrentUser();

        }


        holder.btn_comfirm_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFriendRequest(FirebaseAuth.getInstance().getUid(), groupPost);

            }
        });

        holder.btn_delete_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequestFriend(FirebaseAuth.getInstance().getUid(), groupPost);
            }
        });

    }

    private void getUsernameCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            usenameTemp = user.getUser_username();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadTimeRequest(RequestInviteGroupAdapter.ViewHolder holder, GroupPost groupPost) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(firebaseUser.getUid())
                .child(groupPost.getGrouppost_id())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        String timestamp = snapshot.child(Constant.REQUEST_TIMESTAMP).getValue().toString();
                        holder.text_timestamp_request_friend.setText(GetTimeAgo.getTimeAgo(Long.parseLong(timestamp), mContext));

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //ham gui thong bao confirm friend
    private void sendNotificationConfirmFriend(String receiver, final String username, final String message) {
        APIService apiService;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(FirebaseAuth.getInstance().getUid(), R.drawable.notify, username + ": " + message, "New Notification", "" + FirebaseAuth.getInstance().getUid(), Constant.TYPE_NOTIFICATION_CONFIRMFRIEND);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            /*Toast.makeText(mContext, R.string.error_sent_notification, Toast.LENGTH_SHORT).show();*/
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //confirm request add friend from sender
    private void confirmFriendRequest(String current_userid, GroupPost groupPost) {
        String timestamp_confrim_friend = System.currentTimeMillis() + "";

        HashMap<String, String> hashMap1 = new HashMap<>();
        hashMap1.put(Constant.ROLE_UID, FirebaseAuth.getInstance().getUid());
        hashMap1.put(Constant.ROLE_ROLE, Constant.ROLE_PARTICIPANT);
        hashMap1.put(Constant.ROLE_TIMESTAMP, timestamp_confrim_friend);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPost.getGrouppost_id())
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(FirebaseAuth.getInstance().getUid())
                .setValue(hashMap1)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
                            ref.child(current_userid)
                                    .child(groupPost.getGrouppost_id())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                ref.child(groupPost.getGrouppost_id())
                                                        .child(current_userid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                } else {
                                                                    //failed
                                                                }
                                                            }
                                                        });
                                            } else {
                                                //failed
                                            }
                                        }
                                    });

                        } else {

                        }
                    }
                });

    }

    //delete request
    private void deleteRequestFriend(String current_userid, GroupPost groupPost) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(current_userid)
                .child(groupPost.getGrouppost_id())
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(groupPost.getGrouppost_id())
                                    .child(current_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                            } else {
                                                //failed
                                            }
                                        }
                                    });
                        } else {
                            //failed
                        }
                    }
                });
    }

    //send request follow
    private void sendRequestFollow(String current_userid, String profileid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        reference.child(current_userid)
                .child(Constant.COLLECTION_FOLLOWING)
                .child(profileid)
                .setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(profileid)
                                    .child(Constant.COLLECTION_FOLLOWER)
                                    .child(current_userid)
                                    .setValue(true)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            }
                                        }
                                    });
                        } else {
                            //add failed
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView img_request_friend;
        public TextView text_username_request_friend, text_timestamp_request_friend, text_title_request_friend;
        public MaterialButton btn_comfirm_request_friend, btn_delete_request_friend;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_request_friend = itemView.findViewById(R.id.img_request_friend);
            text_username_request_friend = itemView.findViewById(R.id.text_username_request_friend);
            text_timestamp_request_friend = itemView.findViewById(R.id.text_timestamp_request_friend);
            btn_comfirm_request_friend = itemView.findViewById(R.id.btn_comfirm_request_friend);
            btn_delete_request_friend = itemView.findViewById(R.id.btn_delete_request_friend);
            text_title_request_friend = itemView.findViewById(R.id.text_title_request_friend);

        }
    }
}
