package com.projects.probability_calculator_app_migration;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Locale;

public class PoissonActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {
    private BarChart chPoisson;
    private TextView tvPres, tvPDescription;
    private EditText etPTraffic, etK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poisson);

        Locale.setDefault(Locale.ENGLISH);

        chPoisson = findViewById(R.id.chPoisson);
        tvPres = findViewById(R.id.tvPres);
        tvPDescription = findViewById(R.id.tvPDescription);
        etPTraffic = findViewById(R.id.etPoissonTraffic);
        etK = findViewById(R.id.etK);
        Button btnCalculate = findViewById(R.id.btnPCalc);

        btnCalculate.setOnClickListener(this);

        etPTraffic.setOnFocusChangeListener(this);
        etK.setOnFocusChangeListener(this);

        chPoisson.setTouchEnabled(false);
        chPoisson.setDrawGridBackground(true);
        chPoisson.getAxisLeft().setAxisMinimum(0);
        chPoisson.getAxisRight().setAxisMinimum(0);
        chPoisson.getAxisRight().setDrawLabels(false);
        chPoisson.getDescription().setEnabled(false);

        if(isTablet(chPoisson.getContext())) {
            chPoisson.setMaxVisibleValueCount(21);
            chPoisson.getLegend().setFormSize(20);
            chPoisson.getLegend().setTextSize(20);
        }
        else {
            chPoisson.setMaxVisibleValueCount(16);
            chPoisson.getLegend().setFormSize(15);
            chPoisson.getLegend().setTextSize(15);
        }

        chPoisson.getLegend().setWordWrapEnabled(true);
        chPoisson.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if(etK.getText()==null || etPTraffic.getText()==null || etK.getText().toString().isEmpty() || etPTraffic.getText().toString().isEmpty()) {
            return;
        }
        int K = Integer.parseInt(etK.getText().toString());
        float A = Float.parseFloat(etPTraffic.getText().toString());
        if(A<1000 && K<2000 && A>0 && K>0) {
            float pK = calculatePk(A, K);
            String pKS;
            if (pK > 0.001d && pK < 1) {
                pKS = String.format(Locale.getDefault(), "%.3f", pK);
            } else {
                pKS = String.format(Locale.getDefault(), "%.1e", pK);
            }
            float perc = pK*100;
            String percentS = String.format(Locale.getDefault(),"%.2f", perc);
            tvPDescription.setText(String.format(getString(R.string.msg_prob_calls), K, percentS));
            tvPres.setText(pKS);
            drawPk(A);
        }else {
            tvPDescription.setText(R.string.msg_OoB);
        }
        hideKeyboard(v);
    }

    private void drawPk(float A) {
        ArrayList<BarEntry> pkEntries = new ArrayList<>();

        int m = (int) A;
        int start;

        if(m>40) {
            int r = (int) (m * 0.25);
            start=m-r;
            if (start<0){
                start=0;
            }
            for (int i = start; i < m + r; i++) {
                float val = calculatePk(A, i);
                pkEntries.add(new BarEntry(i, val));
            }
        }
        else {
            start = m-10;
            if (start<0){
                start=0;
            }
            for (int i = start; i < m + 10; i++) {
                float val = calculatePk(A, i);
                pkEntries.add(new BarEntry(i, val));
            }
        }

        BarDataSet pkSet = new BarDataSet(pkEntries, getString(R.string.chBinomial_label));

        pkSet.setColor(Color.RED);

        chPoisson.setData(new BarData(pkSet));

        chPoisson.invalidate();
    }

    private float calculatePk(float A, int K) {
        return (float)((Math.exp(K*Math.log(A)-A*Math.log(Math.E)-factorial(K))));
    }

    private double factorial(int C) {
        double fact = 0;
        for (int n=1; n<=C; n++) {
            fact = fact + Math.log(n);
        }
        return fact;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId()== R.id.etPoissonTraffic) {
            if (v.hasFocus()) {
                tvPDescription.setText(getString(R.string.msg_descr_A));
            }
        }
        else if (v.getId()== R.id.etK) {
            if(v.hasFocus()) {
                    tvPDescription.setText(R.string.msg_descr_calls);
            }
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideKeyboard(v);
        return false;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
