package com.mustafakurt.museumsapp.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Index;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.mustafakurt.museumsapp.R;
import com.mustafakurt.museumsapp.databinding.ActivityMapsBinding;
import com.mustafakurt.museumsapp.model.Place;
import com.mustafakurt.museumsapp.roomdb.PlaceDao;
import com.mustafakurt.museumsapp.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    //    disposable kullan-at olarak kullanilabilir
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    //  harita olu??turuldu??unda ??al????anlar
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();
//        //kucuk bilgilerimizi SharedPreferences alt??nda kaydedebiliyoruz
        sharedPreferences = this.getSharedPreferences("com.mustafakurt.museumsapp", MODE_PRIVATE);
        info = false;

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "Places").build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;
        binding.saveButton.setEnabled(false);
    }

    //    harita haz??r oldu??unda onMapReady cag??r??l??r
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String intentInfo = intent.getStringExtra("info");

        if (intentInfo.equals("new")) {
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);

            //location manager -> konum servisleri
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //location listener -> konum yoneticisinden konumun degistigine dair uyar??lar?? alabilmek icin
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    info = sharedPreferences.getBoolean("info", false);
//                burada yapt??????m??z i??lem, kullan??c?? konumuna kamera bir kere yak??nla??t??ktan sonra sharedPreferences de??erimiz(default=false)
//                true olacak ve ondan sonra her konum yenilendi??inde kamera bir daha
                    if (!info) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true).apply();
                    }
                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //request permission -> izin istemek gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(), "Harita icin izin gerekli", Snackbar.LENGTH_INDEFINITE).setAction("Izin Ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.clear();
            selectedPlace = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(selectedPlace.latitude, selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            binding.museumNameText.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }

//        kay??t butonu bir yer secilmeden ??nce en basta inaktif durumda yapt??k

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //-> kullan??c?? izni olmadan izin vermiyor

    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //kullan??c?? izin verdiyse
                        //harita bizden her saniye (de??eri 0 girdi??imiz i??in) konumumuzu ??ekerek g??ncelleyecek
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //burada izin veriyor
                        Location sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (sonKonum != null) {
                            LatLng sonKullaniciKonumum = new LatLng(sonKonum.getLatitude(), sonKonum.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumum, 15));
                        }
                    }
                } else {
                    //kullan??c?? izin vermediyse
                    Toast.makeText(MapsActivity.this, "Izin Gerekli!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //  haritada uzun t??klama ile ??al????anlar
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
//      koyulan isaretcileri harita uzerinden temizler
        mMap.clear();
//      harita uzerine isaretci yerlestirir
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

//        kullan??c?? bir yer secemeden kay??t butonu inaktif olsun
        binding.saveButton.setEnabled(true);

    }

    public void save(View view) {
        Place place = new Place(binding.museumNameText.getText().toString(), selectedLatitude, selectedLongitude);
        placeDao.insert(place);
        compositeDisposable.add(placeDao.insert(place).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));

    }

    private void handleResponse() {
        Intent intent = new Intent(MapsActivity.this, UserInterface.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view) {
        if (selectedPlace != null) {
            compositeDisposable.add(placeDao.delete(selectedPlace).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(MapsActivity.this::handleResponse));

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}