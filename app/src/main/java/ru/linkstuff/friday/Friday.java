package ru.linkstuff.friday;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.annotation.Nullable;

import android.speech.RecognitionListener;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.linkstuff.friday.Executors.Security;
import ru.linkstuff.friday.HelperClasses.Corridor;
import ru.linkstuff.friday.PhraseService.ContinueWorkPhraseService;
import ru.linkstuff.friday.Splitter.Query;
import ru.linkstuff.friday.Splitter.Splitter;
import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.SpeechKit;

import static android.Manifest.permission.RECORD_AUDIO;

public class Friday extends Activity implements RecognitionListener {
    private static final String API_KEY = "6f29195e-0ac8-4979-ad8f-b70398be65b8";

    public static LinearLayout fridayMainLayout;
    private LinearLayout backgroundLayout;
    private Recognizer recognizer;
    private Intent phraseService;
    private Activity activity;
    private TextView monitor;
    private Context context;
    private String result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friday_layout);

        activity = this;
        context = this;

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.inKeyguardRestrictedInputMode()){
            Security.unlock(activity, context);
        }

        SpeechKit.getInstance().configure(context, API_KEY);
        createAndStart();
    }

    private void createAndStart(){
        activity.checkSelfPermission(RECORD_AUDIO);
        resetRecognizer();
        recognizer = Recognizer.create(
                Recognizer.Language.RUSSIAN,
                Recognizer.Model.NOTES,
                Friday.this
        );
        recognizer.start();
    }

    private void resetRecognizer() {
        if (recognizer != null){
            recognizer.cancel();
            recognizer = null;
        }
    }

    private void sendTextToMonitor(String text){
        if (monitor != null) monitor.setText(text);
    }

    @Override
    protected void onStart() {
        super.onStart();

        phraseService = new Intent(this, ContinueWorkPhraseService.class);
        fridayMainLayout = findViewById(R.id.friday_main);
        backgroundLayout = findViewById(R.id.friday_cr_layout_bg);
        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetRecognizer();
                startService(phraseService);
                finish();
            }
        });

        monitor = findViewById(R.id.friday_cr_monitor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SpeechKit.getInstance().configure(context, API_KEY);
        createAndStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        resetRecognizer();
    }


    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {
        Log.d("FRIDAY_RECOGNITION", result);
        result = recognition.getBestResultText();
        if (result.contains("пятница отмена")){
            resetRecognizer();
            startService(phraseService);
            finish();
        }
        sendTextToMonitor(result);
    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        resetRecognizer();

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();

        if (result != null && !result.equals("")){
            ArrayList<Query> queries = Splitter.getSplitter().getQuery(context, result);
            for (Query q:queries){
                Log.e("QUERY", q.getCommand() + " " + q.getArg() + " " + q.getExtraArg() + " " + q.getArgOfChange());
                Corridor.findRoom(activity, q);
            }
        }

        startService(phraseService);
        finish();
    }


    @Override
    public void onBackPressed() {
        resetRecognizer();
        super.onBackPressed();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
