package vn.edu.stu.luanvanmxhhippo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Adapter.MyFotoAdapter;
import vn.edu.stu.Adapter.PostAdapter;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.Token;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;

public class InfoProfileFriendActivity extends AppCompatActivity {

    private String profileid = "";
    private String state_btn_add_friend;

    private String current_userid = "";

    private ImageView imageViewBack, image_background, more_toolbar;
    private TextView username, fullname, total_friend, mutual_friends;

    private LinearLayout linearLayout_add_friend, linearLayout_request_friend, linearLayout_friend, layout_info, about_info;

    private CircleImageView image_profile;
    private MaterialButton btn_friend, btn_chat_friend_layout, btn_more_friend_layout,
            btn_delete_request_friend, btn_comfirm_request_friend, btn_more_request_layout,
            btn_add_friend, btn_chat_friend, btn_follow_friend, btn_edit_profile, btn_more_info_layout,
            btn_post_info, btn_photo_info, btn_about_info;

    private TextView text_follower, text_bio, text_birthday, text_gender;

    private APIService apiService;

    private List<Post> postList;
    private List<Post> postListPhoto;

    private List<String> stringListIdGroup;

    private PostAdapter postAdapter;
    private MyFotoAdapter myFotoAdapter;

    private String usenameTemp = "";


    private RecyclerView recycler_view_post, recycler_view_mutual_friend, recycler_view_photo;
    private LinearLayoutManager linearLayoutManager;

    private String fullname_temp = "";

    private LinearProgressIndicator progress_circular;
    private boolean isBlockUser = false;
    private boolean isBlockFriend = false;

    private boolean isBlocked_Friend = false;
    private boolean isBlocked_By_Friend = false;

