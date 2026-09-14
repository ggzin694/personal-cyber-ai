package com.ggzin694.personalcyberai;

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
}
