package com.ibercivis.stopamianto.clases;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ibercivis.stopamianto.R;
import com.ibercivis.stopamianto.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptadorObservaciones extends RecyclerView.Adapter<ViewHolderObservaciones>{

    List<Observacion> ListaObjeto;
    LinearLayout marco_visualizar;
    ImageView fotoView;
    LinearLayout cargando;
    Context context;

    public AdaptadorObservaciones(List<Observacion> listaObjeto, LinearLayout marco_visualizar, ImageView fotoView, LinearLayout cargando,
                          Context context
    ) {
        ListaObjeto = listaObjeto;
        this.marco_visualizar = marco_visualizar;
        this.fotoView = fotoView;
        this.cargando = cargando;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolderObservaciones onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.observacion_card, parent, false);
        return new ViewHolderObservaciones(vista, ListaObjeto);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderObservaciones holder, int position) {

        Log.d("AdaptadorObservaciones", "OnBindViewHolder");
        holder.build.setText(ListaObjeto.get(position).getBuild());
        holder.quantity.setText(ListaObjeto.get(position).getQuantity());
        String photo_0 = ListaObjeto.get(position).getFoto();
        Integer id = ListaObjeto.get(position).getId();
        if(ListaObjeto.get(position).getHasPhoto() == 1) {
            Picasso.with(holder.photo_0.getContext()).load(photo_0).into(holder.photo_0);
            holder.photo_0.setClipToOutline(true);
        }
        holder.info.setText(ListaObjeto.get(position).getInfo());
        holder.date.setText(ListaObjeto.get(position).getDate());

        holder.deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.zonaA.setVisibility(View.GONE);
                holder.zonaB.setVisibility(View.VISIBLE);
            }
        });

        holder.delete_volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.zonaA.setVisibility(View.VISIBLE);
                holder.zonaB.setVisibility(View.GONE);
            }
        });

        holder.delete_eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMarkerRequest(id, cargando, ListaObjeto, holder.getAdapterPosition());
            }
        });


        if(ListaObjeto.get(position).getHasPhoto() == 1) {
            holder.photo_0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (photo_0 != null) {
                        marco_visualizar.setVisibility(View.VISIBLE);
                        Picasso.with(fotoView.getContext()).load(photo_0).into(fotoView);
                    }
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        return ListaObjeto.size();
    }

    public void setFilter(ArrayList<Observacion> listaFiltrada){

        this.ListaObjeto = new ArrayList<>();
        this.ListaObjeto.addAll(listaFiltrada);
        notifyDataSetChanged();

    }

    public void deleteMarkerRequest(Integer id, LinearLayout cargar, List<Observacion> ListaObjeto, int indice) {



        cargar.setVisibility(View.VISIBLE);


        // Url for the webservice
        String url = "https://interfungi.ibercivis.es/deleteMarcador.php";

        RequestQueue queue = Volley.newRequestQueue(cargar.getContext());
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

                        text = "El marcador ha sido eliminado";
                        toast = Toast.makeText(cargar.getContext(), text, duration);
                        toast.show();

                        cargar.setVisibility(View.GONE);

                        ListaObjeto.remove(indice);
                        Intent intent3 = new Intent(cargar.getContext(), User.class);
                        cargar.getContext().startActivity(intent3);


                    } else {
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast;
                        CharSequence text;

                        text = "Algo ha ocurrido. Inténtalo más tarde.";
                        toast = Toast.makeText(cargar.getContext(), text, duration);
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
                toast = Toast.makeText(cargar.getContext(), text, duration);
                toast.show();
                cargar.setVisibility(View.GONE);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> login_params = new HashMap<String, String>();

                SessionManager session = new SessionManager(cargar.getContext());
                login_params.put("idUser", String.valueOf(session.getIdUser()));
                login_params.put("token", String.valueOf(session.getToken()));
                login_params.put("id", String.valueOf(id));


                return login_params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(sr);
    }

}



