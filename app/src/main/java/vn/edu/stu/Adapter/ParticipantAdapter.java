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

        String name = user.getFullname();
        String email = user.getEmail();
        String image = user.getImageurl();
        String uid = user.getId();

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
                ref.child(groupId).child("Participants").child(uid)
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
                                            builder1.setTitle("Choose option");
                                            builder1.setCancelable(true);

                                            builder1.setPositiveButton("Remove Admin", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove Admin clicked
                                                    removeAdmin(user);
                                                }
                                            });

                                            builder1.setNegativeButton("Remove User", new DialogInterface.OnClickListener() {
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
                                            builder.setTitle("Choose option");
                                            builder.setCancelable(true);

                                            builder.setPositiveButton("Make Admin", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Make Admin clicked
                                                    makeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton("Remove User", new DialogInterface.OnClickListener() {
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
                                            Toast.makeText(context, "Creator of group", Toast.LENGTH_SHORT).show();
                                        } else if (hisPreviousRole.equals("admin")) {
                                            // im admin, he is admin too
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle("Choose option");
                                            builder.setCancelable(true);

                                            builder.setPositiveButton("Remove Admin", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            builder.setNegativeButton("Remove User", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //Remove User clicked
                                                    removeParticipant(user);
                                                }
                                            });

                                            AlertDialog alert = builder.create();
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
                                            //im admin, he is particitpant
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle("Choose option");
                                            builder.setCancelable(true);

                                            builder.setPositiveButton("Make Admin", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //make Admin clicked
                                                    makeAdmin(user);
                                                }
                                            });

                                            builder.setNegativeButton("Remove User", new DialogInterface.OnClickListener() {
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
                                                        //Remove Admin clicked
                                                        makeAdmin(user);

                                                    } else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();*/
                                        }
                                    }
                                } else {
                                    //user doesn't exists/not-participant : add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addParticipant(user);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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
        reference.child(groupId).child("Participants").child(user.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //make admin
                        Toast.makeText(context, "The user is now admin...", Toast.LENGTH_SHORT).show();

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
        hashMap.put("uid", user.getId());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", "" + timstamp);
        //add that user in Groups>groupId>Participants
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_GROUPS);
        ref.child(groupId).child("Participants").child(user.getId()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //added successfully
                        Toast.makeText(context, "Added successfully...", Toast.LENGTH_SHORT).show();
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
        reference.child(groupId).child("Participants").child(user.getId()).removeValue()
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
        reference.child(groupId).child("Participants").child(user.getId()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //make admin
                        Toast.makeText(context, "The user is no longer admin...", Toast.LENGTH_SHORT).show();

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
        reference.child(groupId).child("Participants").child(user.getId())
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
