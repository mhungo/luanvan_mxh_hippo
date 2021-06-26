package vn.edu.stu.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
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
import vn.edu.stu.luanvanmxhhippo.R;

public class UserBlockAdapter extends RecyclerView.Adapter<UserBlockAdapter.ViewHolder> {
    private List<User> userList;
    private Context context;

    public UserBlockAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_blocked_item, parent, false);
        return new UserBlockAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserBlockAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {
            //set data
            holder.username.setText(user.getUser_username());
            holder.fullname.setText(user.getUser_fullname());

            try {
                Glide.with(context).load(user.getUser_imageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.image_profile);
            } catch (Exception e) {
                holder.image_profile.setImageResource(R.drawable.placeholder);
            }

            holder.btn_unblock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unBlockUser(FirebaseAuth.getInstance().getUid(), user.getUser_id(), holder);
                }
            });

        }

    }

    //unblock user
    private void unBlockUser(String current_user_id, String user_chat, ViewHolder holder) {
        DatabaseReference ref_block = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        ref_block.child(current_user_id)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(user_chat)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                dataSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Unblock successfull

                                                Snackbar.make(holder.btn_unblock, "UnBlocked successfully", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                //Unlock failed
                                                Snackbar.make(holder.btn_unblock, "UnBlocked failed !!", BaseTransientBottomBar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, fullname;
        public CircleImageView image_profile;
        public MaterialButton btn_unblock;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_unblock = itemView.findViewById(R.id.btn_unblock);

        }
    }
}
