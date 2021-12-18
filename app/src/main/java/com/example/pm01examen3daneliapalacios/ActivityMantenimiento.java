package com.example.pm01examen3daneliapalacios;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.pm01examen3daneliapalacios.Tablas.Medicamentos;
import com.example.pm01examen3daneliapalacios.configuraciones.SQLiteConexion;
import com.example.pm01examen3daneliapalacios.configuraciones.Transacciones;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityMantenimiento extends AppCompatActivity {
    ImageView img;
    EditText id,descripcion,cantidad,periodicidad;
    Spinner tiempo;
    String opciones[] = {"Horas", "Diaria"};
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAN = 101;
    String currentPhotoPath;
    Uri fotoUri;
    SQLiteConexion conexion;
    Bitmap bitmap, bitmapUpdate;
    Button btnModificar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mantenimiento);

        conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);

        img = (ImageView) findViewById(R.id.imageView2);
        id = (EditText) findViewById(R.id.txtMId);
        descripcion = (EditText) findViewById(R.id.txtMDescripcion);
        cantidad = (EditText) findViewById(R.id.txtMCantidad);
        tiempo = (Spinner) findViewById(R.id.spMTiempo);
        periodicidad = (EditText) findViewById(R.id.txtMPeriodicidad);

        id.setEnabled(false);

        tiempo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(tiempo.getSelectedItem().toString().equals("Horas")){
                    periodicidad.setEnabled(true);
                }else{
                    periodicidad.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                periodicidad.setEnabled(false);
            }

        });

        ArrayAdapter<String> spinnerArrayAdapterTiempo = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opciones);
        spinnerArrayAdapterTiempo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tiempo.setAdapter(spinnerArrayAdapterTiempo);


        Medicamentos medicamentos = (Medicamentos) getIntent().getExtras().getSerializable("medicamento");

        byte[] blob =medicamentos.getImagen();
        ByteArrayInputStream bais = new ByteArrayInputStream(blob);
        bitmap = BitmapFactory.decodeStream(bais);

        img.setImageBitmap(bitmap);
        id.setText(medicamentos.getId_Medicamento().toString());
        descripcion.setText(medicamentos.getDescripcion().toString());
        cantidad.setText(medicamentos.getCantidad().toString());
        periodicidad.setText(medicamentos.getPeriodicidad().toString());

        Button btnTomarFoto = (Button) findViewById(R.id.btnMTomarFoto);
        btnModificar = (Button) findViewById(R.id.btnMModificar);
        Button btnEliminar = (Button) findViewById(R.id.btnMEliminar);

        btnModificar.setEnabled(false);

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });

        btnModificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    bitmapUpdate = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoUri);
                    Actualizar();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Eliminar();
            }
        });
    }

    private void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PETICION_ACCESO_CAN);
        }else{
            tomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int RequestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(RequestCode, permissions, grantResults);

        if (RequestCode == PETICION_ACCESO_CAN){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                tomarFoto();
            }
        }else {
            Toast.makeText(getApplicationContext(), "Se necesita el permiso de camara", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE  && resultCode == RESULT_OK) {

            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoUri);
                img.setImageBitmap(bitmap);
                btnModificar.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */);
        // Save a file: path for use with ACTION_VIEW
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void tomarFoto(){
        Intent Intenttakephoto= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Intenttakephoto.resolveActivity(getPackageManager()) != null){
            File foto = null;
            try {
                foto = createImageFile();
            }
            catch (Exception ex){
                ex.toString();
            }
            if (foto!= null){
                fotoUri = FileProvider.getUriForFile(this, "com.example.pm01examen3daneliapalacios.fileprovider",foto);
                Intenttakephoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(Intenttakephoto, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void Actualizar() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(20480);
        bitmapUpdate.compress(Bitmap.CompressFormat.PNG, 100 , baos);
        byte[] blob = baos.toByteArray();
        byte[] imgInsert = imageComprimida(blob);


        SQLiteDatabase db = conexion.getWritableDatabase();
        String[] params = {id.getText().toString()};

        ContentValues valores = new ContentValues();
        valores.put(Transacciones.Descripcion, descripcion.getText().toString());
        valores.put(Transacciones.Cantidad, cantidad.getText().toString());
        valores.put(Transacciones.Tiempo, tiempo.getSelectedItem().toString());
        valores.put(Transacciones.Periodicidad, periodicidad.getText().toString());
        valores.put(Transacciones.Periodicidad, periodicidad.getText().toString());
        valores.put(Transacciones.Imagen, imgInsert);


        if(id.getText().toString().isEmpty()||
                descripcion.getText().toString().isEmpty() ||
                cantidad.getText().toString().isEmpty() ||
                tiempo.getSelectedItem().toString().isEmpty() ||
                tiempo.getSelectedItem().toString().equals("Horas") && periodicidad.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(), "Debe completar todos los campos", Toast.LENGTH_LONG).show();
        }else{
            db.update(Transacciones.tablaMedicamentos, valores, Transacciones.Id_medicamento + "=?", params);
            Toast.makeText(getApplicationContext(), "Dato actualizado", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }
    }

    private byte[] imageComprimida(byte[] imagem_img){

        while (imagem_img.length > 500000){
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagem_img, 0, imagem_img.length);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*0.8), (int)(bitmap.getHeight()*0.8), true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imagem_img = stream.toByteArray();
        }
        return imagem_img;

    }

    private void Eliminar() {
        SQLiteDatabase db = conexion.getWritableDatabase();
        String [] params = {id.getText().toString()};
        String wherecond = Transacciones.Id_medicamento + "=?";
        if(!id.getText().toString().isEmpty()){
            db.delete(Transacciones.tablaMedicamentos, wherecond, params);
            Toast.makeText(getApplicationContext(), "Dato eliminado", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), "Campo de Id Vacio", Toast.LENGTH_LONG).show();
        }

    }
}