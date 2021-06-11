package vn.edu.stu.luanvanmxhhippo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoProfileFriendActivity extends AppCompatActivity {

    private String profileid = "";
    private ImageView imageViewBack, image_background;
    private TextView username, fullname, total_friend, mutual_friends;

    private LinearLayout linearLayout_add_friend, linearLayout_request_friend, linearLayout_friend;

    private CircleImageView image_profile;
    private MaterialButton btn_friend, btn_chat_friend_layout, btn_more_friend_layout,
            btn_request_friend, btn_chat_friend_request_layout, btn_more_request_layout,
            btn_add_friend, btn_chat_friend, btn_follow_friend;

    private RecyclerView recycler_view_post, recycler_view_mutual_friend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_profile_friend);

        addControls();
        getDataIntent();
        addEvent();

    }

    //get intent data
    private void getDataIntent() {
        SharedPreferences prefs = getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");
    }

    private void addControls() {
        imageViewBack = findViewById(R.id.back);
        image_background = findViewById(R.id.image_background);
        username = findViewById(R.id.username);
        fullname = findViewById(R.id.fullname);
        total_friend = findViewById(R.id.total_friend);
        mutual_friends = findViewById(R.id.mutual_friends);
        linearLayout_add_friend = findViewById(R.id.layout_add_friend);
        linearLayout_request_friend = findViewById(R.id.layout_request_friend);
        linearLayout_friend = findViewById(R.id.layout_friend);
        image_profile = findViewById(R.id.image_profile);
        btn_friend = findViewById(R.id.btn_friend);
        btn_chat_friend_layout = findViewById(R.id.btn_chat_friend_layout);
        btn_more_friend_layout = findViewById(R.id.btn_more_friend_layout);
        btn_request_friend = findViewById(R.id.btn_request_friend);
        btn_chat_friend_request_layout = findViewById(R.id.btn_chat_friend_request_layout);
        btn_more_request_layout = findViewById(R.id.btn_more_request_layout);
        btn_add_friend = findViewById(R.id.btn_add_friend);
        btn_chat_friend = findViewById(R.id.btn_chat_friend);
        btn_follow_friend = findViewById(R.id.btn_follow_friend);

        recycler_view_post = findViewById(R.id.recycler_view_post);
        recycler_view_mutual_friend = findViewById(R.id.recycler_view_mutual_friend);


    }

    private void addEvent() {


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}