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

public class BinomialActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnTouchListener, View.OnClickListener {
    private BarChart chBinomial;
    private TextView tvBres, tvDescriptionB;
    private EditText etTrafficB, etKB, etUsersB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binomial);

        Locale.setDefault(Locale.ENGLISH);

        chBinomial = findViewById(R.id.chBinomial);
        tvBres = findViewById(R.id.tvBres);
        tvDescriptionB = findViewById(R.id.tvDescriptionB);
        etTrafficB = findViewById(R.id.etTrafficB);
        etKB = findViewById(R.id.etKB);
        etUsersB = findViewById(R.id.etUsersB);
        Button btnCalculate = findViewById(R.id.btnGoB);

        btnCalculate.setOnClickListener(this);

        etTrafficB.setOnFocusChangeListener(this);
        etKB.setOnFocusChangeListener(this);
        etUsersB.setOnFocusChangeListener(this);

        chBinomial.setTouchEnabled(false);
        chBinomial.setDrawGridBackground(true);
        chBinomial.getAxisLeft().setAxisMinimum(0);
        chBinomial.getAxisRight().setAxisMinimum(0);
        chBinomial.getAxisRight().setDrawLabels(false);
        chBinomial.getDescription().setEnabled(false);

        if(isTablet(chBinomial.getContext())) {
            chBinomial.setMaxVisibleValueCount(21);
            chBinomial.getLegend().setFormSize(20);
            chBinomial.getLegend().setTextSize(20);
        }
        else {
            chBinomial.setMaxVisibleValueCount(16);
            chBinomial.getLegend().setFormSize(15);
            chBinomial.getLegend().setTextSize(15);
        }

        chBinomial.getLegend().setWordWrapEnabled(true);
        chBinomial.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        int K = Integer.parseInt(etKB.getText().toString());
        int N = Integer.parseInt(etUsersB.getText().toString());
        float A = Float.parseFloat(etTrafficB.getText().toString());
        if(A<1 && K<=N) {
            float pK = calculatePk(N,K,A);
            String pKS;
            if (pK > 0.001d && pK < 1) {
                pKS = String.format(Locale.getDefault(), "%.3f", pK);
            } else {
                pKS = String.format(Locale.getDefault(), "%.1e", pK);
            }
            float perc = pK*100;
            String percentS = String.format(Locale.getDefault(),"%.2f", perc);
            tvDescriptionB.setText(String.format(getString(R.string.msg_prob_calls), K, percentS));
            tvBres.setText(pKS);
            drawPk(N,A);
        }else {
            tvDescriptionB.setText(R.string.msg_OoB);
        }
        hideKeyboard(v);
    }

    private void drawPk(int N, float A) {
        ArrayList<BarEntry> pkEntries = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            float val = calculatePk(N,i,A);
            pkEntries.add(new BarEntry(i, val));
        }

        BarDataSet pkSet = new BarDataSet(pkEntries,getString(R.string.chBinomial_label));

        pkSet.setColor(Color.RED);

        chBinomial.setData(new BarData(pkSet));

        chBinomial.invalidate();
    }

    private float calculatePk(int N, int K, float A) {

        return (float)(Math.exp(factorial(N) - factorial(K) - factorial(N-K) + K*Math.log(A) + (N-K)*Math.log(1-A)));
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
        if (v.getId()==R.id.etTrafficB) {
            if (v.hasFocus()) {
                tvDescriptionB.setText(getString(R.string.msg_descr_A) + " [0-1]");

            }
        }
        else if (v.getId()==R.id.etKB) {
            if (v.hasFocus()) {
                tvDescriptionB.setText(R.string.msg_descr_calls);
            }
        }
        else if (v.getId()==R.id.etUsersB) {
            if(v.hasFocus()) {
                tvDescriptionB.setText(R.string.msg_descr_users);
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
