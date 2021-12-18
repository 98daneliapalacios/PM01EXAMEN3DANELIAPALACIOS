package com.example.pm01examen3daneliapalacios;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pm01examen3daneliapalacios.Tablas.Medicamentos;

import java.io.ByteArrayInputStream;
import java.util.List;

public class Adaptador extends BaseAdapter {

    Context context;
    List<Medicamentos> lstMedicamentos;

    public Adaptador(Context context, List<Medicamentos> lstMedicamentos) {
        this.context = context;
        this.lstMedicamentos = lstMedicamentos;
    }

    @Override
    public int getCount() {
        return lstMedicamentos.size();
    }

    @Override
    public Object getItem(int i) {
        return lstMedicamentos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return lstMedicamentos.get(i).getId_Medicamento();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vista = view;
        LayoutInflater inflater = LayoutInflater.from(context);

        vista = inflater.inflate(R.layout.itemlistview,null);

        ImageView img = (ImageView) vista.findViewById(R.id.imgviewItem);
        TextView descripcion = (TextView) vista.findViewById(R.id.txtItemDescripcion);
        TextView cantidad = (TextView) vista.findViewById(R.id.txtItemCantidad);
        TextView tiempo = (TextView) vista.findViewById(R.id.txtItemTiempo);
        TextView periodicidad = (TextView) vista.findViewById(R.id.txtItemPeriodicidad);

        byte[] blob =lstMedicamentos.get(i).getImagen();
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        Bitmap bitmap = BitmapFactory.decodeStream(bais);

        img.setImageBitmap(bitmap);
        descripcion.setText(lstMedicamentos.get(i).getDescripcion().toString());
        cantidad.setText(lstMedicamentos.get(i).getCantidad().toString());
        tiempo.setText(lstMedicamentos.get(i).getTiempo().toString());
        periodicidad.setText(lstMedicamentos.get(i).getPeriodicidad().toString());

        return vista;
    }
}
