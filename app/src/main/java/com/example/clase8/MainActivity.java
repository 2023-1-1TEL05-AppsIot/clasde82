package com.example.clase8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.clase8.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseFirestore db;
    private static String TAG = "msg-test";
    ListenerRegistration listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            binding.textView.setText(currentUser.getEmail());
        }

        db = FirebaseFirestore.getInstance();


        binding.button18.setOnClickListener(v -> {
            //guardarEnMongoDb();
            guardarEnCloudFireStore();
        });

        binding.button.setOnClickListener(view -> {
            String dni = binding.editTextBuscar.getText().toString();
            db.collection("usuarios")
                    .document(dni)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                //Map<String, Object> data = documentSnapshot.getData();
                                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                Toast.makeText(MainActivity.this, "Usuario encontrado!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "nombre: " + usuario.getNombre());
                            } else {
                                Toast.makeText(MainActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "Task unsuccessful");
                        }
                    });
        });

        //leerUnaColeccionUnaVez();
        binding.buttonLogout.setOnClickListener( view -> {

            AuthUI.getInstance().signOut(MainActivity.this)
                    .addOnCompleteListener(task -> {
                        Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                        startActivity(intent);
                        finish();
                    });
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        leerUnaColeccionEnTiempoReal();
    }

    @Override
    protected void onStop() {
        super.onStop();
        listener.remove();
    }

    public void guardarEnMongoDb() {

        UsuarioRepository usuarioRepository = new Retrofit.Builder()
                .baseUrl("http://10.100.191.229:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UsuarioRepository.class);

        usuarioRepository.guardar(binding.editTextNombre.getText().toString(),
                        binding.editTextApellido.getText().toString(),
                        binding.editTextDni.getText().toString(),
                        binding.editTextCorreo.getText().toString(),
                        binding.editTextEdad.getText().toString())
                .enqueue(new Callback<UsuarioResponse>() {
                    @Override
                    public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                        if (response.isSuccessful()) {
                            Log.d("msg-test", "exitoso");
                            UsuarioResponse body = response.body();
                            Log.d("msg-test", "id: " + body.getIdInsertado());
                            Toast.makeText(MainActivity.this, "creado correctamente", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
    }

    public void guardarEnCloudFireStore() {

        binding.button18.setEnabled(false);

        Usuario usuario = new Usuario();
        usuario.setNombre(binding.editTextNombre.getText().toString());
        usuario.setApellido(binding.editTextApellido.getText().toString());
        usuario.setDni(binding.editTextDni.getText().toString());
        usuario.setCorreo(binding.editTextCorreo.getText().toString());
        usuario.setEdad(Integer.parseInt(binding.editTextEdad.getText().toString()));

        guardarYoManejoElID(usuario);
        guardarFbManejaElID(usuario);

    }

    public void guardarYoManejoElID(Usuario usuario) {
        db.collection("usuarios")
                .document(usuario.getDni())
                .set(usuario)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MainActivity.this, "Usuario guardado existosamente", Toast.LENGTH_SHORT).show();
                    binding.editTextNombre.setText("");
                    binding.editTextApellido.setText("");
                    binding.editTextDni.setText("");
                    binding.editTextCorreo.setText("");
                    binding.editTextEdad.setText("");
                    binding.button18.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error al guardar!", Toast.LENGTH_SHORT).show();
                    binding.button18.setEnabled(true);
                });
    }

    public void guardarFbManejaElID(Usuario usuario) {
        db.collection("usuarios")
                .add(usuario)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(MainActivity.this, "Usuario guardado existosamente", Toast.LENGTH_SHORT).show();
                    binding.editTextNombre.setText("");
                    binding.editTextApellido.setText("");
                    binding.editTextDni.setText("");
                    binding.editTextCorreo.setText("");
                    binding.editTextEdad.setText("");
                    binding.button18.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Error al guardar!", Toast.LENGTH_SHORT).show();
                    binding.button18.setEnabled(true);
                });
    }

    public void leerUnaColeccionUnaVez() {
        db.collection("usuarios")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        for (QueryDocumentSnapshot documentSnapshot : result) {
                            Usuario usuario = documentSnapshot.toObject(Usuario.class);
                            Log.d(TAG, "nombre: " + usuario.getNombre() + " | id: " + documentSnapshot.getId());
                        }
                    } else {
                        Log.d(TAG, "error al leer la colección");
                    }
                });
    }

    public void leerUnaColeccionEnTiempoReal() {

        /*FirebaseFirestoreSettings settings =
                new FirebaseFirestoreSettings.Builder(db.getFirestoreSettings())
                        .setPersistenceEnabled(false).build();
        db.setFirestoreSettings(settings);*/

        listener = db.collection("usuarios")
                .addSnapshotListener((collSnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Ocurrió algo :/ gg", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot documentSnapshot : collSnapshot) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);
                        Log.d(TAG, "nombre: " + usuario.getNombre() + " | apellido: " + usuario.getApellido());
                    }
                });
    }
}