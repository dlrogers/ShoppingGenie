package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater ;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.symdesign.smartlist.MainActivity.email ;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.currList;

/**
 * Created by dennis on 11/20/16.
 * Dialog to enter auth info for the first time
 */

public class AdminDialog extends DialogFragment {
    EditText emailView,passwdView;

    public AdminDialog() {
        // Empty contstuctor required for DialogFragment
    }
    public interface AdminDialogListener {
        void onFinishAdminDialog(String email,String passwd,Boolean reg);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View optionView = inflater.inflate(R.layout.admin, container, false);
        emailView = (EditText) optionView.findViewById(R.id.email);
        passwdView = (EditText) optionView.findViewById(R.id.passwd);
        Button checkViewCreate = (Button) optionView.findViewById(R.id.check_create);
        Button checkViewSign = (Button) optionView.findViewById(R.id.check_sign);
        getDialog().setTitle(R.string.registration);
        emailView.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        checkViewCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String nemail = emailView.getText().toString();
            String npasswd = passwdView.getText().toString();
            AdminDialogListener activity = (AdminDialogListener) getActivity();
            activity.onFinishAdminDialog(nemail,npasswd,true);
            getDialog().dismiss();
            }
        });
        checkViewSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            String nemail = emailView.getText().toString();
            String npasswd = passwdView.getText().toString();
            AdminDialogListener activity = (AdminDialogListener) getActivity();
            activity.onFinishAdminDialog(nemail,npasswd,false);
            getDialog().dismiss();
            }
        });
        return optionView;
    }
//        protected void onPostExecute(String result) {
//            log("post execute admin");
//        }
}
