package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dennis on 2/9/16.
 */
public class SpeechRecognitionHelper {

    static int VOICE_RECOGNITION_REQUEST_CODE = 2;

/**
 * Running the recognition process. Checks availability of recognition Activity,
 * If Activity is absent, send user to Google Play to install Google Voice Search.
 * If Activity is available, send Intent for running.
 *
 * @param callingActivity = Activity, that initializing recognition process
 */
    public static void run(Activity callingActivity) {
        // check if there is recognition Activity
        if (isSpeechRecognitionActivityPresented(callingActivity) == true) {
            // if yes – running recognition
            startRecognitionActivity(callingActivity);
        } else {
            // if no, then showing notification to install Voice Search
            Toast.makeText(callingActivity, "In order to activate speech recognition you must install Google Voice Search", Toast.LENGTH_LONG).show();
            // start installing process
            installGoogleVoiceSearch(callingActivity);
        }
    }

    /**
     * Checks availability of speech recognizing Activity
     *
     * @param callerActivity – Activity that called the checking
     */
    private static boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            // getting an instance of package manager
            PackageManager pm = callerActivity.getPackageManager();
            // a list of activities, which can process speech recognition Intent
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {    // if list not empty
                return true;                // then we can recognize the speech
            }
        } catch(Exception e) {

        }
        return false; // we have no activities to recognize the speech
    }
    /**
     * Send an Intent with request on speech
     * @param callerActivity  - Activity, that initiated a request
     */
    private static void startRecognitionActivity(Activity callerActivity) {

        // creating an Intent with “RecognizerIntent.ACTION_RECOGNIZE_SPEECH” action
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // giving additional parameters:
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say item (speak clearly)");    // user hint
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);    // setting recognition model, optimized for short phrases – search queries
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);    // quantity of results we want to receive
        //choosing only 1st -  the most relevant

        // start Activity ant waiting the result
        callerActivity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }
    /**
     * Запрашивает разрешения на становку Голосового Поиска Google, отображая диалог. Если разрешение
     * получино - направляет пользователя в маркет.
     * @param ownerActivity активити иниировавшая установку
     */
    private static void installGoogleVoiceSearch(final Activity ownerActivity) {

        Dialog dialog = new AlertDialog.Builder(ownerActivity)
                .setMessage("Для распознавания речи необходимо установить \"Голосовой поиск Google\"")	// сообщение
                .setTitle("Внимание")	// заголовок диалога
                .setPositiveButton("Установить", new DialogInterface.OnClickListener() {	// положительная кнопка

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {
                        }
                    }})

                .setNegativeButton("Отмена", null)	// негативная кнопка
                .create();

        dialog.show();	// показываем диалог
    }
}
