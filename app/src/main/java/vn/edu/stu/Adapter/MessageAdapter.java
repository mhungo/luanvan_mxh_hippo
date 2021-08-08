package vn.edu.stu.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.Util.GetReviewUrl;
import vn.edu.stu.Util.GetTimeAgo;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.OpenImagenActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private FirebaseAuth firebaseAuth;

    private Context mContext;
    private List<Messages> messagesList;
    private String user_chat;


    public MessageAdapter(Context mContext, List<Messages> messagesList, String user_chat) {
        this.mContext = mContext;
        this.messagesList = messagesList;
        this.user_chat = user_chat;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_layout_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Messages messages = messagesList.get(position);
        String message = messages.getMessage_message();
        String timestamp = messages.getMessage_timestamp();
        String type = messages.getMessage_type();
        String urlimage = messages.getMessage_image();

        /*//convert time stamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();*/

        if (type.equals("text")) {
            //text message, hide image, show textview
            holder.message_image_chat.setVisibility(View.GONE);
            holder.message_text_chat.setVisibility(View.VISIBLE);
            holder.message_text_chat.setText(message);

            if (checkIsUrl(message)) {
                holder.layout_review.setVisibility(View.VISIBLE);

                //click link;
                holder.message_text_chat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(message));
                        mContext.startActivity(intent);
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
            }


        } else if (type.equals("image")) {
            //image message
            holder.message_image_chat.setVisibility(View.VISIBLE);
            holder.message_text_chat.setVisibility(View.GONE);
            try {
                Glide.with(mContext).load(urlimage)
                        .placeholder(R.drawable.placeholder)
                        .into(holder.message_image_chat);
            } catch (Exception e) {
                holder.message_image_chat.setImageResource(R.drawable.placeholder);
            }
        }

        holder.profile_image_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", messages.getMessage_from());
                editor.apply();

                Intent intent = new Intent(mContext, InfoProfileFriendActivity.class);
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (messages.getMessage_type().equals("image")) {
                    Intent intent = new Intent(mContext, OpenImagenActivity.class);
                    intent.putExtra("image_url_open", messages.getMessage_image());
                    mContext.startActivity(intent);
                }
            }
        });

        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                chooseDeleteOrRecall(messages, user_chat);
                return true;
            }
        });*/

        //set time
        holder.time_message_chat.setText(GetTimeAgo.getTimeAgo(Long.parseLong(timestamp), mContext));

        //set image user
        loadImageUser(messages, holder);
    }

    private boolean checkIsUrl(String text) {
        try {
            new URL(text).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void chooseDeleteOrRecall(Messages messages, String user_chat) {
        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.choose_delete_recall_message);

        LinearLayout recall_messages_layout = dialog.findViewById(R.id.recall_messages_layout);
        LinearLayout delete_messages_layout = dialog.findViewById(R.id.delete_messages_layout);

        if (!messages.getMessage_from().equals(FirebaseAuth.getInstance().getUid())) {
            recall_messages_layout.setVisibility(View.GONE);
        } else {
            recall_messages_layout.setVisibility(View.VISIBLE);
        }

        delete_messages_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMessages(messages, user_chat);
                dialog.dismiss();

            }
        });

        recall_messages_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recallMessages(messages, user_chat);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);


    }

    private void recallMessages(Messages messages, String user_chat) {
        Dialog dialog = new Dialog(mContext);
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
        textviewtitile.setText(mContext.getString(R.string.are_you_recall_messages));

        //confirm unfollow
        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
                reference.child(FirebaseAuth.getInstance().getUid())
                        .child(user_chat)
                        .child(messages.getMessage_id())
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    reference.child(user_chat)
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child(messages.getMessage_id())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        messagesList.remove(messages);
                                                        notifyDataSetChanged();
                                                        Toast.makeText(mContext, mContext.getString(R.string.mesages_has_been_withdraw), Toast.LENGTH_SHORT).show();
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

    private void deleteMessages(Messages messages, String user_chat) {
        Dialog dialog = new Dialog(mContext);
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
        textviewtitile.setText(mContext.getString(R.string.do_you_want_delete));

        //confirm unfollow
        btn_confirm_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_MESSAGES);
                reference.child(FirebaseAuth.getInstance().getUid())
                        .child(user_chat)
                        .child(messages.getMessage_id())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                messagesList.remove(messages);
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


    private void loadImageUser(Messages messages, ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(messages.getMessage_from());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                try {
                    Glide.with(mContext).load(user.getUser_imageurl())
                            .placeholder(R.drawable.placeholder)
                            .into(holder.profile_image_chat);
                } catch (Exception e) {
                    holder.profile_image_chat.setImageResource(R.drawable.placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getMessage_from().equals(firebaseAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profile_image_chat;
        public TextView message_text_chat;
        public ImageView message_image_chat, img_review;
        public TextView time_message_chat, txt_decription_review, txt_title;
        public LinearLayout layout_review;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image_chat = itemView.findViewById(R.id.profile_image_chat);
            message_text_chat = itemView.findViewById(R.id.message_text_chat);
            message_image_chat = itemView.findViewById(R.id.message_image_chat);
            time_message_chat = itemView.findViewById(R.id.time_message_chat);
            layout_review = itemView.findViewById(R.id.layout_review);
            txt_decription_review = itemView.findViewById(R.id.txt_decription_review);
            txt_title = itemView.findViewById(R.id.txt_title);
            img_review = itemView.findViewById(R.id.img_review);
        }
    }


}