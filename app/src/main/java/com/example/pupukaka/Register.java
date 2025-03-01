package com.example.pupukaka;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    private EditText loginR, passwordR;
    private Button register;
    private FirebaseAuth firebaseauth;
    private String email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);



        firebaseauth = FirebaseAuth.getInstance();

        loginR=findViewById(R.id.emailEditText);
        passwordR=findViewById(R.id.passwordEditText);
        register=findViewById(R.id.registerButton);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validData();
            }
        });

    }

    private void validData() {
        email = loginR.getText().toString().trim();
        password = passwordR.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginR.setError("Неправильная почта");
        } else if (TextUtils.isEmpty(password)) {
            passwordR.setError("Пароль не может быть пустым");
        }else if (password.length()<5) {
            passwordR.setError("Пароль должен содержать не менее 5 символов");
        }

        else {
            firebaseRegister();
        }
    }

    private void firebaseRegister() {
        firebaseauth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(Register.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Register.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register.this, "Ошибка" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

