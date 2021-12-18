package com.example.pm01examen3daneliapalacios;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.lights.LightState;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.pm01examen3daneliapalacios.Tablas.Medicamentos;
import com.example.pm01examen3daneliapalacios.configuraciones.SQLiteConexion;
import com.example.pm01examen3daneliapalacios.configuraciones.Transacciones;

import java.io.Serializable;
import java.util.ArrayList;

public class ActivityLista extends AppCompatActivity {
    ListView lvListMedicamentos;
    ArrayList<Medicamentos> lista;
    SQLiteConexion conexion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
        lvListMedicamentos = (ListView) findViewById(R.id.lvListMedicamentos);
        lista = new ArrayList<Medicamentos>();

        obtenerMedicamentos();

        Adaptador adaptador = new Adaptador(getApplicationContext(),lista);
        lvListMedicamentos.setAdapter(adaptador);

        lvListMedicamentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Medicamentos medicamentos = (Medicamentos) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(getApplicationContext(), ActivityMantenimiento.class);
                intent.putExtra("medicamento", (Serializable) medicamentos);
                startActivity(intent);
            }
        });

    }

    private void obtenerMedicamentos() {
        SQLiteDatabase db = conexion.getReadableDatabase();
        Medicamentos medicamentos = null;
        //lista = new ArrayList<Medicamentos>();

        //cursor de  bd: nos apoya a recorrer la informacion de la tabla a la cual consltamos
        Cursor cursor = db.rawQuery("SELECT * FROM " + Transacciones.tablaMedicamentos , null);

        //recorrer la informacion del cursor
        while (cursor.moveToNext()){
            medicamentos = new Medicamentos();
            medicamentos.setId_Medicamento(cursor.getInt( 0));
            medicamentos.setDescripcion(cursor.getString( 1));
            medicamentos.setCantidad(cursor.getInt(2));
            medicamentos.setTiempo(cursor.getString( 3));
            medicamentos.setPeriodicidad(cursor.getInt( 4));
            medicamentos.setImagen(cursor.getBlob( 5));

            lista.add(medicamentos);
        }

        cursor.close();

    }
}