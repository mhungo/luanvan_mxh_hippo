package vn.edu.stu.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.stu.Model.Client;
import vn.edu.stu.Model.Data;
import vn.edu.stu.Model.MyResponse;
import vn.edu.stu.Model.Sender;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.HolderParticipantAdd> {

    private Context context;
    private ArrayList<User> userList;

    private String groupId, myGroupRole; //creator/admin/participant

    public ParticipantAdapter(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @NotNull
    @Override
    public HolderParticipantAdd onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.paticipant_item, parent, false);

        return new HolderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ParticipantAdapter.HolderParticipantAdd holder, int position) {
        //get data

        User user = userList.get(position);

        String name = user.getUser_fullname();
        String email = user.getUser_email();
        String image = user.getUser_imageurl();
        String uid = user.getUser_id();

        //set data
        holder.nameTv.setText(name);
        holder.emailTv.setText(email);
        try {
            Glide.with(context).load(image).placeholder(R.drawable.placeholder)
                    .into(holder.avataIv);
        } catch (Exception e) {
            holder.avataIv.setImageResource(R.drawable.placeholder);
        }

        checkIfAlreadyExists(user, holder);

        //handle click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if user already added or not
                 * if add: show remove-paticipant/admin/removed admin option, admin will not change role of create
                 * if not, show oaticipants option*/

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
                ref.child(groupId).child(Constant.COLLECTION_PARTICIPANTS).child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    //user exists/not-participant
                                    String hisPreviousRole = "" + snapshot.child("role").getValue();

                                    //option to display in dialog
                                    String[] option;

                                    /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Choose option");*/
                                    if (myGroupRole.equals("creator")) {
                                        if (hisPreviousRole.equals("admin")) {
                                            //im creator, he is admin

                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                            builder1.setTitle(R.string.choose_option);
                                            builder1.setCancelable(true);

                                            builder1.setPositiveButton(R.string.remove_admin, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove Admin clicked
                                                    removeAdmin(user);
                                                }
                                            });

                                            builder1.setNegativeButton(R.string.remove_user, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder1.create();
                                            alert.show();

                                            /*option = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Remove Admin clicked
                                                        removeAdmin(user);

                                                    } else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();*/
                                        } else if (hisPreviousRole.equals("participant")) {
                                            //im creator, he is participant
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle(R.string.choose_option);
                                            builder.setCancelable(true);

                                            builder.setPositiveButton(context.getString(R.string.make_admin), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Make Admin clicked
                                                    makeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton(context.getString(R.string.remove_user), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder.create();
                                            alert.show();

                                            /*option = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //handle item clicks
                                                    if (which == 0) {
                                                        //Make Admin clicked
                                                        makeAdmin(user);

                                                    } else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();*/
                                        }
                                    } else if (myGroupRole.equals("admin")) {
                                        if (hisPreviousRole.equals("creator")) {
                                            //im admin, he is creator
                                            Toast.makeText(context, R.string.creator_of_group, Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("admin")) {
                                            // im admin, he is admin too
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle(R.string.choose_option);
                                            builder.setCancelable(true);

                                            builder.setPositiveButton(context.getString(R.string.remove_admin), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton(context.getString(R.string.remove_user), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder.create();
                                            alert.show();

                                        } else if (hisPreviousRole.equals("participant")) {
                                            //im admin, he is particitpant
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle(R.string.choose_option);
                                            builder.setCancelable(true);

                                            builder.setPositiveButton(context.getString(R.string.make_admin), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //make Admin clicked
                                                    makeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton(context.getString(R.string.remove_user), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder.create();
                                            alert.show();

                                        }
                                    }
                                } else {
                                    //user doesn't exists/not-participant : add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle(R.string.add_participant)
                                            .setMessage(R.string.add_user_in_this_group)
                                            .setPositiveButton(context.getString(R.string.add), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addParticipant(user);
                                                }
                                            })
                                            .setNegativeButton(context.getString(R.string.cancels), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
            }
        });
    }

    private void makeAdmin(User user) {
        //setup data - change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin"); //role are : participant/admin/creator
        //update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId).child(Constant.COLLECTION_PARTICIPANTS).child(user.getUser_id()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //make admin
                        Toast.makeText(context, R.string.user_is_now_admin, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed making admin
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void addParticipant(User user) {
        //setup user data - add user in group
        String timstamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUser_id());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", "" + timstamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //added successfully
                        sendNotification(user.getUser_token(), context.getString(R.string.click_to_know_more));
                        Toast.makeText(context, R.string.add_susccessfull, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //fail adding user in group
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeParticipant(User user) {
        //remove participant from group
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //removed successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //fail removing participant

                    }
                });
    }

    private void removeAdmin(User user) {
        //setup data - remove admin - just change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant"); //role are : participant/admin/creator
        //update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //make admin
                        Toast.makeText(context, R.string.user_is_nolonger_admin, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        //failed making admin
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void checkIfAlreadyExists(User user, HolderParticipantAdd holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        reference.child(groupId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //already exists
                            String hisRole = "" + snapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        } else {
                            //don't exists
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void sendNotification(String receiver, String message) {
        APIService apiService;
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        Data data = new Data(FirebaseAuth.getInstance().getUid(), R.drawable.notify, message, context.getString(R.string.you_have_just_add_group), groupId, Constant.TYPE_NOTIFICATION_ADD_PARTICIPANT);

        Sender sender = new Sender(data, receiver);

        apiService.sendNotification(sender)
                .enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.code() == 200) {
                            if (response.body().success != 1) {
                                Toast.makeText(context, R.string.error_sent_notification, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class HolderParticipantAdd extends RecyclerView.ViewHolder {

        private ImageView avataIv;
        private TextView nameTv, emailTv, statusTv;

        public HolderParticipantAdd(@NonNull @NotNull View itemView) {
            super(itemView);

            avataIv = itemView.findViewById(R.id.avataTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
