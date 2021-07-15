package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
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
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class SuggestionFriendAdapter extends RecyclerView.Adapter<SuggestionFriendAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    private String usenameTemp = "";

    private String state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;

    public SuggestionFriendAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.suggestion_friend_item, parent, false);
        return new SuggestionFriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SuggestionFriendAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {
            try {
                Glide.with(context).load(user.getUser_imageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.img_suggestion_friend);
            } catch (Exception e) {
                holder.img_suggestion_friend.setImageResource(R.drawable.placeholder);
            }

            holder.text_username_suggestion_friend.setText(user.getUser_fullname());

            //click itemview => info profile
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                    context.startActivity(intent);
                }
            });

            //click image => info profile
            holder.img_suggestion_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                    context.startActivity(intent);
                }
            });

            //click name user => info profile
            holder.text_username_suggestion_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                    context.startActivity(intent);
                }
            });

            //click btnadd friend
            holder.btn_add_suggestion_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_NOTFRIEND)) {
                        sentRequestAddFriend(user, holder);
                        sendNotificationAddFriend(user.getUser_id(), usenameTemp, context.getString(R.string.sent_reqest_add_friend));
                        sentActionNotification(context.getString(R.string.send_friend_request), FirebaseAuth.getInstance().getUid(), "", false, user);
                    } else if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_SENT)) {
                        cancelRequestAddFriend(user, holder);
                    }

                }
            });
        }

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

    //ham gui thong bao add friend
    private void sendNotificationAddFriend(String receiver, final String username, final String message) {
        APIService apiService;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(FirebaseAuth.getInstance().getUid(), R.drawable.notify, username + ": " + message, "New Notification", "" + FirebaseAuth.getInstance().getUid(), Constant.TYPE_NOTIFICATION_ADDFRIEND);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(context, R.string.error_sent_notification, Toast.LENGTH_SHORT).show();
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

    private void sentActionNotification(String text, String current_userid, String post_id, boolean isPost, User user) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_NOTIFICATION);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.ACTION_USERID, current_userid);
        hashMap.put(Constant.ACTION_TEXT, text);
        hashMap.put(Constant.ACTION_TIMESTAMP, System.currentTimeMillis() + "");
        hashMap.put(Constant.ACTION_POSTID, post_id);
        hashMap.put(Constant.ACTION_ISPOST, isPost);

        reference.child(user.getUser_id()).push().setValue(hashMap);
    }

    //cancel request add friend
    private void cancelRequestAddFriend(User user, ViewHolder holder) {
        if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_SENT)) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
            reference.child(FirebaseAuth.getInstance().getUid())
                    .child(user.getUser_id())
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reference.child(user.getUser_id()).child(FirebaseAuth.getInstance().getUid())
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;
                                                    holder.btn_add_suggestion_friend.setText(R.string.add_friend);
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
            //not type
        }
    }

    //check state btn add
    private void checkStateButtonAddFriend(User user, ViewHolder holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST)
                .child(FirebaseAuth.getInstance().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                //Friend Request
                if (snapshot.hasChild(user.getUser_id())) {
                    //get type request (receiver or sent)
                    String request_type = snapshot.child(user.getUser_id()).child(Constant.REQUEST_TYPE).getValue().toString();

                    //check type
                    //Type = received
                    if (request_type.equals(Constant.REQUEST_TYPE_RECEIVED)) {
                        state_btn_add_friend = Constant.REQUEST_TYPE_RECEIVED;

                    }
                    //Type = sent
                    else if (request_type.equals(Constant.REQUEST_TYPE_SENT)) {
                        state_btn_add_friend = Constant.REQUEST_TYPE_SENT;
                        holder.btn_add_suggestion_friend.setText(R.string.cancel_request);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //sent request add friend
    private void sentRequestAddFriend(User user, ViewHolder holder) {
        String timestamp = System.currentTimeMillis() + "";

        //create hashmap
        HashMap<String, Object> hashMapRequest = new HashMap<>();
        hashMapRequest.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_SENT);
        hashMapRequest.put(Constant.REQUEST_TIMESTAMP, timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(FirebaseAuth.getInstance().getUid())
                .child(user.getUser_id())
                .setValue(hashMapRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //create hashmap request received
                            HashMap<String, Object> hashMapRequestReceived = new HashMap<>();
                            hashMapRequestReceived.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_RECEIVED);
                            hashMapRequestReceived.put(Constant.REQUEST_TIMESTAMP, timestamp);

                            reference.child(user.getUser_id())
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .setValue(hashMapRequestReceived)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                state_btn_add_friend = Constant.REQUEST_TYPE_SENT;
                                                holder.btn_add_suggestion_friend.setText(R.string.cancel_request);

                                                sendRequestFollow(user, holder);

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

    private void sendRequestFollow(User user, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        reference.child(FirebaseAuth.getInstance().getUid())
                .child(Constant.COLLECTION_FOLLOWING)
                .child(user.getUser_id())
                .setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(user.getUser_id())
                                    .child(Constant.COLLECTION_FOLLOWER)
                                    .child(FirebaseAuth.getInstance().getUid())
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
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView img_suggestion_friend;
        public TextView text_username_suggestion_friend;
        public MaterialButton btn_add_suggestion_friend;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            img_suggestion_friend = itemView.findViewById(R.id.img_suggestion_friend);
            text_username_suggestion_friend = itemView.findViewById(R.id.text_username_suggestion_friend);
            btn_add_suggestion_friend = itemView.findViewById(R.id.btn_add_suggestion_friend);

        }
    }
}
