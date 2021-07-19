package vn.edu.stu.luanvanmxhhippo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import vn.edu.stu.Adapter.HobbyAdapter;
import vn.edu.stu.Model.Hobby;
import vn.edu.stu.Util.Constant;

public class ChooseHobbyActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SearchView search_hobby;
    private ListView recycler_view_hobby;
    private MaterialButton btn_save_hobby;

    private FirebaseUser firebaseUser;

    private HobbyAdapter hobbyAdapter;
    private ArrayList<Hobby> hobbyList;
    private ArrayList<Hobby> hobbiesList;

    private ChipGroup chipgroup_hobby;

    private ArrayAdapter<Hobby> adapter;

    private int countHobbyUser = 0;

    private ArrayList<Hobby> selectedHobby;

    private LinearProgressIndicator progress_circular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_hobby);

        addControls();
        addEvents();
        loadHobby();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getHobbyUser();
            }
        }, 1000);

        search_hobby.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //searchHobby(newText);
                return false;
            }
        });
    }

    private void setChip(Hobby hobby) {
        final Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.chip_item, null, false);
        chip.setText(hobby.getTitle());
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chipgroup_hobby.removeView(chip);
            }
        });
        chipgroup_hobby.addView(chip);
    }

    //load Chips hobby
    private void loadChipGroup() {
        chipgroup_hobby.removeAllViews();
        if (selectedHobby != null) {
            for (Hobby hobby : selectedHobby) {
                final Chip chip = (Chip) this.getLayoutInflater().inflate(R.layout.chip_item, null, false);
                chip.setText(hobby.getTitle());
                chipgroup_hobby.addView(chip);
            }
        } else {

        }
    }

    //get hobby this user
    private void getHobbyUser() {
        hobbiesList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        reference.child(firebaseUser.getUid())
                .child(Constant.COLLECTION_INFO_HOBBY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String category = dataSnapshot.child("category").getValue().toString();
                            String sub_category = dataSnapshot.child("subCategory").getValue().toString();
                            String title = dataSnapshot.child("title").getValue().toString();

                            Hobby hobby = new Hobby(category, sub_category, title);
                            //selectedHobby.add(hobby);
                            hobbiesList.add(hobby);

                            int i = hobbyList.indexOf(hobby);
                            recycler_view_hobby.setItemChecked(i, true);

                            /*Log.i("OOO", "onDataChange: " + i);
                            Log.i("OOORR", "onDataChange: " + hobbyList);*/
                        }

                        selectedHobby.clear();
                        for (int i = 0; i < recycler_view_hobby.getCount(); i++) {
                            if (recycler_view_hobby.isItemChecked(i)) {
                                selectedHobby.add((Hobby) recycler_view_hobby.getItemAtPosition(i));
                                loadChipGroup();
                            }
                        }

                        progress_circular.setVisibility(View.GONE);
                        /*Log.i("GGG", "onDataChange: " + hobbiesList);*/

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void searchHobby(String newText) {
        for (Hobby hobby : hobbyList) {
            if (hobby.getTitle().contains(newText)) {

            }
        }
    }

    //load All hobby
    private void loadHobby() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_HOBBY);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                hobbyList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    String category = dataSnapshot.child("category").getValue().toString();
                    String sub_category = dataSnapshot.child("subCategory").getValue().toString();
                    String title = dataSnapshot.child("title").getValue().toString();

                    Hobby hobby = new Hobby(category, sub_category, title);
                    hobbyList.add(hobby);
                }
                adapter = new ArrayAdapter<Hobby>(ChooseHobbyActivity.this, android.R.layout.simple_list_item_multiple_choice, hobbyList);
                recycler_view_hobby.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void addEvents() {
        //back
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //click save => save hobby
        btn_save_hobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHobby();
            }
        });

        recycler_view_hobby.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (recycler_view_hobby.isItemChecked(position)) {
                    selectedHobby.clear();
                    for (int i = 0; i < recycler_view_hobby.getCount(); i++) {
                        if (recycler_view_hobby.isItemChecked(i)) {
                            selectedHobby.add((Hobby) recycler_view_hobby.getItemAtPosition(i));
                            loadChipGroup();
                        }
                    }

                } else {
                    selectedHobby.clear();
                    for (int i = 0; i < recycler_view_hobby.getCount(); i++) {
                        if (recycler_view_hobby.isItemChecked(i)) {
                            selectedHobby.add((Hobby) recycler_view_hobby.getItemAtPosition(i));
                            loadChipGroup();
                        }
                    }
                }

            }
        });
    }

    //save hobby selected
    private void saveHobby() {
        selectedHobby.clear();
        for (int i = 0; i < recycler_view_hobby.getCount(); i++) {
            if (recycler_view_hobby.isItemChecked(i)) {
                selectedHobby.add((Hobby) recycler_view_hobby.getItemAtPosition(i));
            }
        }

        //Upload database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(Constant.COLLECTION_INFOUSER);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(firebaseUser.getUid())) {
                    reference.child(firebaseUser.getUid())
                            .child(Constant.COLLECTION_INFO_HOBBY)
                            .setValue(selectedHobby)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChooseHobbyActivity.this, getString(R.string.sucessfull_update), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChooseHobbyActivity.this, getString(R.string.update_fail), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    reference.child(firebaseUser.getUid())
                            .child(Constant.COLLECTION_INFO_HOBBY)
                            .setValue(selectedHobby);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.choose_hobby);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        selectedHobby = new ArrayList<>();
        hobbyList = new ArrayList<>();
        recycler_view_hobby = findViewById(R.id.recycler_view_hobby);
        /*recycler_view_hobby.setHasFixedSize(true);
        recycler_view_hobby.setLayoutManager(new LinearLayoutManager(this));*/

        search_hobby = findViewById(R.id.search_hobby);
        btn_save_hobby = findViewById(R.id.btn_save_hobby);
        chipgroup_hobby = findViewById(R.id.chipgroup_hobby);

        progress_circular = findViewById(R.id.progress_circular);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}