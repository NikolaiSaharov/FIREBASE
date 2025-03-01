package com.example.pupukaka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {
    private EditText userEmailEditText, userRoleEditText, searchServiceEditText, filterCategoryEditText;
    private Button addUserButton, deleteUserButton, updateUserButton, searchServiceButton, filterServicesButton, viewLogsButton;
    private ListView servicesListView;
    private FirebaseFirestore db;
    private List<String> serviceIds;
    private List<String> serviceNames;
    private ArrayAdapter<String> adapter;
    private String selectedServiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        userEmailEditText = findViewById(R.id.userEmailEditText);
        userRoleEditText = findViewById(R.id.userRoleEditText);
        searchServiceEditText = findViewById(R.id.searchServiceEditText);
        filterCategoryEditText = findViewById(R.id.filterCategoryEditText);
        addUserButton = findViewById(R.id.addUserButton);
        deleteUserButton = findViewById(R.id.deleteUserButton);
        updateUserButton = findViewById(R.id.updateUserButton);
        searchServiceButton = findViewById(R.id.searchServiceButton);
        filterServicesButton = findViewById(R.id.filterServicesButton);
        viewLogsButton = findViewById(R.id.viewLogsButton);
        servicesListView = findViewById(R.id.servicesListView);

        db = FirebaseFirestore.getInstance();
        serviceIds = new ArrayList<>();
        serviceNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, serviceNames);
        servicesListView.setAdapter(adapter);
        servicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        loadServices();

        servicesListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedServiceId = serviceIds.get(position);
            // You can add more logic here if needed
        });

        addUserButton.setOnClickListener(v -> addUser());
        deleteUserButton.setOnClickListener(v -> deleteUser());
        updateUserButton.setOnClickListener(v -> updateUser());
        searchServiceButton.setOnClickListener(v -> searchService());
        filterServicesButton.setOnClickListener(v -> filterServices());
        viewLogsButton.setOnClickListener(v -> viewLogs());
    }

    private void loadServices() {
        db.collection("services").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceNames.clear();
                    serviceIds.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String serviceName = documentSnapshot.getString("serviceName");
                        String serviceId = documentSnapshot.getId();
                        serviceNames.add(serviceName);
                        serviceIds.add(serviceId);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
                });
    }

    private void addUser() {
        String email = userEmailEditText.getText().toString().trim();
        String role = userRoleEditText.getText().toString().trim();

        if (email.isEmpty() || role.isEmpty()) {
            Toast.makeText(AdminActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("role", role);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AdminActivity.this, "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                    logAdminAction("Добавлен пользователь: " + email);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка добавления пользователя", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteUser() {
        String email = userEmailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(AdminActivity.this, "Введите email пользователя", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("users").document(userId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AdminActivity.this, "Пользователь удален", Toast.LENGTH_SHORT).show();
                                    logAdminAction("Удален пользователь: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AdminActivity.this, "Ошибка удаления пользователя", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(AdminActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка поиска пользователя", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUser() {
        String email = userEmailEditText.getText().toString().trim();
        String role = userRoleEditText.getText().toString().trim();

        if (email.isEmpty() || role.isEmpty()) {
            Toast.makeText(AdminActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Map<String, Object> updatedUser = new HashMap<>();
                        updatedUser.put("role", role);

                        db.collection("users").document(userId).update(updatedUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AdminActivity.this, "Пользователь обновлен", Toast.LENGTH_SHORT).show();
                                    logAdminAction("Обновлен пользователь: " + email);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AdminActivity.this, "Ошибка обновления пользователя", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(AdminActivity.this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка поиска пользователя", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchService() {
        String serviceName = searchServiceEditText.getText().toString().trim();

        if (serviceName.isEmpty()) {
            Toast.makeText(AdminActivity.this, "Введите название услуги", Toast.LENGTH_SHORT).show();
            return;
        }

        searchServiceByName(serviceName);
    }

    private void searchServiceByName(String serviceName) {
        db.collection("services").whereEqualTo("serviceName", serviceName).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        serviceNames.clear();
                        serviceIds.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("serviceName");
                            String id = document.getId();
                            serviceNames.add(name);
                            serviceIds.add(id);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(AdminActivity.this, "Услуга не найдена", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка поиска услуги", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterServices() {
        String category = filterCategoryEditText.getText().toString().trim();

        if (category.isEmpty()) {
            Toast.makeText(AdminActivity.this, "Введите категорию", Toast.LENGTH_SHORT).show();
            return;
        }

        filterServicesByCategory(category);
    }

    private void filterServicesByCategory(String category) {
        db.collection("services").whereEqualTo("category", category).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        serviceNames.clear();
                        serviceIds.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("serviceName");
                            String id = document.getId();
                            serviceNames.add(name);
                            serviceIds.add(id);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(AdminActivity.this, "Услуги не найдены", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка фильтрации услуг", Toast.LENGTH_SHORT).show();
                });
    }

    private void logAdminAction(String action) {
        Map<String, Object> log = new HashMap<>();
        log.put("action", action);
        log.put("timestamp", FieldValue.serverTimestamp());

        db.collection("admin_logs").add(log)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AdminActivity.this, "Действие залогировано", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this, "Ошибка логирования", Toast.LENGTH_SHORT).show();
                });
    }

    private void viewLogs() {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }
}
