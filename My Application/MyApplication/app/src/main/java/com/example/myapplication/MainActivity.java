package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import android.util.Base64;

public class MainActivity extends AppCompatActivity {

    // Setup Server information
    String server;
    int port;
    CheckBox checkBoxCamas, checkBoxMesas, checkBoxSabanas, checkBoxSillas, checkBoxSillones;
    EditText etCamas, etMesas, etSabanas, etSillas, etSillones;
    String keystorePassword;
    String privateKeyAlias;
    String privateKeyPassword;
    String truststorePassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = getResources().getString(R.string.server);
        port = getResources().getInteger(R.integer.port);
        keystorePassword = getResources().getString(R.string.keystore_password);
        privateKeyAlias = getResources().getString(R.string.private_key_alias);
        privateKeyPassword = getResources().getString(R.string.private_key_password);
        truststorePassword = getResources().getString(R.string.truststore_password);
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
        InputFilter valueRangeFilter = new InputFilter() {



            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                try {
                    // Intenta convertir el nuevo texto a un número entero
                    int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length()).toString());
                    // Verifica si el número está dentro del rango permitido
                    if (input >= 0 && input <= 300) {
                        // El valor está dentro del rango permitido, se acepta
                        return null;
                    } else {
                        // El valor está fuera del rango permitido, lanza una excepción
                        throw new IllegalArgumentException("El valor debe estar entre 0 y 300");
                    }
                } catch (NumberFormatException ignored) {
                    // Ignorar excepciones y no permitir la entrada si no es un número válido
                } catch (IllegalArgumentException e) {
                    // Capturar la excepción y mostrar un mensaje de error
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                // El valor está fuera del rango permitido, se rechaza
                return "";
            }
        };

// Aplica el validador a los EditText correspondientes
        etCamas.setFilters(new InputFilter[] { valueRangeFilter });
        etMesas.setFilters(new InputFilter[] { valueRangeFilter });
        etSabanas.setFilters(new InputFilter[] { valueRangeFilter });
        etSillas.setFilters(new InputFilter[] { valueRangeFilter });
        etSillones.setFilters(new InputFilter[] { valueRangeFilter });
    }

    // Creación de un cuadro de diálogo para confirmar pedido
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
            // Firmar la solicitud y agregar la firma al objeto JSON
            String signature = signRequest(jsonObject.toString());
            if (signature != null) {
                jsonObject.put("signature", signature);
            } else {
                throw new Exception("Error al firmar la solicitud");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private String signRequest(String requestData) {
        try {


            // Cargar el keystore desde la carpeta de recursos
            KeyStore keystore = KeyStore.getInstance("BKS");
            InputStream keystoreFile = getResources().openRawResource(R.raw.client_keystore);
            keystore.load(keystoreFile, keystorePassword.toCharArray());

            // Obtener la clave privada del keystore
            String alias = privateKeyAlias;
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(alias,
                    new KeyStore.PasswordProtection(privateKeyPassword.toCharArray()));
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            // Crear una instancia del objeto Signature y usar la clave privada para firmar
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(requestData.getBytes("UTF-8"));

            // Firmar la solicitud y codificar en Base64
            byte[] signedData = signature.sign();
            return Base64.encodeToString(signedData, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendDataToServer(String server, int port, String data) {
        new SendDataTask(this, truststorePassword).execute(server, String.valueOf(port), data);
    }

    private static class SendDataTask extends AsyncTask<String, Void, String> {
        private final WeakReference<MainActivity> activityReference;
        private final String truststorePassword;

        SendDataTask(MainActivity context, String truststorePassword) {
            activityReference = new WeakReference<>(context);
            this.truststorePassword = truststorePassword;
        }

        protected String doInBackground(String... params) {

            String server = params[0];
            int port = Integer.parseInt(params[1]);
            String data = params[2];
            MainActivity activity = activityReference.get();

            if (activity == null || activity.isFinishing()) {
                return null;
            }

            String response = null;
            try {
                // Cargar el truststore desde la carpeta de recursos
                KeyStore truststore = KeyStore.getInstance("BKS");
                InputStream truststoreFile = activity.getResources().openRawResource(R.raw.client_truststore);
                truststore.load(truststoreFile, this.truststorePassword.toCharArray());

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
                response = in.readLine();
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
            return response;
        }
        @Override
        protected void onPostExecute(String response) {
            MainActivity activity = activityReference.get();
            if (activity != null && !activity.isFinishing() && response != null) {
                Toast.makeText(activity, "Respuesta del servidor: " + response, Toast.LENGTH_SHORT).show();
            }
        }
    }

}