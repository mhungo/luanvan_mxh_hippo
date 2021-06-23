package vn.edu.stu.luanvanmxhhippo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.edu.stu.Model.City;
import vn.edu.stu.Model.ModelCity;
import vn.edu.stu.Model.User;
import vn.edu.stu.Services.APIService;
import vn.edu.stu.Util.Constant;

public class EditProfileActivity extends AppCompatActivity {

    private Calendar calendar;
    private int year, month, day;

    private SimpleDateFormat dayTimeNow;

    private ImageView close, image_profile;
    private TextView save, tv_change, birthDay;
    private TextInputEditText fullname, username, bio, hobby;

    private RadioGroup radioGroup;
    private MaterialRadioButton radioButton;

    private String birth;
    private FirebaseUser firebaseUser;

    private DatePickerDialog datePickerDialog;

    private CheckBox checkbox_birthday_hiden, checkbox_gender_hiden;

    private Spinner spiner_livein;

    private String urlAPICity = "https://thongtindoanhnghiep.co/api/";

    private List<City> listCity;

    private DatabaseReference referenceSettingProfile;

    private LinearProgressIndicator progress_circular;


    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        addControls();
        addEvents();
        loadCity();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDataProfile();
            }
        }, 1000);

    }

    private void loadCity() {
        listCity = new ArrayList<>();
        listCity.clear();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(urlAPICity)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService service = retrofit.create(APIService.class);
        Call<ModelCity> modelCityCall = service.getCity();

        modelCityCall.enqueue(new Callback<ModelCity>() {
            @Override
            public void onResponse(Call<ModelCity> call, Response<ModelCity> response) {
                ModelCity city = response.body();
                listCity = city.getLtsItem();

                ArrayAdapter<City> cityArrayAdapter = new ArrayAdapter(EditProfileActivity.this, android.R.layout.simple_list_item_1, listCity);
                cityArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spiner_livein.setAdapter(cityArrayAdapter);
            }

            @Override
            public void onFailure(Call<ModelCity> call, Throwable t) {

            }
        });

    }

    private void getDataProfile() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS);
        reference.child(firebaseUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            fullname.setText(user.getUser_fullname());
                            username.setText(user.getUser_username());
                            bio.setText(user.getUser_bio());

                            //set birth
                            if (user.getUser_birthday().equalsIgnoreCase("default")) {
                                birthDay.setText("Not update");
                            } else {
                                birthDay.setText(user.getUser_birthday());
                            }

                            //set gender
                            if (user.getUser_gender().equals("male")) {
                                radioGroup.check(R.id.gender_male);
                            } else if (user.getUser_gender().equals("female")) {
                                radioGroup.check(R.id.gender_female);
                            } else {
                                radioGroup.check(R.id.gender_other);
                            }

                            //set image
                            try {
                                Glide.with(getApplicationContext()).load(user.getUser_imageurl()).into(image_profile);
                            } catch (Exception e) {
                                image_profile.setImageResource(R.drawable.placeholder);
                            }
                        } else {
                            //null
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //Get data role hiden or visible birthday, gender of user
        reference.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(Constant.COLLECTION_SETTINGPROFILE)) {
                            boolean isHiddenBirthday = (boolean) snapshot.child(Constant.COLLECTION_SETTINGPROFILE).child(Constant.SETTING_HIDEN_BIRTHDAY).getValue();
                            boolean isHiddenGendery = (boolean) snapshot.child(Constant.COLLECTION_SETTINGPROFILE).child(Constant.SETTING_HIDEN_GENDER).getValue();

                            checkbox_birthday_hiden.setChecked(isHiddenBirthday);
                            checkbox_gender_hiden.setChecked(isHiddenGendery);
                        } else {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        //Get data live in, hobby of user
        DatabaseReference refInfo = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_INFOUSER);
        refInfo.child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String favorite = snapshot.child(Constant.INFO_HOBBY).getValue().toString();
                            String city = snapshot.child(Constant.INFO_LIVEIN).getValue().toString();

                            hobby.setText(favorite);
                            for (City ct : listCity) {
                                if (ct.getTitle().equals(city)) {
                                    spiner_livein.setSelection(listCity.indexOf(ct));
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        progress_circular.setVisibility(View.GONE);

    }

    private void addEvents() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                        .start(EditProfileActivity.this);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int radioId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioId);
                // get value radio button
                String gender = radioButton.getTag().toString();
                birth = birthDay.getText().toString();
                updateProfile(fullname.getText().toString(),
                        username.getText().toString(),
                        bio.getText().toString(), birth, gender);

            }
        });

        birthDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        birth = dayOfMonth + "-" + month + "-" + year;
                        birthDay.setText(birth);
                    }
                };
                datePickerDialog = new DatePickerDialog(EditProfileActivity.this,
                        AlertDialog.THEME_HOLO_LIGHT,
                        dateSetListener,
                        year,
                        month,
                        day);

                datePickerDialog.show();
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void addControls() {

        close = findViewById(R.id.close);
        image_profile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        tv_change = findViewById(R.id.tv_change);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        birthDay = findViewById(R.id.bithday);
        hobby = findViewById(R.id.hobby);
        radioGroup = findViewById(R.id.rdo_group);
        checkbox_birthday_hiden = findViewById(R.id.checkbox_birthday_hiden);
        checkbox_gender_hiden = findViewById(R.id.checkbox_gender_hiden);
        spiner_livein = findViewById(R.id.spiner_livein);
        progress_circular = findViewById(R.id.progress_circular);

        calendar = Calendar.getInstance();
        dayTimeNow = new SimpleDateFormat("dd/MM/yyyy");
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);


        referenceSettingProfile = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_SETTINGPROFILE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");

    }

    private void updateProfile(String fullname, String username, String bio,
                               String bithday, String gender) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                .child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.USER_FULLNAME, fullname);
        hashMap.put(Constant.USER_USERNAME, username);
        hashMap.put(Constant.USER_BIRTHDAY, bithday);
        hashMap.put(Constant.USER_GENDER, gender);
        hashMap.put(Constant.USER_BIO, bio);

        reference.updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        /* Toast.makeText(EditProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();*/
                        Snackbar.make(save, "Successfully updated!", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        /*Toast.makeText(EditProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();*/
                        Snackbar.make(save, "Update failed", BaseTransientBottomBar.LENGTH_SHORT).show();
                    }
                });

        /*------------------------Update Info: live in, hobby-------------------------------*/
        City city = (City) spiner_livein.getSelectedItem();

        HashMap<String, Object> hashMapInfo = new HashMap<>();
        hashMapInfo.put(Constant.INFO_HOBBY, hobby.getText().toString());
        hashMapInfo.put(Constant.INFO_LIVEIN, city.getTitle());
        hashMapInfo.put(Constant.INFO_USERID, firebaseUser.getUid());

        DatabaseReference referenceInfoUser = FirebaseDatabase.getInstance().getReference().child(Constant.COLLECTION_INFOUSER);
        referenceInfoUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.hasChild(firebaseUser.getUid())) {
                    referenceInfoUser.child(firebaseUser.getUid())
                            .updateChildren(hashMapInfo);
                } else {
                    referenceInfoUser.child(firebaseUser.getUid())
                            .setValue(hashMapInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        /*------------------------Update Setting User: hiden birthday, gender----------------*/

        HashMap<String, Object> hashMapSetting = new HashMap<>();
        hashMapSetting.put(Constant.SETTING_HIDEN_BIRTHDAY, checkbox_birthday_hiden.isChecked());
        hashMapSetting.put(Constant.SETTING_HIDEN_GENDER, checkbox_gender_hiden.isChecked());

        reference.child(Constant.COLLECTION_SETTINGPROFILE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            reference.child(Constant.COLLECTION_SETTINGPROFILE).updateChildren(hashMapSetting);
                        } else {
                            reference.child(Constant.COLLECTION_SETTINGPROFILE).setValue(hashMapSetting);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        Toast.makeText(this, city.getTitle() + hobby.getText() + checkbox_birthday_hiden.isChecked() + checkbox_gender_hiden.isChecked(), Toast.LENGTH_SHORT).show();

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        if (mImageUri != null) {
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.COLLECTION_USERS)
                                .child(firebaseUser.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(Constant.USER_IMAGEURL, "" + myUrl);

                        reference.updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(EditProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {

                                    }
                                });
                        pd.dismiss();

                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Image selected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadImage();
        } else {
            Toast.makeText(this, "Something gone worng", Toast.LENGTH_SHORT).show();
        }

    }


}