package com.example.image_viewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ImageButton previous,next,AddImage,GmailShare,close,submit;
    EditText EmailAddress, Subject,Message;
    ImageView imageView;
    private ArrayList<byte[]> ImageByteArrayList;
    private byte[] Image;
    private static final int PICK_IMAGE_REQUEST = 1;
    private int ListOrder = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageByteArrayList = new ArrayList<>();
        previous = findViewById(R.id.previousButton);
        next = findViewById(R.id.nextButton);
        AddImage = findViewById(R.id.AddImage);
        imageView = findViewById(R.id.imageView);
        GmailShare = findViewById(R.id.GmailShare);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ListOrder > 0){
                    ListOrder -= 1;
                    SetImage();
                }else {
                    SetImage();
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SIZE",String.valueOf(ListOrder<ImageByteArrayList.size()));
                if(ListOrder+1 < ImageByteArrayList.size()){
                    ListOrder += 1;
                    SetImage();
                }else {
                    SetImage();
                }
            }
        });
        AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        GmailShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                Image = byteArrayOutputStream.toByteArray();
                ImageByteArrayList.add(Image);
                SetImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void  SetImage(){
        byte[] imageData = ImageByteArrayList.get(ListOrder);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        imageView.setImageBitmap(bitmap);
    }
    public void sendEmailWithImage(String emailAddress, String subject, String message, Uri imageUri) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        // Attach the image (make sure to pass the image URI)
        emailIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        emailIntent.setPackage("com.google.android.gm"); // Specify GMAIL package name
        startActivity(emailIntent);
        Toast.makeText(MainActivity.this, "Check your email", Toast.LENGTH_SHORT).show();
    }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private Uri copyImageToCache(byte[] imageBytes) {
        try {
            File cacheDir = getCacheDir();
            File imageFile = new File(cacheDir, "temp_image.png");

            // Write the imageBytes to the temporary file
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(imageBytes);
            fos.close();

            // Get a content:// URI for the temporary file using FileProvider
            return FileProvider.getUriForFile(this, "com.example.image_viewer.fileprovider", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_sendmail);
        EmailAddress = dialog.findViewById(R.id.EmailAddress);
        Subject = dialog.findViewById(R.id.Subject);
        Message = dialog.findViewById(R.id.Message);
        submit = dialog.findViewById(R.id.Submit);
        close = dialog.findViewById(R.id.close);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = EmailAddress.getText().toString().trim();
                String subject = Subject.getText().toString().trim();
                String message = Message.getText().toString().trim();
                if (!isValidEmail(emailAddress)) {
                    Toast.makeText(MainActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the subject is not empty
                if (subject.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Subject cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the message is not empty
                if (message.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ImageByteArrayList.size()==0){
                    Toast.makeText(MainActivity.this,"You haven't upload any image",Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] imageBytes = ImageByteArrayList.get(ListOrder);
                Uri imageUri = copyImageToCache(imageBytes);
                sendEmailWithImage(emailAddress,subject,message,imageUri);
                dialog.dismiss();
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

    }
}