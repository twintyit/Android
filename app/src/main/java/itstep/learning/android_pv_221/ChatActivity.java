package itstep.learning.android_pv_221;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.ROOT
    );
    private final String chatUrl = "https://chat.momentfor.fun/";
    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final Gson gson = new Gson();
    private final List<ChatMessage> messages = new ArrayList<>();
    private boolean isAuthorFixed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTitle = findViewById(R.id.chat_tv_title);
        chatContainer = findViewById(R.id.chat_ll_container);
        chatScroller = findViewById(R.id.chat_scroller);
        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendButtonClick);
        loadChat();
    }

    private void sendButtonClick(View view) {
        String author = etAuthor.getText().toString();
        if (author.isEmpty()) {
            Toast.makeText(this, "Empty field: Author", Toast.LENGTH_SHORT).show();
        }

        String message = etMessage.getText().toString();
        if (message.isEmpty()) {
            Toast.makeText(this, "Empty field: Message", Toast.LENGTH_SHORT).show();
        }

        CompletableFuture.runAsync( () ->
                sendMessage(new ChatMessage()
                        .setAuthor(author)
                        .setText(message)
                        .setMoment( sqlDateFormat.format( new Date() ) )
                ),
                threadPool
        );


    }

    private void sendMessage(ChatMessage chatMessage) {
        try {
            URL url = new URL( chatUrl );

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput( true );
            connection.setDoOutput( true );
            connection.setChunkedStreamingMode( 0 );
            connection.setRequestMethod( "POST" );
            connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setRequestProperty( "Accept", "application/json" );
            connection.setRequestProperty( "Connection", "close" );

            OutputStream bodyStream = connection.getOutputStream();
            bodyStream.write(
                    String.format("author=%s&msg=%s",
                            chatMessage.getAuthor(),
                            chatMessage.getText()
                    ).getBytes( StandardCharsets.UTF_8 )
            );
            bodyStream.flush();
            bodyStream.close();

            int statusCode = connection.getResponseCode();
            if ( statusCode >= 200 && statusCode < 300 ){
                Log.i("sendMessage", "Message sent");
                if (!isAuthorFixed){
                    isAuthorFixed = true;
                    etAuthor.setEnabled(false);
                }
                loadChat();
            }
            else {
                InputStream responseStream = connection.getErrorStream();
                Log.e("sendMessage", readString( responseStream ) );
                responseStream.close();
            }
            connection.disconnect();
        }
        catch (Exception ex){
            Log.e("sendMessage",
                    ex.getMessage() == null ? ex.getClass().toString() : ex.getMessage()
            );
        }
    }

    private void loadChat() {
        CompletableFuture
                .supplyAsync(this::getChatAsString, threadPool)
                .thenApply(this::processChatResponse)
                .thenAccept(this::displayChatMessages);
    }

    private String getChatAsString() {
        try (InputStream urlStream = new URL(chatUrl).openStream()) {
            return readString(urlStream);
        } catch (MalformedURLException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage());
        } catch (IOException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "IOException" : ex.getMessage());
        }
        return null;
    }

    private ChatMessage[] processChatResponse(String jsonString) {
        ChatResponse chatResponse = gson.fromJson(jsonString, ChatResponse.class);
        return chatResponse.data;
    }

    @SuppressLint("ResourceAsColor")
    private void displayChatMessages(ChatMessage[] chatMessages) {
        boolean wasNew = false;
        for ( ChatMessage cm : chatMessages ) {
            if( messages.stream().noneMatch( m -> m.getId().equals(cm.getId() ) ) ){
                messages.add(cm);
                wasNew = true;
            }
        }

        if(!wasNew) {
            return;
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 15, 50, 5);
        Drawable dbOther = getResources().getDrawable(R.drawable.chat_msg_other, getTheme());
        Drawable dbMy = getResources().getDrawable(R.drawable.chat_msg_my, getTheme());
        runOnUiThread( () -> chatContainer.removeAllViews() );
        for ( ChatMessage cm : chatMessages ) {
            LinearLayout outerLayout = new LinearLayout(ChatActivity.this);
            outerLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams outerLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            outerLayoutParams.setMargins(10, 15, 50, 5);

            LinearLayout messageLayout = new LinearLayout(ChatActivity.this);
            messageLayout.setOrientation(LinearLayout.VERTICAL);

            TextView authorTextView = new TextView(ChatActivity.this);
            authorTextView.setText(cm.getAuthor());
            authorTextView.setPadding(30, 5, 30, 5);
            messageLayout.addView(authorTextView);

            TextView messageTextView = new TextView(ChatActivity.this);
            messageTextView.setText(cm.getText());
            messageTextView.setPadding(20, 5, 30, 5);
            messageLayout.addView(messageTextView);

            // Определяем стиль и расположение в зависимости от автора сообщения
            if (etAuthor.getText().toString().equals(cm.getAuthor())) {
                messageLayout.setBackground(dbMy);
                outerLayoutParams.gravity = Gravity.END; // Сообщения текущего пользователя справа
            } else {
                messageLayout.setBackground(dbOther);
                outerLayoutParams.gravity = Gravity.START; // Сообщения других пользователей слева
            }

            messageLayout.setLayoutParams(outerLayoutParams);
            outerLayout.addView(messageLayout);

            runOnUiThread(() -> chatContainer.addView(outerLayout));
        }

    }

    private String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString(StandardCharsets.UTF_8.name());
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        threadPool.shutdownNow();
        super.onDestroy();
    }

    class ChatResponse {
        private int status;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChatMessage[] getData() {
            return data;
        }

        public void setData(ChatMessage[] data) {
            this.data = data;
        }
    }

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public ChatMessage setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getText() {
            return text;
        }

        public ChatMessage setText(String text) {
            this.text = text;
            return this;
        }

        public String getMoment() {
            return moment;
        }

        public ChatMessage setMoment(String moment) {
            this.moment = moment;
            return this;
        }
    }
}


