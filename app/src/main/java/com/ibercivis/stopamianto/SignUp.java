package com.ibercivis.stopamianto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.ibercivis.stopamianto.clases.SessionManager;
import com.ibercivis.stopamianto.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    TextInputEditText signup_username_textview, signup_email_textview, signup_password_textview;

    String error_check;

    LinearLayout lopd;
    CheckBox checkDatos1, checkDatos2;

    Button btn_login, btn_acept_datos, btn_cancel_datos, btn_sign;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //PERMISOS
        /*
        if(SDK_INT >= 30){
            if(!Environment.isExternalStorageManager()){
                Snackbar.make(findViewById(android.R.id.content), "Permission needed!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                                    startActivity(intent);
                                } catch (Exception ex){
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    startActivity(intent);
                                }
                            }
                        })
                        .show();
            } }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED  ){
            if (SDK_INT >= Build.VERSION_CODES.R) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            recreate();
            return ;
        }
        */
        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);



                return ;
            }
        }

        btn_login = findViewById(R.id.btn_back);
        btn_sign = findViewById(R.id.entrar_btn);

        lopd = findViewById(R.id.proteccion_datos);
        btn_acept_datos = findViewById(R.id.acept_datos);
        btn_cancel_datos = findViewById(R.id.cancel_datos);

        checkDatos1 = findViewById(R.id.checkdatos1);
        checkDatos2 = findViewById(R.id.checkdatos2);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLogin();
            }
        });

        btn_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lopd.setVisibility(View.VISIBLE);
            }
        });

        btn_acept_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkDatos1.isChecked() == true) {
                    if(checkDatos2.isChecked() == true){
                        signupRequest();
                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;

                        text = "Para poder registrarte debes aceptar la Política de Protección de Datos.";
                        toast = Toast.makeText(getApplicationContext(), text, duration);
                        toast.show();
                    }

                } else {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast;
                    CharSequence text;

                    text = "Para poder registrarte debes aceptar la Política de Protección de Datos.";
                    toast = Toast.makeText(getApplicationContext(), text, duration);
                    toast.show();
                }
            }
        });

        btn_cancel_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lopd.setVisibility(View.GONE);
            }
        });
    }

    public void signupRequest () {
        final LinearLayout cargar = findViewById(R.id.cargando);

        lopd.setVisibility(View.GONE);
        cargar.setVisibility(View.VISIBLE);
        signup_username_textview =  findViewById(R.id.signup_user);
        signup_email_textview =  findViewById(R.id.signup_email);
        signup_password_textview = findViewById(R.id.signup_pass);


        if(checkInputSignup()) {
            // Input data ok, so go with the request

            // Url for the webservice
            String url = getString(R.string.base_url) + "/signup.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.getCache().clear();
            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        System.out.println(response.toString());

                        JSONObject responseJSON = new JSONObject(response);

                        if ((int) responseJSON.get("result") == 1){
                            SessionManager session = new SessionManager(getApplicationContext());
                            session.setLogin(true, signup_username_textview.getText().toString());
                            Toast toast;
                            cargar.setVisibility(View.GONE);

                            toast = Toast.makeText(getApplicationContext(), "Bienvenido", Toast.LENGTH_LONG);
                            toast.show();
                            openLogin();

                        }
                        else {
                            showError("Error while signing up: " + responseJSON.get("message") + ".");

                            // Clean the text fields for new entries
                            signup_username_textview.setText("");
                            signup_email_textview.setText("");
                            signup_password_textview.setText("");

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
                    cargar.setVisibility(View.GONE);
                    int duration = Toast.LENGTH_LONG;
                    Toast toast;

                    toast = Toast.makeText(getApplicationContext(), "Ha sucedido un error. Es posible que ya exista un usuario registrado con esa dirección de email.", duration);
                    toast.show();

                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> signup_params = new HashMap<String, String>();
                    signup_params.put("username", signup_username_textview.getText().toString());
                    signup_params.put("email", signup_email_textview.getText().toString());
                    signup_params.put("password", signup_password_textview.getText().toString());


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
        else {
            // Si llego aquí es ruina. nothing, error has been shown in a toast and views clean
        }
    }

    private void showError (CharSequence error) {
        int duration = Toast.LENGTH_LONG;
        Toast toast;

        toast = Toast.makeText(getApplicationContext(), error, duration);
        toast.show();
    }

    private boolean checkLength( String text, String fieldName, int min, int max ) {
        if ( text.length() > max || text.length() < min ) {
            error_check = error_check + "Length of " + fieldName + " must be between " +
                    min + " and " + max + ".\n";
            return false;
        } else {
            return true;
        }
    }

    private boolean checkRegexp(String text, Pattern regexp, String errorMessage) {
        if (!regexp.matcher(text).matches()) {
            error_check = error_check + errorMessage + "\n";
            return false;
        } else {
            return true;
        }
    }

    private boolean checkSelect(Spinner spinner, String errorMessage) {

        if (spinner.getSelectedItemPosition() == 0) {
            error_check = error_check + "You must select one of the options from the " + errorMessage + " drop-down.\n";
            return false;
        }
        return true;
    }

    private boolean checkInputSignup () {

        error_check = "";
        boolean valid = true;
        String emailRegex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        // valid is evaluated in the second part to force the check function being called always, so all the errors are displayed at the same time (&& conditional evaluation)
        valid = checkLength( signup_username_textview.getText().toString(), "username", 3, 16 ) && valid;
        valid = checkLength( signup_email_textview.getText().toString(), "email", 6, 80 ) && valid;
        valid = checkLength( signup_password_textview.getText().toString(), "password", 5, 16 ) && valid;
//"/^[a-z]([0-9a-z_ ])+$/i"
        // In the regular expression for the username and password we do not use {3,16} (for instance),
        // to control the length through the regex, since it is most accurate to indicate the length error
        // separately, so it is not considered the length in the regex (it has been taken into account previously
        valid = checkRegexp( signup_username_textview.getText().toString(), Pattern.compile("^[a-zA-Z][a-zA-Z0-9 _]+$"), "Username may consist of a-z, 0-9, underscores, spaces and must begin with a letter." ) && valid;
        valid = checkRegexp( signup_email_textview.getText().toString(), Pattern.compile(emailRegex), "Wrong email address, eg. user@odourcollect.com" ) && valid;
        valid = checkRegexp( signup_password_textview.getText().toString(), Pattern.compile("^[0-9a-zA-Z]+$"), "Password field only allow : a-z 0-9") && valid;




        if (!error_check.equals("")){
            showError(error_check);
        }

        return valid;
    }

    public void openLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

}