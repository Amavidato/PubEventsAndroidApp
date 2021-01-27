package com.amavidato.pubevents.utility;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.amavidato.pubevents.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ImageManager {
    private static final String TAG = ImageManager.class.getSimpleName();

    public final static long MAX_IMG_SIZE = 1024 * 1024;

    public static void loadImageIntoView(final String path, final ImageView view){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(path);

        pathReference.getBytes(MAX_IMG_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "Image loaded. Path:"+ path);
                view.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Image NOT loaded. Path:"+ path);
                view.setImageResource(R.drawable.ic_menu_gallery);
            }
        });
    }

    public static void uploadImage(final String path, Uri photoUri){
        /*
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child(path);

        // adding listeners on upload
        // or failure of image
        ref.putFile(photoUri)
                .addOnSuccessListener(
                        new OnSuccessListener<UploadTask.TaskSnapshot>() {

                            @Override
                            public void onSuccess(
                                    UploadTask.TaskSnapshot taskSnapshot)
                            {
                                Log.d(TAG, "Uploaded image. Path:"+path.toString()+"***");
                            }
                        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(TAG, "FAIL Uploading image. Path:"+path.toString()+"***");
                    }
                });

         */
    }
}
