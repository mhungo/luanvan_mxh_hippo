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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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
import vn.edu.stu.Model.ReplyComment;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class ReplyCommentAdapter extends RecyclerView.Adapter<ReplyCommentAdapter.ViewHolder> {

    private Context context;
    private List<ReplyComment> replyCommentList;
    private String postid;
    private String commentid;

    private FirebaseUser firebaseUser;

    private boolean isBlock = false;

    public ReplyCommentAdapter(Context context, List<ReplyComment> replyCommentList, String postid, String commentid) {
        this.context = context;
        this.replyCommentList = replyCommentList;
        this.postid = postid;
        this.commentid = commentid;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reply_comment_item, parent, false);
        return new ReplyCommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ReplyCommentAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ReplyComment replyComment = replyCommentList.get(position);
        if (replyComment != null) {

            //get comments
            holder.comment.setText(replyComment.getReplycomment_comment());

            //get timestamp
            holder.timecomment.setText(GetTimeAgo.getTimeAgo(Long.parseLong(replyComment.getReplycomment_timstamp()), context));

            //checkblock
            checkBlockClickEvents(replyComment);

            //click likecomment
            holder.likecomment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.likecomment, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        if (holder.likecomment.getTag().equals("liked")) {
                            unLikeReplyComments(replyComment, holder);
                        } else {
                            likeReplyComments(replyComment, holder);
                        }
                    }
                }
            });

            //click reply user => info profile
            holder.reply_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.reply_user, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", replyComment.getReplycomment_replyuserid());
                        editor.apply();

                        Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

            //click image profile => infoprofile
            holder.image_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", replyComment.getReplycomment_publisher());
                        editor.apply();

                        Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

            //click username user => info profile
            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", replyComment.getReplycomment_publisher());
                        editor.apply();

                        Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

            //long click itemview => delete comments
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteReplyComments(replyComment);
                    return true;
                }
            });

            //get image user
            loadImageUser(replyComment, holder);
            loadUsernameReply(replyComment, holder);
            isLikeReplyComments(replyComment, holder);

        } else {
            //null
        }

    }

    //load id user blocked
    private void checkBlockClickEvents(ReplyComment replyComment) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(replyComment.getReplycomment_publisher())
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

    //load username comment replys
    private void loadUsernameReply(ReplyComment replyComment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(replyComment.getReplycomment_replyuserid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            holder.reply_user.setText(user.getUser_username());
                        } else {
                            //null
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void likeReplyComments(ReplyComment replyComment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(commentid)
                .child(Constant.COLLECTION_REPLYCOMMENT)
                .child(replyComment.getReplycomment_commentid())
                .child(Constant.COLLECTION_LIKECOMMENTS)
                .child(firebaseUser.getUid())
                .setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "You like this comments", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {

                    }
                });
    }

    //unlike reply comments
    private void unLikeReplyComments(ReplyComment replyComment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(commentid)
                .child(Constant.COLLECTION_REPLYCOMMENT)
                .child(replyComment.getReplycomment_commentid())
                .child(Constant.COLLECTION_LIKECOMMENTS)
                .child(firebaseUser.getUid())
                .removeValue();
    }

    private void deleteReplyComments(ReplyComment replyComment) {
        if (replyComment.getReplycomment_publisher().equals(firebaseUser.getUid())) {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Do you want to delete this comments?");
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
                            FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                                    .child(postid)
                                    .child(Constant.COLLECTION_COMMENTS)
                                    .child(commentid)
                                    .child(Constant.COLLECTION_REPLYCOMMENT)
                                    .child(replyComment.getReplycomment_commentid())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(context, "Deleted !", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(context, "Delete failed !!!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private void loadImageUser(ReplyComment replyComment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(replyComment.getReplycomment_publisher())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            holder.username.setText(user.getUser_username());
                            try {
                                Glide.with(context).load(user.getUser_imageurl())
                                        .placeholder(R.drawable.placeholder)
                                        .into(holder.image_profile);
                            } catch (Exception e) {
                                holder.image_profile.setImageResource(R.drawable.placeholder);
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

    //check islike reply comment
    private void isLikeReplyComments(ReplyComment replyComment, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .child(commentid)
                .child(Constant.COLLECTION_REPLYCOMMENT)
                .child(replyComment.getReplycomment_commentid())
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

    @Override
    public int getItemCount() {
        return replyCommentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView image_profile;
        public TextView username, reply_user, comment, timecomment, likecomment;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            reply_user = itemView.findViewById(R.id.reply_user);
            comment = itemView.findViewById(R.id.comment);
            timecomment = itemView.findViewById(R.id.timecomment);
            likecomment = itemView.findViewById(R.id.likecomment);

        }
    }
}
