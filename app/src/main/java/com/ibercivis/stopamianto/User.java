package com.ibercivis.stopamianto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ibercivis.stopamianto.clases.AdaptadorObservaciones;
import com.ibercivis.stopamianto.clases.Observacion;
import com.ibercivis.stopamianto.clases.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    //Propios de esta Activity
    RecyclerView recyclerObservaciones;
    AdaptadorObservaciones recyclerAdapter;
    ArrayList<Observacion> ListaObservaciones = new ArrayList<>();
    LinearLayout marco_photo;
    ImageView photoView;
    Button backViewButton;
    TextView noObservaciones_txt, username_txt;
    Button btn_logout;

    Bitmap bitmap_logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        SessionManager session = new SessionManager(User.this);

        /*-----Hooks-----*/
        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.usuario);

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
                        startActivity(new Intent(getApplicationContext(), Report.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.usuario:

                        return true;
                }

                return false;
            }
        });

        marco_photo = findViewById(R.id.marco_visualizar_foto);
        photoView = findViewById(R.id.foto_ver);
        backViewButton = findViewById(R.id.foto_volver);
        btn_logout = findViewById(R.id.reporte_logoutBtn);
        noObservaciones_txt = findViewById(R.id.txt_no_observaciones);
        username_txt = findViewById(R.id.user_usernametxt);

        username_txt.setText(session.getUsername());

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager session = new SessionManager(getApplicationContext());
                session.setClear();
                Intent intent4 = new Intent(getApplicationContext(), Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent4);
                finish();
            }
        });

        backViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marco_photo.setVisibility(View.GONE);
            }
        });

        getUserRequest();
    }

    public void getUserRequest () {
        final LinearLayout cargar = findViewById(R.id.cargando);
        final String user;
        int item;
        SessionManager session = new SessionManager(User.this);
        cargar.setVisibility(View.VISIBLE);
        recyclerObservaciones = findViewById(R.id.recyclerobservaciones);

        // Input data ok, so go with the request

        // Url for the webservice
        user = String.valueOf(session.getUsername());

        String url = getString(R.string.base_url) + "/getMyReports.php?user=" + user;

        RequestQueue queue = Volley.newRequestQueue(User.this);
        queue.getCache().clear();
        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    System.out.println(response.toString());

                    JSONObject responseJSON = new JSONObject(response);

                    if ((int) responseJSON.get("result") == 1){

                        int value;
                        int i = 0;
                        JSONArray jsonArray = responseJSON.getJSONArray("data");

                        for (i=0; i < jsonArray.length(); i++){

                            // JSONArray jsonArray1 = jsonArray.getJSONArray(i); //Diferentes proyectos


                            int id = Integer.valueOf(String.valueOf(jsonArray.getJSONObject(i).get("id")));
                            String foto = "https://amianto.ibercivis.es/uploads/"+String.valueOf(jsonArray.getJSONObject(i).get("id"));
                            int hasPhoto = Integer.valueOf(String.valueOf(jsonArray.getJSONObject(i).get("hasphoto")));
                            String fechaObservacion = String.valueOf(jsonArray.getJSONObject(i).get("report_date"));
                            String estructura = String.valueOf(jsonArray.getJSONObject(i).get("build"));
                            String cantidad = String.valueOf(jsonArray.getJSONObject(i).get("quantity"));
                            String info = String.valueOf(jsonArray.getJSONObject(i).get("info"));
                            Double latitud = Double.parseDouble(String.valueOf(jsonArray.getJSONObject(i).get("latitude")));
                            Double longitud = Double.parseDouble(String.valueOf(jsonArray.getJSONObject(i).get("longitude")));
                            ListaObservaciones.add(new Observacion(id, hasPhoto, user, fechaObservacion, latitud, longitud, estructura, cantidad, info, foto));

                        }

                        if(ListaObservaciones.size() > 0){
                            Log.d("ListaObservaciones", ListaObservaciones.toString());
                            // texto_noHayGustan.setVisibility(View.GONE);
                            recyclerObservaciones.setVisibility(View.VISIBLE);
                            recyclerObservaciones.setHasFixedSize(true);
                            recyclerAdapter = new AdaptadorObservaciones(ListaObservaciones, marco_photo, photoView, cargar, User.this);
                            recyclerObservaciones.setAdapter(recyclerAdapter);
                            LinearLayoutManager layout = new LinearLayoutManager(User.this);
                            layout.setOrientation(LinearLayoutManager.VERTICAL);
                            recyclerObservaciones.setLayoutManager(layout);
                        } else {
                            noObservaciones_txt.setVisibility(View.VISIBLE);
                            recyclerObservaciones.setVisibility(View.GONE);
                        }



                        cargar.setVisibility(View.GONE);
                        // usernametxt.setText(user);
                    }
                    else {
                        Log.println(Log.ASSERT, "Error", "Algo ha fallado que la respuesta es 0");
                    }
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

    public class GetImageFromUrl extends AsyncTask<String, Void, Bitmap> {

        Bitmap bitmap;

        @Override
        protected Bitmap doInBackground(String... url) {
            String stringUrl = url[0];
            bitmap = null;
            InputStream inputStream;
            try {
                inputStream = new java.net.URL(stringUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap_logo = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            super.onPostExecute(bitmap);
        }
    }

}
