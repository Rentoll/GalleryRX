package com.example.mleykin.handlerthread;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;




public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    private Bitmap currentBitmap;
    private Button startButton, stopButton;
    private static final int GALLERY_PERMISSION = 0;
    private Subscription subscription;
    ArrayList<String> imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView3);
        startButton = findViewById(R.id.startBtn);
        stopButton = findViewById(R.id.stopBtn);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
            }
        }
        else {
            setup();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setup();
                } else {
                }
                return;
            }
        }
    }

    private void startShowing() {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        Collections.shuffle(imagePath);
        Observable<String> observableImagePaths = Observable.from(imagePath);
        subscription = observableImagePaths.subscribeOn(Schedulers.io()).doOnNext(s -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imagePath -> {
                    try {
                        currentBitmap = BitmapFactory.decodeFile(imagePath);
                        mImageView.setImageBitmap(currentBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void setup(){
        String[] projection = new String[] {
                MediaStore.Images.Media.DATA,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = managedQuery(images, projection, "",null, "");

        imagePath = new ArrayList<String>();
        if (cur.moveToFirst()) {
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                imagePath.add(cur.getString(dataColumn));
            } while (cur.moveToNext());
        }
        cur.close();

        startButton.setOnClickListener(v -> startShowing());
        stopButton.setOnClickListener(v -> stopShowing());
    }


    private void stopShowing() {
        subscription.unsubscribe();
        mImageView.setImageResource(android.R.color.transparent);
    }
}