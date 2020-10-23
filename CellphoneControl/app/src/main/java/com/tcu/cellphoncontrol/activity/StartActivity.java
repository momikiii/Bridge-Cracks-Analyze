package com.tcu.cellphoncontrol.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tcu.cellphoncontrol.R;
import com.tcu.cellphoncontrol.view.VerticalSeekBar;
import com.xuexiang.xui.utils.ViewUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.popupwindow.popup.XUISimplePopup;
import com.xuexiang.xui.widget.textview.marqueen.ComplexItemEntity;
import com.xuexiang.xui.widget.textview.marqueen.ComplexViewMF;
import com.xuexiang.xui.widget.textview.marqueen.MarqueeFactory;
import com.xuexiang.xui.widget.textview.marqueen.MarqueeView;
import com.xuexiang.xui.widget.textview.marqueen.SimpleNoticeMF;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import static com.xuexiang.xui.XUI.getContext;
import static com.xuexiang.xui.utils.ViewUtils.Direction.BOTTOM_TO_TOP;
import static com.xuexiang.xui.utils.ViewUtils.Direction.LEFT_TO_RIGHT;
import static com.xuexiang.xui.utils.ViewUtils.Direction.RIGHT_TO_LEFT;
import static com.xuexiang.xui.utils.ViewUtils.Direction.TOP_TO_BOTTOM;


public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private String host = "192.168.137.1";
    private int port = 50;

    private TextView textViewIP;
    private TextView textViewPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        //Get the last IP and port value, and show them
        getInfoFromSharedPreference();
        textViewIP = (TextView) findViewById(R.id.carIP);
        textViewPort = (TextView) findViewById(R.id.carPort);
        textViewIP.setText(host);
        textViewPort.setText(String.valueOf(port));
        Button buttonEnter = (Button) findViewById(R.id.enter);
        buttonEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host = textViewIP.getText().toString();
                port = Integer.parseInt(textViewPort.getText().toString());
                Intent startMainActivityIntent = new Intent(StartActivity.this, MainActivity.class);
                startMainActivityIntent.putExtra("tcpHost", host);
                startMainActivityIntent.putExtra("tcpPort", port);
                startActivity(startMainActivityIntent);
                //Save the ip and port values
                setInfoFromSharedPreference();
                finish();
            }
        });

        initAnimations();

    }

    private void initAnimations() {
        ViewUtils.slideIn(findViewById(R.id.enter), 1500, null, BOTTOM_TO_TOP);
        ViewUtils.slideIn(findViewById(R.id.carIP), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.carPort), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.carIPTextView), 1500, null, LEFT_TO_RIGHT);
        ViewUtils.slideIn(findViewById(R.id.carPortTextView), 1500, null, LEFT_TO_RIGHT);

    }


    private void getInfoFromSharedPreference() {
        SharedPreferences preferences = getSharedPreferences("connectPara", MODE_PRIVATE);
        host = preferences.getString("IP", "192.168.1.1");
        port = preferences.getInt("Port", 50);

    }

    private void setInfoFromSharedPreference() {
        SharedPreferences.Editor editor = getSharedPreferences("connectPara", MODE_PRIVATE).edit();
        editor.putString("IP", host);
        editor.putInt("Port", port);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        new MaterialDialog.Builder(this)
                .title("设置")
                .items(R.array.menu_values)
                .show();
    }
}
