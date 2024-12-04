package com.nteksystem.mobilepaymentterminal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class PaidAmount extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button btnfinish;
    private EditText etcash;
    private int vat;
    private String orno;
    private String cashier;
    private String status;
    private TextView tvbill;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_paid_amount);

        etcash = findViewById(R.id.etBillOnHand);
        btnfinish = findViewById(R.id.btnContinue3);
        gridLayout = findViewById(R.id.gridLayout);
        tvbill = findViewById(R.id.tvBillToPaid);

        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("access");
        String code = intent.getStringExtra("code");
        String gate = intent.getStringExtra("gate");
        String vclass = intent.getStringExtra("vclass");
        String etime = intent.getStringExtra("etime");
        String paytime = intent.getStringExtra("paytime");
        String ptime = intent.getStringExtra("ptime");
        String bill = intent.getStringExtra("bill");
        String paymode = intent.getStringExtra("paymode");

        String phpbill = "PHP " + bill;
        tvbill.setText(phpbill);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonClick((Button) v);
                    }
                });
            }
        }

        btnfinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPayNowButtonClick(v);
            }
        });
    }

    public void onButtonClick(Button button) {
        String buttonText = button.getText().toString();

        if (buttonText.equals("C")) {
            onClearButtonClick();
        } else {
            onNumericButtonClick(button);
        }
    }

    public void onClearButtonClick() {
        etcash.setText("");
    }

    public void onNumericButtonClick(Button button) {
        String buttonText = button.getText().toString();
        String currentText = etcash.getText().toString();
        etcash.setText(currentText + buttonText);
    }
    public void onPayNowButtonClick(View view) {
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("access");
        String code = intent.getStringExtra("code");
        String gate = intent.getStringExtra("gate");
        String vclass = intent.getStringExtra("vclass");
        String etime = intent.getStringExtra("etime");
        String paytime = intent.getStringExtra("paytime");
        String ptime = intent.getStringExtra("ptime");
        String bill = intent.getStringExtra("bill");
        String paymode = intent.getStringExtra("paymode");

        String amountText = etcash.getText().toString();
        if (!amountText.isEmpty() && isNumeric(amountText)) {
            double amount = Double.parseDouble(amountText);
            double billAmount = Double.parseDouble(bill);

            if (amount >= billAmount) {

                UpdateParkingTask updateParkingTask = new UpdateParkingTask();
                updateParkingTask.execute(status, id, access, code, gate, vclass, etime, paytime, ptime, bill, paymode);

            } else {
                showNotEnoughAmountDialog(PaidAmount.this);
            }
        } else {
            showNoAmountDialog(PaidAmount.this);
        }
    }

    private class UpdateParkingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String apiUrl = "http://10.0.0.130/parkingci/Handheld/terminalBillPaid";
