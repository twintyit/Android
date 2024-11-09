package itstep.learning.android_pv_221.exchange;

import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExchangeRatesLoader {
    private static final String NBU_URL = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
    private static final Gson gson = new Gson();

    public static ExchangeRate[] loadRates() {
        try (InputStream stream = new URL(NBU_URL).openStream()) {
            String json = readString(stream);
            return gson.fromJson(json, ExchangeRate[].class);
        } catch (IOException e) {
            Log.e("ExchangeRatesLoader", "Failed to load exchange rates: " + e.getMessage());
            return null;
        }
    }

    private static String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        return byteBuilder.toString(StandardCharsets.UTF_8.name());
    }
}
