package activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appmanagerpassword.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import models.UserPass;

public class ShowPasswordsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PasswordAdapter passwordAdapter;
    private Button backButton, addButton;
    private List<UserPass> passwordList = new ArrayList<>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_passwords_activity);

        addButton = findViewById(R.id.addButton);
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.passwordRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();

        backButton.setOnClickListener(v -> {
            // Cerrar la sesión
            FirebaseAuth.getInstance().signOut();

            // Redirigir al login
            Intent intent = new Intent(ShowPasswordsActivity.this, MainActivity.class); // Asegúrate de que LoginActivity sea la actividad de inicio de sesión
            startActivity(intent);
            finish(); // Finaliza la actividad actual para que no se quede en el stack
        });

        // Implementar el onClick para el botón "Add"
        addButton.setOnClickListener(v -> {
            // Iniciar la actividad AddEditPasswordActivity al presionar el botón "Add"
            Intent intent = new Intent(ShowPasswordsActivity.this, AddEditPasswordActivity.class);
            startActivity(intent);
        });

        // Cargar las contraseñas directamente
        loadPasswords();
    }

    private void loadPasswords() {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d("ShowPasswordsActivity", "User ID: " + userId);

        // Obtener una referencia a la base de datos de Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("pass");

        // Configurar un ValueEventListener para actualizar automáticamente la lista
        database.orderByChild("id").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                passwordList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String idTarjeta = snapshot.child("idTarjeta").getValue(String.class);
                    String id = snapshot.child("id").getValue(String.class);
                    String apuntes = snapshot.child("apuntes").getValue(String.class);
                    String pass = snapshot.child("pass").getValue(String.class);
                    String nombrePagina = snapshot.child("nombrePagina").getValue(String.class);
                    String nombreUsuario = snapshot.child("nombreUsuario").getValue(String.class);

                    UserPass passwordObj = new UserPass(idTarjeta, id, apuntes, pass, nombrePagina, nombreUsuario);
                    passwordList.add(passwordObj);
                }

                // Actualizar el adaptador
                if (passwordAdapter == null) {
                    passwordAdapter = new PasswordAdapter(passwordList, ShowPasswordsActivity.this);
                    recyclerView.setAdapter(passwordAdapter);
                } else {
                    passwordAdapter.notifyDataSetChanged();
                }

                // Mostrar un mensaje si la lista está vacía
                if (passwordList.isEmpty()) {
                    Toast.makeText(ShowPasswordsActivity.this, "No se encontraron contraseñas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShowPasswordsActivity.this, "Error al cargar las contraseñas", Toast.LENGTH_SHORT).show();
            }
        });
    }
}