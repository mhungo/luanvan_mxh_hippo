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
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.R;

public class RequestFriendAdapter extends RecyclerView.Adapter<RequestFriendAdapter.ViewHolder> {

    private Context mContext;
    private List<User> userList;

    private DatabaseReference reference;


    public RequestFriendAdapter(Context mContext, List<User> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.request_friend_item, parent, false);
        return new RequestFriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RequestFriendAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {

            holder.text_username_request_friend.setText(user.getUser_fullname());

            try {
                Glide.with(mContext).load(user.getUser_imageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.img_request_friend);
            } catch (Exception e) {
                holder.img_request_friend.setImageResource(R.drawable.placeholder);
            }

            holder.text_title_request_friend.setText("sent request friend");
            loadTimeRequest(holder, user);

        }


        holder.btn_comfirm_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFriendRequest(FirebaseAuth.getInstance().getUid(), user.getUser_id());

            }
        });

        holder.btn_delete_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequestFriend(FirebaseAuth.getInstance().getUid(), user.getUser_id());
            }
        });

    }

    private void loadTimeRequest(ViewHolder holder, User user) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(firebaseUser.getUid())
                .child(user.getUser_id())
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

    //confirm request add friend from sender
    private void confirmFriendRequest(String current_userid, String profileid) {
        String timestamp_confrim_friend = System.currentTimeMillis() + "";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(current_userid)
                .child(profileid)
                .child(Constant.FRIEND_TIMESTAMP)
                .setValue(timestamp_confrim_friend)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(profileid)
                                    .child(current_userid)
                                    .child(Constant.FRIEND_TIMESTAMP)
                                    .setValue(timestamp_confrim_friend)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
                                                reference.child(current_userid)
                                                        .child(profileid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    reference.child(profileid)
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
                                            //add failed
                                            else {

                                            }
                                        }
                                    });
                        }
                        //add failed
                        else {

                        }
                    }
                });

    }

    //delete request
    private void deleteRequestFriend(String current_userid, String profileid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(current_userid)
                .child(profileid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(profileid)
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

    @Override
    public int getItemCount() {
        return userList.size();
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
