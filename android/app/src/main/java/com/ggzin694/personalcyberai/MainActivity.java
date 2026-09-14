package com.ggzin694.personalcyberai;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends android.app.Activity {
    private static final String API_URL = "https://personal-cyber-ai.onrender.com/api/chat";
    private static final String PREFIX = "•";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private EditText commandInput;
    private TextView resultView;
    private TextView statusView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); buildUi(); handleIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); handleIntent(intent);
    }

    private void buildUi() {
        int padding = dp(18);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(Color.rgb(7, 13, 29));

        TextView eyebrow = new TextView(this);
        eyebrow.setText("VIXER EYE  //  PERSONAL CYBER AI");
        eyebrow.setTextColor(Color.rgb(67, 219, 255)); eyebrow.setTextSize(12);
        root.addView(eyebrow, new LinearLayout.LayoutParams(-1, dp(24)));

        TextView title = new TextView(this);
        title.setText("Conhecimento  •  Planejamento  •  Soluções");
        title.setTextColor(Color.WHITE); title.setTextSize(21);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(38)));

        VixerEyeView eye = new VixerEyeView(this);
        LinearLayout.LayoutParams eyeParams = new LinearLayout.LayoutParams(-1, dp(145));
        eyeParams.gravity = Gravity.CENTER_HORIZONTAL; root.addView(eye, eyeParams);

        TextView brain = new TextView(this);
        brain.setText("ENERGY-AI // NÚCLEO NEURAL  ·  ONLINE\nEntrada segura: Compartilhar / Selecionar texto");
        brain.setTextColor(Color.rgb(154, 224, 255)); brain.setTextSize(12); brain.setGravity(Gravity.CENTER);
        root.addView(brain, new LinearLayout.LayoutParams(-1, dp(45)));

        statusView = new TextView(this); statusView.setText("●  Pronto para receber contexto");
        statusView.setTextColor(Color.rgb(131, 214, 160)); statusView.setTextSize(14);
        root.addView(statusView, new LinearLayout.LayoutParams(-1, dp(30)));

        commandInput = new EditText(this); commandInput.setHint("• descreva o que devo verificar");
        commandInput.setHintTextColor(Color.rgb(140, 155, 180)); commandInput.setTextColor(Color.WHITE);
        commandInput.setTextSize(16); commandInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commandInput.setGravity(Gravity.TOP | Gravity.START); commandInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        commandInput.setBackgroundColor(Color.rgb(19, 32, 58)); root.addView(commandInput, new LinearLayout.LayoutParams(-1, dp(105)));

        Button send = new Button(this); send.setText("ENVIAR PARA O NÚCLEO  •"); send.setTextColor(Color.WHITE);
        send.setOnClickListener(v -> sendCommand());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(52)); buttonParams.topMargin = dp(10);
        root.addView(send, buttonParams);

        ScrollView scroll = new ScrollView(this);
        resultView = new TextView(this); resultView.setTextColor(Color.WHITE); resultView.setTextSize(15);
        resultView.setPadding(dp(12), dp(16), dp(12), dp(16));
        resultView.setText("O relatório do agente aparecerá aqui.\n\nAções externas nunca são executadas sem aprovação.");
        scroll.addView(resultView); LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, 0, 1);
        scrollParams.topMargin = dp(10); root.addView(scroll, scrollParams); setContentView(root);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return; String incoming = null;
        if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) incoming = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        else if (Intent.ACTION_SEND.equals(intent.getAction())) incoming = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (incoming == null) { Uri data = intent.getData(); if (data != null) incoming = data.toString(); }
        if (incoming != null && !incoming.trim().isEmpty()) {
            String text = incoming.trim(); commandInput.setText(text.startsWith(PREFIX) ? text : PREFIX + " " + text);
            commandInput.setSelection(commandInput.length()); statusView.setText("Conteúdo recebido pelo Compartilhar · pronto para enviar");
        } else { commandInput.setText(PREFIX + " "); commandInput.setSelection(commandInput.length()); }
    }

    private void sendCommand() {
        String text = commandInput.getText().toString().trim();
        if (text.isEmpty() || text.equals(PREFIX)) { statusView.setText("Digite um comando depois do prefixo •"); return; }
        if (!text.startsWith(PREFIX)) text = PREFIX + " " + text; final String command = text;
        statusView.setText("Enviando para a CENTRAL..."); resultView.setText("Processando com segurança...");
        executor.execute(() -> { try {
            JSONObject response = postJson(command); JSONObject agentObject = response.optJSONObject("agent");
            String agent = agentObject != null ? agentObject.optString("name", "CENTRAL") : "CENTRAL";
            String message = response.optString("message", "Sem resposta textual."); String provider = response.optString("provider", "local_fallback");
            runOnUiThread(() -> { statusView.setText("Concluído · " + agent + " · " + provider); resultView.setText(message); });
        } catch (Exception error) { runOnUiThread(() -> { statusView.setText("Não foi possível concluir agora"); resultView.setText("O serviço pode estar acordando ou sem conexão. Tente novamente em alguns segundos."); }); } });
    }

    private JSONObject postJson(String command) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection(); connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000); connection.setReadTimeout(60000); connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); JSONObject body = new JSONObject(); body.put("message", command);
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8); try (OutputStream output = connection.getOutputStream()) { output.write(bytes); }
        InputStream stream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream(); if (stream == null) throw new IllegalStateException("empty response");
        StringBuilder content = new StringBuilder(); try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) { String line; while ((line = reader.readLine()) != null) content.append(line); }
        return new JSONObject(content.toString());
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
}
