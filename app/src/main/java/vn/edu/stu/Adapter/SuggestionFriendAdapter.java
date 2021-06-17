package vn.edu.stu.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.edu.stu.Model.User;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class SuggestionFriendAdapter extends RecyclerView.Adapter<SuggestionFriendAdapter.ViewHolder> {

    private Context context;
    private List<User> userList;

    public SuggestionFriendAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.suggestion_friend_item, parent, false);
        return new SuggestionFriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SuggestionFriendAdapter.ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user != null) {
            try {
                Glide.with(context).load(user.getUser_imageurl())
                        .placeholder(R.drawable.placeholder)
                        .into(holder.img_suggestion_friend);
            } catch (Exception e) {
                holder.img_suggestion_friend.setImageResource(R.drawable.placeholder);
            }

            holder.text_username_suggestion_friend.setText(user.getUser_fullname());

            //click image => info profile
            holder.img_suggestion_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                    context.startActivity(intent);
                }
            });

            //click name user => info profile
            holder.text_username_suggestion_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    Intent intent = new Intent(context.getApplicationContext(), InfoProfileFriendActivity.class);
                    context.startActivity(intent);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView img_suggestion_friend;
        public TextView text_username_suggestion_friend;
        public MaterialButton btn_add_suggestion_friend;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            img_suggestion_friend = itemView.findViewById(R.id.img_suggestion_friend);
            text_username_suggestion_friend = itemView.findViewById(R.id.text_username_suggestion_friend);
            btn_add_suggestion_friend = itemView.findViewById(R.id.btn_add_suggestion_friend);

        }
    }
}
