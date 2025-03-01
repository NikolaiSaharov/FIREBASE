package com.example.pupukaka;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private ListView ServicesListView;
    private Button bookServices;
    private FirebaseFirestore db;
    private List<String> Idservices;
    private List<String> SevicesList;
    private ArrayAdapter<String> adapter;

    private Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        db = FirebaseFirestore.getInstance();
        bookServices = findViewById(R.id.bookServiceButton);
        ServicesListView = findViewById(R.id.servicesListView);
        Idservices = new ArrayList<>();
        SevicesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, SevicesList);

        ServicesListView.setAdapter(adapter);
        ServicesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        loadServices();

        bookServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedPosition = ServicesListView.getCheckedItemPosition();
                if (selectedPosition != ListView.INVALID_POSITION) {
                    String serviceId = Idservices.get(selectedPosition);
                    showDateTimePickerDialog(serviceId);
                } else {
                    Toast.makeText(UserActivity.this, "Выберите услугу", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadServices() {
        db.collection("services").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    SevicesList.clear();
                    Idservices.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String serviceName = documentSnapshot.getString("serviceName");
                        String serviceId = documentSnapshot.getId();
                        SevicesList.add(serviceName);
                        Idservices.add(serviceId);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserActivity.this, "Ошибка загрузки услуг", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDateTimePickerDialog(String serviceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите дату и время");
        View view = getLayoutInflater().inflate(R.layout.dialog_date_time_picker,null);
        builder.setView(view);

        TextView dateField = view.findViewById(R.id.dateField);
        TextView timeField = view.findViewById(R.id.timeField);

        dateField.setOnClickListener(v -> ShowDatePickerDialog(dateField));
        timeField.setOnClickListener(view1 -> ShowDatePickerDialog(timeField));

        builder.setPositiveButton("Записаться",(dialog,which)->{
            String date = dateField.getText().toString().trim();
            String time = timeField.getText().toString().trim();
            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(UserActivity.this, "Заоплните поле для времени и даты", Toast.LENGTH_SHORT).show();
                return;
            }
            bookServices(serviceId,date,time);
        });
        builder.setNegativeButton("Отмемна",((dialog, which) -> dialog.dismiss()));
        builder.create().show();


        String date = dateField.getText().toString().trim();
        String time = timeField.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty()){
            Toast.makeText(this,"Заполните дату и время",Toast.LENGTH_SHORT).show();
        }
        bookServices(serviceId,date,time);

        builder.create().show();
    }

    private void bookServices(String serviceId, String date, String time) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String clientName = user.getEmail();
            db.collection("services").document(serviceId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String serviceName = documentSnapshot.getString("serviceName");
                    Map<String,Object> appointment = new HashMap<>();
                    appointment.put("clientId",user.getUid());
                    appointment.put("clientName",clientName);
                    appointment.put("srviceName",serviceName);
                    appointment.put("date",date);
                    appointment.put("time",time);
                    db.collection("appointments").add(appointment).addOnSuccessListener(documentReference -> {
                        Toast.makeText(UserActivity.this,"Запись была создана",Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e ->{
                                Toast.makeText(UserActivity.this,"Ошибка создания записи",Toast.LENGTH_SHORT).show();
                            }
                    );
                }
                else{
                    Toast.makeText(UserActivity.this,"Услуга не найдена",Toast.LENGTH_SHORT).show();
                }

            }).addOnFailureListener(e ->{
                Toast.makeText(UserActivity.this,"Пользователь не авторизован",Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void ShowDatePickerDialog(TextView dateField){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,(view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    private void ShowTimePickerDialog(TextView timeField){
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hour, minute) -> {
                    calendar.set(Calendar.HOUR, hour);
                    calendar.set(Calendar.MINUTE, minute);
                },
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

}