package vn.edu.stu.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.Comment;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.GroupChatRelayCommentActivity;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupPostCommentAdapter extends RecyclerView.Adapter<GroupPostCommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComments;
    private String postid;
    private String groupPostId;

    private FirebaseUser firebaseUser;

    private boolean isBlock = false;

    public GroupPostCommentAdapter(Context mContext, List<Comment> mComments, String postid, String groupPostId) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.postid = postid;
        this.groupPostId = groupPostId;
    }

    @NonNull
    @Override
    public GroupPostCommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new GroupPostCommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = mComments.get(position);

        if (comment != null) {
            //get info user
            getUserInfo(holder.image_profile, holder.username, comment.getComment_publisher());

            //load comment
            holder.comment.setText(comment.getComment_comment());
            //set timestamp comment
            holder.timecomment.setText(GetTimeAgo.getTimeAgo(Long.parseLong(comment.getComment_timstamp()), mContext));

            //like
            holder.likecomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.likecomment.getTag().equals("liked")) {
                        unLikeComments(comment, holder);
                    } else {
                        likeComments(comment, holder);
                    }
                }
            });

            //click username -> infoprofile
            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                /*Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher());
                mContext.startActivity(intent);*/

                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", comment.getComment_publisher());
                        editor.apply();

                        Intent intent = new Intent(mContext, InfoProfileFriendActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            });

            //click imageprofile -> infoprofile
            holder.image_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                /*Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherid", comment.getPublisher());
                mContext.startActivity(intent);*/
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", comment.getComment_publisher());
                        editor.apply();

                        Intent intent = new Intent(mContext, InfoProfileFriendActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            });

            //click reply comments
            holder.replycomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(mContext, GroupChatRelayCommentActivity.class);
                        intent.putExtra("postid", postid);
                        intent.putExtra("publisher", comment.getComment_publisher());
                        intent.putExtra("groupPostId", groupPostId);
                        intent.putExtra("comment_id", comment.getComment_commentid());
                        mContext.startActivity(intent);
                    }
                }
            });

            //click text more comments
            holder.txt_view_more_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(mContext, GroupChatRelayCommentActivity.class);
                        intent.putExtra("postid", postid);
                        intent.putExtra("publisher", comment.getComment_publisher());
                        intent.putExtra("groupPostId", groupPostId);
                        intent.putExtra("comment_id", comment.getComment_commentid());
                        mContext.startActivity(intent);
                    }
                }
            });

            //click layout last reply comments
            holder.layout_reply_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(mContext, GroupChatRelayCommentActivity.class);
                        intent.putExtra("postid", postid);
                        intent.putExtra("publisher", comment.getComment_publisher());
                        intent.putExtra("groupPostId", groupPostId);
                        intent.putExtra("comment_id", comment.getComment_commentid());
                        mContext.startActivity(intent);
                    }
                }
            });

            //long click itemview -> delete comment
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteComments(comment);
                    return true;
                }
            });

            //check is like comments
            isLikeComments(comment, holder);
            checkBlockClickEvents(comment);
            checkCountCommentReply(comment, holder);
            loadLastReplyComments(comment, holder);
        }
    }

    //load id user blocked
    private void checkBlockClickEvents(Comment comment) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(comment.getComment_publisher())
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                isBlock = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadLastReplyComments(Comment comment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST)
                .child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(comment.getComment_commentid())
                .child(Constant.COLLECTION_REPLYCOMMENT);
        Query query = reference.limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.layout_reply_comment.setVisibility(View.VISIBLE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String comment_reply = dataSnapshot.child(Constant.REPLY_COMMENT).getValue().toString();
                        String publisher_reply = dataSnapshot.child(Constant.REPLY_PUBLISHER).getValue().toString();
                        holder.comment_reply_user.setText(comment_reply);

                        loadInfoUser(publisher_reply, holder);
                    }

                } else {
                    holder.layout_reply_comment.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void loadInfoUser(String publisher_reply, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(publisher_reply)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            holder.username_reply_user.setText(user.getUser_username());
                            try {
                                Glide.with(mContext).load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(holder.img_reply_user);
                            } catch (Exception e) {
                                holder.img_reply_user.setImageResource(R.drawable.placeholder);
                            }
                        } else {
                            //null
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void checkCountCommentReply(Comment comment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(comment.getComment_commentid())
                .child(Constant.COLLECTION_REPLYCOMMENT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        int countReplyComments = (int) snapshot.getChildrenCount();
                        if (countReplyComments == 0) {
                            holder.txt_view_more_reply.setVisibility(View.GONE);
                        } else {
                            holder.txt_view_more_reply.setVisibility(View.VISIBLE);
                            holder.txt_view_more_reply.setText("View " + countReplyComments + " more reply");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void isLikeComments(Comment comment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(comment.getComment_commentid())
                .child(Constant.COLLECTION_LIKECOMMENTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.child(firebaseUser.getUid()).exists()) {
                            holder.likecomment.setTextColor(Color.BLUE);
                            holder.likecomment.setTypeface(null, Typeface.BOLD);
                            holder.likecomment.setTag("liked");
                        } else {
                            holder.likecomment.setTextColor(Color.BLACK);
                            holder.likecomment.setTypeface(null, Typeface.NORMAL);
                            holder.likecomment.setTag("like");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void likeComments(Comment comment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(comment.getComment_commentid())
                .child(Constant.COLLECTION_LIKECOMMENTS)
                .child(firebaseUser.getUid())
                .setValue(true);
    }

    private void unLikeComments(Comment comment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(comment.getComment_commentid())
                .child(Constant.COLLECTION_LIKECOMMENTS)
                .child(firebaseUser.getUid())
                .removeValue();
    }

    private void deleteComments(Comment comment) {
        if (comment.getComment_publisher().equals(firebaseUser.getUid())) {
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("Do you want to delete?");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST)
                                    .child(groupPostId)
                                    .child(Constant.COLLECTION_POSTS)
                                    .child(postid)
                                    .child(Constant.COLLECTION_COMMENTS)
                                    .child(comment.getComment_commentid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile;
        public TextView username, comment, timecomment, likecomment, replycomment, txt_view_more_reply, username_reply_user, comment_reply_user;
        public CircleImageView img_reply_user;
        public RelativeLayout layout_reply_comment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            timecomment = itemView.findViewById(R.id.timecomment);
            likecomment = itemView.findViewById(R.id.likecomment);
            replycomment = itemView.findViewById(R.id.replycomment);
            img_reply_user = itemView.findViewById(R.id.img_reply_user);
            txt_view_more_reply = itemView.findViewById(R.id.txt_view_more_reply);
            username_reply_user = itemView.findViewById(R.id.username_reply_user);
            comment_reply_user = itemView.findViewById(R.id.comment_reply_user);
            layout_reply_comment = itemView.findViewById(R.id.layout_reply_comment);
        }
    }

    //Get thong tin user
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(publisherid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        //set image user
                        try {
                            Glide.with(mContext).load(user.getUser_imageurl())
                                    .placeholder(R.drawable.placeholder)
                                    .into(imageView);
                        } catch (Exception e) {
                            imageView.setImageResource(R.drawable.placeholder);
                        }
                        //set username
                        username.setText(user.getUser_username());
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

}
