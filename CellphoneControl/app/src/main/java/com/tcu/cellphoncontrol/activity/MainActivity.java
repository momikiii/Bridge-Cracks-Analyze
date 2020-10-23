package com.tcu.cellphoncontrol.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kongqw.rockerlibrary.view.RockerView;
import com.tcu.cellphoncontrol.R;
import com.tcu.cellphoncontrol.service.TcpCommandService;
import com.tcu.cellphoncontrol.service.UDPFrameRecService;
import com.tcu.cellphoncontrol.view.VerticalSeekBar;
import com.xuexiang.xui.XUI;
import com.xuexiang.xui.utils.ViewUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.io.File;

import static com.xuexiang.xui.utils.ViewUtils.Direction.BOTTOM_TO_TOP;
import static com.xuexiang.xui.utils.ViewUtils.Direction.LEFT_TO_RIGHT;
import static com.xuexiang.xui.utils.ViewUtils.Direction.RIGHT_TO_LEFT;
import static com.xuexiang.xui.utils.ViewUtils.Direction.TOP_TO_BOTTOM;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    public static Bitmap bitmap=null;
    private String host;
    private int port;
    public static Context context;
    private TextView textViewTest;
    private TextView showImageViewSize;
    private ImageView cameraDisplayImageView;
    public TcpCommandService.SendCommandBinder sendCommandBinder=null; //Use this binder to notify service sending command data
    private ServiceConnection tcpConnection=new ServiceConnection() { //The connection between Main activity and TcpCommandService
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sendCommandBinder=(TcpCommandService.SendCommandBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public UDPFrameRecService.NoticeBinder noticeUDPBinder=null;//Use this binder to notify UDP service
    private ServiceConnection udpConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(MainActivity.this,"Activity与Service连接成功",Toast.LENGTH_LONG).show();
            noticeUDPBinder=(UDPFrameRecService.NoticeBinder)service;
            bitmap=noticeUDPBinder.getBitmap();
            //Log.i("BITMAP",bit.toString());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_layout);

        //--------------移植UI---------------
        VerticalSeekBar verticalSeekBarLeft = findViewById(R.id.leftSeekBar);
        verticalSeekBarLeft.setThumbSize(22, 22);
        verticalSeekBarLeft.setmInnerProgressWidth(7);
        VerticalSeekBar verticalSeekBarRight = findViewById(R.id.rightSeekBar);
        verticalSeekBarRight.setThumbSize(22, 22);
        verticalSeekBarRight.setmInnerProgressWidth(7);

        findViewById(R.id.settings).setOnClickListener(this);

        findViewById(R.id.album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivity(intent);
            }
        });

        //--------------移植UI---------------
        //context=getApplicationContext();

        context=this;//Get the context of this activity so that the static functions can use

        Intent startInfoIntent=getIntent();
        host=startInfoIntent.getStringExtra("tcpHost");
        port=startInfoIntent.getIntExtra("tcpPort",50);

        Intent startTcpServiceIntent=new Intent(this,TcpCommandService.class);
        startTcpServiceIntent.putExtra("tcpHost",host);
        startTcpServiceIntent.putExtra("tcpPort",port);
        //Start and bind TcpCommandService
        startService(startTcpServiceIntent);
        bindService(startTcpServiceIntent,tcpConnection,BIND_AUTO_CREATE);
        initButtons();
        initRockers();
        initAnimations();
        //Start and bind UDPFrameRecService
        startService(new Intent(this,UDPFrameRecService.class));
        bindService(new Intent(this,UDPFrameRecService.class),udpConnection,BIND_AUTO_CREATE);

        //textViewTest=(TextView)findViewById(R.id.texttest);
        cameraDisplayImageView=(ImageView)findViewById(R.id.cameraDisplay);

//        showImageViewSize=(TextView)findViewById(R.id.imageViewSize);

    }

    public void savesys(){
        //UDPFrameRecService ne = new UDPFrameRecService();
        //Intent intent=new Intent(MainActivity.this,UDPFrameRecService.class);
        //startService(intent);
       // System.out.println("******************");
       // System.out.println(ne.getbitmap());
        //Bitmap bitmap = ne.getbitmap();
        //Bitmap bitmap=bit;
        MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,"title","description");
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File("/sdcard/Boohee/image.jpg"))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(tcpConnection);
        stopService(new Intent(this,TcpCommandService.class));
        unbindService(udpConnection);
        stopService(new Intent(this,UDPFrameRecService.class));
    }


