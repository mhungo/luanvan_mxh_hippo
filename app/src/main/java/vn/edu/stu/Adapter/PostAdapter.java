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
import android.os.Environment;
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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
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

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    private ArrayList<Uri> listUriImage;
    private List<String> listUrlImage;

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
            //Hide imageslider
            holder.post_image.setVisibility(View.GONE);
            holder.post_video.setVisibility(View.VISIBLE);

            try {
                String videoUrl = post.getPost_video();

                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(holder.post_video);

                Uri uriVideo = Uri.parse(videoUrl);
                holder.post_video.setMediaController(mediaController);
                holder.post_video.setVideoURI(uriVideo);

                holder.post_video.requestFocus();
            } catch (Exception e) {
                //error...
            }

            holder.post_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                }
            });

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

        }
        //type = text
        else {
            holder.post_image.setVisibility(View.GONE);
            holder.post_video.setVisibility(View.GONE);
        }

        //Check text decription
        if (post != null) {
            //Kiem tra post co mo ta khong
            checkTextEmpty(holder, post);

            //Event like, commemt,not like, getcomment, save
            publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPost_publisher());
            loadRoleAndTime(post, holder);
            isLiked(post.getPost_id(), holder.like);
            nrLikes(holder.likes, post.getPost_id());
            getComments(post.getPost_id(), holder.comments);
            isSaved(post.getPost_id(), holder.save);
        }

        //action
        //Click vao hinh dai dien
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPost_publisher());
                editor.apply();

                    /*((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();*/
                Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                mContext.startActivity(intent);
            }
        });

        //Click vao username
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPost_publisher());
                editor.apply();

                Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                mContext.startActivity(intent);

                /*((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();*/
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
                if (holder.save.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPost_id()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPost_id()).removeValue();
                }
            }
        });

        //Click nut like
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS).child(post.getPost_id())
                            .child("Likes").child(firebaseUser.getUid()).setValue(true);
                    //addNotifications(post.getPost_publisher(), post.getPost_id());
                } else {
                    FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS).child(post.getPost_id())
                            .child("Likes").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        //Click nut comment
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
                                    Toast.makeText(mContext, "You must is friend to view comment and comment !", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(mContext, "You must is friend to view comment and comment !", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPost_id());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        //Click nut chat
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("user_id", post.getPost_publisher());
                mContext.startActivity(intent);
            }
        });

        //Click nut share
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check type post: image/video/text
                //share post text
                if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_TEXT)) {
                    shareText(post.getPost_description());
                }
                //share post image/list image
                else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
                    Toast.makeText(mContext, "Image sharing is not supported yet", Toast.LENGTH_SHORT).show();
                    //shareImage(post.getPost_id());
                }
                //share post video
                else if (post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
                    shareVideo(post.getPost_video());
                }
            }
        });

        //Click nut more
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                //edit decription post
                                editPost(post.getPost_id());
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
                                textviewtitile.setText("Are you sure want delete posts ?");

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
                                Toast.makeText(mContext, "Report clicked!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(mContext, "Post is deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(mContext, "Delete is failed", Toast.LENGTH_SHORT).show();
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

    private void loadImagePost(ViewHolder holder, Post post) {
        //Hide videoview
        holder.post_image.setVisibility(View.VISIBLE);
        holder.post_video.setVisibility(View.GONE);

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
    }

    private void shareVideo(String urlVideo) {
        Toast.makeText(mContext, "Video sharing is not supported yet", Toast.LENGTH_SHORT).show();
        /*Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri video = Uri.parse(urlVideo);

        sharingIntent.setType("video/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, video);
        mContext.startActivity(Intent.createChooser(sharingIntent, "Share video using"));*/
    }

    private void shareImage(String post) {

        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait prepare share...");
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

        mContext.startActivity(Intent.createChooser(intent, "Share..."));
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

    @Override
    public int getItemCount() {
        return mPost.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, like, comment, save, chat, more, share, filterImage, iconrole;
        public TextView username, likes, publisher, description, comments, time;
        public ImageSlider post_image;
        private VideoView post_video;

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
                commnets.setText("View All " + snapshot.getChildrenCount() + " Comments");
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
        hashMap.put(Constant.ACTION_TEXT, "Like your post");
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
                likes.setText(snapshot.getChildrenCount() + " like");
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

    private void editPost(final String postid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Edit Post");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        builder.setView(editText);

        getText(postid, editText);

        builder.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(postid).updateChildren(hashMap);
                    }
                });

        builder.setNegativeButton("Cancel",
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
                    if (snapshot.child("postid").getValue().equals(postid)) {
                        snapshot.getRef().removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
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
