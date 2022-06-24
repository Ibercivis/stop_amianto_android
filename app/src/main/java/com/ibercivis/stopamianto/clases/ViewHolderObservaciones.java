package com.ibercivis.stopamianto.clases;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.ibercivis.stopamianto.R;

import java.util.List;

public class ViewHolderObservaciones extends RecyclerView.ViewHolder implements View.OnClickListener{

    TextView build;
    TextView quantity;


    TextView buttonViewOptions;

    CardView card;

    ImageView photo_0;

    List<Observacion> ListaSetas;
    Button deletebutton;
    Button delete_eliminar;
    Button delete_volver;

    TextView info, date;
    LinearLayout zonaA;
    LinearLayout zonaB;

    public ViewHolderObservaciones(@NonNull View itemView, List<Observacion> datos) {
        super(itemView);

        build = itemView.findViewById(R.id.titulo);
        quantity = itemView.findViewById(R.id.titulo2);
        ListaSetas = datos;
        card = itemView.findViewById(R.id.marcador_card);
        photo_0= itemView.findViewById(R.id.photo_0);

        deletebutton = itemView.findViewById(R.id.delete_btn);
        delete_eliminar = itemView.findViewById(R.id.eliminar_confirmar);
        delete_volver = itemView.findViewById(R.id.eliminar_volver);
        zonaA = itemView.findViewById(R.id.zona_A);
        zonaB = itemView.findViewById(R.id.zona_B);
        info = itemView.findViewById(R.id.dato_agusanamiento);
        date = itemView.findViewById(R.id.dato_presion);

    }

    @Override
    public void onClick(View v) {
        int position = getAdapterPosition();
        final Observacion marcador = ListaSetas.get(position);


        if(v.getId() == buttonViewOptions.getId()){

           /* PopupMenu popup = new PopupMenu(buttonViewOptions.getContext(), buttonViewOptions);
            //inflating menu from xml resource
            popup.inflate(R.menu.menu_card);
            //adding click listener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu1:
                            //handle menu1 click
                            int id = marcador.getId();
                            Intent intent = new Intent(buttonViewOptions.getContext(), DescargarDatos.class);
                            intent.putExtra("idProyecto", id);
                            startActivity(btn.getContext(), intent, null);
                            break;
                        case R.id.menu2:
                            //handle menu1 click
                            int id2 = marcador.getId();
                            Intent intent2 = new Intent(buttonViewOptions.getContext(), BorrarProyecto.class);
                            intent2.putExtra("idProyecto", id2);
                            startActivity(btn.getContext(), intent2, null);
                            break;
                        case R.id.menu3:
                            //handle menu1 click
                            int id3 = marcador.getId();
                            String tituloSeta = "titulo";
                            String url_photo_0 = marcador.getPhoto0();
                            Intent intent3 = new Intent(buttonViewOptions.getContext(), EditarProyecto.class);
                            intent3.putExtra("idProyecto", id3);
                            intent3.putExtra("urlLogo", url_photo_0);
                            startActivity(btn.getContext(), intent3, null);
                            break;
                    }
                    return false;
                }
            });
            SessionManager session = new SessionManager(buttonViewOptions.getContext());
            popup.show();*/

        }
    }
}
