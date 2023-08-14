package com.ibercivis.stopamianto;

import static android.content.ContentValues.TAG;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.ibercivis.stopamianto.clases.AdaptadorObservaciones;
import com.ibercivis.stopamianto.clases.Observacion;
import com.ibercivis.stopamianto.clases.SessionManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    MapView mapView;
    RelativeLayout view_parent;
    private MyLocationNewOverlay mLocationOverlay;
    ImageButton centerButton;
    MapController mapController;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private RadiusMarkerClusterer clusterer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.mapa);
        mapView = findViewById(R.id.map_view);
        centerButton = findViewById(R.id.center_button);
        mapController = (MapController) mapView.getController();
        view_parent = findViewById(R.id.parentView);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.mapa:
                        return true;
                    case R.id.reportar:
                        startActivity(new Intent(getApplicationContext(), Report.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.usuario:
                        startActivity(new Intent(getApplicationContext(), User.class));
                        overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });
        // Configuración inicial de osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView.setMinZoomLevel(6.0);
        mapView.setMaxZoomLevel(20.0);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE); // Aquí se pueden ajustar otros TILES
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        clusterer = new RadiusMarkerClusterer(this);

        // Verificar y solicitar permisos en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permiso ya concedido, puedes iniciar la obtención de ubicación aquí TODO: REVISAR SI PASAMOS TODA LA CARGA DEL MAPA A ESTA ZONA
            // DE MODO QUE NO SE PUEDA ACCEDER AL MAPA SI EL USUARIO NO CONCEDE LOS PERMISOS DE UBICACIÓN
            this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()),mapView);
            this.mLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(this.mLocationOverlay);
            centerButton.setOnClickListener(view -> centerOnUserLocation());
            centerOnUserLocation();

        }
        mapView.getOverlays().add(clusterer);
        getReportsRequest();


        final Handler longClickHandler = new Handler();
        Overlay overlay = new Overlay(getBaseContext()) {
            @Override
            public boolean onLongPress(MotionEvent e, MapView mapView) {
                longClickHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Projection proj = mapView.getProjection();
                        GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());

                        // Aquí manejas el longclick
                        Intent intent = new Intent(MainActivity.this, Report.class);
                        intent.putExtra("fromLongClick", true);
                        intent.putExtra("latitude", loc.getLatitude());
                        intent.putExtra("longitude", loc.getLongitude());
                        startActivity(intent);
                    }
                }, 1000); // Establece un tiempo (en milisegundos) para considerar un longclick
                return true;
            }

            @Override
            public boolean onTouchEvent(MotionEvent event, MapView mapView) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    longClickHandler.removeCallbacksAndMessages(null); // Cancela el longclick si el dedo se levanta
                }
                return super.onTouchEvent(event, mapView);
            }
        };

        mapView.getOverlays().add(overlay);


    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // Llamamos a este método en onResume para que el mapa se actualice correctamente
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // Llamamos a este método en onPause para pausar actualizaciones del mapa
    }

    void centerOnUserLocation() {
        // Verificar y solicitar permisos en tiempo de ejecución
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permiso ya concedido, puedes iniciar la obtención de ubicación aquí
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);

            if (location == null) {
                location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
            }

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Puedes usar estos valores para centrar el mapa en la ubicación del usuario
            GeoPoint userLocation = new GeoPoint(latitude, longitude);
            mapController.animateTo(userLocation);
            mapController.setZoom(16.00);
            mapView.invalidate();


        }

    }

    public void getReportsRequest () {
        //final LinearLayout cargar = findViewById(R.id.cargando);
        //cargar.setVisibility(View.VISIBLE);

        String url = getString(R.string.base_url) + "/getreportsfinal.php";

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.getCache().clear();
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println(response.toString());

                    JSONObject responseJSON = new JSONObject(response);
                    JSONArray reportsArray = responseJSON.getJSONArray("reports");
                    for (int i = 0; i < reportsArray.length(); i++) {
                        final JSONObject[] report = {reportsArray.getJSONObject(i)};

                        //Obtenemos la información del JSON
                        int id = Integer.valueOf(String.valueOf(report[0].get("report_id")));
                        String foto = "https://amianto.ibercivis.es/uploads/"+String.valueOf(report[0].get("report_id"));
                        int hasPhoto = Integer.valueOf(String.valueOf(report[0].get("hasphoto")));
                        String fechaObservacion = String.valueOf(report[0].get("report_date"));
                        String estructura = String.valueOf(report[0].get("build"));
                        String cantidad = String.valueOf(report[0].get("quantity"));
                        String info = String.valueOf(report[0].get("info"));
                        String user = String.valueOf(report[0].get("username"));

                        double latitude = Double.parseDouble(report[0].getString("latitude"));
                        double longitude = Double.parseDouble(report[0].getString("longitude"));

                        GeoPoint location = new GeoPoint(latitude, longitude);

                        Marker marker = new Marker(mapView);
                        marker.setPosition(location);
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        marker.setIcon(getResources().getDrawable(R.drawable.ic_marker)); // TODO: Buscar un icono adecuado para los marcadores

                        //Creamos instancia de Observacion
                        Observacion observacion = new Observacion(id, hasPhoto, user, fechaObservacion, latitude, longitude, estructura, cantidad, info, foto);

                        // Asignar la instancia de Observacion como relatedObject al marcador
                        marker.setRelatedObject(observacion);

                        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                Log.d(TAG, "onMarkerClick: prueba");
                                Observacion report = (Observacion) marker.getRelatedObject();
                                if (report != null) {
                                    // Inflar el diseño del CardView
                                    View cardViewLayout = LayoutInflater.from(MainActivity.this).inflate(R.layout.observacion_card, view_parent, false);
                                    MaterialCardView cardView = cardViewLayout.findViewById(R.id.marcador_card);

                                    // Obtener los elementos del CardView que vamos a emplear
                                    TextView build = cardView.findViewById(R.id.titulo);
                                    TextView quantity = cardView.findViewById(R.id.titulo2);
                                    ImageView photoView= cardView.findViewById(R.id.photo_0);
                                    TextView info = cardView.findViewById(R.id.dato_agusanamiento);
                                    TextView date = cardView.findViewById(R.id.dato_presion);
                                    Button backButton = cardView.findViewById(R.id.delete_btn);
                                    // Llenar los elementos con la información del reporte
                                    build.setText(report.getBuild());
                                    quantity.setText(report.getQuantity());
                                    info.setText(report.getInfo());
                                    date.setText(report.getDate());
                                    String photo = report.getFoto();
                                    backButton.setText("Cerrar");

                                    if(report.getHasPhoto() == 1){
                                        Picasso.with(MainActivity.this).load(photo).into(photoView);
                                        photoView.setClipToOutline(true);
                                    }

                                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cardViewLayout.getLayoutParams();
                                    layoutParams.setMargins(0, 32, 0, 0);
                                    cardViewLayout.setLayoutParams(layoutParams);

                                    // Asignar al botón del cardView la funcionalidad de "desinflar" el Layout
                                    backButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            view_parent.removeView(cardViewLayout);
                                        }
                                    });

                                    // Mostrar el CardView en el mapa
                                    view_parent.addView(cardViewLayout);
                                }
                                return true;
                            }
                        });

                        //mapView.getOverlays().add(marker);
                        clusterer.add(marker);
                        mapView.invalidate();
                    }

                    mapView.invalidate(); // Actualiza el mapa para mostrar los marcadores
                    centerOnUserLocation();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> signup_params = new HashMap<String, String>();


                return signup_params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        sr.setShouldCache(false);
        queue.add(sr);
    }

}