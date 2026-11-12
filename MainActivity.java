package com.z8project.ia;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.*;
import android.text.InputType;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(16,17,26);
    private final int PANEL = Color.rgb(29,31,45);
    private final int PURPLE = Color.rgb(124,92,252);
    private final int WHITE = Color.WHITE;
    private final int MUTED = Color.rgb(174,177,195);
    private LinearLayout chatList;
    private ScrollView scroll;
    private EditText messageInput, apiKeyInput, modelInput, endpointInput;
    private TextView status;
    private Button sendButton;
    private JSONArray history = new JSONArray();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private boolean busy = false;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs = getSharedPreferences("z8_settings", MODE_PRIVATE);
        buildUi();
        addBubble("Z8 Project IA", "مرحبا بك! أنا المساعد ديالك. دخل API Key واسم الموديل من الإعدادات، ومن بعد سولني أي سؤال.", false);
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius);
        return d;
    }

    private TextView label(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private EditText field(String hint, String value, boolean secret) {
        EditText e = new EditText(this);
        e.setSingleLine(true); e.setText(value); e.setHint(hint); e.setTextColor(WHITE);
        e.setHintTextColor(MUTED); e.setTextSize(14); e.setPadding(14, 8, 14, 8);
        e.setBackground(shape(PANEL, 18));
        if (secret) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return e;
    }

    private void buildUi() {
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG); page.setPadding(16, 14, 16, 12);
        setContentView(page);

        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);
        TextView title = label("Z8 Project IA", 22, WHITE); title.setTypeface(null, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button settings = new Button(this); settings.setText("⚙"); settings.setTextSize(18);
        settings.setTextColor(WHITE); settings.setBackground(shape(PANEL, 18));
        header.addView(settings, new LinearLayout.LayoutParams(52, 46));
        page.addView(header);

        TextView sub = label("مساعدك الذكي", 12, MUTED);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2); sp.topMargin = 3;
        page.addView(sub, sp);

        scroll = new ScrollView(this); scroll.setFillViewport(true);
        chatList = new LinearLayout(this); chatList.setOrientation(LinearLayout.VERTICAL);
        chatList.setPadding(0, 14, 0, 14); scroll.addView(chatList);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, 0, 1); cp.topMargin = 8; cp.bottomMargin = 8;
        page.addView(scroll, cp);

        status = label("جاهز", 12, MUTED); page.addView(status);

        LinearLayout composer = new LinearLayout(this); composer.setGravity(Gravity.CENTER_VERTICAL);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        messageInput = new EditText(this); messageInput.setHint("كتب رسالتك هنا...");
        messageInput.setTextColor(WHITE); messageInput.setHintTextColor(MUTED); messageInput.setTextSize(15);
        messageInput.setPadding(14, 8, 14, 8); messageInput.setMinHeight(50);
        messageInput.setBackground(shape(PANEL, 22));
        composer.addView(messageInput, new LinearLayout.LayoutParams(0, -2, 1));
        sendButton = new Button(this); sendButton.setText("إرسال"); sendButton.setTextColor(WHITE);
        sendButton.setBackground(shape(PURPLE, 22));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, 50); bp.leftMargin = 8;
        composer.addView(sendButton, bp);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, -2); fp.topMargin = 8;
        page.addView(composer, fp);

        settings.setOnClickListener(v -> showSettings());
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void addBubble(String who, String text, boolean user) {
        LinearLayout wrap = new LinearLayout(this); wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setGravity(user ? Gravity.RIGHT : Gravity.LEFT);
        TextView name = label(who, 11, MUTED);
        TextView body = label(text, 15, WHITE); body.setPadding(14, 11, 14, 11);
        body.setBackground(shape(user ? Color.rgb(70,52,145) : PANEL, 18));
        body.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels * 0.82));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2); bp.topMargin = 4;
        wrap.addView(name); wrap.addView(body, bp);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(-1, -2); wp.bottomMargin = 14;
        chatList.addView(wrap, wp);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void showSettings() {
        LinearLayout box = new LinearLayout(this); box.setPadding(8, 4, 8, 4); box.setOrientation(LinearLayout.VERTICAL);
        TextView info = label("إعدادات الاتصال بـ Respan", 16, WHITE);
        box.addView(info);
        endpointInput = field("API Base URL", prefs.getString("endpoint", "https://gateway.respan.ai/v1"), false);
        modelInput = field("Model (حسب الموديلات المتاحة عندك)", prefs.getString("model", "gpt-4o-mini"), false);
        apiKeyInput = field("Respan API Key", prefs.getString("key", ""), true);
        addSetting(box, "Base URL", endpointInput);
        addSetting(box, "Model", modelInput);
        addSetting(box, "API Key", apiKeyInput);
        TextView note = label("ملاحظة: المفتاح كيتخزن فهاد الهاتف فقط. ما تشاركوش مع حتى واحد.", 11, MUTED);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(-1, -2); np.topMargin = 8; box.addView(note, np);

        ScrollView sv = new ScrollView(this); sv.addView(box);
        new android.app.AlertDialog.Builder(this).setTitle("الإعدادات")
            .setView(sv).setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ", (d, w) -> {
                prefs.edit()
                    .putString("endpoint", endpointInput.getText().toString().trim())
                    .putString("model", modelInput.getText().toString().trim())
                    .putString("key", apiKeyInput.getText().toString().trim()).apply();
                status.setText("تم حفظ الإعدادات");
            }).show();
    }

    private void addSetting(LinearLayout parent, String title, EditText edit) {
        TextView t = label(title, 12, MUTED);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2); tp.topMargin = 10;
        parent.addView(t, tp);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(-1, 48); ep.topMargin = 5;
        parent.addView(edit, ep);
    }

    private void sendMessage() {
        if (busy) return;
        String prompt = messageInput.getText().toString().trim();
        if (prompt.isEmpty()) return;
        String key = prefs.getString("key", "").trim();
        String model = prefs.getString("model", "gpt-4o-mini").trim();
        String base = prefs.getString("endpoint", "https://gateway.respan.ai/v1").trim();
        if (key.isEmpty()) {
            addBubble("تنبيه", "دخل API Key من ⚙ الإعدادات قبل ما تبدا.", false); return;
        }
        if (model.isEmpty()) {
            addBubble("تنبيه", "دخل اسم الموديل فالإعدادات.", false); return;
        }

        messageInput.setText("");
        addBubble("أنت", prompt, true);
        try {
            JSONObject user = new JSONObject(); user.put("role", "user"); user.put("content", prompt); history.put(user);
        } catch (Exception ignored) {}
        busy = true; sendButton.setEnabled(false); status.setText("كنفكر...");
        final String endpoint = (base.endsWith("/") ? base : base + "/") + "chat/completions";
        final JSONArray messages = new JSONArray(history.toString());
        executor.execute(() -> {
            String answer;
            try {
                answer = callApi(endpoint, key, model, messages);
                JSONObject assistant = new JSONObject(); assistant.put("role", "assistant"); assistant.put("content", answer); history.put(assistant);
            } catch (Exception e) {
                answer = "وقع مشكل فالاتصال: " + (e.getMessage() == null ? "خطأ غير معروف" : e.getMessage());
                try { history.remove(history.length() - 1); } catch (Exception ignored) {}
            }
            final String result = answer;
            runOnUiThread(() -> {
                addBubble("Z8 Project IA", result, false);
                busy = false; sendButton.setEnabled(true); status.setText("جاهز");
            });
        });
    }

    private String callApi(String endpoint, String key, String model, JSONArray messages) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(endpoint).openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(25000); c.setReadTimeout(60000);
        c.setDoOutput(true);
        c.setRequestProperty("Authorization", "Bearer " + key);
        c.setRequestProperty("Content-Type", "application/json");
        JSONObject body = new JSONObject(); body.put("model", model); body.put("messages", messages);
        body.put("temperature", 0.7);
        try (OutputStream os = c.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }
        int code = c.getResponseCode();
        InputStream stream = (code >= 200 && code < 300) ? c.getInputStream() : c.getErrorStream();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = br.readLine()) != null) out.append(line);
        }
        c.disconnect();
        JSONObject response = new JSONObject(out.toString());
        if (code < 200 || code >= 300) {
            String msg = response.optString("error", out.toString());
            if (msg.length() > 500) msg = msg.substring(0, 500);
            throw new Exception("HTTP " + code + ": " + msg);
        }
        return response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content", "ما وصلني حتى جواب.");
    }

    @Override protected void onDestroy() {
        super.onDestroy(); executor.shutdownNow();
    }
}
