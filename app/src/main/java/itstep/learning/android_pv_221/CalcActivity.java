package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class CalcActivity extends AppCompatActivity {
    private static final String unicodeMinus = "\u2212";
    private static final int maxDigits = 9;
    private TextView tvResult;
    private TextView tvHistory;

    private double operand1 = 0;
    private double operand2 = 0;
    private String operator = "";
    private boolean isNewInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc);

        tvResult = findViewById(R.id.calc_tv_result);
        tvHistory = findViewById(R.id.calc_tv_history);

        // Установка обработчиков для кнопок с цифрами
        for (int i = 0; i < 10; i++) {
            String btnIdName = "calc_btn_digit_" + i;
            @SuppressLint("DiscouragedApi") int btnId = getResources().getIdentifier(
                    btnIdName, "id", getPackageName()
            );
            findViewById(btnId).setOnClickListener(this::onDigitClick);
        }

        // Установка обработчиков для операций
        findViewById(R.id.calc_btn_plus).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::onEqualClick);

        // Обработчики для функций "C" и "Backspace"
        findViewById(R.id.calc_btn_c).setOnClickListener(v -> clearAll());
        findViewById(R.id.calc_btn_backspace).setOnClickListener(v -> backspace());

        // Обработчики для кнопок специальных операций
        findViewById(R.id.calc_btn_square).setOnClickListener(v -> applyUnaryOperation("square"));
        findViewById(R.id.calc_btn_inverse).setOnClickListener(v -> applyUnaryOperation("inverse"));
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(v -> applyUnaryOperation("sqrt"));
        findViewById(R.id.calc_btn_percent).setOnClickListener(v -> applyUnaryOperation("percent"));

        // Начальный сброс
        clearAll();
    }

    private void onDigitClick(View view) {
        String digit = ((Button) view).getText().toString();
        String currentText = tvResult.getText().toString();

        if(tvHistory.getText().toString().contains(String.valueOf('='))){
            tvHistory.setText("");
        }

        if (isNewInput || currentText.equals("0")) {
            currentText = "";
            isNewInput = false;
        }
        if (currentText.length() < maxDigits) {
            tvResult.setText(currentText + digit);
        } else {
            Toast.makeText(this, R.string.calc_msg_too_long, Toast.LENGTH_SHORT).show();
        }
    }

    private void onOperatorClick(View view) {
        String selectedOperator = ((Button) view).getText().toString();

        if (!operator.isEmpty()) {  // Если оператор уже задан
            onEqualClick(view);      // Вычисляем текущий результат
        }

        // Устанавливаем новый оператор и обновляем операнд1 на основе результата
        operator = selectedOperator;
        operand1 = Double.parseDouble(tvResult.getText().toString());
        tvHistory.setText(String.format(Locale.getDefault(), "%.0f %s", operand1, operator));
        isNewInput = true;
    }

    private void onEqualClick(View view) {
        if (operator.isEmpty()) {
            return; // Если оператор не выбран, ничего не делаем
        }

        operand2 = Double.parseDouble(tvResult.getText().toString());
        double result = 0;

        switch (operator) {
            case "+":
                result = operand1 + operand2;
                break;
            case unicodeMinus:
                result = operand1 - operand2;
                break;
            case "×":
                result = operand1 * operand2;
                break;
            case "÷":
                if (operand2 != 0) {
                    result = operand1 / operand2;
                } else {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                    clearAll();
                    return;
                }
                break;
        }

        // Форматируем и добавляем в историю полное выражение с результатом
        String fullExpression = String.format(Locale.getDefault(), "%.0f %s %.0f =", operand1, operator, operand2);
        tvHistory.setText(fullExpression);

        // Обновляем результат и операнд для дальнейших вычислений
        tvResult.setText(formatResult(result));
        operand1 = result;
        operator = ""; // Сбрасываем оператор для нового вычисления
        isNewInput = true;
    }

    private void clearAll() {
        tvResult.setText("0");
        tvHistory.setText("");
        operand1 = 0;
        operand2 = 0;
        operator = "";
        isNewInput = true;
    }

    private void backspace() {
        String currentText = tvResult.getText().toString();
        if (currentText.length() > 1) {
            tvResult.setText(currentText.substring(0, currentText.length() - 1));
        } else {
            tvResult.setText("0");
        }
    }

    private void applyUnaryOperation(String operation) {
        double value = Double.parseDouble(tvResult.getText().toString());
        double result = value;

        switch (operation) {
            case "square":
                result = value * value;
                tvHistory.setText( String.format("sqr(%.0f)", value));
                break;
            case "inverse":
                if (value != 0) result = 1 / value;
                else {
                    Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "sqrt":
                if (value >= 0) {
                    result = Math.sqrt(value);
                    tvHistory.setText( String.format("sqrt(%.0f)", value));
                }
                else {
                    Toast.makeText(this, "Invalid input for square root", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case "percent":
                result = value / 100;
                break;
        }

        tvResult.setText(formatResult(result));
        isNewInput = true;
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.format(Locale.getDefault(), "%d", (long) result);
        } else {
            return String.format(Locale.getDefault(), "%.2f", result);
        }
    }

    // Сохранение состояния
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("tv_result", tvResult.getText());
        outState.putCharSequence("tv_history", tvHistory.getText());
        outState.putDouble("operand1", operand1);
        outState.putDouble("operand2", operand2);
        outState.putString("operator", operator);
        outState.putBoolean("isNewInput", isNewInput);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("tv_result"));
        tvHistory.setText(savedInstanceState.getCharSequence("tv_history"));
        operand1 = savedInstanceState.getDouble("operand1");
        operand2 = savedInstanceState.getDouble("operand2");
        operator = savedInstanceState.getString("operator");
        isNewInput = savedInstanceState.getBoolean("isNewInput");
    }
}