    private SwipeRefreshLayout mRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_profile_friend);

        addControls();
        getDataIntent();
        addEvent();

        if (profileid.equals(FirebaseAuth.getInstance().getUid())) {
            more_toolbar.setVisibility(View.INVISIBLE);
        }

        loadUserneCurrentUser();

        checkIsBlock();
        checkBlockClickEvents();
        checkBlockByClickEvents();

        /*getUserInfo();
        getCountFriend();
        getCountFollower();
        isFollowing();

        checkStateButtonAddFriend();
        //load id group
        loadIdGroup();
        //load post
        loadPost();
        loadPhoto();*/
    }

    //load id user blocked
    private void checkBlockClickEvents() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(profileid)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(current_userid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                isBlocked_Friend = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //load id user blocked
    private void checkBlockByClickEvents() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(current_userid)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(profileid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                isBlocked_By_Friend = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    //check block user - friend
    private void checkIsBlock() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference1.child(profileid)
                .child(Constant.COLLECTION_BLOCKUSER)
                .orderByChild(Constant.BLOCK_USER_ID)
                .equalTo(current_userid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                isBlockFriend = true;
                            }
                        }
                        /*------------------------------------------------------------------------*/
                        reference1.child(current_userid)
                                .child(Constant.COLLECTION_BLOCKUSER)
                                .orderByChild(Constant.BLOCK_USER_ID)
                                .equalTo(profileid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            if (dataSnapshot.exists()) {
                                                isBlockUser = true;
                                                return;
                                            }
                                        }
                                        /*--------------------------------------------------------*/
                                        if (isBlockUser == false && isBlockFriend == false) {
                                            getUserInfo();
                                            getCountFriend();
                                            getCountFollower();
                                            isFollowing();

                                            checkStateButtonAddFriend();
                                            //load id group
                                            loadIdGroup();
                                            //load post
                                            loadPost();
                                            loadPhoto();

                                        } else {
                                            //block
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void getCountFollower() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        reference.child(profileid)
                .child(Constant.COLLECTION_FOLLOWER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        text_follower.setText(snapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void getCountFriend() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(profileid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        total_friend.setText(snapshot.getChildrenCount() + " " + getString(R.string.friend));
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void loadPhoto() {
        postListPhoto = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postListPhoto.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);

                    if (post.getPost_publisher().equals(profileid) && post.getPost_type().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
                        postListPhoto.add(post);

                    }
                    Collections.reverse(postListPhoto);
                    myFotoAdapter = new MyFotoAdapter(InfoProfileFriendActivity.this, postListPhoto);
                    recycler_view_photo.setAdapter(myFotoAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadPost() {
        postList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);

                    if (post.getPost_publisher().equals(profileid)) {

                        if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PUBLIC)) {
                            postList.add(post);
                        } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_PRIVATE)) {
                            if (post.getPost_publisher().equals(current_userid)) {
                                postList.add(post);
                            } else {
                                continue;
                            }
                        } else if (post.getPost_rules().equals(Constant.DEFAULT_POST_ROLE_ONLYFRIEND)) {
                            if (stringListIdGroup.contains(post.getPost_member())) {
                                postList.add(post);
                            }
                        }
                    }
                }
                postAdapter = new PostAdapter(InfoProfileFriendActivity.this, postList);
                recycler_view_post.setAdapter(postAdapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadIdGroup() {
        stringListIdGroup.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child(Constant.COLLECTION_PARTICIPANTS).child(current_userid).exists()) {
                        stringListIdGroup.add(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void isFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW)
                .child(current_userid)
                .child(Constant.COLLECTION_FOLLOWING);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()) {
                    btn_follow_friend.setTag("following");
                    try {
                        btn_follow_friend.setIcon(getDrawable(R.drawable.ic_isfollow));
                    } catch (Exception e) {

                    }

                } else {
                    btn_follow_friend.setTag("follow");
                    try {
                        btn_follow_friend.setIcon(getDrawable(R.drawable.ic_follow));
                    } catch (Exception e) {

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void updateTextButtonAddFriend() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
        reference.child(current_userid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(profileid)) {
                            String request_type = snapshot.child(profileid)
                                    .child(Constant.REQUEST_TYPE).getValue().toString();

                            if (request_type.equals(Constant.REQUEST_TYPE_SENT)) {
                                state_btn_add_friend = Constant.REQUEST_TYPE_SENT;
                                btn_add_friend.setText(getString(R.string.cancel_request));
                            } else {

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void getUserInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(profileid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    String imgprofile = user.getUser_imageurl();
                    String imgbackground = user.getUser_imgbackgroundurl();
                    String full_name = user.getUser_fullname();
                    String user_name = user.getUser_username();
                    String user_bio = user.getUser_bio();
                    String user_birthday = user.getUser_birthday();
                    String user_gender = user.getUser_gender();

                    //set fullname, username
                    fullname.setText(full_name);
                    username.setText(user_name);
                    fullname_temp = full_name;

                    //set data for about layout

                    //set birthday
                    if (user_birthday.equals(Constant.BIRTHDAY_DEFAULT)) {
                        text_birthday.setText(getString(R.string.not_update));
                    } else {
                        text_birthday.setText(user_birthday);
                    }

                    //set gender
                    if (user_gender.equals(Constant.GENDER_DEFAULT)) {
                        text_gender.setText(getString(R.string.not_update));
                    } else {
                        if (user_gender.equals(Constant.MALE)) {
                            text_gender.setText(R.string.male);
                        } else if (user_gender.equals(Constant.FEMALE)) {
                            text_gender.setText(R.string.female);
                        } else {
                            text_gender.setText(R.string.other_genders);
                        }
                    }
                    text_bio.setText(user_bio);
                    //check hiden gender, birthday
                    if (snapshot.hasChild(Constant.COLLECTION_SETTINGPROFILE)) {
                        boolean isHiddenBirthday = (boolean) snapshot.child(Constant.COLLECTION_SETTINGPROFILE).child(Constant.SETTING_HIDEN_BIRTHDAY).getValue();
                        boolean isHiddenGender = (boolean) snapshot.child(Constant.COLLECTION_SETTINGPROFILE).child(Constant.SETTING_HIDEN_GENDER).getValue();

                        if (isHiddenBirthday == true) {
                            text_birthday.setText("***");
                        }
                        if (isHiddenGender == true) {
                            text_gender.setText("***");
                        }
                    } else {

                    }


                    //set image profile, imagebackground
                    try {
                        Glide.with(InfoProfileFriendActivity.this).load(imgprofile)
                                .placeholder(R.drawable.placeholder)
                                .into(image_profile);

                        Glide.with(InfoProfileFriendActivity.this).load(imgbackground)
                                .placeholder(R.drawable.placeholder)
                                .into(image_background);

                    } catch (Exception e) {
                        image_profile.setImageResource(R.drawable.placeholder);
                        image_background.setImageResource(R.drawable.placeholder);
                    }

                } else {
                    //user is null
                }
                progress_circular.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void loadUserneCurrentUser() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(current_userid)
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

    private void addEvent() {
        /*----------------------------------------------*/
        //add friend  or cancel request friend
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_add_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_NOTFRIEND)) {
                        sentRequestAddFriend();
                        sentActionNotification(getString(R.string.send_a_friend_request), current_userid, "", false);
                    } else if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_SENT)) {
                        cancelRequestAddFriend();
                    }
                }
            }
        });

        btn_follow_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_follow_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    followFriend();
                    sentActionNotification(getString(R.string.start_following_you), current_userid, "", false);
                }

            }
        });

        btn_chat_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_follow_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(InfoProfileFriendActivity.this, MessageActivity.class);
                    intent.putExtra("user_id", profileid);
                    startActivity(intent);
                }
            }
        });
        /*----------------------------------------------*/

        /*----------------------------------------------*/
        //confirm friends
        btn_comfirm_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_follow_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    confirmFriendRequest();
                }
            }
        });

        //delete friend
        btn_delete_request_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequestFriend();
            }
        });
        /*----------------------------------------------*/

        /*----------------------------------------------*/

        btn_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_follow_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    showDialog();
                }
            }
        });

        btn_chat_friend_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(btn_follow_friend, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(InfoProfileFriendActivity.this, MessageActivity.class);
                    intent.putExtra("user_id", profileid);
                    startActivity(intent);
                }
            }
        });

        btn_more_friend_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        /*----------------------------------------------*/

        /*----------------------------------------------*/

        btn_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InfoProfileFriendActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btn_more_info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /*----------------------------------------------*/

        /*----------------------------------------------*/
        btn_post_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycler_view_photo.setVisibility(View.GONE);
                about_info.setVisibility(View.GONE);
                recycler_view_post.setVisibility(View.VISIBLE);

            }
        });

        btn_photo_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycler_view_post.setVisibility(View.GONE);
                about_info.setVisibility(View.GONE);
                recycler_view_photo.setVisibility(View.VISIBLE);

            }
        });

        btn_about_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recycler_view_post.setVisibility(View.GONE);
                recycler_view_photo.setVisibility(View.GONE);
                about_info.setVisibility(View.VISIBLE);

            }
        });
        /*----------------------------------------------*/

        //more toolbar
        more_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBlocked_Friend == true || isBlocked_By_Friend == true) {
                    Snackbar.make(more_toolbar, getString(R.string.you_are_block_by_user), BaseTransientBottomBar.LENGTH_SHORT).show();
                } else {
                    showChooseMoreOption();
                }
            }
        });

        //click total friend
        total_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileid.equals(FirebaseAuth.getInstance().getUid())) {
                    Intent intent = new Intent(InfoProfileFriendActivity.this, FollowersActivity.class);
                    intent.putExtra("id", "friends");
                    intent.putExtra("title", "friends");
                    startActivity(intent);
                } else {

                }
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadData();
            }
        });

    }

    private void reloadData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadUserneCurrentUser();

                checkIsBlock();
                checkBlockClickEvents();
                checkBlockByClickEvents();

                mRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }

    private void showChooseMoreOption() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout unfriendLayout = dialog.findViewById(R.id.unfriend_layout);
        LinearLayout unfollowLayout = dialog.findViewById(R.id.unfollow_layout);
        LinearLayout followLayout = dialog.findViewById(R.id.follow_layout);
        LinearLayout blockLayout = dialog.findViewById(R.id.blockuser_layout);

        unfriendLayout.setVisibility(View.GONE);
        unfollowLayout.setVisibility(View.GONE);
        followLayout.setVisibility(View.GONE);
        blockLayout.setVisibility(View.VISIBLE);

        blockLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BlockUser(current_userid, profileid, usenameTemp);
                dialog.dismiss();

            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    //Block user
    //User is blocked doesn't Add friend, follow, like, comment, share, chat, with post.
    private void BlockUser(String current_user_id, String user_chat, String name_user_chat) {
        Dialog dialog = new Dialog(InfoProfileFriendActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_layout);

        dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.setCancelable(true);

        MaterialButton btn_confirm_dialog, btn_cancel_dialog;
        TextView textviewtitile;
        btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
        btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
        textviewtitile = dialog.findViewById(R.id.textviewtitile);
        textviewtitile.setText(getString(R.string.are_you_want_block) + name_user_chat + "?" + getString(R.string.you_will_unfollow_unfriend));

        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timestamp = System.currentTimeMillis() + "";

                //create hashmap clockuser
                HashMap<String, Object> hashMapBlockUser = new HashMap<>();
                hashMapBlockUser.put(Constant.BLOCK_USER_ID, user_chat);
                hashMapBlockUser.put(Constant.BLOCK_USER_TIMESTAMP, timestamp);

                //Upload to db
                DatabaseReference ref_block = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
                ref_block.child(current_user_id)
                        .child(Constant.COLLECTION_BLOCKUSER)
                        .child(user_chat)
                        .setValue(hashMapBlockUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                more_toolbar.setVisibility(View.INVISIBLE);
                                unFollowUser(current_user_id, user_chat);
                                unFriend();
                                Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.block_susccessfull), Toast.LENGTH_SHORT).show();
                                Snackbar.make(btn_confirm_dialog, getString(R.string.block_susccessfull), BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.block_fail), Toast.LENGTH_SHORT).show();
                                Snackbar.make(btn_confirm_dialog, getString(R.string.block_fail), BaseTransientBottomBar.LENGTH_SHORT).show();
                            }
                        });

                dialog.dismiss();
            }
        });

        btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    //Unfollow
    private void unFollowUser(String current_user_id, String user_chat) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        reference.child(current_user_id)
                .child(Constant.COLLECTION_FOLLOWING)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(user_chat)) {
                            reference.child(current_user_id)
                                    .child(Constant.COLLECTION_FOLLOWING)
                                    .child(user_chat)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                reference.child(user_chat)
                                                        .child(Constant.COLLECTION_FOLLOWER)
                                                        .child(current_user_id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                } else {
                                                                    //not success
                                                                }
                                                            }
                                                        });
                                            } else {
                                                //not success
                                            }
                                        }
                                    });
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
        /*-----------------------------------------------------------------------*/
        reference.child(user_chat)
                .child(Constant.COLLECTION_FOLLOWING)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(current_user_id)) {
                            reference.child(user_chat)
                                    .child(Constant.COLLECTION_FOLLOWING)
                                    .child(current_user_id)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                reference.child(current_user_id)
                                                        .child(Constant.COLLECTION_FOLLOWER)
                                                        .child(user_chat)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {

                                                                } else {
                                                                    //not success
                                                                }
                                                            }
                                                        });
                                            } else {
                                                //not success
                                            }
                                        }
                                    });
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void sentActionNotification(String text, String current_userid, String post_id, boolean isPost) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_NOTIFICATION);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.ACTION_USERID, current_userid);
        hashMap.put(Constant.ACTION_TEXT, text);
        hashMap.put(Constant.ACTION_TIMESTAMP, System.currentTimeMillis() + "");
        hashMap.put(Constant.ACTION_POSTID, post_id);
        hashMap.put(Constant.ACTION_ISPOST, isPost);

        reference.child(profileid).push().setValue(hashMap);
    }

    //ham gui thong bao follow
    private void sendNotificationFollow(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(current_userid, R.drawable.notify, username + ": " + message, getString(R.string.start_following_you), "" + current_userid, Constant.TYPE_NOTIFICATION_FOLLOWING);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.error_sent_notification), Toast.LENGTH_SHORT).show();
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

    //ham gui thong bao add friend
    private void sendNotificationAddFriend(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(current_userid, R.drawable.notify, username + ": " + message, getString(R.string.send_friend_request), "" + current_userid, Constant.TYPE_NOTIFICATION_ADDFRIEND);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.error_sent_notification), Toast.LENGTH_SHORT).show();
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

    //ham gui thong bao confirm friend
    private void sendNotificationConfirmFriend(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(current_userid, R.drawable.notify, username + ": " + message, getString(R.string.new_notification), "" + current_userid, Constant.TYPE_NOTIFICATION_CONFIRMFRIEND);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.error_sent_notification), Toast.LENGTH_SHORT).show();
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

    //send request follow
    private void sendRequestFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        if (btn_follow_friend.getTag().toString().equals("follow")) {
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
                                                    isFollowing();
                                                    sendNotificationFollow(profileid, usenameTemp, getString(R.string.start_following_you));
                                                }
                                            }
                                        });
                            } else {
                                //add failed
                            }
                        }
                    });
        }
    }

    //sent request unfollow
    private void sendRequestUnFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        reference.child(current_userid)
                .child(Constant.COLLECTION_FOLLOWING)
                .child(profileid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(profileid)
                                    .child(Constant.COLLECTION_FOLLOWER)
                                    .child(current_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                isFollowing();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    //follow and unfollow
    @SuppressLint("SetTextI18n")
    private void followFriend() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
        //follow
        if (btn_follow_friend.getTag().toString().equals("follow")) {
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
                                                    isFollowing();
                                                    sendNotificationFollow(profileid, usenameTemp, getString(R.string.start_following_you));
                                                }
                                            }
                                        });
                            }
                        }
                    });
            //addNotifications(user.getUser_id());

        }
        //unfollow
        else {
            Dialog dialog = new Dialog(InfoProfileFriendActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottom_sheet_layout);

            dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.setCancelable(true);

            MaterialButton btn_confirm_dialog, btn_cancel_dialog;
            TextView textviewtitile;
            btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
            btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
            textviewtitile = dialog.findViewById(R.id.textviewtitile);
            textviewtitile.setText(getString(R.string.are_you_unfollow) + fullname_temp + getString(R.string.as_your_friend));

            //confirm unfollow
            btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reference.child(current_userid)
                            .child(Constant.COLLECTION_FOLLOWING)
                            .child(profileid)
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        reference.child(profileid)
                                                .child(Constant.COLLECTION_FOLLOWER)
                                                .child(current_userid)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            isFollowing();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                    dialog.dismiss();
                }
            });

            //ccancel
            btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
    }

    //delete request add friend
    private void deleteRequestFriend() {
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
                                                state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;
                                                linearLayout_request_friend.setVisibility(View.GONE);
                                                linearLayout_add_friend.setVisibility(View.VISIBLE);
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

    //show dialog unfriend, unfollow
    private void showDialog() {
        Dialog dialogAll = new Dialog(this);
        dialogAll.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogAll.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout unfriendLayout = dialogAll.findViewById(R.id.unfriend_layout);
        LinearLayout unfollowLayout = dialogAll.findViewById(R.id.unfollow_layout);
        LinearLayout followLayout = dialogAll.findViewById(R.id.follow_layout);

        if (btn_follow_friend.getTag().equals("following")) {
            followLayout.setVisibility(View.GONE);
            unfollowLayout.setVisibility(View.VISIBLE);
        } else {
            unfollowLayout.setVisibility(View.GONE);
            followLayout.setVisibility(View.VISIBLE);
        }

        //unfriend
        unfriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(InfoProfileFriendActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.setCancelable(true);

                MaterialButton btn_confirm_dialog, btn_cancel_dialog;
                TextView textviewtitile;
                btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
                btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
                textviewtitile = dialog.findViewById(R.id.textviewtitile);
                textviewtitile.setText(getString(R.string.are_you_remove) + " " + fullname_temp + " " + getString(R.string.as_your_friend));

                //confirm unfollow
                btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unFriend();
                        dialog.dismiss();
                        dialogAll.dismiss();
                    }
                });

                //cancel
                btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        //unfolow
        unfollowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(InfoProfileFriendActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.setContentView(R.layout.custom_dialog_unfriend_layout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.setCancelable(true);

                MaterialButton btn_confirm_dialog, btn_cancel_dialog;
                TextView textviewtitile;
                btn_confirm_dialog = dialog.findViewById(R.id.btn_confirm_dialog);
                btn_cancel_dialog = dialog.findViewById(R.id.btn_cancel_dialog);
                textviewtitile = dialog.findViewById(R.id.textviewtitile);
                textviewtitile.setText(getString(R.string.are_you_unfollow) + " " + fullname_temp + " " + getString(R.string.as_your_friend));

                btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
                        reference.child(current_userid)
                                .child(Constant.COLLECTION_FOLLOWING)
                                .child(profileid)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            reference.child(profileid)
                                                    .child(Constant.COLLECTION_FOLLOWER)
                                                    .child(current_userid)
                                                    .removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                isFollowing();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                        dialog.dismiss();
                        dialogAll.dismiss();
                    }
                });

                btn_cancel_dialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });

        //follow
        followLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FOLLOW);
                if (btn_follow_friend.getTag().toString().equals("follow")) {
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
                                                            isFollowing();
                                                            Toast.makeText(InfoProfileFriendActivity.this, getString(R.string.you_following) + fullname_temp, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
                dialogAll.dismiss();
            }
        });

        dialogAll.show();
        dialogAll.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAll.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogAll.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialogAll.getWindow().setGravity(Gravity.BOTTOM);
    }

    //unfriend
    private void unFriend() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS);
        reference.child(current_userid).child(profileid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            reference.child(profileid).child(current_userid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;
                                                linearLayout_friend.setVisibility(View.GONE);

                                                //check state button : received, sent, friend, or not friend
                                                checkStateButtonAddFriend();

                                                //unfollow after unfriend
                                                sendRequestUnFollow();

                                                //set text btn add
                                                btn_add_friend.setText(getString(R.string.add_friend));
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

    //confirm request add friend from sender
    private void confirmFriendRequest() {
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
                                                                                        state_btn_add_friend = Constant.REQUEST_TYPE_FRIEND;
                                                                                        linearLayout_request_friend.setVisibility(View.GONE);
                                                                                        linearLayout_friend.setVisibility(View.VISIBLE);
                                                                                        sendNotificationConfirmFriend(profileid, usenameTemp, getString(R.string.accept_request_add_friend));
                                                                                        sendRequestFollow();

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

    //cancel request add friend
    private void cancelRequestAddFriend() {
        if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_SENT)) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
            reference.child(current_userid)
                    .child(profileid)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                reference.child(profileid).child(current_userid)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;
                                                    btn_add_friend.setText(getString(R.string.add_friend));
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

    //sent request add friend
    private void sentRequestAddFriend() {
        String timestamp = System.currentTimeMillis() + "";
        if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_NOTFRIEND)) {
            //create hashmap
            HashMap<String, Object> hashMapRequest = new HashMap<>();
            hashMapRequest.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_SENT);
            hashMapRequest.put(Constant.REQUEST_TIMESTAMP, timestamp);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST);
            reference.child(current_userid)
                    .child(profileid)
                    .setValue(hashMapRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //create hashmap request received
                                HashMap<String, Object> hashMapRequestReceived = new HashMap<>();
                                hashMapRequestReceived.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_RECEIVED);
                                hashMapRequestReceived.put(Constant.REQUEST_TIMESTAMP, timestamp);

                                reference.child(profileid)
                                        .child(current_userid)
                                        .setValue(hashMapRequestReceived)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    state_btn_add_friend = Constant.REQUEST_TYPE_SENT;
                                                    btn_add_friend.setText(getString(R.string.cancel_request));
                                                    sendNotificationAddFriend(profileid, usenameTemp, getString(R.string.send_friend_request));
                                                    sendRequestFollow();

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
    private void checkStateButtonAddFriend() {
        if (current_userid.equals(profileid)) {
            layout_info.setVisibility(View.VISIBLE);

        } else {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDREQUEST)
                    .child(current_userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    //Friend Request
                    if (snapshot.hasChild(profileid)) {
                        //get type request (receiver or sent)
                        String request_type = snapshot.child(profileid).child(Constant.REQUEST_TYPE).getValue().toString();

                        //check type
                        //Type = received
                        if (request_type.equals(Constant.REQUEST_TYPE_RECEIVED)) {
                            state_btn_add_friend = Constant.REQUEST_TYPE_RECEIVED;

                            linearLayout_add_friend.setVisibility(View.GONE);
                            layout_info.setVisibility(View.GONE);
                            linearLayout_friend.setVisibility(View.GONE);
                            //visible layout
                            linearLayout_request_friend.setVisibility(View.VISIBLE);
                            //updateTextButtonAddFriend();

                        }
                        //Type = sent
                        else if (request_type.equals(Constant.REQUEST_TYPE_SENT)) {
                            state_btn_add_friend = Constant.REQUEST_TYPE_SENT;

                            linearLayout_request_friend.setVisibility(View.GONE);
                            layout_info.setVisibility(View.GONE);
                            linearLayout_friend.setVisibility(View.GONE);

                            //visible layout
                            linearLayout_add_friend.setVisibility(View.VISIBLE);
                            updateTextButtonAddFriend();
                        }

                    }
                    //Friend List
                    else {
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_FRIENDS)
                                .child(current_userid);
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.hasChild(profileid)) {
                                    state_btn_add_friend = Constant.REQUEST_TYPE_FRIEND;
                                    linearLayout_add_friend.setVisibility(View.GONE);
                                    linearLayout_request_friend.setVisibility(View.GONE);
                                    layout_info.setVisibility(View.GONE);
                                    linearLayout_friend.setVisibility(View.VISIBLE);

                                } else {
                                    linearLayout_request_friend.setVisibility(View.GONE);
                                    layout_info.setVisibility(View.GONE);
                                    linearLayout_friend.setVisibility(View.GONE);
                                    state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;
                                    btn_add_friend.setText(getString(R.string.add_friend));
                                    linearLayout_add_friend.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            if (state_btn_add_friend.equals(Constant.REQUEST_TYPE_NOTFRIEND)) {
                //visible layout

            }
        }

    }

    //get intent data
    private void getDataIntent() {
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");
    }

    private void addControls() {

        current_userid = FirebaseAuth.getInstance().getUid();
        stringListIdGroup = new ArrayList<>();
        state_btn_add_friend = Constant.REQUEST_TYPE_NOTFRIEND;

        progress_circular = findViewById(R.id.progress_circular);
        mRefreshLayout = findViewById(R.id.mRefreshLayout);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        imageViewBack = findViewById(R.id.back);
        image_background = findViewById(R.id.image_background);
        more_toolbar = findViewById(R.id.more_toolbar);
        image_profile = findViewById(R.id.image_profile);
        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        total_friend = findViewById(R.id.total_friend);
        mutual_friends = findViewById(R.id.mutual_friends);

        linearLayout_add_friend = findViewById(R.id.layout_add_friend);
        linearLayout_request_friend = findViewById(R.id.layout_request_friend);
        linearLayout_friend = findViewById(R.id.layout_friend);
        layout_info = findViewById(R.id.layout_info);
        about_info = findViewById(R.id.about_info);

        btn_friend = findViewById(R.id.btn_friend);
        btn_chat_friend_layout = findViewById(R.id.btn_chat_friend_layout);
        btn_more_friend_layout = findViewById(R.id.btn_more_friend_layout);
        btn_comfirm_request_friend = findViewById(R.id.btn_comfirm_request_friend);
        btn_delete_request_friend = findViewById(R.id.btn_delete_request_friend);
        btn_more_request_layout = findViewById(R.id.btn_more_request_layout);
        btn_add_friend = findViewById(R.id.btn_add_friend);
        btn_chat_friend = findViewById(R.id.btn_chat_friend);
        btn_follow_friend = findViewById(R.id.btn_follow_friend);
        btn_edit_profile = findViewById(R.id.btn_edit_profile);
        btn_more_info_layout = findViewById(R.id.btn_more_info_layout);

        btn_post_info = findViewById(R.id.btn_post_info);
        btn_about_info = findViewById(R.id.btn_about_info);
        btn_photo_info = findViewById(R.id.btn_photo_info);

        text_gender = findViewById(R.id.text_gender);
        text_birthday = findViewById(R.id.text_birthday);
        text_bio = findViewById(R.id.text_bio);
        text_follower = findViewById(R.id.text_follower);

        recycler_view_mutual_friend = findViewById(R.id.recycler_view_mutual_friend);

        recycler_view_post = findViewById(R.id.recycler_view_post);
        recycler_view_post.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(InfoProfileFriendActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_view_post.setLayoutManager(linearLayoutManager);

        recycler_view_photo = findViewById(R.id.recycler_view_photo);
        recycler_view_photo.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(InfoProfileFriendActivity.this, 3);
        recycler_view_photo.setLayoutManager(linearLayoutManager);

        //hiden layout
        linearLayout_add_friend.setVisibility(View.GONE);
        linearLayout_request_friend.setVisibility(View.GONE);
        linearLayout_friend.setVisibility(View.GONE);
        layout_info.setVisibility(View.GONE);

        recycler_view_post.setVisibility(View.VISIBLE);
        about_info.setVisibility(View.GONE);
        recycler_view_photo.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}