//            String apiUrl = "http://192.168.1.171/parkingci/Handheld/terminalBillPaid";

            String status = params[0];
            String id = params[1];
            String access = params[2];
            String code = params[3];
            String gate = params[4];
            String vclass = params[5];
            String etime = params[6];
            String paytime = params[7];
            String ptime = params[8];
            String bill = params[9];
            String paymode = params[10];

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);

                // Create the POST data
                String postData = "status=" + status +
                        "&id=" + id +
                        "&access=" + access +
                        "&code=" + code +
                        "&gate=" + gate +
                        "&vclass=" + vclass +
                        "&etime=" + etime +
                        "&paytime=" + paytime +
                        "&ptime=" + ptime +
                        "&bill=" + bill +
                        "&paymode=" + paymode;

                OutputStream os = urlConnection.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // Get the response from the server
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                return result.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Handle the result as needed (e.g., display a message to the user)
            // Note: This method runs on the UI thread, so you can update UI elements here
            handleJsonData(result);
            if (status.equals("success")) {
                showSuccessDialog(PaidAmount.this);
            }
            checkBluetoothPermission();
        }

        private void handleJsonData(String jsonData) {
            try {
                JSONObject jsonObject = new JSONObject(jsonData);

                vat = jsonObject.getInt("vat");
                orno = jsonObject.getString("OR");
                cashier = jsonObject.getString("Cashier");
                status = jsonObject.getString("Status");

                if (status == "success") {
                    showSuccessDialog(PaidAmount.this);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }


    }

    private void showSuccessDialog(Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.payment_succes_dialog, null);
        builder.setView(dialogView);
        Intent intent = getIntent();
        String bill = intent.getStringExtra("bill");

        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        TextView dialogAmount = dialogView.findViewById(R.id.tvChangeAmount);
        Button okButton = dialogView.findViewById(R.id.okButton);
        dialogMessage.setText("Payment Paid!");
        String coh = etcash.getText().toString();
        int billam = Integer.parseInt(bill);
        int casham = Integer.parseInt(coh);

        int changeval =  casham - billam;

        String clientch = "PHP " + String.valueOf(changeval);

        dialogAmount.setText(clientch);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle OK button click if needed
                alertDialog.dismiss();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void showNoAmountDialog(Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.noamount_dialog, null);
        builder.setView(dialogView);
        Intent intent = getIntent();
        String bill = intent.getStringExtra("bill");

        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.okButton);
        dialogMessage.setText("Please enter valid amount!");
        String coh = etcash.getText().toString();


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showNotEnoughAmountDialog(Context context) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.notenough_amount, null);
        builder.setView(dialogView);
        Intent intent = getIntent();
        String bill = intent.getStringExtra("bill");

        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.okButton);
        dialogMessage.setText("Insufficient amount. Enter a valid amount");


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle OK button click if needed
                alertDialog.dismiss();
            }
        });
    }
    private void checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            // Bluetooth permissions are granted, proceed with printing
            performBluetoothPrint();
        } else {
            // Request Bluetooth permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    private void performBluetoothPrint() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(PaidAmount.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled, prompt the user to enable it
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth admin permission is granted, proceed with enabling Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    // Request Bluetooth admin permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_BLUETOOTH_PERMISSION);
                }
            } else {
                // Bluetooth is available and enabled, proceed with further operations
                printViaBluetooth();
            }
        }
    }

    private void printViaBluetooth() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
            // Bluetooth permissions are granted, proceed with printing
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            // Choose a paired device for printing, you may want to let the user select a device
            BluetoothDevice selectedDevice = null;
            for (BluetoothDevice device : pairedDevices) {
                selectedDevice = device;
                break; // For simplicity, we select the first paired device. Modify this as needed.
            }

            if (selectedDevice == null) {
                Toast.makeText(PaidAmount.this, "No paired Bluetooth device found", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothSocket socket = null;
            try {
                // Create a Bluetooth socket and connect to the device
                socket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();

                // Send data to the printer (replace this with your actual print data)
                OutputStream outputStream = socket.getOutputStream();
                String data = getReceiptData();
                outputStream.write(data.getBytes());

                Toast.makeText(PaidAmount.this, "Print successful", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Log.e("BluetoothPrint", "Error during printing", e);
                Toast.makeText(PaidAmount.this, "Error during printing", Toast.LENGTH_SHORT).show();

            } finally {
                try {
                    // Close the Bluetooth socket
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    Log.e("BluetoothPrint", "Error closing socket", e);
                }
            }
        } else {
            // Request Bluetooth permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH, android.Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_BLUETOOTH_PERMISSION);
        }
    }

    private String getReceiptData() {
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("access");
        String code = intent.getStringExtra("code");
        String gate = intent.getStringExtra("gate");
        String vclass = intent.getStringExtra("vclass");
        String etime = intent.getStringExtra("etime");
        String paytime = intent.getStringExtra("paytime");
        String ptime = intent.getStringExtra("ptime");
        String bill = intent.getStringExtra("bill");
        String paymode = intent.getStringExtra("paymode");

        String coh = etcash.getText().toString();
        int billam = Integer.parseInt(bill);
        int casham = Integer.parseInt(coh);

        int changeval =  casham - billam;

//        String clientch = "PHP " + String.valueOf(changeval);

        String parkingTime = ptime;
        String[] timeParts = parkingTime.split(":");
        int hours = Integer.parseInt(timeParts[0]);
        int minutes = Integer.parseInt(timeParts[1]);

        StringBuilder receiptData = new StringBuilder();

        String vehicle = "";
        switch (vclass){
            case "1":
                vehicle ="Motorcycle";
                break;
            case "2":
                vehicle ="Car";
                break;
            case "3":
                vehicle = "Bus/Truck";
                break;
            default:
                vehicle = "Unknown";
                break;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        int totalAmount = Integer.parseInt(bill);
        double vatableSales = totalAmount / 1.12; //for vatable sales
        double vat = (totalAmount / 1.12) * 0.12; //for vat

        receiptData.append("    Philippine International\n");
        receiptData.append("        Convention Center\n");
        receiptData.append("  Date: ").append(currentDate).append("\n");
        receiptData.append(" PICC Complex, 1307 Pasay City,\n");
        receiptData.append("         Metro Manila\n");
        receiptData.append("       (+63) 87894789\n\n");
        receiptData.append("\nVAT REG TIN: 001-114-766-00000\n");
        receiptData.append("         MIN: 234290423\n");
        receiptData.append("         OR NO: ").append(orno).append("\n");
        receiptData.append("     ").append(access).append(": ").append(code).append("\n");
        receiptData.append("      Vehicle Class: ").append(vehicle).append("\n\n");
        receiptData.append("\n            Receipt\n");
        receiptData.append("Cashier                 Handheld\n");
        receiptData.append("================================\n");
        receiptData.append("Gate In:     ").append(etime).append("\n");
        receiptData.append("Bill Time:   ").append(paytime).append("\n");
        receiptData.append("Parking Time: ").append(hours).append(" Hrs ").append(minutes).append(" Min)\n");
        receiptData.append("Amount Due:          PHP ").append(String.format("%.2f", vatableSales)).append("\n");
        receiptData.append("Vat (12%):           PHP ").append(String.format("%.2f", vat)).append("\n");
        receiptData.append("Cash:                PHP ").append(casham).append("\n");
        receiptData.append("Total Amount Due:    PHP ").append(bill).append("\n");
        receiptData.append("================================\n");
        receiptData.append("Change:              PHP ").append(changeval).append("\n");
        receiptData.append("================================\n");
        receiptData.append("Vatable Sales:       PHP ").append(String.format("%.2f", vatableSales)).append("\n");
        receiptData.append("Vat-Examp:           PHP 0.0\n");
        receiptData.append("Discount:            PHP 0.0\n");
        receiptData.append("Payment Mode:        ").append(paymode).append("\n");
        receiptData.append("================================\n");
        receiptData.append("  NTEKSYSTEMS Incorporation\n");
        receiptData.append("ACCREDITATION: 044778686889755\n");
        receiptData.append("    Valid Until: 12/12/2023\n");
        receiptData.append("BIR PTU NO: FP2782342-23476268\n");
        receiptData.append(" PTU DATE ISSUED: 11/24/2020\n");
        receiptData.append(" THIS SERVES AS OFFICIAL RECEIPT\n\n\n");
        receiptData.append("\n");

        return receiptData.toString();
    }


}