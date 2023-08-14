package com.ibercivis.stopamianto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ibercivis.stopamianto.clases.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Report extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    EditText txt_reporte;
    ImageView tejado_img, deposito_img, tuberia_img, escombros_img, otros_img, poco_img, medio_img, mucho_img, camera_img, foto;
    TextView tejado_txt, deposito_txt, tuberia_txt, escombros_txt, otros_txt, poco_txt, medio_txt, mucho_txt, texto_fase;
    Button continuar_btn;
    LinearLayout back_btn;

    LinearLayout fase1, fase2, fase3, fase4;
    String build, quantity;

    Boolean tejado_bool = false, deposito_bool = false, tuberia_bool = false, escombros_bool = false, otros_bool = false;
    Boolean poco_bool = false, medio_bool = false, mucho_bool = false, foto_bool = false, txt_bool = false;

    // PARA FOTO CÁMARA
    public static final int REQUEST_CODE_TAKE_PHOTO = 0 /*1*/;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoPath1;
    private Uri photoURI;
    private Uri photoURI1;
    String base64String = "";

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private MyLocationNewOverlay mLocationOverlay;
    public MapView map;

    ByteArrayOutputStream arrayParaBlob = new ByteArrayOutputStream();

    byte[] foto_blob;

    Double latitud, longitud;

    Boolean fromLongClick;

    Integer fase = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.reportar);

        fromLongClick = getIntent().getBooleanExtra("fromLongClick", false); // Con esta variable se detecta si se viene desde un LongClick en el mapa
        if (fromLongClick) {
            int duration = Toast.LENGTH_LONG;
            Toast toast;
            CharSequence text;
            text = "Vas a subir un marcador en un punto seleccionado en el mapa";
            toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        } else {
            int duration = Toast.LENGTH_LONG;
            Toast toast;
            CharSequence text;
            text = "Vas a subir un marcador asociado a tu ubicación actual";
            toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.mapa:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.reportar:
                        return true;
                    case R.id.usuario:
                        startActivity(new Intent(getApplicationContext(), User.class));
                        overridePendingTransition(0,0);
                        return true;
                }

                return false;
            }
        });

        // VINCULAMOS VARIABLES CON LAYOUTS
        fase1=findViewById(R.id.reporte_fase1);
        fase2=findViewById(R.id.reporte_fase2);
        fase3=findViewById(R.id.reporte_fase3);
        fase4=findViewById(R.id.reporte_fase4);

        txt_reporte = findViewById(R.id.reporte_texto);
        texto_fase = findViewById(R.id.reporte_faseTxt);

        tejado_img = findViewById(R.id.image_tejado); deposito_img = findViewById(R.id.image_deposito); tuberia_img = findViewById(R.id.image_tuberia); escombros_img = findViewById(R.id.image_escombros); otros_img = findViewById(R.id.image_otros);
        tejado_txt = findViewById(R.id.texto_tejado); deposito_txt = findViewById(R.id.texto_deposito); tuberia_txt = findViewById(R.id.texto_tuberia); escombros_txt = findViewById(R.id.texto_escombros); otros_txt = findViewById(R.id.texto_otros);

        poco_img = findViewById(R.id.image_poco); medio_img = findViewById(R.id.image_medio); mucho_img = findViewById(R.id.image_mucho);
        poco_txt = findViewById(R.id.texto_poco); medio_txt = findViewById(R.id.texto_medio); mucho_txt = findViewById(R.id.texto_mucho);

        camera_img = findViewById(R.id.image_camera);

        continuar_btn = findViewById(R.id.reporte_aceptarBtn); back_btn = findViewById(R.id.reporte_backfaseBtn);

        // FASE 1
        texto_fase.setText("Fase 1/4");
        back_btn.setVisibility(View.GONE);

        tejado_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaEstructura("tejado");
            }
        });

        deposito_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaEstructura("deposito");
            }
        });

        tuberia_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaEstructura("tuberias");
            }
        });

        escombros_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaEstructura("escombros");
            }
        });

        otros_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaEstructura("otros");
            }
        });

        //AQUÍ ABAJO ESTÁ TODA LA LÓGICA DE LAS FASES
        continuar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fase==1){

                if(tejado_bool==true || deposito_bool==true || tuberia_bool==true || escombros_bool==true || otros_bool==true){

                    fase1.setVisibility(View.GONE);
                    fase2.setVisibility(View.VISIBLE);
                    back_btn.setVisibility(View.VISIBLE);
                    fase = 2;
                    texto_fase.setText("Fase 2/4");

                } else {
                    //FASE 1 Y NO HAS SELECCIONADO NADA
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast;
                    CharSequence text;
                    text = "Debes seleccionar un tipo de estructura";
                    toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();

                }
                } else if(fase==2){

                    if(poco_bool == true || medio_bool == true || mucho_bool ==true){
                        fase = 3;
                        fase2.setVisibility(View.GONE);
                        fase3.setVisibility(View.VISIBLE);
                        texto_fase.setText("Fase 3/4");
                    } else {
                        //FASE 2 Y NO HAS SELECCIONADO NADA
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;
                        text = "Debes seleccionar una cantidad";
                        toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                    }

                } else if(fase==3){
                    //FASE 3
                    fase = 4;
                    fase3.setVisibility(View.GONE);
                    fase4.setVisibility(View.VISIBLE);
                    texto_fase.setText("Fase 4/4");
                } else if(fase ==4){
                    //AQUÍ SE SUBE YA EL REPORTE
                    saberUbicacion();
                }
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fase==2){
                    back_btn.setVisibility(View.GONE);
                    fase2.setVisibility(View.GONE);
                    fase1.setVisibility(View.VISIBLE);
                    fase = 1;
                    texto_fase.setText("Fase 1/4");
                } else if(fase==3){
                    fase = 2;
                    fase3.setVisibility(View.GONE);
                    fase2.setVisibility(View.VISIBLE);
                    texto_fase.setText("Fase 2/4");
                } else if(fase==4){
                    fase = 3;
                    fase4.setVisibility(View.GONE);
                    fase3.setVisibility(View.VISIBLE);
                    texto_fase.setText("Fase 3/4");
                }
            }
        });

        // FASE 2

        poco_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaCantidad("poco");
            }
        });

        medio_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaCantidad("medio");
            }
        });

        mucho_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionaCantidad("mucho");
            }
        });

        //FASE 3

        camera_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhoto(0);
            }
        });

        //FASE 4

        txt_reporte.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                return false;
            }
        });



    }

    void seleccionaEstructura(String eleccion){

        switch (eleccion){
            case "tejado":
                build = "Tejado";

                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_activo));
                tejado_bool = true;
                tejado_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.BOLD);

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_inactivo));
                deposito_bool = false;
                deposito_txt.setTextColor(getResources().getColor(R.color.white));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.NORMAL);

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_inactivo));
                tuberia_bool = false;
                tuberia_txt.setTextColor(getResources().getColor(R.color.white));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.NORMAL);

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_inactivo));
                escombros_bool = false;
                escombros_txt.setTextColor(getResources().getColor(R.color.white));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.NORMAL);

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_inactivo));
                otros_bool = false;
                otros_txt.setTextColor(getResources().getColor(R.color.white));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.NORMAL);
                break;
            case "deposito":
                build = "Deposito";

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_activo));
                deposito_bool = true;
                deposito_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.BOLD);

                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_inactivo));
                tejado_bool = false;
                tejado_txt.setTextColor(getResources().getColor(R.color.white));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.NORMAL);

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_inactivo));
                tuberia_bool = false;
                tuberia_txt.setTextColor(getResources().getColor(R.color.white));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.NORMAL);

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_inactivo));
                escombros_bool = false;
                escombros_txt.setTextColor(getResources().getColor(R.color.white));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.NORMAL);

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_inactivo));
                otros_bool = false;
                otros_txt.setTextColor(getResources().getColor(R.color.white));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.NORMAL);
                break;
            case "tuberias":
                build = "Tuberia";

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_activo));
                tuberia_bool = true;
                tuberia_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.BOLD);

                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_inactivo));
                tejado_bool = false;
                tejado_txt.setTextColor(getResources().getColor(R.color.white));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.NORMAL);

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_inactivo));
                deposito_bool = false;
                deposito_txt.setTextColor(getResources().getColor(R.color.white));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.NORMAL);

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_inactivo));
                escombros_bool = false;
                escombros_txt.setTextColor(getResources().getColor(R.color.white));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.NORMAL);

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_inactivo));
                otros_bool = false;
                otros_txt.setTextColor(getResources().getColor(R.color.white));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.NORMAL);
                break;
            case "escombros":
                build = "Escombro";

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_activo));
                escombros_bool = true;
                escombros_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.BOLD);

                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_inactivo));
                tejado_bool = false;
                tejado_txt.setTextColor(getResources().getColor(R.color.white));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.NORMAL);

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_inactivo));
                deposito_bool = false;
                deposito_txt.setTextColor(getResources().getColor(R.color.white));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.NORMAL);

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_inactivo));
                tuberia_bool = false;
                tuberia_txt.setTextColor(getResources().getColor(R.color.white));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.NORMAL);

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_inactivo));
                otros_bool = false;
                otros_txt.setTextColor(getResources().getColor(R.color.white));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.NORMAL);
                break;
            case "otros":
                build = "Otros";

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_activo));
                otros_bool = true;
                otros_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.BOLD);

                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_inactivo));
                tejado_bool = false;
                tejado_txt.setTextColor(getResources().getColor(R.color.white));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.NORMAL);

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_inactivo));
                deposito_bool = false;
                deposito_txt.setTextColor(getResources().getColor(R.color.white));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.NORMAL);

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_inactivo));
                tuberia_bool = false;
                tuberia_txt.setTextColor(getResources().getColor(R.color.white));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.NORMAL);

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_inactivo));
                escombros_bool = false;
                escombros_txt.setTextColor(getResources().getColor(R.color.white));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.NORMAL);
                break;
            default:
                tejado_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tejado_inactivo));
                tejado_bool = false;
                tejado_txt.setTextColor(getResources().getColor(R.color.white));
                tejado_txt.setTypeface(tejado_txt.getTypeface(), Typeface.NORMAL);

                deposito_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_deposito_inactivo));
                deposito_bool = false;
                deposito_txt.setTextColor(getResources().getColor(R.color.white));
                deposito_txt.setTypeface(deposito_txt.getTypeface(), Typeface.NORMAL);

                tuberia_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_tuberia_inactivo));
                tuberia_bool = false;
                tuberia_txt.setTextColor(getResources().getColor(R.color.white));
                tuberia_txt.setTypeface(tuberia_txt.getTypeface(), Typeface.NORMAL);

                escombros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_escombro_inactivo));
                escombros_bool = false;
                escombros_txt.setTextColor(getResources().getColor(R.color.white));
                escombros_txt.setTypeface(escombros_txt.getTypeface(), Typeface.NORMAL);

                otros_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_otros_inactivo));
                otros_bool = false;
                otros_txt.setTextColor(getResources().getColor(R.color.white));
                otros_txt.setTypeface(otros_txt.getTypeface(), Typeface.NORMAL);
                break;

        }

    }

    void seleccionaCantidad(String eleccion){
        switch (eleccion){
            case "poco":
                quantity = "Poco";

                poco_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_poco_activo));
                poco_bool = true;
                poco_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                poco_txt.setTypeface(poco_txt.getTypeface(), Typeface.BOLD);

                medio_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_medio_inactivo));
                medio_bool = false;
                medio_txt.setTextColor(getResources().getColor(R.color.white));
                medio_txt.setTypeface(medio_txt.getTypeface(), Typeface.NORMAL);

                mucho_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_mucho_inactivo));
                mucho_bool = false;
                mucho_txt.setTextColor(getResources().getColor(R.color.white));
                mucho_txt.setTypeface(mucho_txt.getTypeface(), Typeface.NORMAL);
                break;

            case "medio":
                quantity = "Media";

                medio_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_medio_activo));
                medio_bool = true;
                medio_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                medio_txt.setTypeface(medio_txt.getTypeface(), Typeface.BOLD);

                poco_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_poco_inactivo));
                poco_bool = false;
                poco_txt.setTextColor(getResources().getColor(R.color.white));
                poco_txt.setTypeface(poco_txt.getTypeface(), Typeface.NORMAL);

                mucho_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_mucho_inactivo));
                mucho_bool = false;
                mucho_txt.setTextColor(getResources().getColor(R.color.white));
                mucho_txt.setTypeface(mucho_txt.getTypeface(), Typeface.NORMAL);
                break;

            case "mucho":
                quantity = "Mucho";

                mucho_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_mucho_activo));
                mucho_bool = true;
                mucho_txt.setTextColor(getResources().getColor(R.color.secondaryColor));
                mucho_txt.setTypeface(mucho_txt.getTypeface(), Typeface.BOLD);

                poco_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_poco_inactivo));
                poco_bool = false;
                poco_txt.setTextColor(getResources().getColor(R.color.white));
                poco_txt.setTypeface(poco_txt.getTypeface(), Typeface.NORMAL);

                medio_img.setImageDrawable(getResources().getDrawable(R.drawable.boton_medio_inactivo));
                medio_bool = false;
                medio_txt.setTextColor(getResources().getColor(R.color.white));
                medio_txt.setTypeface(medio_txt.getTypeface(), Typeface.NORMAL);
                break;
        }
    }

    public void addPhoto(Integer tipo) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        225);
            }


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        226);
            }
        } else {
            dispatchTakePictureIntent(tipo);
        }
    }

    private void dispatchTakePictureIntent(Integer tipo) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        //  if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile(tipo);
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "MyPicture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
            if (tipo == 0) {
                Log.d("Camera", "Tipo is 0");
                photoURI = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                photoURI1 = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }

            //Uri photoURI = FileProvider.getUriForFile(AddActivity.this, "com.example.android.fileprovider", photoFile);

            if (tipo == 0) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            } else {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI1);
            }
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PHOTO);
        }
    }
    //}

    private File createImageFile(Integer tipo) throws IOException {
        // Create an image file name
        Log.d("Camera", "Here");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        if (tipo == 0) {
            mCurrentPhotoPath = image.getAbsolutePath();
            Log.d("Camera", "Tipo is 0 in createFileImage");
            Log.d("Camera", mCurrentPhotoPath.toString());
        } else {
            mCurrentPhotoPath1 = image.getAbsolutePath();
            Log.d("Camera", mCurrentPhotoPath1.toString());
        }
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Camera", "Here2");


        if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {

            Bitmap bitmap;
            Bitmap bitmap1;
            Bitmap rotatedBitmap;
            Bitmap rotatedBitmap1;
            if (photoURI != null) {
                try {
                    Log.d("Camera PhotoURI", photoURI.toString());
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                    Bitmap scaledBitmap = getScaledBitmap(bitmap, 800, 600);



                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, arrayParaBlob);

                    rotatedBitmap = rotateImageIfRequired(this, scaledBitmap, photoURI);



                    base64String = getBase64String(rotatedBitmap); // foto en base64

                    foto_blob = arrayParaBlob.toByteArray();
                    String debugfoto = String.valueOf(foto_blob);

                    camera_img.setImageBitmap(rotatedBitmap);
                    //miniatura_camara.setRotation(0);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            /*if (requestCode == REQUEST_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); // Aquí es null
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mPhotoImageView.setImageBitmap(imageBitmap);
            }*/

        }
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private Bitmap getScaledBitmap(Bitmap b, int reqWidth, int reqHeight) {
        int bWidth = b.getWidth();
        int bHeight = b.getHeight();

        int nWidth = bWidth;
        int nHeight = bHeight;

        if (nWidth > reqWidth) {
            int ratio = bWidth / reqWidth;
            if (ratio > 0) {
                nWidth = reqWidth;
                nHeight = bHeight / ratio;
            }
        }

        if (nHeight > reqHeight) {
            int ratio = bHeight / reqHeight;
            if (ratio > 0) {
                nHeight = reqHeight;
                nWidth = bWidth / ratio;
            }
        }

        return Bitmap.createScaledBitmap(b, nWidth, nHeight, true);
    }

    private String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

        byte[] imageBytes = baos.toByteArray();

        String base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

        return base64String;
    }

    void saberUbicacion(){

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);

                recreate();

                return;
            }
        }

        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if(currentLocation != null) {

            //latitud = currentLocation.getLatitude();
            //longitud = currentLocation.getLongitude();
            latitud = getIntent().getDoubleExtra("latitude", currentLocation.getLatitude());
            longitud = getIntent().getDoubleExtra("longitude", currentLocation.getLongitude());
            subirMarcador();

        } else {
           /* GpsMyLocationProvider provider = new GpsMyLocationProvider(this.getApplicationContext());
            mLocationOverlay = new MyLocationNewOverlay(provider, map);
            mLocationOverlay.enableMyLocation();
            GeoPoint myPoint = mLocationOverlay.getMyLocation();
            latitud = myPoint.getLatitude();
            longitud = myPoint.getLongitude();
            subirMarcador(); */
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(currentLocation != null) {
                //latitud = currentLocation.getLatitude();
                //longitud = currentLocation.getLongitude();
                latitud = getIntent().getDoubleExtra("latitude", currentLocation.getLatitude());
                longitud = getIntent().getDoubleExtra("longitude", currentLocation.getLongitude());
                subirMarcador();

            } else {
            int duration = Toast.LENGTH_SHORT;
            Toast toast;
            CharSequence text;



            text = "Estamos teniendo problemas para obtener tu ubicación. Por favor, vuelve a intentarlo.";
            toast = Toast.makeText(getApplicationContext(), text, duration);
            toast.show();
        }

        }

    }

    void subirMarcador(){

        final LinearLayout cargar = findViewById(R.id.cargando);


        cargar.setVisibility(View.VISIBLE);


        // Input data ok, so go with the request

        // Url for the webservice
        String url = getString(R.string.base_url) + "/addreport.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println(response.toString());

                    JSONObject responseJSON = new JSONObject(response);

                    if ((int) responseJSON.get("result") == 1) {

                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;



                        text = "Marcador subido correctamente";
                        toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                        txt_reporte.setText("");
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();


                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;

                        text = "Error while login: " + responseJSON.get("message") + ".";
                        toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();

                        // Clean the text fields for new entries

                        cargar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    cargar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                int duration = Toast.LENGTH_SHORT;
                Toast toast;
                CharSequence text;
                text = "Error while login: " + error.getLocalizedMessage() + ".";
                toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
                cargar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> login_params = new HashMap<String, String>();
                Long tsLong = System.currentTimeMillis();
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(tsLong);
                String date = DateFormat.format("yyy-MM-dd hh:mm:ss", cal).toString();
                SessionManager session = new SessionManager(getApplicationContext());
                Log.d("Corte", date);
                login_params.put("build", String.valueOf(build));
                login_params.put("quantity", String.valueOf(quantity));
                login_params.put("info", txt_reporte.getText().toString());
                login_params.put("user", String.valueOf(session.getUsername()));
                login_params.put("datetime", date);
                login_params.put("latitude", String.valueOf(latitud));
                login_params.put("longitude", String.valueOf(longitud));
                login_params.put("foto", String.valueOf(base64String));










                Log.d("Date", date);


                return login_params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
        Log.v("Queue", queue.toString());
    }

}


