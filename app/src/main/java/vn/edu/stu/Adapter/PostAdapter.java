package vn.edu.stu.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetReviewUrl;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.CommentsActivity;
import vn.edu.stu.luanvanmxhhippo.FollowersActivity;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.MessageActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPost;

    private FirebaseUser firebaseUser;
    private String postid;
    private String usenameTemp = "";

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    private ArrayList<Uri> listUriImage;
    private List<String> listUrlImage;

    private boolean isBlock = false;
    private boolean isExist = false;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        //Lay user hien tai
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPost.get(position);
        postid = post.getPost_id();

        //An nut chat
        if (post.getPost_publisher().equals(firebaseUser.getUid())) {
            holder.chat.setVisibility(View.GONE);
        }

        //show image or video
        //check type post
        //type = image
        if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
            loadImagePost(holder, post);
        }
        //type = video
        else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
            loadVideoPost(holder, post);
        }
        //type = text
        else {
            loadTextPost(holder, post);
        }

        //Check text decription
        if (post != null) {
            //Kiem tra post co mo ta khong
            checkTextEmpty(holder, post);

            //Event like, commemt,not like, getcomment, save
            publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPost_publisher());
            loadRoleAndTime(post, holder);
            checkBlockClickEvents(post);
            isLiked(post.getPost_id(), holder.like);
            nrLikes(holder.likes, post.getPost_id());
            getComments(post.getPost_id(), holder.comments);
            isSaved(post.getPost_id(), holder.save);
            getUsernameCurrentUser();
        }

        //action
        //Click vao hinh dai dien
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                                editor.putString("profileid", post.getPost_publisher());
                                editor.apply();

                                Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                                mContext.startActivity(intent);
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click vao username
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                                editor.putString("profileid", post.getPost_publisher());
                                editor.apply();

                                Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                                mContext.startActivity(intent);
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        });

        //Click vao name
        /*holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });*/

        //Click vao hinh bai post
        holder.filterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                Intent intent = new Intent(mContext, PostDetailActivity.class);
                mContext.startActivity(intent);*/
                /*Toast.makeText(mContext, post.getPostvideo().toString(), Toast.LENGTH_SHORT).show();*/
            }
        });

        //Click nut save bai post
        holder.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                if (holder.save.getTag().equals("save")) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Constant.COLLECTION_SAVE)
                                            .child(firebaseUser.getUid())
                                            .child(post.getPost_id())
                                            .setValue(true);
                                    Snackbar.make(holder.like, R.string.you_save_post, BaseTransientBottomBar.LENGTH_SHORT).show();
                                } else {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Constant.COLLECTION_SAVE)
                                            .child(firebaseUser.getUid())
                                            .child(post.getPost_id())
                                            .removeValue();
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click nut like
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                if (holder.like.getTag().equals("like")) {
                                    FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS).child(post.getPost_id())
                                            .child(Constant.COLLECTION_LIKES).child(firebaseUser.getUid()).setValue(true);
                                    Snackbar.make(holder.like, R.string.you_like_post, BaseTransientBottomBar.LENGTH_SHORT).show();
                                    postid = post.getPost_id();
                                    //sent top notification/ oreonotification
                                    sendNotification(post.getPost_publisher(), usenameTemp, mContext.getString(R.string.has_like_your_post));
                                    addNotifications(post.getPost_publisher(), post.getPost_id());
                                } else {
                                    FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS).child(post.getPost_id())
                                            .child(Constant.COLLECTION_LIKES).child(firebaseUser.getUid()).removeValue();
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click nut comment
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
                            reference.child(firebaseUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.child(post.getPost_publisher()).exists() || firebaseUser.getUid().equals(post.getPost_publisher())) {
                                                Intent intent = new Intent(mContext, CommentsActivity.class);
                                                intent.putExtra("postid", post.getPost_id());
                                                intent.putExtra("publisherid", post.getPost_publisher());
                                                mContext.startActivity(intent);
                                            } else {
                                                Toast.makeText(mContext, R.string.must_friend_comments, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click dong comment
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
                            reference.child(firebaseUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.child(post.getPost_publisher()).exists() || firebaseUser.getUid().equals(post.getPost_publisher())) {
                                                Intent intent = new Intent(mContext, CommentsActivity.class);
                                                intent.putExtra("postid", post.getPost_id());
                                                intent.putExtra("publisherid", post.getPost_publisher());
                                                mContext.startActivity(intent);
                                            } else {
                                                Toast.makeText(mContext, R.string.must_friend_comments, Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click text like
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(mContext, FollowersActivity.class);
                                intent.putExtra("id", post.getPost_id());
                                intent.putExtra("title", "likes");
                                mContext.startActivity(intent);
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click nut chat
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                Intent intent = new Intent(mContext, MessageActivity.class);
                                intent.putExtra("user_id", post.getPost_publisher());
                                mContext.startActivity(intent);
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        //Click nut share
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                //check type post: image/video/text
                //share post text
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_TEXT)) {
                                    shareText(post.getPost_description());
                                }
                                //share post image/list image
                                else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
                                    //Toast.makeText(mContext, R.string.image_share_not_sp, Toast.LENGTH_SHORT).show();
                                    shareImage(post.getPost_id());
                                }
                                //share post video
                                else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
                                    shareVideo(post.getPost_video());
                                }
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }
        });

        //Click nut more
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog
                ProgressDialog progressDialog = new ProgressDialog(mContext);
                progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
                progressDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            progressDialog.dismiss();
                            if (isBlock == true) {
                                Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                            } else {
                                PopupMenu popupMenu = new PopupMenu(mContext, view);
                                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        switch (menuItem.getItemId()) {
                                            case R.id.edit:
                                                //edit decription post
                                                editPost(post);
                                                return true;
                                            case R.id.delete:
                                                //init dialog custom
                                                Dialog dialog = new Dialog(mContext);
                                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                dialog.setContentView(R.layout.custom_dialog_layout);
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                                dialog.getWindow().setGravity(Gravity.BOTTOM);
                                                dialog.setCancelable(true);

                                                //add controls dialog custom
                                                MaterialButton btn_confirm_dialog, btn_cancel_dialog;
                                                TextView textviewtitile;

                                                btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
                                                btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
                                                textviewtitile = dialog.findViewById(R.id.textviewtitile);
                                                textviewtitile.setText(R.string.are_you_delete_posts);

                                                //button confirm delete
                                                btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deletePost(post);
                                                        dialog.dismiss();
                                                    }
                                                });

                                                //button cancel delete
                                                btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                //show dialog
                                                dialog.show();
                                                return true;
                                            case R.id.report:
                                                Toast.makeText(mContext, R.string.report, Toast.LENGTH_SHORT).show();
                                                return true;

                                            default:
                                                return false;
                                        }
                                    }
                                });
                                popupMenu.inflate(R.menu.post_menu);
                                if (!post.getPost_publisher().equals(firebaseUser.getUid())) {
                                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                                }
                                popupMenu.show();
                            }
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(holder.image_profile, R.string.postsisnotexist, BaseTransientBottomBar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

    }

    private boolean checkExistPost(Post post) {
        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
        progressDialog.show();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(post.getPost_id())) {
                            isExist = true;
                            progressDialog.dismiss();
                        } else {
                            isExist = false;
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }
        });

        return isExist;

    }

    //load id user blocked
    private void checkBlockClickEvents(Post post) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(post.getPost_publisher())
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

    private void loadRoleAndTime(Post post, ViewHolder holder) {
        if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
            holder.iconrole.setImageResource(R.drawable.ic_role_public);
        } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PRIVATE)) {
            holder.iconrole.setImageResource(R.drawable.ic_role_private);
        } else {
            holder.iconrole.setImageResource(R.drawable.ic_role_friend);
        }
        String timeago = GetTimeAgo.getTimeAgo(Long.parseLong(post.getPost_timestamp()), mContext);
        holder.time.setText(timeago);

    }

    //sent top notification
    private void sendNotification(String receiver, final String username, final String message) {
        APIService apiService;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(postid, R.drawable.notify, username + ": " + message, mContext.getString(R.string.favorite), "" + firebaseUser.getUid(), Constant.TYPE_NOTIFICATION_LIKE);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
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
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkIsFriend(Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.child(post.getPost_publisher()).exists() || firebaseUser.getUid().equals(post.getPost_publisher())) {

                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void deletePost(Post post) {
        final String id = post.getPost_id();
        //delete notification post
        //deleteNotifications(id, firebaseUser.getUid());
        //delete post
        FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(post.getPost_id()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        mPost.remove(post);
                        notifyDataSetChanged();

                        deleteNotifications(post.getPost_id(), firebaseUser.getUid());
                        Toast.makeText(mContext, R.string.post_delete, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(mContext, R.string.post_delete_fail, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //check text empty
    private void checkTextEmpty(ViewHolder holder, Post post) {
        if (post.getPost_description().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getPost_description());
        }
    }

    private void loadVideoPost(ViewHolder holder, Post post) {
        holder.post_image.setVisibility(View.GONE);
        holder.layout_review.setVisibility(View.GONE);
        holder.post_video.setVisibility(View.VISIBLE);

        holder.description.setText(post.getPost_description());
        if (checkIsUrl(post.getPost_description())) {
            //click link;
            holder.description.setTextColor(Color.BLUE);
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(post.getPost_description()));
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder.description.setTextColor(Color.BLACK);
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        try {
            String videoUrl = post.getPost_video();

            MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(holder.post_video);

            Uri uriVideo = Uri.parse(videoUrl);
            holder.post_video.setMediaController(mediaController);
            holder.post_video.setVideoURI(uriVideo);

            holder.post_video.requestFocus();

            holder.post_video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                            return true;
                        }
                    }
                    return false;
                }
            });

            holder.post_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                }
            });
        } catch (Exception e) {
            //error...
        }
    }

    private void loadImagePost(ViewHolder holder, Post post) {
        //Hide videoview
        holder.post_image.setVisibility(View.VISIBLE);
        holder.post_video.setVisibility(View.GONE);
        holder.layout_review.setVisibility(View.GONE);

        List<SlideModel> sliderList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(post.getPost_id()).child(Constant.POST_IMAGE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                sliderList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    sliderList.add(new SlideModel(dataSnapshot.child("image").getValue().toString(), ScaleTypes.CENTER_INSIDE));
                }
                holder.post_image.setImageList(sliderList, ScaleTypes.CENTER_INSIDE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.description.setText(post.getPost_description());
        if (checkIsUrl(post.getPost_description())) {
            holder.description.setTextColor(Color.BLUE);
            //click link;
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(post.getPost_description()));
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder.description.setTextColor(Color.BLACK);
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private void loadTextPost(ViewHolder holder, Post post) {
        holder.post_image.setVisibility(View.GONE);
        holder.post_video.setVisibility(View.GONE);

        holder.txt_title.setText("");
        holder.txt_decription_review.setText("");

        if (post.getPost_description().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getPost_description());
            if (checkIsUrl(post.getPost_description())) {
                holder.description.setTextColor(Color.BLUE);
                holder.layout_review.setVisibility(View.VISIBLE);

                //click link;
                holder.description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(post.getPost_description()));
                        mContext.startActivity(intent);
                    }
                });
                //get meta web
                GetReviewUrl.getJsoupContent(post.getPost_description())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                                    Elements metaTags = result.getElementsByTag("meta");
                                    for (Element elements : metaTags) {
                                        if (elements.attr("property").equals("og:image"))
                                            try {
                                                Glide.with(mContext).load(elements.attr("content"))
                                                        .placeholder(R.drawable.placeholder)
                                                        .into(holder.img_review);

                                            } catch (Exception e) {
                                                holder.img_review.setImageResource(R.drawable.placeholder);
                                            }
                                        else if (elements.attr("name").equals("title")
                                                || elements.attr("property").equals("og:title"))
                                            holder.txt_title.setText(elements.attr("content"));
                                        else if (elements.attr("name").equals("description"))
                                            holder.txt_decription_review.setText(elements.attr("content"));
                                        else if (elements.attr("property").equals("og:url")) {
                                            holder.layout_review.setOnClickListener(v -> {
                                                String url = elements.attr("content");
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(url));
                                                mContext.startActivity(intent);
                                            });
                                        }


                                    }
                                },
                                error -> {
                                    Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
                                });
            } else {
                holder.layout_review.setVisibility(View.GONE);
                holder.description.setTextColor(Color.BLACK);
            }
        }
    }

    private boolean checkIsUrl(String text) {
        try {
            new URL(text).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void shareVideo(String urlVideo) {
        Toast.makeText(mContext, mContext.getString(R.string.videonotspshare), Toast.LENGTH_SHORT).show();
        /*Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri video = Uri.parse(urlVideo);

        sharingIntent.setType("video/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, video);
        mContext.startActivity(Intent.createChooser(sharingIntent, "Share video using"));*/
    }

    private void shareImage(String post) {

        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle(mContext.getString(R.string.please_wait_minute_dont_exit_app));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        //getDataListImageShare(post);
        listUrlImage = new ArrayList<>();
        listUriImage = new ArrayList<>();

        listUrlImage.clear();
        listUriImage.clear();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(post).child(Constant.POST_IMAGE);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String urlimage = dataSnapshot.child("image").getValue().toString();
                        listUrlImage.add(urlimage);

                        try {
                            Glide.with(mContext).asBitmap().load(urlimage).into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull @NotNull Bitmap resource, @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition) {
                                    listUriImage.add(getLocalBitmapUri(resource, mContext));
                                    if (listUriImage.size() == listUrlImage.size()) {
                                        progressDialog.dismiss();
                                        callShareImage();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable @org.jetbrains.annotations.Nullable Drawable placeholder) {

                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(mContext, "Error while process", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void callShareImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, listUriImage);
        intent.setType("image/*");
        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_TEXT, "Share image");

        mContext.startActivity(Intent.createChooser(intent, "Share Image"));
    }

    private Uri getLocalBitmapUri(Bitmap bitmap, Context mContext) {
        Uri bmUri = null;
        try {
            File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    , "shared_images" + System.currentTimeMillis() + ".png");
            FileOutputStream output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, output);
            output.close();
            bmUri = Uri.fromFile(file);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bmUri = FileProvider.getUriForFile(mContext, "vn.edu.stu.luanvanmxhhippo" + ".provider", file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmUri;
    }

    private void shareText(String description) {
        Intent intentShareText = new Intent(Intent.ACTION_SEND);
        intentShareText.setType("text/plain");
        intentShareText.putExtra(Intent.EXTRA_SUBJECT, "");
        intentShareText.putExtra(Intent.EXTRA_TEXT, description);

        mContext.startActivity(Intent.createChooser(intentShareText, "Share posts text"));
    }

    private void getUsernameCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
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

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, like, comment, save, chat, more, share, filterImage, iconrole, img_review;
        public TextView username, likes, publisher, description, comments, time, txt_decription_review, txt_title;
        public ImageSlider post_image;
        private VideoView post_video;
        private LinearLayout layout_review;

        public ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            post_video = itemView.findViewById(R.id.post_video);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            chat = itemView.findViewById(R.id.chat);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            share = itemView.findViewById(R.id.share);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);
            filterImage = itemView.findViewById(R.id.filterImage);
            layout_review = itemView.findViewById(R.id.layout_review);
            txt_decription_review = itemView.findViewById(R.id.txt_decription_review);
            txt_title = itemView.findViewById(R.id.txt_title);
            img_review = itemView.findViewById(R.id.img_review);

            iconrole = itemView.findViewById(R.id.iconrole);
            time = itemView.findViewById(R.id.time);

            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constant.COLLECTION_USERS)
                .child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                try {
                    Glide.with(mContext.getApplicationContext()).load(user.getUser_imageurl())
                            .placeholder(R.drawable.placeholder)
                            .into(image_profile);
                } catch (Exception e) {
                    image_profile.setImageResource(R.drawable.placeholder);
                }
                //set username, text username
                username.setText(user.getUser_username());
                publisher.setText(user.getUser_username());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getComments(String postid, final TextView commnets) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(postid).child(Constant.COLLECTION_COMMENTS);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                commnets.setText(mContext.getString(R.string.view) + " " + snapshot.getChildrenCount() + " " + mContext.getString(R.string.comments));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void isLiked(String postid, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_LIKES);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_like_red);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like_outline);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //Ham them thong bao
    private void addNotifications(String userid, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_NOTIFICATION)
                .child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.ACTION_USERID, firebaseUser.getUid());
        hashMap.put(Constant.ACTION_TEXT, mContext.getString(R.string.like_your_post));
        hashMap.put(Constant.ACTION_POSTID, postid);
        hashMap.put(Constant.ACTION_TIMESTAMP, System.currentTimeMillis() + "");
        hashMap.put(Constant.ACTION_ISPOST, true);

        reference.push().setValue(hashMap);

    }

    private void nrLikes(final TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_LIKES);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() + " " + mContext.getString(R.string.like));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constant.COLLECTION_SAVE)
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.ic_saved);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void editPost(final Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.edit_post);

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        builder.setView(editText);

        getText(post.getPost_id(), editText);

        builder.setPositiveButton(mContext.getString(R.string.edit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.POST_DESCRIPTION, editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                                .child(post.getPost_id()).updateChildren(hashMap);

                        mPost.get(mPost.indexOf(post)).setPost_description(editText.getText().toString());
                        notifyDataSetChanged();
                    }
                });

        builder.setNegativeButton(mContext.getString(R.string.cancels),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        builder.show();

    }

    private void getText(String postid, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getPost_description());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Ham delete thong bao khi xoa bai
    private void deleteNotifications(final String postid, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_NOTIFICATION)
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child(Constant.ACTION_POSTID).getValue().equals(postid)) {
                        snapshot.getRef()
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
