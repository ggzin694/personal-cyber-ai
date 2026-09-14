package com.ggzin694.personalcyberai;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;

public class CyberVoiceInteractionSession extends VoiceInteractionSession {
    public CyberVoiceInteractionSession(Context context) {
        super(context);
    }

    private void openMainActivity() {
        Intent launch = new Intent(getContext(), MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startAssistantActivity(launch);
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        openMainActivity();
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        openMainActivity();
    }
}
