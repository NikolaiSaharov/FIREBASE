package com.example.pupukaka;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeActivity extends AppCompatActivity {
    private EditText serviceNameEditText, serviceDescriptionEditText, searchServiceEditText;
    private Button addServiceButton, deleteServiceButton, updateServiceButton, searchServiceButton;
    private ListView servicesListView;
    private FirebaseFirestore db;
    private List<String> serviceIds;
    private List<String> serviceNames;
    private ArrayAdapter<String> adapter;
    private String selectedServiceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        serviceNameEditText = findViewById(R.id.serviceNameEditText);
        serviceDescriptionEditText = findViewById(R.id.serviceDescriptionEditText);
        searchServiceEditText = findViewById(R.id.searchServiceEditText);
        addServiceButton = findViewById(R.id.addServiceButton);
        deleteServiceButton = findViewById(R.id.deleteServiceButton);
        updateServiceButton = findViewById(R.id.updateServiceButton);
        searchServiceButton = findViewById(R.id.searchServiceButton);
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
            serviceNameEditText.setText(serviceNames.get(position));
            // Load the description of the selected service
            loadServiceDetails(selectedServiceId);
        });

        addServiceButton.setOnClickListener(v -> addService());
        deleteServiceButton.setOnClickListener(v -> deleteService());
        updateServiceButton.setOnClickListener(v -> updateService());
        searchServiceButton.setOnClickListener(v -> searchService());
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
                    Toast.makeText(EmployeeActivity.this, "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadServiceDetails(String serviceId) {
        db.collection("services").document(serviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String description = documentSnapshot.getString("serviceDescription");
                        serviceDescriptionEditText.setText(description);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeActivity.this, "Ошибка загрузки описания услуги", Toast.LENGTH_SHORT).show();
                });
    }

    private void addService() {
        String serviceName = serviceNameEditText.getText().toString().trim();
        String serviceDescription = serviceDescriptionEditText.getText().toString().trim();

        if (serviceName.isEmpty() || serviceDescription.isEmpty()) {
            Toast.makeText(EmployeeActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> service = new HashMap<>();
        service.put("serviceName", serviceName);
        service.put("serviceDescription", serviceDescription);

        db.collection("services").add(service)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EmployeeActivity.this, "Услуга добавлена", Toast.LENGTH_SHORT).show();
                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeActivity.this, "Ошибка добавления услуги", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteService() {
        if (selectedServiceId == null) {
            Toast.makeText(EmployeeActivity.this, "Выберите услугу для удаления", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("services").document(selectedServiceId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EmployeeActivity.this, "Услуга удалена", Toast.LENGTH_SHORT).show();
                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeActivity.this, "Ошибка удаления услуги", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateService() {
        if (selectedServiceId == null) {
            Toast.makeText(EmployeeActivity.this, "Выберите услугу для обновления", Toast.LENGTH_SHORT).show();
            return;
        }

        String serviceName = serviceNameEditText.getText().toString().trim();
        String serviceDescription = serviceDescriptionEditText.getText().toString().trim();

        if (serviceName.isEmpty() || serviceDescription.isEmpty()) {
            Toast.makeText(EmployeeActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedService = new HashMap<>();
        updatedService.put("serviceName", serviceName);
        updatedService.put("serviceDescription", serviceDescription);

        db.collection("services").document(selectedServiceId).update(updatedService)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EmployeeActivity.this, "Услуга обновлена", Toast.LENGTH_SHORT).show();
                    loadServices();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeActivity.this, "Ошибка обновления услуги", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchService() {
        String serviceName = searchServiceEditText.getText().toString().trim();

        if (serviceName.isEmpty()) {
            Toast.makeText(EmployeeActivity.this, "Введите название услуги", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EmployeeActivity.this, "Услуга не найдена", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EmployeeActivity.this, "Ошибка поиска услуги", Toast.LENGTH_SHORT).show();
                });
    }
}
