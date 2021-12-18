package com.example.pm01examen3daneliapalacios;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
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

import com.example.pm01examen3daneliapalacios.configuraciones.SQLiteConexion;
import com.example.pm01examen3daneliapalacios.configuraciones.Transacciones;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ActivityMedicamentos extends AppCompatActivity {
    EditText descripcion, cantidad, periodicidad;
    Spinner tiempo;
    ImageView img;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_CAN = 101;
    String currentPhotoPath;
    Uri fotoUri;
    String opciones[] = {"Horas", "Diaria"};
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicamentos);

        descripcion = (EditText) findViewById(R.id.txtADescripcion);
        cantidad = (EditText) findViewById(R.id.txtACantidad);
        tiempo = (Spinner) findViewById(R.id.spTiempo);
        periodicidad = (EditText) findViewById(R.id.txtAPeriodicidad);
        img = (ImageView) findViewById(R.id.imageView);

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

        Button btnFoto = (Button) findViewById(R.id.btnATomarFoto);
        btnGuardar = (Button) findViewById(R.id.btnAGuardar);
        btnGuardar.setEnabled(false);

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fotoUri);
                    guardarImagen(descripcion.getText().toString(),
                            cantidad.getText().toString(),
                            tiempo.getSelectedItem().toString(),
                            periodicidad.getText().toString(),
                            bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                btnGuardar.setEnabled(true);
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

    private void guardarImagen(String Descripcion, String Cantidad, String Tiempo, String Periodicidad,Bitmap bitmap) {

        if(Descripcion.equals("")
            || Cantidad.equals("")
            || Tiempo.equals("")
            || Tiempo.equals("Horas") && Periodicidad.equals("")
        ){
            Toast.makeText(getApplicationContext(), "Complete todos los campos", Toast.LENGTH_LONG).show();
        }else {
            SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.NameDatabase, null, 1);
            SQLiteDatabase db = conexion.getWritableDatabase();

            ByteArrayOutputStream baos = new ByteArrayOutputStream(20480);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] blob = baos.toByteArray();
            byte[] imgInsert = imageComprimida(blob);

            String sql = "INSERT INTO Medicamentos (Descripcion,Cantidad, Tiempo, Periodicidad, Imagen) VALUES(?,?,?,?,?)";
            SQLiteStatement insert = db.compileStatement(sql);
            insert.clearBindings();
            insert.bindString(1, Descripcion);
            insert.bindString(2, Cantidad);
            insert.bindString(3, Tiempo);
            insert.bindString(4, Periodicidad);
            insert.bindBlob(5, imgInsert);
            Long resultado = insert.executeInsert();
            Toast.makeText(getApplicationContext(), "Medicamento Guardado : Id " + resultado.toString(), Toast.LENGTH_LONG).show();
            db.close();
            limpiar();
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

    private void limpiar(){
        img.setImageBitmap(null);
        descripcion.setText(null);
        cantidad.setText(null);
        periodicidad.setText(null);
        btnGuardar.setEnabled(false);
    }
}