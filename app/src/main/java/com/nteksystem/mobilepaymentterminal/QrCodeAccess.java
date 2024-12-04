package com.nteksystem.mobilepaymentterminal;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QrCodeAccess extends AppCompatActivity {
    private EditText etqrcode;
    private Button btnscan, btncontinue;
    private LinearLayout llprogress;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    showCamera();
                }
            });

    private void setResult(String contents){
        etqrcode.setText(contents);
    }

    private void showCamera(){
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scanOptions.setPrompt("Scan QR Code now");
        scanOptions.setCameraId(0);
        scanOptions.setBeepEnabled(false);
        scanOptions.setBarcodeImageEnabled(true);
        scanOptions.setOrientationLocked(false);

        qrCodeLauncher.launch(scanOptions);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_qr_code_access);

        btncontinue = findViewById(R.id.btnContinueScan);
        btnscan = findViewById(R.id.btnScannow);
        etqrcode = findViewById(R.id.etQrcode);
        llprogress = findViewById(R.id.linearProgressQr);

        btnscan.setOnClickListener(view -> {
            checkPermissionandShowActivity(this);
        });

        btncontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrcode = etqrcode.getText().toString();
                new HttpRequestTask().execute(qrcode);
            }
        });


    }
    private class HttpRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String code = params[0];
            String apiUrl = "http://10.0.0.130/parkingci/handheld/payment?access=QR&code=" + code;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                handleJsonData(result);
            } else {
                showWarningDialog(QrCodeAccess.this, "Failed to fetch data. Please try again.");
            }
        }
    }

    private ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result ->{
        if (result.getContents() == null){
            Toast.makeText(this, "Cancelled!", Toast.LENGTH_SHORT).show();
        }else {
            setResult(result.getContents());
        }
    });



    private void handleJsonData(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String status = jsonObject.getString("status");

            if (status.equals("fail")) {
                String message = jsonObject.getString("message");
                showWarningDialog(QrCodeAccess.this, message);
            } else if (status.equals("success")) {
                Intent intent = new Intent(getApplicationContext(), TransactionDetails.class);

                // Pass all required data to the next activity
                intent.putExtra("status", status);
                intent.putExtra("message", jsonObject.getString("message"));
                intent.putExtra("id", jsonObject.getString("id"));
                intent.putExtra("accessType", jsonObject.getString("accessType"));
                intent.putExtra("parkingCode", jsonObject.getString("parkingCode"));
                intent.putExtra("gateEntry", jsonObject.getString("gateEntry"));
                intent.putExtra("vehicleClass", jsonObject.getString("vehicleClass"));
                intent.putExtra("unixEntryTime", jsonObject.getString("unixEntryTime"));
                intent.putExtra("decodeEntryTime", jsonObject.getString("decodeEntryTime"));
                intent.putExtra("unixPaymentTime", jsonObject.getString("unixPaymentTime"));
                intent.putExtra("decodePaymentTime", jsonObject.getString("decodePaymentTime"));
                intent.putExtra("parkingTime", jsonObject.getString("parkingTime"));
                intent.putExtra("totalParkTime", jsonObject.getString("totalParkTime"));
                intent.putExtra("picturePath", jsonObject.getString("picturePath"));
                intent.putExtra("pictureName", jsonObject.getString("pictureName"));
                intent.putExtra("amount", jsonObject.getString("amount"));
                intent.putExtra("vehicleRate", jsonObject.getString("vehicleRate"));
                intent.putExtra("paymentStatus", jsonObject.getString("paymentStatus"));

                startActivity(intent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showWarningDialog(this, "Invalid data received. Please try again.");
        }
    }


    private void checkPermissionandShowActivity(Context context){
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
        )  == PackageManager.PERMISSION_GRANTED) {
            showCamera();
        }else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            Toast.makeText(context, "Camera permission is required!", Toast.LENGTH_SHORT).show();
        }else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

    }

    private void showWarningDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog, null);
        builder.setView(dialogView);

        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.okButton);
        dialogMessage.setText(message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}