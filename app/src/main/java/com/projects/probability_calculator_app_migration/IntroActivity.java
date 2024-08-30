package com.projects.probability_calculator_app_migration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Button btnOP = findViewById(R.id.btnOP);

        btnOP.setOnClickListener(this);

        Button btnErl = findViewById(R.id.btnErl);

        btnErl.setOnClickListener(this);
//
//        Button btnCN = findViewById(R.id.btnCN);
//
//        btnCN.setOnClickListener(this);
//
        Button btnPoisson = findViewById(R.id.btnPoisson);

        btnPoisson.setOnClickListener(this);
//
//        Button btnBinomial = findViewById(R.id.btnBinomial);
//
//        btnBinomial.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        if (v.getId() == R.id.btnOP) {
            i = new Intent(this, OutageActivity.class);
            startActivity(i);
            //
//            case R.id.btnErl:
//                i = new Intent(this, ErlangActivity.class);
//                startActivity(i);
//                break;
//            case R.id.btnCN:
//                i = new Intent(this, CellularNetworksActivity.class);
//                startActivity(i);
//                break;
//            case R.id.btnPoisson:
//                i = new Intent(this, PoissonActivity.class);
//                startActivity(i);
//                break;
//            case R.id.btnBinomial:
//                i = new Intent(this, BinomialActivity.class);
//                startActivity(i);
//                break;
        }
        else if (v.getId() == R.id.btnPoisson) {
            i = new Intent(this, PoissonActivity.class);
            startActivity(i);
        }
        else if (v.getId() == R.id.btnErl) {
            i = new Intent(this, ErlangActivity.class);
            startActivity(i);
        }
    }
}
