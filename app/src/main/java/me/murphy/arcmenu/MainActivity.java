package me.murphy.arcmenu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArcMenu menu = (ArcMenu) findViewById(R.id.arc_menu);
        menu.setOnMenuClickListener(new ArcMenu.MenuClickListener() {
            @Override
            public void onMenuClick(int pos) {
                Toast.makeText(MainActivity.this, "Click " + pos + " Menu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
