package com.nteksystem.mobilepaymentterminal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentSolutions extends AppCompatActivity {
    private RadioGroup rgpaymentmode;
    private Button btncontinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_solutions);

        rgpaymentmode = findViewById(R.id.rgModeSelector);
        btncontinue = findViewById(R.id.btnContinue2);

        // Receive data from previous activity
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        String id = intent.getStringExtra("id");
        String access = intent.getStringExtra("accessType");
        String code = intent.getStringExtra("parkingCode");
        String gate = intent.getStringExtra("gateEntry");
        String vclass = intent.getStringExtra("vehicleClass");
        String etime = intent.getStringExtra("unixEntryTime");
        String billtime = intent.getStringExtra("unixPaymentTime");
        String ptime = intent.getStringExtra("totalParkTime");
        String bill = intent.getStringExtra("amount");
        String vrate = intent.getStringExtra("vehicleRate");
        String paymode = intent.getStringExtra("amount");

        rgpaymentmode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedRadioBtn = findViewById(checkedId);
                if (selectedRadioBtn != null && selectedRadioBtn.isChecked()) {
                    String mop = selectedRadioBtn.getText().toString();
                    if ("GCash".equals(mop) || "PayMaya".equals(mop)) {
                        showWarningDialog(PaymentSolutions.this, mop);
                    }else if("Cash".equals(mop)){
                        Toast.makeText(PaymentSolutions.this, "Paymode: " + mop, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btncontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected payment mode
                RadioButton selectedRadioButton = findViewById(rgpaymentmode.getCheckedRadioButtonId());
                String selectedMode = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";


                if ("Cash".equals(selectedMode)) {

                    Toast.makeText(PaymentSolutions.this, "Payment Time: " + billtime, Toast.LENGTH_SHORT).show();
                    Intent c = new Intent(getApplicationContext(), CashPayment.class);
                    c.putExtra("status", status);
                    c.putExtra("id", intent.getStringExtra("id"));
                    c.putExtra("accessType", access);
                    c.putExtra("parkingCode", code);
                    c.putExtra("gateEntry", gate);
                    c.putExtra("vehicleClass", vclass);
                    c.putExtra("unixEntryTime", etime);
                    c.putExtra("decodeEntryTime", etime);
                    c.putExtra("paytime", billtime);
                    c.putExtra("totalParkTime", ptime);
                    c.putExtra("amount", bill);
                    c.putExtra("vehicleRate", vrate);
                    c.putExtra("paymode", selectedMode);
                    startActivity(c);
                    finish();
                } else {
                    Intent g = new Intent(getApplicationContext(), CashPayment.class);
                    g.putExtra("status", status);
                    g.putExtra("id", intent.getStringExtra("id"));
                    g.putExtra("accessType", access);
                    g.putExtra("parkingCode", code);
                    g.putExtra("gateEntry", gate);
                    g.putExtra("vehicleClass", vclass);
                    g.putExtra("unixEntryTime", etime);
                    g.putExtra("decodeEntryTime", etime);
                    g.putExtra("paytime", billtime);
                    g.putExtra("totalParkTime", ptime);
                    g.putExtra("amount", bill);
                    startActivity(g);
                    finish();
                }
            }
        });
    }

    private void showWarningDialog(Context context, String selectedMode) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.warning_dialog, null);
        builder.setView(dialogView);

        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button okButton = dialogView.findViewById(R.id.okButton);

        dialogMessage.setText(selectedMode + " is not yet available at the moment!");

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
}
