package com.mustafakurt.museumsapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mustafakurt.museumsapp.R;
import com.mustafakurt.museumsapp.adapter.PlaceAdapter;
import com.mustafakurt.museumsapp.databinding.ActivityMapsBinding;
import com.mustafakurt.museumsapp.databinding.ActivityUserInterfaceBinding;
import com.mustafakurt.museumsapp.model.Place;
import com.mustafakurt.museumsapp.roomdb.PlaceDao;
import com.mustafakurt.museumsapp.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserInterface extends AppCompatActivity {

    private ActivityUserInterfaceBinding binding;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    PlaceDatabase db;
    PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserInterfaceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        compositeDisposable.add(placeDao.getAll().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(UserInterface.this::handleResponse));

    }

    private void handleResponse(List<Place> placeList) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this
        ));
        PlaceAdapter placeAdapter = new PlaceAdapter(placeList);
        binding.recyclerView.setAdapter(placeAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu yönlendirmesi yaptık
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.travel_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //    menuden bir şey secilirse ne olacak
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_museum) {
            Intent intent = new Intent(UserInterface.this, MapsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}