package com.example.clase8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.clase8.databinding.ActivityStorageBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class StorageActivity extends AppCompatActivity {

    ActivityStorageBinding binding;
    private static String TAG = "msg-test";
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference reference = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStorageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth != null) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                Log.d(TAG, "user: " + currentUser.getUid());
            }
        }

        binding.button2.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            launcher.launch(intent);
        });

        binding.buttonBuscar.setOnClickListener(view -> {
            StorageReference pdfsRef = reference.child("pdfs");
            String texto = binding.editTextBuscar.getText().toString();
            pdfsRef.child(texto).getMetadata()
                    .addOnSuccessListener(storageMetadata -> {
                        Log.i(TAG, "Si existe el archivo!");
                        binding.buttonDescargar.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "no existe el archivo"));
        });

        binding.buttonDescargar.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(directory, "descarga.pdf");

                StorageReference pdfsRef = reference.child("pdfs");
                StorageReference storageReference = pdfsRef.child(binding.editTextBuscar.getText().toString());

                storageReference.getFile(file)
                        .addOnSuccessListener(task -> {
                            Toast.makeText(StorageActivity.this, "Download correcto", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "error al descargar");
                        })
                        .addOnProgressListener(snapshot -> {
                            long bytesTransferred = snapshot.getBytesTransferred();
                            long totalByteCount = snapshot.getTotalByteCount();
                            double prog = (bytesTransferred * 100.0d) / totalByteCount;
                            long round = Math.round(prog);
                            Log.d(TAG, "progreso: " + round + "%");
                        });

            } else {
                Log.d(TAG, "no tengo permiso :(");
                launcher2.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });

        StorageReference pdfsRef = reference.child("pdfs");
        pdfsRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference ref : listResult.getItems()) {
                Log.d(TAG, "Nombre: " + ref.getName());
            }
        });


        StorageReference img1 = pdfsRef.child("descarga.jpeg");
        StorageReference img2 = pdfsRef.child("fibra.png");

        Glide.with(this)
                .load(img1)
                .into(binding.imageView2);

        Glide.with(this)
                .load(img2)
                .into(binding.imageView3);


    }

    public void guardarEnDispositivo() {

    }

    ActivityResultLauncher<String> launcher2 = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "permiso concedido");
                } else {
                    Log.d(TAG, "no me dio el permisooo GGWP");
                }
            }
    );

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri uri = result.getData().getData();
                    String fileName = uri.getLastPathSegment();
                    String[] strings = fileName.split("/");
                    String fileNameFinal = strings[1];
                    Log.i(TAG, fileNameFinal);

                    StorageReference pdfsRef = reference.child("pdfs");

                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setCustomMetadata("author", "Stefano tu favorito")
                            .build();

                    pdfsRef.child(fileNameFinal).putFile(uri, metadata)
                            .addOnSuccessListener(taskSnapshot -> Toast.makeText(StorageActivity.this, "Guardado exitoso", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.e(TAG, "algo pasó :/"))
                            .addOnProgressListener(snapshot -> {
                                long bytesTransferred = snapshot.getBytesTransferred();
                                long totalByteCount = snapshot.getTotalByteCount();
                                double prog = (bytesTransferred * 100.0d) / totalByteCount;
                                long round = Math.round(prog);
                                Log.d(TAG, "progreso: " + round + "%");
                                binding.textViewProgreso.setText(round + "%");
                            });


                } else {
                    Toast.makeText(StorageActivity.this, "No seleccionó un archivo", Toast.LENGTH_SHORT).show();
                }
            }
    );
}