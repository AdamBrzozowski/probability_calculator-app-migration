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
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

public class ErlangActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener,  View.OnTouchListener {
    private BarChart chErlang;
    private TextView tvSolution, tvDescription;
    private EditText etArg1, etArg2;
    private RadioButton radBtnTraffic, radBtnBlock;

    private float offeredTraffic, blockingProb;
    private int channels, option;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_erlang);

        Locale.setDefault(Locale.ENGLISH);

        chErlang = findViewById(R.id.chErl);
        tvSolution = findViewById(R.id.tvSolution);
        tvDescription = findViewById(R.id.tvDescriptionErl);
        etArg1 = findViewById(R.id.etArg1);
        etArg2 = findViewById(R.id.etArg2);
        Button btnCalculate = findViewById(R.id.btnCalculate);
        radBtnBlock = findViewById(R.id.radBtnBlock);
        radBtnTraffic = findViewById(R.id.radBtnTraffic);

        btnCalculate.setOnClickListener(this);
        radBtnTraffic.setOnClickListener(this);
        radBtnBlock.setOnClickListener(this);
        etArg1.setOnClickListener(this);
        etArg2.setOnClickListener(this);
        etArg1.setOnFocusChangeListener(this);
        etArg2.setOnFocusChangeListener(this);

        radBtnBlock.isChecked();
        option = 0;

        chErlang.setTouchEnabled(false);
        chErlang.setDrawGridBackground(true);
        chErlang.getAxisLeft().setAxisMinimum(0);
        chErlang.getAxisRight().setAxisMinimum(0);
        chErlang.getAxisRight().setDrawLabels(false);
        chErlang.getDescription().setEnabled(false);

        if(isTablet(chErlang.getContext())) {
            chErlang.setMaxVisibleValueCount(21);
            chErlang.getLegend().setFormSize(20);
            chErlang.getLegend().setTextSize(20);
        }
        else {
            chErlang.setMaxVisibleValueCount(16);
            chErlang.getLegend().setFormSize(15);
            chErlang.getLegend().setTextSize(15);
        }

        chErlang.getLegend().setWordWrapEnabled(true);
        chErlang.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnCalculate) {
            hideKeyboard(v);
            if (option == 1) {
                if (etArg1.getText().toString().equals("") || etArg2.getText().toString().equals("")) {
                    tvDescription.setText(R.string.msg_missing_data);
                } else {
                    try {
                        offeredTraffic = Float.parseFloat(etArg1.getText().toString());
                        channels = Integer.parseInt(etArg2.getText().toString());
                        if (offeredTraffic > 200 || channels > 200) {
                            if (offeredTraffic > 200) {
                                tvDescription.setText(R.string.msg_in_OoB_A);
                            }
                            if (channels > 200) {
                                tvDescription.setText(R.string.msg_in_OoB_C);
                            }
                        } else {
                            blockingProb = calculatePk(offeredTraffic, channels, channels);
                            float blockPerc;
                            String bP;
                            if (blockingProb > 0.001) {
                                blockingProb = Float.parseFloat(String.format(Locale.getDefault(), "%.3f", blockingProb));
                            } else {
                                blockingProb = Float.parseFloat(String.format(Locale.getDefault(), "%.2e", blockingProb));
                            }
                            blockPerc = blockingProb * 100;
                            if (blockPerc > 0.001) {
                                bP = String.format(Locale.getDefault(), "%.3f", blockPerc);
                            } else {
                                bP = String.format(Locale.getDefault(), "%.2e", blockPerc);
                            }
                            tvSolution.setText(String.valueOf(blockingProb));
                            tvDescription.setText(String.format(getString(R.string.msg_format_BlockPerc), bP));
                            drawPk(offeredTraffic, channels);
                        }
                    } catch (NumberFormatException e) {
                        tvDescription.setText(R.string.msg_in_invalid);
                    }
                }
            } else if (option == 2) {
                if (etArg1.getText().toString().equals("") || etArg2.getText().toString().equals("")) {
                    tvDescription.setText(R.string.msg_missing_data);
                } else {
                    try {
                        channels = Integer.parseInt(etArg2.getText().toString());
                        blockingProb = Float.parseFloat(etArg1.getText().toString()) / 100;
                        if (channels <= 50) {
                            if (blockingProb < 0.0069 && blockingProb > 0.000007) {
                                int line = channels;
                                String csvFile = "ERLi(1-50)C(0.001-0.6)%.csv";
                                offeredTraffic = trafficErl(csvFile, line, blockingProb);
                                offeredTraffic = Float.parseFloat(String.format(Locale.getDefault(), "%.3f", offeredTraffic));
                                tvSolution.setText(String.valueOf(offeredTraffic));
                                drawPk(offeredTraffic, channels);
                            } else if (blockingProb >= 0.0069 && blockingProb <= 0.45) {
                                int line = channels;
                                String csvFile = "ERLi(1-50)C(0.7-40)%.csv";
                                offeredTraffic = trafficErl(csvFile, line, blockingProb);
                                offeredTraffic = Float.parseFloat(String.format(Locale.getDefault(), "%.3f", offeredTraffic));
                                tvSolution.setText(String.valueOf(offeredTraffic));
                                drawPk(offeredTraffic, channels);
                            } else {
                                tvDescription.setText(R.string.msg_in_OoB_B);
                            }
                        } else if (channels >= 51 && channels <= 100) {
                            if (blockingProb < 0.0069 && blockingProb > 0.000007) {
                                int line = channels - 50;
                                String csvFile = "ERLi(51-100)C(0.001-0.6)%.csv";
                                offeredTraffic = trafficErl(csvFile, line, blockingProb);
                                offeredTraffic = Float.parseFloat(String.format(Locale.getDefault(), "%.3f", offeredTraffic));
                                tvSolution.setText(String.valueOf(offeredTraffic));
                                drawPk(offeredTraffic, channels);
                            } else if (blockingProb >= 0.0069 && blockingProb <= 0.45) {
                                int line = channels - 50;
                                String csvFile = "ERLi(51-100)C(0.7-40)%.csv";
                                offeredTraffic = trafficErl(csvFile, line, blockingProb);
                                offeredTraffic = Float.parseFloat(String.format(Locale.getDefault(), "%.3f", offeredTraffic));
                                tvSolution.setText(String.valueOf(offeredTraffic));
                                drawPk(offeredTraffic, channels);
                            } else {
                                tvDescription.setText(R.string.msg_in_OoB_B);
                            }
                        } else {
                            tvDescription.setText(R.string.msg_in_OoB_C);
                        }
                    } catch (NumberFormatException e) {
                        tvDescription.setText(R.string.msg_in_invalid);
                    }
                }
            }

        }
        else if (v.getId() == R.id.radBtnBlock) {
            option = 1;
            etArg1.setHint("ERL");
            etArg2.setHint("Ch");
            etArg2.setText("");
            etArg1.setText("");
            tvSolution.setText("");
            if (etArg1.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_A);
            } else if (etArg2.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_C);
            }
        }
        else if (v.getId() == R.id.radBtnTraffic) {
            option = 2;
            etArg1.setHint("B%");
            etArg2.setHint("Ch");
            etArg2.setText("");
            etArg1.setText("");
            tvSolution.setText("");
            if(etArg1.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_Br);
            }
            else if(etArg2.hasFocus()) {
                tvDescription.setText(R.string.msg_descr_Cr);
            }

        }
    }

    private float trafficErl(String csvFile, int line , float blockP) {
        try {
            float offeredTraffic;
            int column = 1;
            InputStream is = getAssets().open(csvFile);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String H = br.readLine();
            H = H.replace(",\"", ";");
            H = H.replace("\",", ";");
            String[] prob = H.split(";");
            float colP = Float.parseFloat(prob[column].replace("\"", "").replace(",","."));



            while (colP <= blockP+0.0000001 && column<=10){
                column++;
                if(column<=10) {
                    colP = Float.parseFloat(prob[column].replace("\"", "").replace(",", "."));
                }
            }


            float x2 = colP;
            float x1 = Float.parseFloat(prob[column-1].replace("\"","").replace(",","."));


            //if(column>1 && column<=10) column--;
            for(int n=0; n<line; n++) H = br.readLine();
            H = H.replace(",\"", ";");
            H = H.replace("\",", ";");
            prob = H.split(";");

            if (column==1) {
                offeredTraffic = Float.parseFloat(prob[column].replace("\"","").replace(",","."));
            }
            else if (column == 11) {
                offeredTraffic = Float.parseFloat(prob[column-1].replace("\"","").replace(",","."));
            }
            else {
                float y1 = Float.parseFloat(prob[column - 1].replace("\"", "").replace(",", "."));
                float y2 = Float.parseFloat(prob[column].replace("\"", "").replace(",", "."));
                float x = blockP;

                offeredTraffic = ((y2 - y1) / (x2 - x1)) * x + (y1 * x2 - y2 * x1) / (x2 - x1);
            }
            br.close();
            return offeredTraffic;
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId()==R.id.etArg1) {
            if(v.hasFocus()) {
                if (option == 1) {
                    tvDescription.setText(R.string.msg_descr_A);
                } else if (option == 2) {
                    tvDescription.setText(R.string.msg_descr_Br);
                }
            }
        }
        else if (v.getId()==R.id.etArg2) {
            if(v.hasFocus()) {
                if(option == 1) {
                    tvDescription.setText(R.string.msg_descr_C);
                }
                else if(option==2) {
                    tvDescription.setText(R.string.msg_descr_Cr);
                }
            }
        }
    }


    private void drawPk(float A, int C) {
        //List<BarDataSet> dataSets = new ArrayList<>();

        ArrayList<BarEntry>  pkEntries = new ArrayList<>();
        ArrayList<BarEntry>  BEntries = new ArrayList<>();
        BarEntry  BEntry = new BarEntry(C, calculatePk(A,C,C));

        for(int i = 0; i<C; i ++) {
            float val = calculatePk(A, C, i);
            pkEntries.add(new BarEntry(i, val));
        }

        BEntries.add(BEntry);

        BarDataSet pkSet = new BarDataSet(pkEntries,"Probability k [0-Ch] channels occupied");
        BarDataSet Bset = new BarDataSet(BEntries, "Blocking Probability");

        Bset.setColor(Color.parseColor("#5555F2"));
        pkSet.setColor(Color.RED);

        //dataSets.add(pkSet);
        //dataSets.add(Bset);

        chErlang.setData(new BarData(pkSet,Bset));

        chErlang.invalidate();
    }

    private float calculatePk(float A , int C, int K) {
        return (float)((Math.exp(K*Math.log(A)-factorial(K)))/sumFact(A, C));

        //return (float)(((Math.pow(A, C))/factorial(C))/sumFact(A,C));
    }

    private double factorial(int C) {
       double fact = 0;
       for (int n=1; n<=C; n++) {
           fact = fact + Math.log(n);
       }
       return fact;

        /* int fact=1;
        for(int n=C; n>0; n--) {
            fact = fact*n;
        }
        return fact;*/
    }

    private double sumFact(float A, int C) {
        double sol = 0;
        for (int n=0; n<=C; n++) {
            sol = sol + Math.exp(n*Math.log(A) - factorial(n));
        }
        return sol;

        /*double sol = 0;
        for(int n=0; n<=C; n++) {
            sol = sol + Math.pow(A,n)/factorial(n);
        }
        return sol;*/
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
