package vn.edu.stu.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.Messages;
import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private FirebaseAuth firebaseAuth;

    private Context mContext;
    private List<Messages> messagesList;


    public MessageAdapter(Context mContext, List<Messages> messagesList) {
        this.mContext = mContext;
        this.messagesList = messagesList;

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

        //convert time stamp to dd/mm/yyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        if (type.equals("text")) {
            //text message, hide image, show textview
            holder.message_image_chat.setVisibility(View.GONE);
            holder.message_text_chat.setVisibility(View.VISIBLE);
            holder.message_text_chat.setText(message);

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

        //set time
        holder.time_message_chat.setText(dateTime);

        //set image user
        loadImageUser(messages, holder);
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
        public ImageView message_image_chat;
        public TextView time_message_chat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image_chat = itemView.findViewById(R.id.profile_image_chat);
            message_text_chat = itemView.findViewById(R.id.message_text_chat);
            message_image_chat = itemView.findViewById(R.id.message_image_chat);
            time_message_chat = itemView.findViewById(R.id.time_message_chat);
        }
    }


}