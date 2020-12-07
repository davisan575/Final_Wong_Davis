package com.example.afinal;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class EditProfileActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private static final int REQUEST_FOR_CAMERA=0011;
    private static final int OPEN_FILE=0012;
    private Uri cardUri=null;
    private Uri profilepicUri=null;
    boolean cardUploaded;
    boolean profileUploaded;

    EditText edit_fn;
    EditText edit_ln;
    EditText edit_company;
    EditText edit_phonenumber;
    EditText edit_education;
    EditText edit_employment;
    EditText edit_hobbies;

    ImageView editProfilePic;
    ImageView editCard;

    public Object saveTimestamp;
    public String saveEmail;
    public String saveProfilepic;
    public String saveCard;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOR_CAMERA && resultCode == RESULT_OK) {
            if(cardUri==null)
            {
                Toast.makeText(this, "Error taking photo.", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(cardUri);
                BitmapFactory bitmapFactory = new BitmapFactory();
                Bitmap bm = bitmapFactory.decodeStream(inputStream);
                editCard.setImageBitmap(bm);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
            //uploadImage();
            return;
        }
        if(requestCode==OPEN_FILE && resultCode==RESULT_OK) {
            cardUri = data.getData();
            File fileLocation = new File(String.valueOf(cardUri)); //file path, which can be String, or Uri
            //Picasso.get().load(fileLocation).into(aImg);
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(cardUri);
                BitmapFactory bitmapFactory = new BitmapFactory();
                Bitmap bm = bitmapFactory.decodeStream(inputStream);
                editCard.setImageBitmap(bm);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }

            //uploadImage();

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity_layout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        edit_fn = (EditText) findViewById(R.id.edit_first_name);
        edit_ln = (EditText) findViewById(R.id.edit_last_name);
        edit_company = (EditText) findViewById(R.id.edit_company);
        edit_phonenumber = (EditText) findViewById(R.id.edit_phone_number);
        edit_education = (EditText) findViewById(R.id.edit_education);
        edit_employment = (EditText) findViewById(R.id.edit_employment);
        edit_hobbies = (EditText) findViewById(R.id.edit_hobbies);

        editCard = (ImageView) findViewById(R.id.edit_business_card);
        editProfilePic = (ImageView) findViewById(R.id.edit_profilepic);

        // Get a reference to our posts
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());

        User read_u = new User();

// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                edit_fn.setText(u.firstname);
                Log.d("onDataChange", u.displayname);
                edit_ln.setText(u.lastname);
                edit_phonenumber.setText(u.phone);
                edit_company.setText(u.company);
                saveTimestamp = u.timestamp;
                saveEmail = u.email;
                saveCard = u.card;
                saveProfilepic = u.profilepic;
                if(u.education != null)
                {
                    //ll_education.setVisibility(View.VISIBLE);
                    edit_education.setText(u.education);
                }
                if(u.employment != null)
                {
                    //ll_employment.setVisibility(View.VISIBLE);
                    edit_employment.setText(u.employment);
                }
                if(u.hobbies != null)
                {
                    //ll_hobbies.setVisibility(View.VISIBLE);
                    edit_hobbies.setText(u.hobbies);
                }

                StorageReference profilePathReference = FirebaseStorage.getInstance().getReference("Profile_Pictures/"+u.profilepic+".jpg");
                StorageReference cardPathReference = FirebaseStorage.getInstance().getReference("Business_Cards/"+u.card+".jpg");

                profilePathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(editProfilePic);
                    }
                });

                cardPathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(editCard);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    public void SaveChanges(View view) {

        if (edit_fn.getText().toString().equals("") || edit_ln.getText().toString().equals("")
                || edit_phonenumber.getText().toString().equals("") || edit_company.getText().toString().equals("")) {
            Toast.makeText(this, "Please provide all required information", Toast.LENGTH_SHORT).show();
            return;
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users/"+currentUser.getUid());

        final User write_user = new User();
        write_user.firstname = edit_fn.getText().toString();
        write_user.lastname = edit_ln.getText().toString();
        write_user.displayname = write_user.firstname + " " + write_user.lastname;
        write_user.company = edit_company.getText().toString();
        write_user.phone = edit_phonenumber.getText().toString();
        write_user.timestamp = saveTimestamp;
        write_user.email = saveEmail;
        write_user.profilepic = saveProfilepic;
        write_user.card = saveCard;
        if(edit_hobbies.getText().toString() != "")
        {
            write_user.hobbies = edit_hobbies.getText().toString();
        }
        if(edit_employment.getText().toString() != "")
        {
            write_user.employment = edit_employment.getText().toString();
        }
        if(edit_education.getText().toString() != "")
        {
            write_user.education = edit_education.getText().toString();
        }
        //////////////////////////////////////////////


        if(cardUploaded == false) {
            Log.d("imageUri", "Equals null");
            write_user.card = saveCard;
        }
        else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final String fileNameInStorage = UUID.randomUUID().toString();
            String path = "Business_Cards/" + fileNameInStorage + ".jpg";
            final StorageReference imageRef = storage.getReference(path);
            Log.d("path", path);
            Log.d("cardeUri", cardUri.toString());
            UploadTask uploadTask = imageRef.putFile(cardUri);
            write_user.card = fileNameInStorage;
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        cardUploaded = true;
                        Uri downloadUri = task.getResult();
                        Log.d("path url version: ", fileNameInStorage + ".jpg");
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference local_ref = database.getReference("Users/"+currentUser.getUid());
                        local_ref.setValue(write_user);
                        finish();


                    } else {
                        Toast.makeText(EditProfileActivity.this, "issue", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        } //end else
        ///////////////////////////////////////
//        ref.setValue(write_user);
//        finish();

    }

    public void UpdateCard(View view) {
        cardUploaded = true;
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED && requestCode==REQUEST_FOR_CAMERA )
        {
            if(ContextCompat.checkSelfPermission(getBaseContext(),
                    android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
            {
                takePhoto();
            }
        }
        else{
            Toast.makeText(this, "We need to access your camera and photos to upload.", Toast.LENGTH_LONG).show();

        }

    }


    private void checkPermissions(){

        if (ContextCompat.checkSelfPermission(getBaseContext(),
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "We need permission to access your camera and photo.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_FOR_CAMERA);
        }
        else
        {
            takePhoto();
        }
    }

    private void takePhoto(){
        cardUploaded = true;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        cardUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cardUri);
        Intent chooser=Intent.createChooser(intent,"Select a Camera App.");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, REQUEST_FOR_CAMERA);}
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.takephoto:
                checkPermissions();
                return true;
            case R.id.upload:
                Intent intent = new Intent().setType("*/*") //when un commented the argument here shall be "start/star"
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select a file"), OPEN_FILE);
                return true;
            default:
                return false;
        }
    }
}
