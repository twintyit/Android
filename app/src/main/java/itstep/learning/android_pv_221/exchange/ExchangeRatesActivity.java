package itstep.learning.android_pv_221.exchange;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;

import java.util.concurrent.CompletableFuture;

import itstep.learning.android_pv_221.R;

public class ExchangeRatesActivity extends AppCompatActivity {
    private LinearLayout ratesContainer;
    private final Gson gson = new Gson();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rates);
        ratesContainer = findViewById(R.id.rates_container);
        loadExchangeRates();
    }

    private void loadExchangeRates() {
        CompletableFuture.supplyAsync(() -> ExchangeRatesLoader.loadRates())
                .thenAccept(this::displayExchangeRates);
    }

    private void displayExchangeRates(ExchangeRate[] exchangeRates) {
        if (exchangeRates == null) return;

        runOnUiThread(() -> {
            ratesContainer.removeAllViews();
            for (ExchangeRate rate : exchangeRates) {
                TextView rateView = new TextView(this);
                rateView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                rateView.setText(rate.getCc() + ": " + rate.getRate());
                ratesContainer.addView(rateView);
            }
        });
    }
}

