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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

import vn.edu.stu.Model.User;
import vn.edu.stu.Util.Constant;
import vn.edu.stu.luanvanmxhhippo.R;

public class GroupPostParticipantAdapter extends RecyclerView.Adapter<GroupPostParticipantAdapter.ViewHolder> {

    private Context context;
    private ArrayList<User> userList;

    private String groupPostId, myGroupRole; //creator/admin/participant

    public GroupPostParticipantAdapter(Context context, ArrayList<User> userList, String groupPostId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupPostId = groupPostId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.paticipant_item, parent, false);

        return new GroupPostParticipantAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupPostParticipantAdapter.ViewHolder holder, int position) {
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

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
                ref.child(groupPostId)
                        .child(Constant.COLLECTION_PARTICIPANTS)
                        .child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    //user exists/not-participant
                                    String hisPreviousRole = "" + snapshot.child(Constant.ROLE_ROLE).getValue();

                                    //option to display in dialog
                                    String[] option;

                                    if (myGroupRole.equals(Constant.ROLE_CREATOR)) {
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

                                        } else if (hisPreviousRole.equals("participant")) {
                                            //im creator, he is participant
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle(R.string.choose_option);
                                            builder.setCancelable(true);

                                            builder.setPositiveButton(R.string.make_admin, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Make Admin clicked
                                                    makeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton(R.string.remove_user, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder.create();
                                            alert.show();

                                        }
                                    } else if (myGroupRole.equals("admin")) {
                                        if (hisPreviousRole.equals("creator")) {
                                            //im admin, he is creator
                                            Toast.makeText(context, context.getString(R.string.creator_of_group), Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("admin")) {
                                            // im admin, he is admin too
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle(R.string.choose_option);
                                            builder.setCancelable(true);

                                            builder.setPositiveButton(context.getString(R.string.remove_admin), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
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
                                            .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    //addParticipant(user);
                                                    inviteJoinToGroup(user);
                                                }
                                            })
                                            .setNegativeButton(R.string.cancels, new DialogInterface.OnClickListener() {
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

    private void inviteJoinToGroup(User user) {

        String timestamp = System.currentTimeMillis() + "";

        //create hashmap
        HashMap<String, Object> hashMapRequest = new HashMap<>();
        hashMapRequest.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_SENT);
        hashMapRequest.put(Constant.REQUEST_TIMESTAMP, timestamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INVITE_GROUP);
        reference.child(groupPostId)
                .child(user.getUser_id())
                .setValue(hashMapRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //create hashmap request received
                            HashMap<String, Object> hashMapRequestReceived = new HashMap<>();
                            hashMapRequestReceived.put(Constant.REQUEST_TYPE, Constant.REQUEST_TYPE_RECEIVED);
                            hashMapRequestReceived.put(Constant.REQUEST_TIMESTAMP, timestamp);

                            reference.child(user.getUser_id())
                                    .child(groupPostId)
                                    .setValue(hashMapRequestReceived)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                            } else {
                                                //failed
                                            }
                                        }
                                    });
                        } else {
                            //failed
                        }
                    }
                });


    }

    //make admin group
    private void makeAdmin(User user) {
        //setup data - change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin"); //role are : participant/admin/creator
        //update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .updateChildren(hashMap)
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

    //remove admin group
    private void removeAdmin(User user) {
        //setup data - remove admin - just change role
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.ROLE_ROLE, Constant.ROLE_PARTICIPANT); //role are : participant/admin/creator
        //update role in database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
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

    private void addParticipant(User user) {
        //setup user data - add user in group
        String timstamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUser_id());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", "" + timstamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.child(groupPostId)
                .child("Participants")
                .child(user.getUser_id())
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //added successfully
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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
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

    private void checkIfAlreadyExists(User user, GroupPostParticipantAdapter.ViewHolder holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUP_POST);
        reference.child(groupPostId)
                .child(Constant.COLLECTION_PARTICIPANTS)
                .child(user.getUser_id())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //already exists
                            String hisRole = "" + snapshot.child(Constant.ROLE_ROLE).getValue();
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

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView avataIv;
        private TextView nameTv, emailTv, statusTv;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            avataIv = itemView.findViewById(R.id.avataTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }

}
