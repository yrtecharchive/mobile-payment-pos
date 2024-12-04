package com.nteksystem.mobilepaymentterminal;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionDetails extends AppCompatActivity {
    private Button btncontinue;
    private TextView tvgate, tvaccess, tvcode, tventrytime, tvpaytime, tvvclass, tvbill, tvptime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_transaction_details);

        tvgate = findViewById(R.id.tvGate);
        tvaccess = findViewById(R.id.tvAccessType);
        tvcode = findViewById(R.id.tvAccessCode);
        tventrytime = findViewById(R.id.tvEntryTime);
        tvpaytime = findViewById(R.id.tvPayTime);
        tvvclass = findViewById(R.id.tvVehicleType);
        tvbill = findViewById(R.id.tvBillAmount);
        tvptime = findViewById(R.id.tvParkingTime);
        btncontinue = findViewById(R.id.btnContinueinTransactionDetails);

        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String access = intent.getStringExtra("accessType");
        String code = intent.getStringExtra("parkingCode");
        String gate = intent.getStringExtra("gateEntry");
        String vclass = intent.getStringExtra("vehicleClass");
        String etime = intent.getStringExtra("decodeEntryTime");
        String entrytime = intent.getStringExtra("unixEntryTime");
        String paymenttime = intent.getStringExtra("unixPaymentTime");
        String paytime = intent.getStringExtra("decodePaymentTime");
        String ptime = intent.getStringExtra("totalParkTime");
        String bill = intent.getStringExtra("amount");
        String vrate = intent.getStringExtra("vehicleRate");

        Toast.makeText(this, "Status: " + vrate, Toast.LENGTH_SHORT).show();
        // Format and display data
        String phpbill = "PHP " + bill;
        String vtype;
        switch (vclass) {
            case "1":
                vtype = "Motorcycle";
                break;
            case "2":
                vtype = "Car";
                break;
            case "3":
                vtype = "Bus/Truck";
                break;
            default:
                vtype = "Unknown";
                break;
        }

        // Format paytime to "Y-m-d H:i:s A"
        String formattedPaytime = formatPaytime(paytime);

        // Handle potential "Hour" and "Min" text in the parking time string
        int hours = 0;
        int minutes = 0;
        if (ptime != null && ptime.contains("Hour") && ptime.contains("Min")) {
            try {
                // Extract the numeric parts for hours and minutes
                hours = Integer.parseInt(ptime.split("Hour")[0].trim());
                minutes = Integer.parseInt(ptime.split("Hour")[1].replace("Min", "").trim());
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Handle invalid format gracefully
            }
        }

        String compresstime = hours + " Hrs " + minutes + " mins";
        tvgate.setText(gate);
        tvaccess.setText(access);
        tvcode.setText(code);
        tventrytime.setText(etime);
        tvpaytime.setText(paytime); // Display the formatted paytime
        tvvclass.setText(vtype);
        tvbill.setText(phpbill);
        tvptime.setText(compresstime);

        btncontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(TransactionDetails.this, "Payment Time: " + paytime, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), PaymentSolutions.class);
                i.putExtra("status", status);
                i.putExtra("id", intent.getStringExtra("id"));
                i.putExtra("accessType", access);
                i.putExtra("parkingCode", code);
                i.putExtra("gateEntry", gate);
                i.putExtra("vehicleClass", vclass);
                i.putExtra("unixEntryTime", entrytime);
                i.putExtra("unixPaymentTime", paymenttime);
                i.putExtra("decodePaymentTime", paymenttime);
                i.putExtra("vehicleRate", vrate);
                i.putExtra("totalParkTime", ptime);
                i.putExtra("amount", bill);
                startActivity(i);
                finish();
            }
        });
    }

    // Method to format the paytime to "Y-m-d H:i:s A"
    private String formatPaytime(String paytime) {
        try {
            long timestamp = Long.parseLong(paytime);  // Assuming paytime is a Unix timestamp in seconds
            Date date = new Date(timestamp * 1000);  // Multiply by 1000 to convert to milliseconds

            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            return newFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return paytime; // Return original paytime if there's an error
        }
    }
}
