package vn.edu.stu.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import vn.edu.stu.Model.GroupPost;
import vn.edu.stu.Model.GroupPostPosts;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetReviewUrl;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.FollowersActivity;
import vn.edu.stu.luanvanmxhhippo.GroupPostCommentActivity;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.MessageActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupPostApprovalAdapter extends RecyclerView.Adapter<GroupPostApprovalAdapter.ViewHolder> {

    private Context mContext;
    private List<GroupPostPosts> groupPostPosts;
    private String groupPostId;

    private boolean isBlock = false;

    public GroupPostApprovalAdapter(Context mContext, List<GroupPostPosts> groupPostPosts, String groupPostId) {
        this.mContext = mContext;
        this.groupPostPosts = groupPostPosts;
        this.groupPostId = groupPostId;
    }

    @NonNull
    @NotNull
    @Override
    public GroupPostApprovalAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.group_post_approval_item, parent, false);
        return new GroupPostApprovalAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupPostApprovalAdapter.ViewHolder holder, int position) {

        GroupPostPosts postPosts = groupPostPosts.get(position);
        if (postPosts != null) {
            //type = image
            if (postPosts.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
                loadImagePost(holder, postPosts);
            }
            //type = video
            else if (postPosts.getPost_type().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
                loadVideoPost(holder, postPosts);
            }
            //type = text
            else {
                loadTextPost(holder, postPosts);
            }

            //click allow
            holder.btn_allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    allowPost(postPosts, holder);
                }
            });

            //click delete post
            holder.btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteApprovalPost(postPosts);
                }
            });

            //action
            //Click vao hinh dai dien
            holder.image_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", postPosts.getPost_publisher());
                        editor.apply();

                    /*((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();*/
                        Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                        mContext.startActivity(intent);
                    }
                }
            });

            //Click vao username
            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                        editor.putString("profileid", postPosts.getPost_publisher());
                        editor.apply();

                        Intent intent = new Intent(mContext.getApplicationContext(), InfoProfileFriendActivity.class);
                        mContext.startActivity(intent);
                    }

                }
            });

            //Click nut save bai post
            holder.save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*if (isBlock == true) {
                        Snackbar.make(holder.image_profile, "You're blocked by that user !", BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        if (holder.save.getTag().equals("save")) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Constant.COLLECTION_SAVE)
                                    .child(firebaseUser.getUid())
                                    .child(post.getPost_id())
                                    .setValue(true);
                            Snackbar.make(holder.like, "You saved this posts", BaseTransientBottomBar.LENGTH_SHORT).show();
                        } else {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Constant.COLLECTION_SAVE)
                                    .child(firebaseUser.getUid())
                                    .child(post.getPost_id())
                                    .removeValue();
                        }
                    }*/
                    Snackbar.make(holder.save, "Not support save !!!", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            });

            //Click nut like
            holder.like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        if (holder.like.getTag().equals("like")) {
                            likeGroupPost(postPosts, holder);
                            Snackbar.make(holder.like, R.string.you_like_post, BaseTransientBottomBar.LENGTH_SHORT).show();
                            /*String postid = postPosts.getPost_id();*/
                            //sent top notification/ oreonotification
                            /*sendNotification(postPosts.getPost_publisher(), usenameTemp, "has like your posts");*/
                            //addNotifications(post.getPost_publisher(), post.getPost_id());
                        } else {
                            unLikeGroupPost(postPosts, holder);
                        }
                    }
                }
            });

            //Click nut comment
            holder.comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, GroupPostCommentActivity.class);
                    intent.putExtra("postid", postPosts.getPost_id());
                    intent.putExtra("groupPostId", postPosts.getPost_group_id());
                    intent.putExtra("publisherid", postPosts.getPost_publisher());
                    mContext.startActivity(intent);
                }
            });

            //Click dong comment
            holder.comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, GroupPostCommentActivity.class);
                    intent.putExtra("postid", postPosts.getPost_id());
                    intent.putExtra("groupPostId", postPosts.getPost_group_id());
                    intent.putExtra("publisherid", postPosts.getPost_publisher());
                    mContext.startActivity(intent);
                }
            });

            //Click text like
            holder.likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(mContext, FollowersActivity.class);
                        intent.putExtra("id", postPosts.getPost_id());
                        intent.putExtra("title", "groupPostLike");
                        mContext.startActivity(intent);
                    }
                }
            });

            //Click nut chat
            holder.chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isBlock == true) {
                        Snackbar.make(holder.image_profile, R.string.you_are_block, BaseTransientBottomBar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("user_id", postPosts.getPost_publisher());
                        mContext.startActivity(intent);
                    }
                }
            });

            publisherInfo(holder.image_profile, holder.username, holder.publisher, postPosts.getPost_publisher());
            loadNameGroup(holder, postPosts);
            isLiked(postPosts, holder.like);
            nrLikes(holder.likes, postPosts);
            loadRoleAndTime(postPosts, holder);
            getComments(postPosts.getPost_id(), holder.comments, postPosts);

        }

    }

    //delete post approval
    private void deleteApprovalPost(GroupPostPosts postPosts) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(postPosts.getPost_group_id())) {
                    reference.child(postPosts.getPost_group_id())
                            .child(Constant.COLLECTION_POSTS)
                            .child(postPosts.getPost_id())
                            .removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(mContext, R.string.post_delete, Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void allowPost(GroupPostPosts postPosts, ViewHolder holder) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.POST_STATUS, Constant.DEFAULT_POST_STATUS);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(postPosts.getPost_group_id())) {
                    reference.child(postPosts.getPost_group_id())
                            .child(Constant.COLLECTION_POSTS)
                            .child(postPosts.getPost_id())
                            .updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(mContext, R.string.approval_post, Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadNameGroup(GroupPostApprovalAdapter.ViewHolder holder, GroupPostPosts postPosts) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(postPosts.getPost_group_id())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        GroupPost posts = snapshot.getValue(GroupPost.class);
                        if (posts != null) {
                            holder.namegroup.setText(posts.getGrouppost_title());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void unLikeGroupPost(GroupPostPosts postPosts, GroupPostApprovalAdapter.ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST)
                .child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postPosts.getPost_id())
                .child(Constant.COLLECTION_LIKES)
                .child(FirebaseAuth.getInstance().getUid())
                .removeValue();
    }

    private void likeGroupPost(GroupPostPosts postPosts, GroupPostApprovalAdapter.ViewHolder holder) {
        FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST)
                .child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postPosts.getPost_id())
                .child(Constant.COLLECTION_LIKES)
                .child(FirebaseAuth.getInstance().getUid())
                .setValue(true);
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
                                /*isBlock = true;*/
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadRoleAndTime(GroupPostPosts post, GroupPostApprovalAdapter.ViewHolder holder) {
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

    //sent top notificaation
    private void sendNotification(String receiver, final String username, final String message, GroupPostPosts posts) {
        APIService apiService;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Token token = dataSnapshot.getValue(Token.class);
                    Data data = new Data(posts.getPost_id(), R.drawable.notify, username + ": " + message, mContext.getString(R.string.favorite), "" + FirebaseAuth.getInstance().getUid(), Constant.TYPE_NOTIFICATION_LIKE);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(mContext.getApplicationContext(), R.string.error_sent_notification, Toast.LENGTH_SHORT).show();
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
        reference.child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.child(post.getPost_publisher()).exists() || FirebaseAuth.getInstance().getUid().equals(post.getPost_publisher())) {

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
    private void checkTextEmpty(PostAdapter.ViewHolder holder, Post post) {
        if (post.getPost_description().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getPost_description());
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
                            /*usenameTemp = user.getUser_username();*/
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load info user post
    private void publisherInfo(ImageView image_profile, TextView username, TextView publisher, String post_publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);

        reference.child(post_publisher)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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

    //load video post
    private void loadVideoPost(GroupPostApprovalAdapter.ViewHolder holder, GroupPostPosts postPosts) {
        holder.post_image.setVisibility(View.GONE);
        holder.layout_review.setVisibility(View.GONE);
        holder.post_video.setVisibility(View.VISIBLE);

        holder.description.setText(postPosts.getPost_description());
        if (checkIsUrl(postPosts.getPost_description())) {
            //click link;
            holder.description.setTextColor(Color.BLUE);
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(postPosts.getPost_description()));
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
            String videoUrl = postPosts.getPost_video();

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

    //load image post
    private void loadImagePost(GroupPostApprovalAdapter.ViewHolder holder, GroupPostPosts postPosts) {
        //Hide videoview
        holder.post_image.setVisibility(View.VISIBLE);
        holder.post_video.setVisibility(View.GONE);
        holder.layout_review.setVisibility(View.GONE);

        List<SlideModel> sliderList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postPosts.getPost_id())
                .child(Constant.POST_IMAGE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
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
        holder.description.setText(postPosts.getPost_description());
        if (checkIsUrl(postPosts.getPost_description())) {
            holder.description.setTextColor(Color.BLUE);
            //click link;
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(postPosts.getPost_description()));
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

    //load text post
    private void loadTextPost(GroupPostApprovalAdapter.ViewHolder holder, GroupPostPosts post) {
        holder.post_image.setVisibility(View.GONE);
        holder.post_video.setVisibility(View.GONE);

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
                holder.description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
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

    private void getComments(String postid, final TextView commnets, GroupPostPosts postPosts) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postid)
                .child(Constant.COLLECTION_COMMENTS)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        commnets.setText(mContext.getString(R.string.view) + snapshot.getChildrenCount() + mContext.getString(R.string.comments));
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    private void isLiked(GroupPostPosts postPosts, final ImageView imageView) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postPosts.getPost_id())
                .child(Constant.COLLECTION_LIKES)
                .addValueEventListener(new ValueEventListener() {
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

    private void nrLikes(final TextView likes, GroupPostPosts postPosts) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(postPosts.getPost_group_id())
                .child(Constant.COLLECTION_POSTS)
                .child(postPosts.getPost_id())
                .child(Constant.COLLECTION_LIKES)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        likes.setText(snapshot.getChildrenCount() + mContext.getString(R.string.like));
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
        builder.setTitle(R.string.edit_post);

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(lp);
        builder.setView(editText);

        getText(postid, editText);

        builder.setPositiveButton(R.string.edit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(postid).updateChildren(hashMap);
                    }
                });

        builder.setNegativeButton(R.string.cancel,
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
                                        Toast.makeText(mContext, R.string.delete, Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return groupPostPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, like, comment, save, chat, more, share, filterImage, iconrole, img_review;
        public TextView username, likes, publisher, description, comments, time, namegroup, txt_decription_review, txt_title;
        public ImageSlider post_image;
        private VideoView post_video;
        private MaterialButton btn_allow, btn_delete;
        private LinearLayout layout_review;

        public ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post_image);
            namegroup = itemView.findViewById(R.id.namegroup);
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
            btn_delete = itemView.findViewById(R.id.btn_delete);
            btn_allow = itemView.findViewById(R.id.btn_allow);

            iconrole = itemView.findViewById(R.id.iconrole);
            time = itemView.findViewById(R.id.time);

            layout_review = itemView.findViewById(R.id.layout_review);
            txt_decription_review = itemView.findViewById(R.id.txt_decription_review);
            txt_title = itemView.findViewById(R.id.txt_title);
            img_review = itemView.findViewById(R.id.img_review);


            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
