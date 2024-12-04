package com.nteksystem.mobilepaymentterminal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CashPayment extends AppCompatActivity {
    private TextView tvAmountToPay;
    private EditText etCashReceived;
    private Button btnContinue;
    private int amountToPay;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String PRINTER_NAME = "IposPrinter"; // Replace with your Bluetooth printer's name

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cash_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Receive the data passed from PaymentSolutions
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("accessType");
        String code = intent.getStringExtra("parkingCode");
        String gate = intent.getStringExtra("gateEntry");
        String vclass = intent.getStringExtra("vehicleClass");
        String etime = intent.getStringExtra("unixEntryTime");
        String paytime = intent.getStringExtra("paytime");
        String ptime = intent.getStringExtra("totalParkTime");
        String bill = intent.getStringExtra("amount");

        tvAmountToPay = findViewById(R.id.tvAmountToPay);
        etCashReceived = findViewById(R.id.etCashReceived);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateCashReceived();
            }
        });
    }

    private void validateCashReceived() {
        String cashReceivedInput = etCashReceived.getText().toString().trim();

        if (cashReceivedInput.isEmpty()) {
            Toast.makeText(this, "Please enter cash received", Toast.LENGTH_SHORT).show();
            return;
        }

        double cashReceived = Double.parseDouble(cashReceivedInput);

        if (cashReceived < amountToPay) {
            Toast.makeText(this, "Cash received is insufficient", Toast.LENGTH_SHORT).show();
        } else {
//            sendTransactionToServer(cashReceived);
            Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
            connectToBluetoothPrinter();
        }
    }

    private void connectToBluetoothPrinter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice printerDevice = findPairedPrinter();
        if (printerDevice == null) {
            Toast.makeText(this, "Printer not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            bluetoothSocket = printerDevice.createRfcommSocketToServiceRecord(PRINTER_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            printReceipt();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not connect to printer", Toast.LENGTH_SHORT).show();
        }
    }

    private BluetoothDevice findPairedPrinter() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(PRINTER_NAME)) {
                return device;
            }
        }
        return null;
    }

    private void printReceipt() {
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("accessType");
        String code = intent.getStringExtra("parkingCode");
        String gate = intent.getStringExtra("gateEntry");
        String vclass = intent.getStringExtra("vehicleClass");
        String etime = intent.getStringExtra("unixEntryTime");
        String paytime = intent.getStringExtra("paytime");
        String ptime = intent.getStringExtra("totalParkTime");
        String bill = intent.getStringExtra("amount");
        String vehicle = vclass;
        String vcat = "";

        // Correcting string comparison to use .equals() for proper comparison
        if (vehicle.equals("1")) {
            vcat = "Motorcycle";
        } else if (vehicle.equals("2")) {
            vcat = "Car";
        } else if (vehicle.equals("3")) {
            vcat = "BUS/Truck";
        } else {
            vcat = "Unknown";
        }
        try {
            StringBuilder receipt = new StringBuilder();


            // Header (Center Aligned)
            receipt.append("               PICC\n");
            receipt.append("    Philippine International\n         Convention Center\n");
            receipt.append(" PICC Complex 1307, Pasay City,\n    Metro Manila Philippines\n");
            receipt.append("  VAT REG TIN: 544-656-656-66166\n");
            receipt.append("          MIN: 36464646898\n");
            receipt.append("          SN: 543646415615\n");
            receipt.append("           (+63)93699475\n");
            receipt.append("\n        TRAINING MODE\n\n\n");
            receipt.append("Date and Time: 2024-11-06 09:11:11\n");
            receipt.append("          S/I: 00-000026\n");
            receipt.append("          " + access +": " + code +"\n");
            receipt.append("          Vehicle: "+ vcat +"\n");
            receipt.append("\n\n          Sales Invoive\n");
            receipt.append("-------------------------------\n");

            // Cashier Name and Username (Left and Right Aligned)
            String cashierName = "Cashier:";
            String username = "John Doe";
            String terminalName = "Terminal";
            String terminalCode = "004";
            int padding = 32 - cashierName.length() - username.length();
            int padd = 37 - cashierName.length() - username.length();
            receipt.append(cashierName).append(" ".repeat(padding)).append(username).append("\n");
            receipt.append(terminalName).append(" ".repeat(padd)).append(terminalCode).append("\n");
            receipt.append("-------------------------------\n");

            // Hardcoded Transaction Details with Labels (Left and Right Aligned)
            String[] labels = {
                    "Gate In",
                    "Billing Time",
                    "Parking Time",
                    "Total Sales",
                    "Vat(12%)",
                    "Total Amount Due"
            };

            String[] values = {
                    etime,
                    paytime,
                    ptime,
                    "53.57",
                    "6.42",
                    bill
            };

            for (int i = 0; i < labels.length; i++) {
                String label = labels[i];
                String value = values[i];
                padding = 32 - label.length() - value.length();
                receipt.append(label).append(" ".repeat(padding)).append(value).append("\n");
            }
            receipt.append("-------------------------------\n");
            String cashReceivedLabel = "Cash Received";
            String cashReceivedValue = etCashReceived.getText().toString();
            String cashChangeLabel = "Cash Change";
            String cashChangeValue = "0.00";
            padding = 32 - cashReceivedLabel.length() - cashReceivedValue.length();
            receipt.append(cashReceivedLabel).append(" ".repeat(padding)).append(cashReceivedValue).append("\n");
            receipt.append(cashChangeLabel).append(" ".repeat(padding)).append(cashChangeValue).append("\n");
            // Divider before additional sales and tax details
            receipt.append("-------------------------------\n");

            // Additional Sales and Tax Details with Labels (Left and Right Aligned)
            String[] additionalLabels = {
                    "Vatable Sales",
                    "Non-Vat Sales",
                    "Vat-Exempt",
                    "Zero Rated Sales",
                    "Discount",
                    "Payment Mode"
            };

            String[] additionalValues = {
                    "53.57",
                    "0.00",
                    "0.00",
                    "0.00",
                    "0.00",
                    "Cash"
            };

            for (int i = 0; i < additionalLabels.length; i++) {
                String label = additionalLabels[i];
                String value = additionalValues[i];
                padding = 32 - label.length() - value.length();
                receipt.append(label).append(" ".repeat(padding)).append(value).append("\n");
            }

            // Footer
            receipt.append("-------------------------------\n");
            receipt.append("BIR PTU NO: ABC1234567-12345678!\n");
            receipt.append("PTU DATE ISSUED: 11/25/2020\n");
            receipt.append("THIS SERVES AS YOUR SALES INVOICE\n");
            receipt.append("\n\n\n");  // Feed extra lines for printing

            // Send data to the printer
            outputStream.write(receipt.toString().getBytes());
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to print receipt", Toast.LENGTH_SHORT).show();
        } finally {
            closeBluetoothConnection();
        }
    }


    private void closeBluetoothConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendTransactionToServer(double cashReceived) {
        Intent intent = getIntent();
        String terminalId = intent.getStringExtra("id");
        String accessType = intent.getStringExtra("accessType");
        String parkingCode = intent.getStringExtra("parkingCode");
        String gateEntry = intent.getStringExtra("gateEntry");
        String vehicleClass = intent.getStringExtra("vehicleClass");
        String entryTime = intent.getStringExtra("unixEntryTime");
        String payTime = intent.getStringExtra("paytime");
        String totalTime = intent.getStringExtra("totalParkTime");
        String amount = intent.getStringExtra("amount");

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://10.0.0.130/parkingci/handheld/CreateBillTransaction");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("terminalid", terminalId);
                    jsonObject.put("access", accessType);
                    jsonObject.put("code", parkingCode);
                    jsonObject.put("gate", gateEntry);
                    jsonObject.put("vclass", vehicleClass);
                    jsonObject.put("etime", entryTime);
                    jsonObject.put("paytime", payTime);
                    jsonObject.put("totaltime", totalTime);
                    jsonObject.put("amount", amount);
                    jsonObject.put("cashReceived", cashReceived);
                    jsonObject.put("paymode", "Cash");


                    // Send JSON data
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.close();

                    // Read the response
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = br.lines().collect(Collectors.joining("\n"));
                    br.close();

                    return response;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        if ("Approved".equalsIgnoreCase(status)) {
                            Toast.makeText(CashPayment.this, "Payment successful", Toast.LENGTH_SHORT).show();
                            connectToBluetoothPrinter();
                        } else {
                            Toast.makeText(CashPayment.this, "Payment failed: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(CashPayment.this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CashPayment.this, "Failed to connect to server", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

}

