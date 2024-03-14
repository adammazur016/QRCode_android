package com.adayup.QRCode;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import test.invoke.sdk.XiaomiWatchHelper;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //for bluetooth connection
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    PERMISSION_REQUEST_CODE);
        }

        //for file open
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
        }

        TextView msg = findViewById(R.id.msg);
        XiaomiWatchHelper instance = XiaomiWatchHelper.getInstance(this);
        instance.setReceiver((id, message) -> {
            runOnUiThread(() -> msg.setText(new String(message, StandardCharsets.UTF_8)));

        });
        instance.setInitMessageListener((device) -> instance.sendMessageToWear("connected", obj -> {
            if (obj.isSuccess()) {
                Log.e(TAG, "Init message send");
            }
        }));

        instance.registerMessageReceiver();
        instance.sendUpdateMessageToWear();
        instance.sendNotify("Title", "Watch connected with app",
                obj -> Log.e(TAG, "send notify ->" + obj.isSuccess()));


        Button sendButton = findViewById(R.id.send);
        TextView filename = findViewById(R.id.filename);
        TextView QRText = findViewById(R.id.QRText);

        //handle the file when clicked
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendString = filename.getText() + ".txt;" + QRText.getText();
                instance.sendMessageToWear(sendString, obj -> {
                    if (obj.isSuccess()) {
                        Log.e(TAG, "Init message send");
                    }
                });
            }
        });

        msg.setOnLongClickListener(new View.OnLongClickListener() {
            private boolean disconnect = false;

            @Override
            public boolean onLongClick(View v) {
                if (!disconnect) {
                    instance.unRegisterWatchHelper();
                    instance.setReCheckConnectDevice();
                    Toast.makeText(MainActivity.this, "Disconnect", Toast.LENGTH_LONG).show();
                    disconnect = true;
                }
                return true;
            }
        });
    }
}