package com.ggzin694.personalcyberai;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
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
    private EditText commandInput; private TextView resultView; private TextView statusView;
    private final int bg = Color.rgb(6, 10, 24), panel = Color.rgb(15, 24, 48), cyan = Color.rgb(77, 224, 255), violet = Color.rgb(157, 117, 255);

    @Override protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); buildUi(); handleIntent(getIntent()); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleIntent(intent); }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16), dp(14), dp(16), dp(14)); root.setBackgroundColor(bg);
        TextView header = text("ENERGY-AI CONSOLE 0.4.0", 18, cyan); header.setTypeface(null, 1); root.addView(header, lp(-1, 32));
        TextView title = text("Central de Conhecimento", 25, Color.WHITE); title.setTypeface(null, 1); root.addView(title, lp(-1, 38));
        TextView sub = text("Planejamento  •  Criação  •  Soluções seguras", 13, Color.rgb(170, 185, 220)); root.addView(sub, lp(-1, 25));
        VixerEyeView eye = new VixerEyeView(this); root.addView(eye, lp(-1, 88));
        TextView online = text("●  NÚCLEO NEURAL ONLINE    ·    ENTRADA SEGURA ATIVA", 11, Color.rgb(126, 235, 190)); online.setGravity(Gravity.CENTER); root.addView(online, lp(-1, 28));
        TextView quickTitle = text("COMANDOS RÁPIDOS", 11, cyan); quickTitle.setPadding(0, dp(8), 0, dp(4)); root.addView(quickTitle, lp(-1, 26));
        String[] quick = {"Alarme", "Humanizar Artigo", "Criar Imagem 8K", "Modelar Espada 3D", "Código Python", "Voz & Áudio"};
        HorizontalScrollView qScroll = new HorizontalScrollView(this); LinearLayout qRow = new LinearLayout(this); qRow.setOrientation(LinearLayout.HORIZONTAL);
        for (String label : quick) { Button b = pill(label); b.setOnClickListener(v -> setCommand(label)); qRow.addView(b, lp(dp(142), 46, 6, 0, 6, 0)); } qScroll.addView(qRow); root.addView(qScroll, lp(-1, 56));
        TextView modules = text("MÓDULOS ENERGY-AI", 11, cyan); modules.setPadding(0, dp(6), 0, dp(4)); root.addView(modules, lp(-1, 25));
        String[] cards = {"Sistema de Alarme", "Fazer Texto e Humanizar", "Imagens & Vídeos IA", "Modelagem 3D & Animação", "Estudo Nacional", "Software & Dev", "Chat de Voz"};
        HorizontalScrollView mScroll = new HorizontalScrollView(this); LinearLayout mRow = new LinearLayout(this); mRow.setOrientation(LinearLayout.HORIZONTAL);
        for (String label : cards) { TextView card = text(label, 12, Color.WHITE); card.setGravity(Gravity.CENTER); card.setPadding(dp(8), 0, dp(8), 0); card.setBackground(round(panel, violet, 1)); card.setOnClickListener(v -> setCommand(label)); mRow.addView(card, lp(dp(150), 55, 0, 0, 8, 0)); } mScroll.addView(mRow); root.addView(mScroll, lp(-1, 65));
        statusView = text("●  Pronto · aguardando seu comando", 13, Color.rgb(126, 235, 190)); root.addView(statusView, lp(-1, 30));
        commandInput = new EditText(this); commandInput.setHint("• O que o núcleo deve fazer?"); commandInput.setHintTextColor(Color.rgb(125, 145, 180)); commandInput.setTextColor(Color.WHITE); commandInput.setTextSize(16); commandInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); commandInput.setGravity(Gravity.TOP | Gravity.START); commandInput.setPadding(dp(13), dp(12), dp(13), dp(12)); commandInput.setBackground(round(panel, Color.rgb(54, 116, 170), 1)); root.addView(commandInput, lp(-1, 82));
        Button send = pill("ENVIAR AO NÚCLEO  •"); send.setOnClickListener(v -> sendCommand()); root.addView(send, lp(-1, 48, 0, 8, 0, 0));
        ScrollView scroll = new ScrollView(this); resultView = text("RELATÓRIO DO NÚCLEO\n\nSua solicitação e a resposta do agente aparecerão aqui.\n\nAções externas nunca são executadas sem aprovação humana.", 14, Color.WHITE); resultView.setPadding(dp(14), dp(14), dp(14), dp(20)); resultView.setBackground(round(Color.rgb(10, 17, 35), Color.rgb(43, 75, 126), 1)); scroll.addView(resultView); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1)); setContentView(root);
    }

    private Button pill(String label) { Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(11); b.setAllCaps(false); b.setPadding(dp(4), 0, dp(4), 0); b.setBackground(round(Color.rgb(24, 40, 74), cyan, 1)); return b; }
    private TextView text(String value, int size, int color) { TextView v = new TextView(this); v.setText(value); v.setTextColor(color); v.setTextSize(size); return v; }
    private GradientDrawable round(int color, int stroke, int width) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(12)); d.setStroke(dp(width), stroke); return d; }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h); p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }
    private void setCommand(String label) { commandInput.setText(PREFIX + " " + label + ": "); commandInput.setSelection(commandInput.length()); commandInput.requestFocus(); statusView.setText("●  Comando selecionado · pronto para enviar"); }

    private void handleIntent(Intent intent) { if (intent == null) return; String incoming = null; if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) incoming = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT); else if (Intent.ACTION_SEND.equals(intent.getAction())) incoming = intent.getStringExtra(Intent.EXTRA_TEXT); if (incoming == null) { Uri data = intent.getData(); if (data != null) incoming = data.toString(); } if (incoming != null && !incoming.trim().isEmpty()) { String t = incoming.trim(); commandInput.setText(t.startsWith(PREFIX) ? t : PREFIX + " " + t); commandInput.setSelection(commandInput.length()); statusView.setText("●  Conteúdo recebido · pronto para enviar"); } else { commandInput.setText(PREFIX + " "); commandInput.setSelection(commandInput.length()); } }
    private void sendCommand() { String t = commandInput.getText().toString().trim(); if (t.isEmpty() || t.equals(PREFIX)) { statusView.setText("Digite um comando depois do prefixo •"); return; } if (!t.startsWith(PREFIX)) t = PREFIX + " " + t; final String command = t; statusView.setText("◌  Núcleo processando com segurança..."); resultView.setText("PROCESSANDO...\n\nA CENTRAL está analisando seu comando. Nenhuma ação externa será executada automaticamente."); executor.execute(() -> { try { JSONObject response = postJson(command); JSONObject a = response.optJSONObject("agent"); String agent = a != null ? a.optString("name", "CENTRAL") : "CENTRAL"; String message = response.optString("message", "Sem resposta textual."); String provider = response.optString("provider", "local_fallback"); runOnUiThread(() -> { statusView.setText("●  Concluído · " + agent + " · " + provider); resultView.setText("SUA SOLICITAÇÃO\n" + command + "\n\nRESPOSTA DO AGENTE\n" + message + "\n\nAções externas nunca são executadas sem aprovação humana."); }); } catch (Exception e) { runOnUiThread(() -> { statusView.setText("○  Serviço indisponível · tente novamente"); resultView.setText("Não foi possível concluir agora. O serviço pode estar acordando ou sem conexão.\n\nNenhuma ação externa foi executada."); }); } }); }
    private JSONObject postJson(String command) throws Exception { HttpURLConnection c = (HttpURLConnection) new URL(API_URL).openConnection(); c.setRequestMethod("POST"); c.setConnectTimeout(15000); c.setReadTimeout(60000); c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); JSONObject body = new JSONObject(); body.put("message", command); try (OutputStream out = c.getOutputStream()) { out.write(body.toString().getBytes(StandardCharsets.UTF_8)); } InputStream stream = c.getResponseCode() >= 400 ? c.getErrorStream() : c.getInputStream(); if (stream == null) throw new IllegalStateException("empty response"); StringBuilder s = new StringBuilder(); try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) { String line; while ((line = r.readLine()) != null) s.append(line); } return new JSONObject(s.toString()); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onDestroy() { executor.shutdownNow(); super.onDestroy(); }
}
