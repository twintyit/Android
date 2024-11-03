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

        findViewById(R.id.calc_btn_plus).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_minus).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_multiply).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_divide).setOnClickListener(this::onOperatorClick);
        findViewById(R.id.calc_btn_equal).setOnClickListener(this::onEqualClick);
        findViewById(R.id.calc_btn_coma).setOnClickListener(this::onDecimalClick);
        findViewById(R.id.calc_btn_pm).setOnClickListener(this::onPlusMinusClick);

        findViewById(R.id.calc_btn_c).setOnClickListener(v -> clearAll());
        findViewById(R.id.calc_btn_backspace).setOnClickListener(v -> backspace());
        findViewById(R.id.calc_btn_ce).setOnClickListener( v -> onCEClick());

        findViewById(R.id.calc_btn_square).setOnClickListener(v -> applyUnaryOperation("square"));
        findViewById(R.id.calc_btn_inverse).setOnClickListener(v -> applyUnaryOperation("inverse"));
        findViewById(R.id.calc_btn_sqrt).setOnClickListener(v -> applyUnaryOperation("sqrt"));
        findViewById(R.id.calc_btn_percent).setOnClickListener(v -> applyUnaryOperation("percent"));

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

        if (!operator.isEmpty()) {
            onEqualClick(view);  // Вычисляем текущий результат, если оператор уже установлен
        }

        operator = selectedOperator;
        operand1 = Double.parseDouble(tvResult.getText().toString());
        tvHistory.setText(formatExpressionForHistory(operand1, operator, null));
        isNewInput = true;
    }

    private void onEqualClick(View view) {
        if (operator.isEmpty()) return;

        operand2 = Double.parseDouble(tvResult.getText().toString());
        Double result = performOperation(operand1, operand2, operator);

        if (result == null) {
            Toast.makeText(this, "Cannot divide by zero", Toast.LENGTH_SHORT).show();
            clearAll();
            return;
        }

        tvHistory.setText(formatExpressionForHistory(operand1, operator, operand2));
        tvResult.setText(formatResult(result));
        operand1 = result;
        operator = "";  // Сбрасываем оператор для нового вычисления
        isNewInput = true;
    }

    private void onPlusMinusClick(View view) {
        String currentText = tvResult.getText().toString();

        if (!currentText.equals("0")) {
            double currentValue = Double.parseDouble(currentText);
            currentValue = -currentValue;
            tvResult.setText(formatResult( currentValue ));
        }
    }

    private void onCEClick() {
        tvResult.setText("0"); // Очищаем поле результата, не затрагивая оператор и историю
        isNewInput = true; // Устанавливаем флаг, чтобы следующее нажатие началось с нового ввода
    }

    private void onDecimalClick(View view) {
        String currentText = tvResult.getText().toString();

        // Если точка уже присутствует в числе, больше не добавляем её
        if (!currentText.contains(".")) {
            if (isNewInput) {
                // Если это начало нового ввода, ставим "0." вместо простой точки
                tvResult.setText("0.");
                isNewInput = false;
            } else {
                // Добавляем точку к текущему числу
                tvResult.setText(currentText + ".");
            }
        }
    }

    private Double performOperation(double operand1, double operand2, String operator) {
        switch (operator) {
            case "+":
                return operand1 + operand2;
            case unicodeMinus:
                return operand1 - operand2;
            case "×":
                return operand1 * operand2;
            case "÷":
                return (operand2 != 0) ? operand1 / operand2 : null;
            default:
                return null;
        }
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
        String resultStr = String.valueOf(result);
        int decimalIndex = resultStr.indexOf('.');

        if (decimalIndex < 0) {
            return resultStr;
        } else {
            int precision = Math.min(maxDigits - decimalIndex, resultStr.length() - decimalIndex - 1);
            return String.format(Locale.getDefault(), "%." + precision + "f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
    }

    private String formatExpressionForHistory(double operand1, String operator, Double operand2) {
        if (operand2 == null) {
            return String.format(Locale.getDefault(), "%s %s", formatResult(operand1), operator);
        } else {
            return String.format(Locale.getDefault(), "%s %s %s =", formatResult(operand1), operator, formatResult(operand2));
        }
    }

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