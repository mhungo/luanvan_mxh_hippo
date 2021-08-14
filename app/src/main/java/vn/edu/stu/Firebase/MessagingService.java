 package vn.edu.stu.Firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import vn.edu.stu.Notification.OreoNotification;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.ChatManagerActivity;
import vn.edu.stu.luanvanmxhhippo.GroupChatActivity;
import vn.edu.stu.luanvanmxhhippo.IncomingInvitationActivity;
import vn.edu.stu.luanvanmxhhippo.InfoProfileFriendActivity;
import vn.edu.stu.luanvanmxhhippo.MainActivity;
import vn.edu.stu.luanvanmxhhippo.MessageActivity;
import vn.edu.stu.luanvanmxhhippo.PostDetailActivity;
import vn.edu.stu.luanvanmxhhippo.R;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get("type");
        if (type != null) {
            switch (type) {
                case "invitation":
                    Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                    intent.putExtra("meetingType", remoteMessage.getData().get("meetingType"));
                    intent.putExtra("name", remoteMessage.getData().get("name"));
                    intent.putExtra("email", remoteMessage.getData().get("email"));
                    intent.putExtra("imageURL", remoteMessage.getData().get("imageURL"));
                    intent.putExtra("invitertoken", remoteMessage.getData().get("invitertoken"));
                    intent.putExtra("meetingRoom", remoteMessage.getData().get("meetingRoom"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;

                case "invitationResponse":
                    Intent intentRespon = new Intent("invitationResponse");
                    intentRespon.putExtra("invitationResponse", remoteMessage.getData().get("invitationResponse"));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentRespon);
                    break;

                case Constant.TYPE_NOTIFICATION_CHAT:
                    String sented = remoteMessage.getData().get("sented");
                    String user = remoteMessage.getData().get("user");

                    SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
                    String currentUser = preferences.getString("currentuser", "none");

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
                        if (!currentUser.equals(user)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                sendOreoNotification(remoteMessage);
                            } else {
                                sendNotification(remoteMessage);
                            }
                        }
                    }
                    break;

                case Constant.TYPE_NOTIFICATION_COMMENT:
                    String sent = remoteMessage.getData().get("sented");
                    if (sent.equals(FirebaseAuth.getInstance().getUid())) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showNotificationOreoComent(remoteMessage);
                        } else {
                            showNotificationComent(remoteMessage);
                        }
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_LIKE:
                    String sentlike = remoteMessage.getData().get("sented");
                    if (sentlike.equals(FirebaseAuth.getInstance().getUid())) {

                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            showNotificationOreoLike(remoteMessage);
                        } else {
                            showNotificationLike(remoteMessage);
                        }
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_FOLLOWING:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoFollowFriend(remoteMessage);
                    } else {
                        showNotificationFollowFriend(remoteMessage);
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_ADDFRIEND:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoAddFriend(remoteMessage);
                    } else {
                        showNotificationAddFriend(remoteMessage);
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_CONFIRMFRIEND:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoConfirmFriend(remoteMessage);
                    } else {
                        showNotificationConfirmFriend(remoteMessage);
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_GROUPCHAT:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoGroupChat(remoteMessage);
                    } else {
                        showNotificationGroupChat(remoteMessage);
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_ADD_PARTICIPANT:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoAddParticipant(remoteMessage);
                    } else {
                        showNotificationAddParticipant(remoteMessage);
                    }
                    break;
                case Constant.TYPE_NOTIFICATION_INVITATION_JOIN_GROUP:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoInvitationJoinGroup(remoteMessage);
                    } else {
                        showNotificationInvitationJoinGroup(remoteMessage);
                    }
                    break;
            }
        }

        /*if (type != null) {
            if (type.equals("invitation")) {
                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra("meetingType",
                        remoteMessage.getData().get("meetingType"));
                intent.putExtra("name",
                        remoteMessage.getData().get("name"));
                intent.putExtra("email",
                        remoteMessage.getData().get("email"));
                intent.putExtra("imageURL",
                        remoteMessage.getData().get("imageURL"));
                intent.putExtra("invitertoken",
                        remoteMessage.getData().get("invitertoken"));
                intent.putExtra("meetingRoom",
                        remoteMessage.getData().get("meetingRoom"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else if (type.equals("invitationResponse")) {
                Intent intent = new Intent("invitationResponse");
                intent.putExtra(
                        "invitationResponse",
                        remoteMessage.getData().get("invitationResponse"));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (type.equals(Constant.TYPE_NOTIFICATION_CHAT)) {
                String sented = remoteMessage.getData().get("sented");
                String user = remoteMessage.getData().get("user");

                SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
                String currentUser = preferences.getString("currentuser", "none");

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
                    if (!currentUser.equals(user)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendOreoNotification(remoteMessage);
                        } else {
                            sendNotification(remoteMessage);
                        }
                    }
                }
            } else if (type.equals(Constant.TYPE_NOTIFICATION_COMMENT)) {
                String sented = remoteMessage.getData().get("sented");
                if (sented.equals(FirebaseAuth.getInstance().getUid())) {

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoComent(remoteMessage);
                    } else {
                        showNotificationComent(remoteMessage);
                    }
                }
            } else if (type.equals(Constant.TYPE_NOTIFICATION_LIKE)) {
                String sented = remoteMessage.getData().get("sented");
                if (sented.equals(FirebaseAuth.getInstance().getUid())) {

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        showNotificationOreoLike(remoteMessage);
                    } else {
                        showNotificationLike(remoteMessage);
                    }
                }
            }
        }*/

        Log.i("CHECK", "onMessageReceived: " + type);

    }

    private void showNotificationInvitationJoinGroup(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment_type", "action");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoInvitationJoinGroup(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment_type", "action");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationAddParticipant(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoAddParticipant(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, ChatManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationGroupChat(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("groupId", sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notifySound)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoGroupChat(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String sender = remoteMessage.getData().get("sented");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, GroupChatActivity.class);
        intent.putExtra("groupId", sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                notifySound, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationFollowFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, InfoProfileFriendActivity.class);

        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoFollowFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, InfoProfileFriendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationConfirmFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, InfoProfileFriendActivity.class);

        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoConfirmFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, InfoProfileFriendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationAddFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, InfoProfileFriendActivity.class);

        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoAddFriend(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileid", user);
        editor.apply();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, InfoProfileFriendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationLike(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, PostDetailActivity.class);

        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("postid", user);
        editor.apply();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoLike(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("postid", user);
        editor.apply();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void showNotificationComent(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, PostDetailActivity.class);
       /* Bundle bundle = new Bundle();
        bundle.putString("user_id", user);
        intent.putExtras(bundle);*/
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("postid", user);
        editor.apply();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager notificationManageComment = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        notificationManageComment.notify(i, builder.build());

    }

    private void showNotificationOreoComent(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("postid", user);
        editor.apply();

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                null, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,
                notifySound, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri notifySound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.soundmess);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notifySound)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        noti.notify(i, builder.build());
    }


}
