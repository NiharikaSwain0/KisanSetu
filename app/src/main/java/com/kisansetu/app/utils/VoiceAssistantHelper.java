package com.kisansetu.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceAssistantHelper {

    private static final String TAG = "VoiceAssistantHelp er";
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private VoiceAssistantListener listener;
    private boolean isTtsReady = false;
    private String pendingSpeechText = null;

    public interface VoiceAssistantListener {
        void onResults(String text);
        void onError(String error);
    }

    public VoiceAssistantHelper(Context context, VoiceAssistantListener listener) {
        this.context = context;
        this.listener = listener;
        initSpeechRecognizer();
        initTextToSpeech();
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) { 
                Log.d(TAG, "Ready for speech"); 
                // We can't easily show a toast from here without context being activity or using handler
            }

            @Override
            public void onBeginningOfSpeech() { Log.d(TAG, "Beginning of speech"); }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() { Log.d(TAG, "End of speech"); }

            @Override
            public void onError(int error) {
                String message = getErrorText(error);
                Log.e(TAG, "Error (" + error + "): " + message);
                listener.onError(message + " (Error Code: " + error + ")");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    Log.d(TAG, "Speech Results: " + text);
                    listener.onResults(text);
                } else {
                    Log.d(TAG, "No speech results matches");
                    listener.onError("Could not understand, please try again");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("hi", "IN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Hindi language is not supported or missing data");
                    textToSpeech.setLanguage(Locale.ENGLISH);
                }
                isTtsReady = true;
                Log.d(TAG, "TTS Initialized successfully");
                
                // If there was a pending speech request, speak it now
                if (pendingSpeechText != null) {
                    speak(pendingSpeechText);
                    pendingSpeechText = null;
                }
            } else {
                Log.e(TAG, "TTS Initialization failed");
            }
        });
    }

    public void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition not available on this device");
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        
        // Better language support for bilingual (Hindi + English)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false);
        
        // For devices that support it, this helps with mixed language
        intent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"hi-IN", "en-IN"});
        
        try {
            speechRecognizer.startListening(intent);
        } catch (Exception e) {
            listener.onError("Error starting listener: " + e.getMessage());
        }
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public void speak(String text) {
        if (textToSpeech != null && isTtsReady) {
            Log.d(TAG, "Speaking: " + text);
            // Check if text contains Hindi characters to set language
            if (text.matches(".*[\\u0900-\\u097F]+.*")) {
                textToSpeech.setLanguage(new Locale("hi", "IN"));
            } else {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
            
            // Set speech rate and pitch for clarity
            textToSpeech.setSpeechRate(1.0f);
            textToSpeech.setPitch(1.0f);
            
            Bundle params = new Bundle();
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "VoiceAssistantID");
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "VoiceAssistantID");
        } else {
            Log.w(TAG, "TTS not ready, queuing speech: " + text);
            pendingSpeechText = text;
        }
    }

    public void shutdown() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT: return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK: return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH: return "No match found";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER: return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "No speech input";
            default: return "Unknown error";
        }
    }
}
