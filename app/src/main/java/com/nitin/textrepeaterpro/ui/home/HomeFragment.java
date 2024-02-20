package com.nitin.textrepeaterpro.ui.home;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import com.nitin.textrepeaterpro.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextToSpeech textToSpeech;


    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;

    private EditText inputEditText, repetitionEditText;
    private Button repeatButton, copyButton, shareButton;
    private CheckBox newLineCheckBox;
    private TextView resultTextView;
    private SpeechRecognizer speechRecognizer;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());





        inputEditText = binding.inputEditText;
        ImageView voiceInputButton = binding.voiceInputButton;

        repetitionEditText = binding.repetitionEditText;
        repeatButton = binding.repeatButton;
        ImageView copyButton = binding.copyButton;
        ImageView shareButton = binding.shareButton;
        newLineCheckBox = binding.newLineCheckBox;
        resultTextView = binding.resultTextView;
        ImageView textToSpeechButton = binding.textToSpeechButton;

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                try {
                    // Parse the input value and check if it's within the allowed range
                    int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source.subSequence(start, end).toString());
                    if (input <= 10000) {
                        return null; // Accept the input
                    }
                } catch (NumberFormatException e) {
                    // Handle the case where the input is not a valid number
                }

                // Reject the input
                return "";
            }
        };

        repetitionEditText.setFilters(new InputFilter[]{filter});

        textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Set language (you can change this based on your requirements)
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getContext(), "Text-to-speech language is not supported.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Text-to-speech initialization failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        textToSpeechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertToAudioAndShare();
            }
        });


        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatText();
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyResultToClipboard();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareResult();
            }
        });
        voiceInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check and request RECORD_AUDIO permission if not granted
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            RECORD_AUDIO_PERMISSION_CODE);
                }
                else {

                    startVoiceInput();
                }
            }
        });

        // Set up RecognitionListener for SpeechRecognizer
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the speech recognizer is ready for speech input
            }

            @Override
            public void onBeginningOfSpeech() {
                // Called when the user starts to speak
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called when the RMS changes
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Called when the audio buffer is received
            }

            @Override
            public void onEndOfSpeech() {
                // Called when the user stops speaking
            }

            @Override
            public void onError(int error) {
                // Called when an error occurs during recognition
                Toast.makeText(getActivity(), "Voice recognition error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                // Called when recognition results are available
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    inputEditText.setText(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial recognition results are available
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when an event related to the recognition is received
            }
        });



        return root;
    }
    // Override onRequestPermissionsResult to handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with voice input functionality
                startVoiceInput();
            } else {
                // Permission denied, inform the user and handle accordingly
                Toast.makeText(getContext(), "Permission denied. Voice input may not work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startVoiceInput() {
        // Start voice recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");  // Change the language code if needed
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        speechRecognizer.startListening(intent);
    }

    private void repeatText() {
        String inputText = inputEditText.getText().toString();
        String repetitionCountText = repetitionEditText.getText().toString();

        // Check if repetitionCountText is empty or not a valid integer
        if (repetitionCountText.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a valid repetition count", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int repetitionCount = Integer.parseInt(repetitionCountText);
            boolean includeNewLine = newLineCheckBox.isChecked();

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < repetitionCount; i++) {
                result.append(inputText);
                if (includeNewLine) {
                    result.append("\n");
                } else if (i < repetitionCount - 1) {
                    result.append(" ");
                }
            }

            resultTextView.setText(result.toString());
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid integer
            Toast.makeText(getActivity(), "Invalid repetition count. Please enter a valid integer.", Toast.LENGTH_SHORT).show();
        }
    }


    private void copyResultToClipboard() {
        String resultText = resultTextView.getText().toString();
        if (!resultText.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Repeated Text", resultText);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), "Result copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Nothing to copy", Toast.LENGTH_SHORT).show();
        }
    }
    // Add the convertToAudioAndShare method
    private void convertToAudioAndShare() {
        String resultText = inputEditText.getText().toString(); // Get your result text

        if (!resultText.isEmpty()) {
            // Set UtteranceId to identify the speech synthesis process
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");

            // Speak the text and set an UtteranceProgressListener
            textToSpeech.speak(resultText, TextToSpeech.QUEUE_FLUSH, params);

        } else {
            Toast.makeText(getContext(), "Nothing to convert to audio", Toast.LENGTH_SHORT).show();
        }
    }





    private void shareResult() {
        String resultText = resultTextView.getText().toString();
        if (!resultText.isEmpty()) {
            // Create an Intent with ACTION_SEND
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);

            // Set the text to be shared
            sendIntent.putExtra(Intent.EXTRA_TEXT, resultText);
            sendIntent.setType("text/plain");

            // Launch the share sheet
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        } else {
            Toast.makeText(getActivity(), "Nothing to share", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroyView() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroyView();
        binding = null;
    }
}