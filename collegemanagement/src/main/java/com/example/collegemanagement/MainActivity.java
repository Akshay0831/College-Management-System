package com.example.collegemanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static String USER_TYPE;

    Button login;
    ProgressBar circularPB;
    EditText inputEmail, inputPass;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateSignIn(currentUser);
    }

    private void updateSignIn(FirebaseUser user) {
        if (user != null)
            db.collection("user")
                    .document(user.getUid())
                    .get()
                    .addOnCompleteListener(t -> {
                        if (t.getResult().exists()) {
//                            Toast.makeText(this, "Logged in Successfully: " + t.getResult().getData(), Toast.LENGTH_SHORT).show();
                            USER_TYPE = t.getResult().getData().get("userType").toString();
                            Intent i = new Intent(MainActivity.this, AdminPage.class);
                            //i.putExtra("userType", t.getResult().getData().get("userType").toString());
                            startActivity(i);
                        }
            });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.emailId);
        inputPass = findViewById(R.id.password);

        login = findViewById(R.id.login_btn);
        circularPB = findViewById(R.id.progressBar);

        login.setOnClickListener(view -> {
            final String emailRegEx = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";
            final String passwordRegEx = "^(?=.*[0-9])"
                    + "(?=.*[a-z])(?=.*[A-Z])"
                    + "(?=.*[@#$%^&+=])"
                    + "(?=\\S+$).{8,20}$";


            Pattern pattern = Pattern.compile(emailRegEx);
            boolean emailValidated = pattern.matcher(inputEmail.getText().toString()).matches();

            pattern = Pattern.compile(passwordRegEx);
            boolean passwordValidated = pattern.matcher(inputPass.getText().toString()).matches();

            if (emailValidated && passwordValidated) {
                circularPB.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(inputEmail.getText().toString(), inputPass.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            db.collection("user").document(user.getUid()).get().addOnCompleteListener(t -> {
                                if (t.getResult().exists()) {
//                                    Toast.makeText(this, "Logged in Successfully: " + t.getResult().getData(), Toast.LENGTH_SHORT).show();
                                    updateSignIn(user);
                                }
                                else{
                                    Toast.makeText(this, " User Doesn't Exist ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    });

//                Toast.makeText(this, inputEmail.getText().toString() + " Logged in Successfully", Toast.LENGTH_SHORT).show();

            }
            else if (!emailValidated) Toast.makeText(this, "Invalid Email Id", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        });
    }
}