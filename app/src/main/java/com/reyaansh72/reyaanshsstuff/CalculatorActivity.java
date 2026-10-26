package com.reyaansh72.reyaanshsstuff;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;
    private final StringBuilder expression = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        ImageButton backButton = findViewById(R.id.calcBackButton);
        backButton.setOnClickListener(v -> finish());

        tvExpression = findViewById(R.id.tvCalcExpression);
        tvResult = findViewById(R.id.tvCalcResult);

        setupKeypad();
    }

    private void setupKeypad() {
        int[] numBtnIds = {
                R.id.btnCalc0, R.id.btnCalc1, R.id.btnCalc2, R.id.btnCalc3, R.id.btnCalc4,
                R.id.btnCalc5, R.id.btnCalc6, R.id.btnCalc7, R.id.btnCalc8, R.id.btnCalc9,
                R.id.btnCalc00, R.id.btnCalcDot
        };

        for (int id : numBtnIds) {
            MaterialButton btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    expression.append(btn.getText());
                    updateDisplay();
                });
            }
        }

        int[] opBtnIds = {
                R.id.btnCalcPlus, R.id.btnCalcMinus, R.id.btnCalcMultiply, R.id.btnCalcDivide, R.id.btnCalcPercent
        };

        for (int id : opBtnIds) {
            MaterialButton btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    expression.append(" ").append(btn.getText()).append(" ");
                    updateDisplay();
                });
            }
        }

        findViewById(R.id.btnCalcClear).setOnClickListener(v -> {
            expression.setLength(0);
            tvExpression.setText("");
            tvResult.setText("0");
        });

        findViewById(R.id.btnCalcDel).setOnClickListener(v -> {
            if (expression.length() > 0) {
                expression.deleteCharAt(expression.length() - 1);
                updateDisplay();
            }
        });

        findViewById(R.id.btnCalcEquals).setOnClickListener(v -> calculateResult());
    }

    private void updateDisplay() {
        tvExpression.setText(expression.toString());
        calculateResult();
    }

    private void calculateResult() {
        String expr = expression.toString().trim();
        if (expr.length() == 0) {
            tvResult.setText("0");
            return;
        }

        try {
            String[] tokens = expr.split(" ");
            if (tokens.length == 0) return;

            double result = Double.parseDouble(tokens[0]);
            for (int i = 1; i < tokens.length - 1; i += 2) {
                String op = tokens[i];
                double val = Double.parseDouble(tokens[i + 1]);

                switch (op) {
                    case "+": result += val; break;
                    case "-": result -= val; break;
                    case "×": result *= val; break;
                    case "÷": if (val != 0) result /= val; break;
                    case "%": result = result % val; break;
                }
            }

            if (result == (long) result) {
                tvResult.setText(String.valueOf((long) result));
            } else {
                tvResult.setText(String.valueOf(result));
            }
        } catch (Exception e) {
            // Expression incomplete while typing
        }
    }
}