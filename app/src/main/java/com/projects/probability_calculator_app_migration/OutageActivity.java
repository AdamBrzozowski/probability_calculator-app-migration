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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutageActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {
    private LineChart chGauss;
    private SeekBar sbMargin, sbSigma;
    private TextView tvMargin, tvSigma, tvOutage, tvDescription;
    private EditText etThr, etRec;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outage);

        Locale.setDefault(Locale.ENGLISH);

        findViewById(R.id.chGauss).setOnTouchListener(this);


        chGauss = findViewById(R.id.chGauss);
        sbMargin = findViewById(R.id.sbMargin);
        sbSigma = findViewById(R.id.sbSigma);
        tvMargin = findViewById(R.id.tvMargin);
        tvSigma = findViewById(R.id.tvSigma);
        tvOutage = findViewById(R.id.tvOutage);
        tvDescription = findViewById(R.id.tvDescription);
        etRec = findViewById(R.id.etReceived);
        etThr = findViewById(R.id.etThr);
        Button btnOutage = findViewById(R.id.btnOutage);

        btnOutage.setOnClickListener(this);
        etThr.setOnClickListener(this);
        etRec.setOnClickListener(this);
        etThr.setOnFocusChangeListener(this);
        etRec.setOnFocusChangeListener(this);

        sbSigma.setOnSeekBarChangeListener(this);
        sbSigma.setMax(90);

        sbMargin.setOnSeekBarChangeListener(this);
        sbMargin.setMax(600);
        sbMargin.setProgress(300);

        chGauss.setTouchEnabled(false);
        chGauss.setDrawGridBackground(true);

        Legend legend = chGauss.getLegend();

        legend.setWordWrapEnabled(true);

        if(isTablet(chGauss.getContext())){
            LegendEntry l1 = new LegendEntry("Margin[decibel]", Legend.LegendForm.SQUARE,20,20,null,Color.RED);
            LegendEntry l2 = new LegendEntry("Shadowing Standard Deviation", Legend.LegendForm.SQUARE,20,20,null,Color.parseColor("#4CAF50"));

            legend.setTextSize(30);
            LegendEntry[] L = {l1,l2};
            legend.setCustom(L);
        }
        else {
            LegendEntry l1 = new LegendEntry("Margin[decibel]", Legend.LegendForm.SQUARE,10,10,null,Color.RED);
            LegendEntry l2 = new LegendEntry("Shadowing Standard Deviation", Legend.LegendForm.SQUARE,10,10,null,Color.parseColor("#4CAF50"));
            legend.setTextSize(15);
            LegendEntry[] L = {l1,l2};
            legend.setCustom(L);
        }


        chGauss.getAxisLeft().setAxisMaximum(0.12f);
        chGauss.getAxisLeft().setAxisMinimum(0);
        chGauss.getAxisRight().setAxisMaximum(0.12f);
        chGauss.getAxisRight().setAxisMinimum(0);
        chGauss.getAxisRight().setDrawLabels(false);
        chGauss.getXAxis().setAxisMaximum(30);
        chGauss.getXAxis().setAxisMinimum(-30);
        chGauss.getDescription().setEnabled(false);

        int s = 4;
        int margin = 0;

        drawGMO(s, margin);
    }

    private void drawGMO(float sigma, float margin) {
        List<ILineDataSet> dataSets = new ArrayList<>();

        List<Entry>  gaussEntries = new ArrayList<>();
        List<Entry>  marginEntries = new ArrayList<>();
        List<Entry>  outEntries = new ArrayList<>();

        for(float i = -30f; i<=39f; i += 0.02f) {
            gaussEntries.add(new Entry(i, (float)(1/(Math.sqrt(2*Math.PI*sigma*sigma))*Math.exp(-i*i/(2*sigma*sigma)))));
        }
        for(float i = 0; i<=0.2f; i += 0.2f) {
            marginEntries.add(new Entry(margin, i));
        }

        for(float i = margin; i<=39f; i += 0.02f) {
            outEntries.add(new Entry(i, (float)(1/(Math.sqrt(2*Math.PI*sigma*sigma))*Math.exp(-i*i/(2*sigma*sigma)))));
        }

        LineDataSet gaussSet = new LineDataSet(gaussEntries,null);
        LineDataSet marginSet = new LineDataSet(marginEntries,"Margin[decibel(db)]");
        LineDataSet outageSet = new LineDataSet(outEntries,null);

        gaussSet.setColor(Color.BLACK);
        gaussSet.setDrawCircles(false);
        gaussSet.setLineWidth(3f);

        marginSet.setColor(Color.RED);
        marginSet.setDrawCircles(false);
        marginSet.setLineWidth(2f);

        outageSet.setColor(Color.BLUE);

        outageSet.setDrawCircles(false);
        outageSet.setLineWidth(0.2f);
        outageSet.setFillAlpha(100);
        outageSet.setDrawFilled(true);
        outageSet.setFillColor(Color.BLUE);

        dataSets.add(outageSet);
        dataSets.add(gaussSet);
        dataSets.add(marginSet);

        chGauss.setData(new LineData(dataSets));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float margin = (float)sbMargin.getProgress();
        if(margin==300) margin=0;
        else if(margin>300) margin=(margin-300)/10;
        else margin=(margin-300)/10;

        float sigma = (float)sbSigma.getProgress()/10 + 4;

        tvMargin.setText(String.valueOf(margin));
        tvSigma.setText(String.valueOf(sigma));

        String sthr = etThr.getText().toString();
        if(sthr.length() > 0) {
            float thr = Float.parseFloat(sthr);
            float rec = thr + margin;
            //etRec.setText(String.valueOf(rec));
            etRec.setText(String.format(Locale.getDefault(),"%.1f", rec));
        }
        drawGMO(sigma, margin);

        chGauss.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.btnOutage) {
            hideKeyboard(v);
            float margin;
            if (etThr.getText().toString().equals("")) {
                etRec.setText("");
                outageClick();
            } else if (!etThr.getText().toString().equals("") && etRec.getText().toString().equals("")) {
                try {
                    float thr = Float.parseFloat(etThr.getText().toString());
                    etRec.setText(String.valueOf(thr + (((float) sbMargin.getProgress() - 300) / 10)));
                    outageClick();
                } catch (NumberFormatException e) {
                    tvDescription.setText(R.string.msg_in_invalid_Thr);
                }
            } else {
                try {
                    float thr = Float.parseFloat(etThr.getText().toString());
                    float rec = Float.parseFloat(etRec.getText().toString());

                    if ((rec - thr) > 30) {
                        tvDescription.setText(R.string.msg_descr_Outz);
                        sbMargin.setProgress(600);
                    } else if ((rec - thr) < -30) {
                        tvDescription.setText(R.string.msg_descr_Outc);
                        sbMargin.setProgress(0);
                    } else {
                        margin = rec - thr;
                        margin = Float.parseFloat(String.format(Locale.getDefault(), "%.1f", margin));
                        margin = (margin * 10) + 300;
                        sbMargin.setProgress((int) margin);
                        outageClick();
                    }


                } catch (NumberFormatException e) {
                    tvDescription.setText(R.string.msg_in_invalid_ThrRec);
                }
            }
        }
        else if (v.getId()==R.id.etThr) {
            if (!etThr.getText().toString().isEmpty()) {
                try {
                    String thrdbS = etThr.getText().toString();
                    float thrdb = Float.parseFloat(thrdbS);
                    String thrWS;
                    if (thrdb < 300 && thrdb > -300) {
                        double thrW = Math.pow(10, thrdb / 10) / 1000;
                        if (thrW > 0.001 && thrW < 10000000) {
                            thrWS = String.format(Locale.getDefault(), "%.4f", thrW);
                        } else {
                            thrWS = String.format(Locale.getDefault(), "%.3e", thrW);
                        }
                        tvDescription.setText(String.format(getString(R.string.msg_format_dbmW), thrdbS, thrWS));

                    } else {
                        tvDescription.setText(R.string.msg_Prange);
                    }
                } catch (NumberFormatException e) {
                    tvDescription.setText(R.string.msg_in_invalid_Thr);
                }
            }

        }
        else if (v.getId()==R.id.etReceived) {
            if (!etRec.getText().toString().isEmpty()) {
                try {
                    String recdbS = etRec.getText().toString();
                    float recdb = Float.parseFloat(recdbS);
                    String recWS;
                    if (recdb < 300 && recdb > -300) {
                        double recW = Math.pow(10, recdb / 10) / 1000;
                        if (recW > 0.001d && recW < 10000000) {
                            recWS = String.format(Locale.getDefault(), "%.4f", recW);
                        } else {
                            recWS = String.format(Locale.getDefault(), "%.3e", recW);
                        }
                        tvDescription.setText(String.format(getString(R.string.msg_format_dbmW), recdbS, recWS));
                    } else {
                        tvDescription.setText(R.string.msg_Prange);
                    }

                } catch (NumberFormatException e) {
                    tvDescription.setText(R.string.msg_in_invalid_Thr);
                }

            }
        }
    }

    private void outageClick() {
        float margin = Float.parseFloat(tvMargin.getText().toString());
        float sigma = Float.parseFloat(tvSigma.getText().toString());
        float doubleOut;
        float outage,erfcArg,percent;
        String percentS;
        if(margin<0) {
            margin = margin - 2*margin;
            erfcArg = (float)(margin/(sigma*Math.sqrt(2)));
            if(erfcArg > 2.07f) {
                tvOutage.setText("> 0.99");
                tvDescription.setText(R.string.msg_descr_Outc);
            }
            else {
                doubleOut = calculateOutage(margin, sigma);
                outage = (1 - doubleOut) + doubleOut / 2;
                outage = Float.parseFloat(String.format(Locale.getDefault(),"%.3f", outage));
                percent = outage*100;
                percentS = String.format(Locale.getDefault(),"%.2f", percent);
                tvOutage.setText(String.valueOf(outage));
                tvDescription.setText(String.format(getString(R.string.msg_format_OutPerc), percentS));

            }
        }
        else if(margin>0) {
            erfcArg = (float)(margin/(sigma*Math.sqrt(2)));
            if(erfcArg > 2.07f) {
                tvOutage.setText("< 0.01");
                tvDescription.setText(R.string.msg_descr_Outz);
            }
            else {
                doubleOut = calculateOutage(margin, sigma);
                outage = doubleOut / 2;
                outage = Float.parseFloat(String.format(Locale.getDefault(),"%.3f", outage));
                percent = outage*100;
                percentS = String.format(Locale.getDefault(),"%.2f", percent);
                tvOutage.setText(String.valueOf(outage));
                tvDescription.setText(String.format(getString(R.string.msg_format_OutPerc), percentS));

            }
        }
        else {
            tvDescription.setText(R.string.msg_Out50);
            tvOutage.setText("0.5");
        }

    }

    public float calculateOutage(float margin, float sigma) {
        float erfArg = (float)(margin/(sigma*Math.sqrt(2)));
        float arg = Float.parseFloat(String.format(Locale.getDefault(),"%.2f", erfArg));
        int line = (int)(arg*10);
        int col = (int)(arg*100) - line*10;
        try {
            InputStream is = getAssets().open("erfc.csv");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String H = br.readLine();
            for(int n=0; n<line; n++) H = br.readLine();
            String[] prob = H.split(",");
            float result = Float.parseFloat(prob[col]);
            br.close();
            return result;
        } catch (IOException e) {
            return 0;
    }

}

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.etThr) {
            if (v.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_hintThr);
            }
        } else if (v.getId() == R.id.etReceived) {
            if (v.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_hintRec);

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
