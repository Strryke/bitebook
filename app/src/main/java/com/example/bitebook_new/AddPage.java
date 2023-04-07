package com.example.bitebook_new;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddPage extends Fragment {

    // use the elements created to get the user inputs and save it to variables
    // initialize elements
    ArrayList<Entry> entries;
    EditText restaurantName;
    EditText menuName;
    EditText price;
    RatingBar rate;
    ImageView pictures;
    Button addPictures;
    EditText foodMemo;
    Button upload;
    Spinner areaSpinner;
    String area;
    Spinner cuisineSpinner;
    String cuisine;
    Bitmap bitmap;
    TextView addPicDes;
    String image_url = null;


    // TODO <ADDITIONAL> change the app icon image

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_page, container, false);

        // initialize each key for further inputs from users
        entries = new ArrayList<>();

        // elements' ids with the elements in fragment
        restaurantName = view.findViewById(R.id.restaurantName);
        menuName = view.findViewById(R.id.menuName);
        price = view.findViewById(R.id.price);
        rate = view.findViewById(R.id.ratingBar);
        pictures = view.findViewById(R.id.imageAdd);
        addPictures = view.findViewById(R.id.addPicture);
        foodMemo = view.findViewById(R.id.foodMemo);
        upload = view.findViewById(R.id.upload);
        areaSpinner = view.findViewById(R.id.areaSpinner);
        cuisineSpinner = view.findViewById(R.id.cuisineSpinner);
        addPicDes = view.findViewById(R.id.addPicDes);

        // set up the spinners
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                getContext(), R.array.areaSpinner, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getContext(), R.array.cuisineSpinner, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(adapter2);

        addPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                // set the height of the picture
                int sizeInPixels = getResources().getDimensionPixelSize(R.dimen.picture_height);
                pictures.getLayoutParams().height = sizeInPixels;

                startActivityForResult(intent, 1);
                addPicDes.setText("");
            }
        });


        // save user inputs from spinners
        areaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cuisine = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                cuisine = null;
            }
        });

        cuisineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                area = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                area = null;
            }
        });


        // when the upload button is clicked the string inputs in each element will be saved in to specific var
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // get String input from the element
                String resName = restaurantName.getText().toString();
                String menName = menuName.getText().toString();
                Integer pri = Integer.valueOf(price.getText().toString());
                float rat = rate.getRating();
                String fooMemo = foodMemo.getText().toString();

                // check any of necessary inputs are empty/ missing
                if (resName.isEmpty() ||
                        menName.isEmpty() ||
                        pri.toString().isEmpty() ||
                        rat == 0.0 ||
                        area == null ||
                        cuisine == null) {
                    // if the resName is empty then show a message
                    Toast.makeText(getActivity(), "Please fill in the blanks", Toast.LENGTH_LONG).show();
                } else {
                    // if food memo is empty then save null instead
                    if (fooMemo.isEmpty()) {
                        fooMemo = null;
                    }

                    // save the user inputs as an object called Entry

                    Entry entry = new Entry(resName, menName, pri, area, rat, fooMemo, cuisine, image_url);
                    FirebaseHelper.createEntry(getContext(), entry, bitmap);

                    // After uploading the food, show the message through the toast
                    Toast.makeText(getActivity(), "YUMMY ! Successfully recorded your food", Toast.LENGTH_LONG).show();

                    // After uploading the food, make all the fill-in blanks as empty
                    restaurantName.setText("");
                    menuName.setText("");
                    price.setText("");
                    rate.setRating(0);
                    foodMemo.setText("");
                    pictures.setImageDrawable(null);
                    addPicDes.setText("");
                    areaSpinner.setSelection(0);
                    cuisineSpinner.setSelection(0);
                }
            }
        });

        return view;
    }

    // This method is called when the user selects an image from their gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            // Get the Uri of the selected image
            Uri selectedImage = data.getData();

            try {
                image_url = FirebaseHelper.generateRandomString();
                // Use the ContentResolver to get a Bitmap from the Uri
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                // Set the Bitmap to the ImageView
                pictures.setImageBitmap(bitmap);
                // Save the Bitmap to the MyObject instance
//                Entry.setFoodImage(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] pictureData = baos.toByteArray();

                FirebaseStorage storage = FirebaseStorage.getInstance();
                // Create a storage reference from our app
                StorageReference storageRef = storage.getReference();
                // Create a child reference
                // imagesRef now points to "images"
                StorageReference imagesRef = storageRef.child(image_url);

                // Child references can also take paths
                // spaceRef now points to "images/space.jpg
                // imagesRef still points to "images"
                UploadTask uploadTask = imagesRef.putBytes(pictureData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                image_url = uri.toString();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                });



            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}