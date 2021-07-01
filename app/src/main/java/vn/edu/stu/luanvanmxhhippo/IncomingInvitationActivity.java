package vn.edu.stu.luanvanmxhhippo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Services.APIServicesCall;
import vn.edu.stu.Services.ApiClient;

public class IncomingInvitationActivity extends AppCompatActivity {

    private ImageView imageViewType, imageViewAccept, imageViewCancel;
    private CircleImageView imageViewUserSent;
    private TextView txtUserName, txtUserEmail;

    private MediaPlayer mediaPlayer;

    private String meetingType = null;
    private String meetingRoom = null;

    private String email, userName, urlImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        addControls();
        getDataIntent();
        addEvents();

        loadInfoFriendCall();

    }

    private void addEvents() {
        imageViewAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                sentInvitationRespones("accepted", getIntent().getStringExtra("invitertoken"));
            }
        });
        imageViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                sentInvitationRespones("rejected", getIntent().getStringExtra("invitertoken"));
            }
        });
    }

    private void loadInfoFriendCall() {
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageViewType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals("audio")) {
                imageViewType.setImageResource(R.drawable.ic_call);
            }
        }

        if (userName != null) {
            txtUserName.setText(userName);
        }

        if (userName != null) {
            txtUserEmail.setText(email);
        }

        if (urlImage != null) {
            try {
                Glide.with(IncomingInvitationActivity.this).load(urlImage)
                        .placeholder(R.drawable.placeholder)
                        .into(imageViewUserSent);
            } catch (Exception e) {
                imageViewUserSent.setImageResource(R.drawable.placeholder);
            }

        }
    }

    private void getDataIntent() {
        meetingRoom = getIntent().getStringExtra("meetingRoom");
        meetingType = getIntent().getStringExtra("meetingType");

        userName = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        urlImage = getIntent().getStringExtra("imageURL");
    }

    private void addControls() {
        imageViewType = findViewById(R.id.ImageMeetingType);
        imageViewUserSent = findViewById(R.id.textFirstChar);
        imageViewAccept = findViewById(R.id.ImageAcceptInvitation);
        imageViewCancel = findViewById(R.id.ImageRejectInvitation);
        txtUserName = findViewById(R.id.textUserName);
        txtUserEmail = findViewById(R.id.textEmail);

        mediaPlayer = MediaPlayer.create(IncomingInvitationActivity.this, R.raw.soundiphone12promax);
        mediaPlayer.start();
    }

    public static HashMap<String, String> getRemoteMessageHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put(
                "Authorization",
                "key=AAAAY-i3_Yw:APA91bErcjLbKrVDrbNKVDQ5ztwgVVx5174JUkVgtU-1vNWPp7XZ6khuKNMjMNvYXDnjuMKAdUrvZLMtjDzbwf5nWqySdxyvLQccQHUQgL7bZrde53kRVXBpldy_PUme57AiX-uR5Sw_");
        header.put("Content-Type", "application/json");
        return header;
    }

    private void sentInvitationRespones(String type, String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("type", "invitationResponse");
            data.put("invitationResponse", type);

            body.put("data", data);
            body.put("registration_ids", token);

            sendRemoteMessage(body.toString(), type);

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void sendRemoteMessage(String remoteMessageBody, final String type) {
        ApiClient.getClient().create(APIServicesCall.class).sendRemoteMessage(
                getRemoteMessageHeader(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (type.equals("accepted")) {
                        try {
                            URL serviceURL = new URL("https://meet.jit.si/");
                            if (meetingType.equals("video")) {
                                JitsiMeetConferenceOptions conferenceOptions =
                                        new JitsiMeetConferenceOptions.Builder()
                                                .setServerURL(serviceURL)
                                                .setWelcomePageEnabled(false)
                                                .setRoom(getIntent().getStringExtra("meetingType"))
                                                .setVideoMuted(false)
                                                .setAudioMuted(false)
                                                .setAudioOnly(false)
                                                .build();
                                JitsiMeetActivity.launch(IncomingInvitationActivity.this, conferenceOptions);
                                finish();
                            } else if (meetingType.equals("audio")) {
                                JitsiMeetConferenceOptions conferenceOptions =
                                        new JitsiMeetConferenceOptions.Builder()
                                                .setServerURL(serviceURL)
                                                .setWelcomePageEnabled(false)
                                                .setRoom(getIntent().getStringExtra("meetingType"))
                                                .setVideoMuted(true)
                                                .setAudioMuted(false)
                                                .setAudioOnly(false)
                                                .build();
                                JitsiMeetActivity.launch(IncomingInvitationActivity.this, conferenceOptions);
                                finish();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(IncomingInvitationActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Cancel", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("invitationResponse");
            if (type != null) {
                if (type.equals("cancelled")) {
                    Toast.makeText(context, "Invitation Accept", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        /*if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }*/
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter("invitationResponse")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }*/
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );

    }
}