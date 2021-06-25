package vn.edu.stu.luanvanmxhhippo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIServicesCall;
import vn.edu.stu.Services.ApiClient;
import vn.edu.stu.Util.Constant;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ImageView imageViewMeetingType, imageViewStopInvitation;

    private TextView textUserName, textEmail;
    private CircleImageView imageUserCall;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private User fuser, currentUser;

    private String userid;
    private String token_Current = null;
    private String meetingRoom = null;
    private String meetingType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        addControls();
        getDataIntent();

        checkTypeCall();
        getUserCall();

        //Get curent user
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUser = user;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        // Sent InitiaMeeting
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_TOKENS);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<String> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    token_Current = task.getResult();
                    if (meetingType != null && fuser != null) {
                        initiaMeeting(meetingType, fuser.getUser_token());
                    }

                }
            }
        });


        //Event click stop
        imageViewStopInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fuser != null) {
                    cancelInvitation(fuser.getUser_token());
                }
            }
        });


        /*FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    token_Current = task.getResult().getToken();
                    if (meetingType != null && fuser != null) {
                        initiaMeeting(meetingType, fuser.getToken());
                    }
                }
            }
        });*/

    }

    private void getUserCall() {
        //Get user sent
        reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    fuser = user;
                    textUserName.setText(user.getUser_username());
                    textEmail.setText(user.getUser_email());
                    try {
                        Glide.with(OutgoingInvitationActivity.this).load(user.getUser_imageurl())
                                .placeholder(R.drawable.placeholder)
                                .into(imageUserCall);
                    } catch (Exception e) {
                        imageUserCall.setImageResource(R.drawable.placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void checkTypeCall() {
        if (meetingType != null) {
            if (meetingType.equals("video")) {
                imageViewMeetingType.setImageResource(R.drawable.ic_video);
            } else if (meetingType.equals("audio")) {
                imageViewMeetingType.setImageResource(R.drawable.ic_call);
            }
        }
    }

    private void getDataIntent() {
        meetingType = getIntent().getStringExtra("typeCall");
        userid = getIntent().getStringExtra("userid");
    }

    private void addControls() {
        imageUserCall = findViewById(R.id.textFirstChar);
        textUserName = findViewById(R.id.textUserName);
        textEmail = findViewById(R.id.textEmail);

        imageViewMeetingType = findViewById(R.id.ImageMeetingType);
        imageViewStopInvitation = findViewById(R.id.ImageStopInvitation);

        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    public static HashMap<String, String> getRemoteMessageHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put(
                "Authorization",
                "key=AAAAY-i3_Yw:APA91bErcjLbKrVDrbNKVDQ5ztwgVVx5174JUkVgtU-1vNWPp7XZ6khuKNMjMNvYXDnjuMKAdUrvZLMtjDzbwf5nWqySdxyvLQccQHUQgL7bZrde53kRVXBpldy_PUme57AiX-uR5Sw_");
        header.put("Content-Type", "application/json");
        return header;

    }

    private void initiaMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("type", "invitation");
            data.put("meetingType", meetingType);
            data.put("name", currentUser.getUser_username());
            data.put("email", currentUser.getUser_email());
            data.put("imageURL", currentUser.getUser_imageurl().toString());
            data.put("invitertoken", token_Current);

            meetingRoom = userid + "_" + UUID.randomUUID().toString().substring(0, 5);
            data.put("meetingRoom", meetingRoom);

            body.put("data", data);
            body.put("registration_ids", token);

            sendRemoteMessage(body.toString(), "invitation");

        } catch (Exception exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (type.equals("invitation")) {
                        Toast.makeText(OutgoingInvitationActivity.this, "Inivitation Sent success", Toast.LENGTH_SHORT).show();
                    } else if (type.equals("invitationResponse")) {
                        Toast.makeText(OutgoingInvitationActivity.this, "Inivitation Cancel", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray token = new JSONArray();
            token.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("type", "invitationResponse");
            data.put("invitationResponse", "cancelled");

            body.put("data", data);
            body.put("registration_ids", token);

            sendRemoteMessage(body.toString(), "invitationResponse");

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("invitationResponse");
            if (type != null) {
                if (type.equals("accepted")) {
                    try {
                        URL serviceURL = new URL("https://meet.jit.si/");
                        if (meetingType.equals("video")) {
                            JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                            .setServerURL(serviceURL)
                                            .setWelcomePageEnabled(false)
                                            .setRoom(meetingType)
                                            .setVideoMuted(false)
                                            .setAudioMuted(false)
                                            .setAudioOnly(false)
                                            .build();
                            JitsiMeetActivity.launch(OutgoingInvitationActivity.this, conferenceOptions);
                            finish();
                        } else if (meetingType.equals("audio")) {
                            JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                            .setServerURL(serviceURL)
                                            .setWelcomePageEnabled(false)
                                            .setRoom(meetingType)
                                            .setVideoMuted(true)
                                            .setAudioMuted(false)
                                            .setAudioOnly(false)
                                            .build();
                            JitsiMeetActivity.launch(OutgoingInvitationActivity.this, conferenceOptions);
                            finish();
                        }

                    } catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals("rejected")) {
                    Toast.makeText(context, "Invitation Reject", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter("invitationResponse")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );

    }
}