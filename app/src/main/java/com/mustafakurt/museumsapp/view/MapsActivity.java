package com.mustafakurt.museumsapp.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
//    SharedPreferences sharedPreferences=MapsActivity.this.getSharedPreferences("com.mustafakurt.museumsapp",MODE_PRIVATE);

//  harita oluşturulduğunda çalışanlar
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
//        //kucuk bilgilerimizi SharedPreferences altında kaydedebiliyoruz
        sharedPreferences = this.getSharedPreferences("com.mustafakurt.museumsapp", MODE_PRIVATE);
        info = false;
    }

    //    harita hazır olduğunda onMapReady cagırılır
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        //location manager -> konum servisleri
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //location listener -> konum yoneticisinden konumun degistigine dair uyarıları alabilmek icin
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                info = sharedPreferences.getBoolean("info", false);
//                burada yaptığımız işlem, kullanıcı konumuna kamera bir kere yakınlaştıktan sonra sharedPreferences değerimiz(default=false)
//                true olacak ve ondan sonra her konum yenilendiğinde kamera bir daha
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

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //-> kullanıcı izni olmadan izin vermiyor

    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //kullanıcı izin verdiyse
                        //harita bizden her saniye (değeri 0 girdiğimiz için) konumumuzu çekerek güncelleyecek
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //burada izin veriyor
                        Location sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (sonKonum != null) {
                            LatLng sonKullaniciKonumum = new LatLng(sonKonum.getLatitude(), sonKonum.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumum, 15));
                        }
                    }
                } else {
                    //kullanıcı izin vermediyse
                    Toast.makeText(MapsActivity.this, "Izin Gerekli!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

//  haritada uzun tıklama ile çalışanlar
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
//      koyulan isaretcileri harita uzerinden temizler
        mMap.clear();
//      harita uzerine isaretci yerlestirir
        mMap.addMarker(new MarkerOptions().position(latLng));

    }

    public void save(View view){

    }

    public void delete(View view){

    }

}