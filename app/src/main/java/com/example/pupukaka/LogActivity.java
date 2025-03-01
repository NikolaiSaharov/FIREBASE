package com.example.pupukaka;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LogActivity extends AppCompatActivity {
    private ListView logsListView;
    private FirebaseFirestore db;
    private List<Log> logs;
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logsListView = findViewById(R.id.logsListView);
        db = FirebaseFirestore.getInstance();
        logs = new ArrayList<>();
        adapter = new LogAdapter(this, logs);
        logsListView.setAdapter(adapter);

        loadLogs();
    }

    private void loadLogs() {
        db.collection("admin_logs").orderBy("timestamp").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logs.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String action = document.getString("action");
                        String timestamp = document.getDate("timestamp").toString();
                        logs.add(new Log(action, timestamp));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LogActivity.this, "Ошибка загрузки логов", Toast.LENGTH_SHORT).show();
                });
    }
}
