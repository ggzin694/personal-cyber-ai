package com.ggzin694.personalcyberai;

import android.content.Intent;
import android.service.voice.VoiceInteractionService;

/**
 * Native Android assistant entry point.
 * The service is intentionally passive: it does not monitor the device,
 * read conversations, or execute external actions automatically.
 */
public class CyberVoiceInteractionService extends VoiceInteractionService {
    @Override
    public void onReady() {
        super.onReady();
    }

    @Override
    public void onLaunchVoiceAssistFromKeyguard() {
        Intent launch = new Intent(this, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(launch);
    }
}
