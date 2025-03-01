package com.example.pupukaka;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Переход к LoginActivity при запуске
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Закрываем MainActivity, чтобы пользователь не мог вернуться к ней
    }
}
