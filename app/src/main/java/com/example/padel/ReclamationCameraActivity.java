package com.example.padel;



import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
public class ReclamationCameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamation_camera);

        previewView = findViewById(R.id.previewView);
        btnCapture = findViewById(R.id.btnCapture);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            startCamera();
        }

        btnCapture.setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        File photoFile = new File(getExternalFilesDir(null), "reclamation_photo.jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(ReclamationCameraActivity.this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        Toast.makeText(ReclamationCameraActivity.this,
                                "Photo enregistrée !",
                                Toast.LENGTH_SHORT).show();

                        sendPhotoByEmail(photoFile);
                    }
                    @Override
                    public void onError(ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                });
    }
    private void sendPhotoByEmail(File photoFile) {
        Uri photoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                photoFile
        );

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("image/jpeg");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rejebmouadhh@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Réclamation PadelPark");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Bonjour,\n\nVeuillez trouver ci-joint la photo de réclamation.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, photoUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(emailIntent, "Envoyer la réclamation"));
    }
}