package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends AppCompatActivity {

    // Setup Server information
    protected static String server = "10.0.2.2";
    protected static int port = 7070;
    CheckBox checkBoxCamas, checkBoxMesas, checkBoxSabanas, checkBoxSillas, checkBoxSillones;
    EditText etCamas, etMesas, etSabanas, etSillas, etSillones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

        // Llama al listener del boton Enviar
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        checkBoxSabanas = findViewById(R.id.checkBox_sabanas);
        etSabanas = findViewById(R.id.et_sabanas);
        checkBoxSabanas.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etSabanas.setEnabled(isChecked);
            }
        });

        checkBoxCamas = findViewById(R.id.checkBox_camas);
        etCamas = findViewById(R.id.et_camas);

        checkBoxCamas.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etCamas.setEnabled(isChecked);
            }
        });

        checkBoxMesas = findViewById(R.id.checkBox_mesas);
        etMesas = findViewById(R.id.et_mesas);

        checkBoxMesas.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etMesas.setEnabled(isChecked);
            }
        });

        checkBoxSillas = findViewById(R.id.checkBox_sillas);
        etSillas = findViewById(R.id.et_sillas);
        checkBoxSillas.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etSillas.setEnabled(isChecked);
            }
        });
        checkBoxSillones = findViewById(R.id.checkBox_sillones);
        etSillones = findViewById(R.id.et_sillones);
        checkBoxSillones.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etSillones.setEnabled(isChecked);
            }
        });
    }
    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog() throws Resources.NotFoundException {
        if (!checkBoxCamas.isChecked() && !checkBoxMesas.isChecked() && !checkBoxSabanas.isChecked() && !checkBoxSillas.isChecked() && !checkBoxSillones.isChecked()) {
            // Mostramos un mensaje emergente;
            Toast.makeText(getApplicationContext(), "Selecciona al menos un elemento", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Enviar")
                    .setMessage("Se va a proceder al envio")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                // Catch ok button and send information
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // 1. Extraer los datos de la vista y crear un objeto JSON
                                    JSONObject data = extractDataFromView();

                                    // 2. Firmar los datos (si es necesario)

                                    // 3. Enviar los datos al servidor
                                    sendDataToServer(server, port, data.toString());

                                    Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                                }
                            }

                    )
                    .

                            setNegativeButton(android.R.string.no, null)

                    .

                            show();
        }
    }

    private JSONObject extractDataFromView() {
        JSONObject jsonObject = new JSONObject();

        try {
            if (checkBoxCamas.isChecked()) {
                String camasQuantity = etCamas.getText().toString();
                jsonObject.put("camas", camasQuantity);
            }

            if (checkBoxMesas.isChecked()) {
                String mesasQuantity = etMesas.getText().toString();
                jsonObject.put("mesas", mesasQuantity);
            }

            if (checkBoxSabanas.isChecked()) {
                String sabanasQuantity = etSabanas.getText().toString();
                jsonObject.put("sabanas", sabanasQuantity);
            }

            if (checkBoxSillas.isChecked()) {
                String sillasQuantity = etSillas.getText().toString();
                jsonObject.put("sillas", sillasQuantity);
            }

            if (checkBoxSillones.isChecked()) {
                String sillonesQuantity = etSillones.getText().toString();
                jsonObject.put("sillones", sillonesQuantity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
    private void sendDataToServer(String server, int port, String data) {
        new SendDataTask(this).execute(server, String.valueOf(port), data);
    }

    private static class SendDataTask extends AsyncTask<String, Void, Void> {
        private final WeakReference<MainActivity> activityReference;

        SendDataTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... params) {
            String server = params[0];
            int port = Integer.parseInt(params[1]);
            String data = params[2];
            MainActivity activity = activityReference.get();

            if (activity == null || activity.isFinishing()) {
                return null;
            }

            try {
                // Cargar el truststore desde la carpeta de recursos
                KeyStore truststore = KeyStore.getInstance("BKS");
                InputStream truststoreFile = activity.getResources().openRawResource(R.raw.truststore);
                truststore.load(truststoreFile, "password".toCharArray());

                // Crear un SSLContext y un SSLSocketFactory a partir del truststore cargado
                SSLContext sslContext = SSLContext.getInstance("TLS");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(truststore);
                sslContext.init(null, tmf.getTrustManagers(), null);
                SSLSocketFactory factory = sslContext.getSocketFactory();

                // Crear un SSLSocket para conectarse al servidor
                SSLSocket socket = (SSLSocket) factory.createSocket(server, port);

                // Escribir datos en el SSLSocket
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(data);
                out.flush();

                // Leer la respuesta del servidor
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                if (response != null) {
                    // Manejar la respuesta del servidor
                    System.out.println("Respuesta del servidor: " + response);
                }

                // Cerrar recursos
                out.close();
                in.close();
                socket.close();
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException |
                     KeyStoreException | CertificateException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}