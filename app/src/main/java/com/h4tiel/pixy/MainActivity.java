package com.h4tiel.pixy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.flaviofaria.kenburnsview.KenBurnsView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String ENDPOINT = "https://source.unsplash.com/random/1440x2560";
    // Initial splash image
    private String imageUrl = ENDPOINT;

    KenBurnsView kenBurnsView;
    Picasso picasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kenBurnsView = findViewById(R.id.image);

        picasso = Picasso.get();
        picasso.load(R.drawable.ic_placeholder).noFade().into(kenBurnsView);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                requestImageUrl();
                Log.d(TAG, "run: ");
            }
        }, 0, 10000);

        kenBurnsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage(imageUrl);
                Toast.makeText(MainActivity.this, "Pixy Dust", Toast.LENGTH_SHORT).show();
                hideSystemUI();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }


    private void requestImageUrl() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(ENDPOINT).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            Handler mainHandler = new Handler(MainActivity.this.getMainLooper());

            public void onResponse(@NotNull Call call, @NotNull Response response) {
                imageUrl = String.valueOf(response.request().url());

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        picasso.load(imageUrl)
                                .placeholder(R.drawable.ic_placeholder)
                                .fetch(new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        picasso.load(imageUrl).noFade().into(kenBurnsView);
                                        Log.d(TAG, "onSuccess: " + imageUrl);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                    }
                                });
                    }
                });
            }

            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
        });
    }

    private void downloadImage(String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Pixy/" + filename + ".jpg");
        Log.d(TAG, "Environment extraData=" + file.getPath());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Pixy dust")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true);
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void hideSystemUI() {
        // Enables regular lean back mode.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                // Hide the nav bar and status bar
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

}