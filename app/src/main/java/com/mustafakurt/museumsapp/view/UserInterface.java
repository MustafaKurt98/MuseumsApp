package com.mustafakurt.museumsapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mustafakurt.museumsapp.R;

public class UserInterface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interface);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu yönlendirmesi yaptık
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.travel_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //    menuden bir şey secilirse ne olacak
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.add_museum){
            Intent intent=new Intent(UserInterface.this,MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}