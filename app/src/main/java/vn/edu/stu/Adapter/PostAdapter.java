package vn.edu.stu.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.Task;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.edu.stu.Model.Post;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
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
        postid = post.getPostid();

        //An nut chat
        if (post.getPublisher().equals(firebaseUser.getUid())) {
            holder.chat.setVisibility(View.GONE);
        }

        //show image or video
        //check type post
        if (post.getPosttype().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {
            //type = image
            //Hide videoview
            holder.post_image.setVisibility(View.VISIBLE);
            holder.post_video.setVisibility(View.GONE);

            List<SlideModel> sliderList = new ArrayList<>();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS)
                    .child(post.getPostid()).child(Constant.POST_IMAGE);
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

        } else if (post.getPosttype().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {
            //type = video
            //Hide imageslider
            holder.post_image.setVisibility(View.GONE);
            holder.post_video.setVisibility(View.VISIBLE);

            String videoUrl = post.getPostvideo();

            MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(holder.post_video);

            Uri uriVideo = Uri.parse(videoUrl);
            holder.post_video.setMediaController(mediaController);
            holder.post_video.setVideoURI(uriVideo);

            holder.post_video.requestFocus();
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

        } else {
            holder.post_image.setVisibility(View.GONE);
            holder.post_video.setVisibility(View.GONE);
        }

        //Check text decription
        if (post != null) {

            //Kiem tra post co mo ta khong
            if (post.getDescription().equals("")) {
                holder.description.setVisibility(View.GONE);
            } else {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(post.getDescription());
            }

            //Event like, commemt,not like, getcomment, save
            publisherInfo(holder.image_profile, holder.username, holder.publisher, post.getPublisher());
            isLiked(post.getPostid(), holder.like);
            nrLikes(holder.likes, post.getPostid());
            getComments(post.getPostid(), holder.comments);
            isSaved(post.getPostid(), holder.save);
        }

        //action
        //Click vao hinh dai dien
        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
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
                editor.putString("profileid", post.getPublisher());
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
                            .child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });

        //Click nut like
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        //Click nut comment
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        //Click dong comment
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        //Click text like
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostid());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        //Click nut chat
        holder.chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("user_id", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        //Click nut share
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (post.getPosttype().equals(Constant.DEFAULT_POST_TYPE_TEXT)) {
                    shareText(post.getDescription());
                } else if (post.getPosttype().equals(Constant.DEFAULT_POST_TYPE_IMAGE)) {

                    shareImage(post.getPostid());

                } else if (post.getPosttype().equals(Constant.DEFAULT_POST_TYPE_VIDEO)) {

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
                                editPost(post.getPostid());
                                return true;
                            case R.id.delete:
                                final String id = post.getPostid();
                                FirebaseDatabase.getInstance().getReference("Posts")
                                        .child(post.getPostid()).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    deleteNotifications(id, firebaseUser.getUid());
                                                }
                                            }
                                        });
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
                if (!post.getPublisher().equals(firebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                popupMenu.show();
            }
        });

    }

    private void shareImage(String post) {

        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Please wait prepare share...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        getDataListImageShare(post);

        listUriImage = new ArrayList<>();
        listUriImage.clear();
        for (int i = 0; i < listUrlImage.size(); i++) {
            Glide.with(mContext).asBitmap().load(listUrlImage.get(i).toString()).into(new CustomTarget<Bitmap>() {
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

        }

        /*  getDataListImageShare(post);*/

       /* listUriImage = new ArrayList<>();
        listUriImage.clear();
        Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/luanvan-94446.appspot.com/o/posts%2F1621837719690.png?alt=media&token=28d28a68-5796-4fe9-b021-627458fbf044");
        Uri uri1 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/luanvan-94446.appspot.com/o/iconposttext.png?alt=media&token=119aff42-5164-4d4d-9c58-ec305291836f");
        Uri uri2 = Uri.parse("https://firebasestorage.googleapis.com/v0/b/luanvan-94446.appspot.com/o/iconvideo.png?alt=media&token=10b8ef1c-55d6-4ae0-946a-9469a82c9f3b");

        *//*listUriImage.add(uri);*//*
        listUriImage.add(uri);
        listUriImage.add(uri2);

        progressDialog.dismiss();
        callShareImage();*/


        //convert url image to Uri
        /*for (String urlImg : listUrlImage) {

            listUriImage.add(Uri.parse(urlImg));

            if (listUriImage.size() == listUrlImage.size()) {
                callShareImage();
                progressDialog.dismiss();
            }

            *//*Picasso.get().load(urlImg).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    //convert url image to bitmap


                    if (listUriImage.size() == listUrlImage.size()) {
                        callShareImage();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });*//*

        }*/


    }

    private Bitmap getBitmapFromURL(URL url) {
        try {
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            return image;
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    private void getDataListImageShare(String post) {
        //get data list image
        listUrlImage = new ArrayList<>();
        listUrlImage.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_POSTS);
        reference.child(post).child(Constant.POST_IMAGE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String urlimage = dataSnapshot.child("image").getValue().toString();
                                listUrlImage.add(urlimage);
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

        public ImageView image_profile, like, comment, save, chat, more, share, filterImage;
        public TextView username, likes, publisher, description, comments;
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

            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final TextView publisher, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext.getApplicationContext()).load(user.getImageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(image_profile);
                username.setText(user.getUsername());
                publisher.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void getComments(String postid, final TextView commnets) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "like your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);

    }

    private void nrLikes(final TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes")
                .child(postid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editText.setText(snapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Ham delete thong bao khi xoa bai
    private void deleteNotifications(final String postid, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
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
