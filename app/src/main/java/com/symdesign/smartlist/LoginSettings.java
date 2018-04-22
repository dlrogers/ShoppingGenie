package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.syncReg;
import static com.symdesign.smartlist.MainActivity.mainActivity;
import static com.symdesign.smartlist.MainActivity.showToast;
/*
 * Created by dennis on 2/12/17.
 */

public class LoginSettings extends Activity  implements GetLists.LoginListener {
    EditText emailView,passwdView;
    TextView resetView;
    ImageView checkView;
    CheckBox syncBox;
    Toast toast;
    Context context;
    Activity activity;
    Boolean doSync = true;
    static PopupWindow popupWindow;
    static GetLists getLists;

    public LoginSettings() {
        // Empty contstuctor required for DialogFragment
    }
//    public interface SettingsListener {
//        void onFinishSettings(String email,String passwd);
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("Starting LoginSettings");
        context = (Context) this;
        activity = this;
        setContentView(R.layout.login);
        final View parentView = (RelativeLayout) findViewById(R.id.main);
        emailView = (EditText) findViewById(R.id.email);
        emailView.requestFocus();
        emailView.setText(MainActivity.prefs.getString("email","no_email"));
        passwdView = (EditText) findViewById(R.id.passwd);
        resetView = (TextView) findViewById(R.id.reset);
        checkView = (ImageView) findViewById(R.id.check);
        syncBox = (CheckBox) findViewById(R.id.sync);
        syncBox.setChecked(true);
        doSync = true;
        emailView.requestFocus();
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get email and passwd from views
                log("Check button pressed");
                String nemail = emailView.getText().toString();
                String npasswd = passwdView.getText().toString();
                syncReg = true;
                // Use SyncList to get database from server
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popup_window,null);
                popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                Button abortButton = (Button) popupView.findViewById(R.id.abort_button);
                abortButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!GetLists.started){
                            showToast(context,"\n Aborting!\n",Toast.LENGTH_LONG);
                        popupWindow.dismiss();
                        getLists.cancel(true);
                        }
                    }
                });
                popupWindow.setContentView(popupView);
//                popupWindow.showAsDropDown(passwdView,Gravity.NO_GRAVITY,0,-30);
                popupWindow.showAsDropDown(passwdView,0,-30);
                getLists = new GetLists(mainActivity,nemail,npasswd, doSync);
                getLists.setListener((GetLists.LoginListener) context);
                getLists.execute();
            }
        });
        syncBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!syncBox.isChecked()){
                    doSync = false;
                }
            }
        });
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast(context,"\nAn Email is being sent to "+email+" containing instructions to reset your password\n",
                        Toast.LENGTH_LONG);
                new Lostpw(email).execute();
            }
        });
        log("Leaving LoginSettings");
        return ;
    }
    @Override
    public void fin(){
        popupWindow.dismiss();
        finish();
    }
}
