package com.example.clase8;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.clase8.databinding.ActivityMain2Binding;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity2 extends AppCompatActivity {

    private static String TAG = "msg-test";
    ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "usuario ya logueado");
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {

            binding.buttonLogin.setOnClickListener(view -> {

                AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout.Builder(R.layout.mi_login)
                        .setEmailButtonId(R.id.buttonCorreo)
                        .setGoogleButtonId(R.id.buttonGoogle)
                        .build();

                Intent intent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAuthMethodPickerLayout(customLayout)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                        )).build();

                singInLauncher.launch(intent);
            });

        }
    }

    ActivityResultLauncher<Intent> singInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Log.d(TAG, "firebaseUID: " + user.getUid());
                    Log.d(TAG, "username: " + user.getEmail());
                    String email = user.getEmail();
                    String[] strings = email.split("@");
                    if (strings[1].equals("pucp.edu.pe")) {

                        user.reload().addOnCompleteListener(task -> {
                            if(user.isEmailVerified()){
                                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(MainActivity2.this, "Se ha enviado un correo para validar su cuenta", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    } else {
                        Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "canceló el flujo");
                }
            }
    );
}