//--------------------------------------------------  momiki's code  -------------------------------------------------------------

    public void initRockers(){
        RockerView rockerViewLeft = (RockerView)findViewById(R.id.rockerViewLeft);
        //自定回传方向
        final RockerView.Direction center = RockerView.Direction.DIRECTION_CENTER;
        if(rockerViewLeft != null){
            rockerViewLeft.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            rockerViewLeft.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
                @Override
                public void onStart() {
                }

                @Override
                public void direction(RockerView.Direction direction) {
                    Log.d(TAG, "direction: " + setDirection(direction));
                }

                @Override
                public void onFinish() {
                    setDirection(center);
                }
            });
        }
    }

    private String setDirection(RockerView.Direction direction) {
        String message = null;
        switch (direction) {
            case DIRECTION_CENTER:
                message = "中间";
                Log.d(TAG, "getDirection: 中间");
                sendCommandBinder.sendCommand("DirStop");
                break;
            case DIRECTION_LEFT:
                message = "左";
                sendCommandBinder.sendCommand("DirLeft");
                break;
            case DIRECTION_RIGHT:
                message = "右";
                sendCommandBinder.sendCommand("DirRight");
                break;
            case DIRECTION_UP:
                message = "上";
                sendCommandBinder.sendCommand("DirForward");
                break;
            case DIRECTION_DOWN:
                message = "下";
                sendCommandBinder.sendCommand("DirBack");
                break;
            case DIRECTION_UP_LEFT:
                message = "左上";
                break;
            case DIRECTION_UP_RIGHT:
                message = "右上";
                break;
            case DIRECTION_DOWN_LEFT:
                message = "左下";
                break;
            case DIRECTION_DOWN_RIGHT:
                message = "右下";
                break;
            default:
                break;
        }
        return message;
    }

    private void initAnimations() {
        ViewUtils.fadeIn(findViewById(R.id.tcpConnect), 5500, null);
        ViewUtils.fadeIn(findViewById(R.id.udpconnect), 5500, null);
        ViewUtils.slideIn(findViewById(R.id.speed_icon), 1500, null,TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.speed), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.speed_unit), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.horizontal_distance_icon), 1500, null,TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.horizontal_distance), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.horizontal_distance_unit), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.vertical_distance_icon), 1500, null,TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.vertical_distance), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.vertical_distance_unit), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.changeMode), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.miniSeekBar), 1500, null, TOP_TO_BOTTOM);
        ViewUtils.slideIn(findViewById(R.id.leftSeekBar), 1500, null, LEFT_TO_RIGHT);
        ViewUtils.slideIn(findViewById(R.id.album), 1500, null, LEFT_TO_RIGHT);
        ViewUtils.slideIn(findViewById(R.id.photograph), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.rightSeekBar), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.J), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.J_value), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.R), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.R_value), 1500, null, RIGHT_TO_LEFT);
        ViewUtils.slideIn(findViewById(R.id.bottomSeekBar), 1500, null, BOTTOM_TO_TOP);
        ViewUtils.fadeIn(findViewById(R.id.X), 4500, null);
        ViewUtils.fadeIn(findViewById(R.id.X_value), 4500, null);
        ViewUtils.fadeIn(findViewById(R.id.Y), 4500, null);
        ViewUtils.fadeIn(findViewById(R.id.Y_value), 4500, null);
        ViewUtils.fadeIn(findViewById(R.id.Z), 4500, null);
        ViewUtils.fadeIn(findViewById(R.id.Z_value), 4500, null);
    }
    //--------------------------------------------------  momiki's code  -------------------------------------------------------------





    /**
     * Init the Direction Buttons and Camera Buttons, set OnTouchListener for these buttons so 
     * command can be sent when buttons are down or up.
     */
    public void initButtons()
    {
        Button buttonCut=(Button)findViewById(R.id.photograph);
        buttonCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savesys();
            }
        });
        //TODO 断开连接
//        Button buttonClose=(Button)findViewById(R.id.buttonClose);
//        buttonClose.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            sendCommandBinder.closeOrShutdown("Close"); //Notify the car this client will close the connection
//        }
//    });
        //TODO 关闭
//        Button buttonShutdown=(Button)findViewById(R.id.buttonShutdown);
//        buttonShutdown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendCommandBinder.closeOrShutdown("Shutdown"); //Notify the car to shutdown itself
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        new MaterialDialog.Builder(this)
                .title("设置")
                .items(R.array.menu_values)
                .show();
    }

    /**
     * When a control button is down or up, send the command 
     */
    class ButtonListener implements View.OnTouchListener{
        private String buttonUpCommand; //Specify the command when button is up
        private String buttonDownCommand; //Specify the command when button is down
        public ButtonListener(String buttonUpCommand,String buttonDownCommand){
            this.buttonUpCommand=buttonUpCommand;
            this.buttonDownCommand=buttonDownCommand;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_UP:
                    //Notify the TcpCommandService to send command
                    sendCommandBinder.sendCommand(buttonUpCommand);
                    //Toast.makeText(MainActivity.this,buttonUpCommand,Toast.LENGTH_SHORT).show();
                    break;
                case MotionEvent.ACTION_DOWN:
                    //Notify the TcpCommandService to send command
                    sendCommandBinder.sendCommand(buttonDownCommand);
                    //Toast.makeText(MainActivity.this,buttonDownCommand,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    }


    public static final int TOAST_TEXT=1;
    public static final int CLOSE_OR_SHUTDOWN=2;
    public static final int UPDATE_IMAGEVIEW=3;
    /**
     * Use the handler to update main activity according to service's message
     */
    //TODO Toast需更改
    @SuppressLint("HandlerLeak")
    public static Handler updateUIHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TOAST_TEXT: //Display the toast according to the message from service
                    Toast.makeText(context,(String)msg.obj,Toast.LENGTH_SHORT).show();
                    break;
                case CLOSE_OR_SHUTDOWN: //The tcp service has sent close or shutdown command, and the main activity should exit now
                    ((MainActivity)context).finish();
                    break;
                case UPDATE_IMAGEVIEW: //The udp service has received a frame, the main activity should display it
                    //((MainActivity)context).textViewTest.setText((String)msg.obj);
                    //int width=((MainActivity)context).cameraDisplayImageView.getWidth();
                    //int height=((MainActivity)context).cameraDisplayImageView.getHeight();
                    //((MainActivity)context).showImageViewSize.setText("width: "+width+" height: "+height);
                    //bit=(Bitmap)msg.obj;
                    //Log.d("MainActivity","bitmap is"+bit.toString());
                    ((MainActivity)context).cameraDisplayImageView.setImageBitmap((Bitmap)msg.obj);

                default:
                    break;
            }
        }
    };
}
