package vn.edu.stu.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vn.edu.stu.Model.GroupChat;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetReviewUrl;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.OpenImagenActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private ArrayList<GroupChat> groupChatArrayList;
    private String id_groupchat;

    private FirebaseAuth firebaseAuth;

    public GroupChatAdapter(Context context, ArrayList<GroupChat> groupChatArrayList, String id_groupchat) {
        this.context = context;
        this.groupChatArrayList = groupChatArrayList;
        this.id_groupchat = id_groupchat;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @NotNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_chat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupChatAdapter.HolderGroupChat holder, int position) {

        //get  data
        GroupChat model = groupChatArrayList.get(position);

        String timestamp = model.getGroudchat_timestamp();
        String message = model.getGroudchat_message();
        String sender = model.getGroudchat_sender();
        String messageType = model.getGroudchat_type();
        String messageImage = model.getGroudchat_image();

        //convert time stamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (messageType.equals("text")) {
            //text message, hide image, show textview
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);

            if (checkIsUrl(message)) {
                holder.layout_review.setVisibility(View.VISIBLE);

                //click link;
                holder.messageTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(message));
                        context.startActivity(intent);
                    }
                });
                //get meta web
                GetReviewUrl.getJsoupContent(message)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                                    Elements metaTags = result.getElementsByTag("meta");
                                    for (Element elements : metaTags) {
                                        if (elements.attr("property").equals("og:image"))
                                            try {
                                                Glide.with(context).load(elements.attr("content"))
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
                                                context.startActivity(intent);
                                            });
                                        }
                                    }
                                },
                                error -> {
                                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                                });

            } else {
                holder.layout_review.setVisibility(View.GONE);
            }


        } else {
            //image message
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
            try {
                Glide.with(context).load(messageImage).placeholder(R.drawable.placeholder)
                        .into(holder.messageIv);
            } catch (Exception e) {
                holder.messageIv.setImageResource(R.drawable.placeholder);
            }
        }
        //set data

        holder.profile_image_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", model.getGroudchat_sender());
                editor.apply();

                Intent intent = new Intent(context, InfoProfileFriendActivity.class);
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messageType.equals("image")) {
                    Intent intent = new Intent(context, OpenImagenActivity.class);
                    intent.putExtra("image_url_open", model.getGroudchat_image());
                    context.startActivity(intent);
                }
            }
        });

        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!model.getGroudchat_sender().equals(FirebaseAuth.getInstance().getUid())) {

                } else {
                    chooseDeleteOrRecall(model, id_groupchat);
                }
                return true;
            }
        });*/

        holder.timeTv.setText(dateTime);

        setUserName(model, holder);

    }

    private boolean checkIsUrl(String text) {
        try {
            new URL(text).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void chooseDeleteOrRecall(GroupChat messages, String id_groupchat) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.choose_delete_recall_message);

        LinearLayout recall_messages_layout = dialog.findViewById(R.id.recall_messages_layout);
        LinearLayout delete_messages_layout = dialog.findViewById(R.id.delete_messages_layout);

        if (!messages.getGroudchat_sender().equals(FirebaseAuth.getInstance().getUid())) {
            recall_messages_layout.setVisibility(View.GONE);
        } else {
            recall_messages_layout.setVisibility(View.VISIBLE);
        }
        delete_messages_layout.setVisibility(View.GONE);

        delete_messages_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMessages(messages, id_groupchat);
                dialog.dismiss();

            }
        });

        recall_messages_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recallMessages(messages, id_groupchat);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }

    private void recallMessages(GroupChat messages, String id_groupchat) {
        Dialog dialog = new Dialog(context);
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
        textviewtitile.setText(context.getString(R.string.are_you_recall_messages));

        //confirm unfollow
        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                reference.child(id_groupchat)
                        .child(Constant.COLLECTION_MESSAGES)
                        .child(messages.getGroudchat_id())
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    groupChatArrayList.remove(messages);
                                    notifyDataSetChanged();
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

    private void deleteMessages(GroupChat messages, String id_groupchat) {
        Dialog dialog = new Dialog(context);
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
        textviewtitile.setText(context.getString(R.string.do_you_want_delete));

        //confirm unfollow
        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                reference.child(id_groupchat)
                        .child(Constant.COLLECTION_MESSAGES)
                        .child(messages.getGroudchat_id())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                groupChatArrayList.remove(messages);
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {

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

    private void setUserName(GroupChat model, HolderGroupChat holder) {
        //get sender info from uid iin model
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.orderByChild(Constant.USER_ID).equalTo(model.getGroudchat_sender())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child(Constant.USER_USERNAME).getValue();
                            String urlimage = "" + ds.child(Constant.USER_IMAGEURL).getValue();

                            holder.nameTv.setText(name);
                            try {
                                Glide.with(context).load(urlimage).placeholder(R.drawable.placeholder)
                                        .into(holder.profile_image_chat);
                            } catch (Exception e) {
                                holder.profile_image_chat.setImageResource(R.drawable.placeholder);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemViewType(int position) {
        if (groupChatArrayList.get(position).getGroudchat_sender().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return groupChatArrayList.size();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv, txt_title, txt_decription_review;
        private ImageView messageIv, profile_image_chat, img_review;
        public LinearLayout layout_review;

        public HolderGroupChat(@NonNull @NotNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            profile_image_chat = itemView.findViewById(R.id.profile_image_chat);
            txt_title = itemView.findViewById(R.id.txt_title);
            txt_decription_review = itemView.findViewById(R.id.txt_decription_review);
            img_review = itemView.findViewById(R.id.img_review);
            layout_review = itemView.findViewById(R.id.layout_review);


        }
    }


}
