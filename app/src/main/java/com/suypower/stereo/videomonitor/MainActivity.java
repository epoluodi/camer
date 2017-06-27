package com.suypower.stereo.videomonitor;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private MediaRecorder mediarecorder;// 录制视频的类
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private View record_state;
    private LinearLayout analysis;
    private TextView record_time;
    private MenuView menuView;
    private ImageView btnmenu;
    private FrameLayout mainview;
    private String uuid = "";

    private int index = 0;
    private Timer timer;//计时
    private int records;//时间

    private boolean IsRecording;
    private boolean IsCanClick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        surfaceView = (SurfaceView) findViewById(R.id.surface);
        mainview = (FrameLayout) findViewById(R.id.mainview);
        record_state = findViewById(R.id.record_state);
        record_state.setVisibility(View.GONE);
        analysis = (LinearLayout) findViewById(R.id.analysis);
        analysis.setVisibility(View.GONE);
        record_time = (TextView) findViewById(R.id.record_time);
        record_time.setText("空闲");
        btnmenu = (ImageView) findViewById(R.id.btnmenu);
        btnmenu.setOnClickListener(onClickListenermenu);
        menuView = new MenuView(this, menuInterFace);
        initPath();
        init();


        if (!checkFileIsExits())
            CopyRaw();

        SuyDB suyDB = new SuyDB(this, getFilesDir() + "/db.sqlite", false);
        SuyDB.setSuyDB(suyDB);
        IsRecording = false;
        IsCanClick = false;
    }


    /**
     * 初始化目录结构
     */
    private void initPath() {

        File file = new File(Environment.getExternalStorageDirectory() + "/录像");
        if (!file.exists())
            file.mkdir();
//        file = new File(Environment.getExternalStorageDirectory() + "/录像/视频");
//        if (!file.exists())
//            file.mkdir();
//        file = new File(Environment.getExternalStorageDirectory() + "/录像/声音");
//        if (!file.exists())
//            file.mkdir();

    }

    private void init() {
        SurfaceHolder holder;
        holder = surfaceView.getHolder();// 取得holder
        holder.addCallback(this); // holder加入回调接口
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera != null)
            mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCamera != null)
            mCamera.stopPreview();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            surfaceHolder = null;
            surfaceView = null;
        }
        if (mediarecorder != null) {
            mediarecorder.stop();
            mediarecorder.release();
            mediarecorder = null;
        }
        IsRecording = false;
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i("key", String.valueOf(keyCode));

        switch (keyCode) {
            case 4:
                if (menuView.getShow()) {
                    mainview.removeView(menuView.getMenuView());
                    menuView.setShow(false);
                }

                return false;
            case 133://停止录像
                if (!IsRecording || IsCanClick)
                    return false;
                IsCanClick = true;
                StopRecord();
                OpenCamera();
                break;
            case 131://开始录像
                if (IsRecording || IsCanClick)
                    return false;
                IsCanClick = true;
                CloseCmera();
                startRecord();

                break;
            case 136:
                if (!menuView.getShow()) {
                    mainview.addView(menuView.getMenuView());
                    menuView.setPlayState(IsRecording);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 打开摄像头预览
     */
    private void OpenCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        int camera = 0;
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                camera = camIdx;
                break;
            }
            break;
        }

        mCamera = Camera.open(camera);
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.getParameters().setPictureSize(640, 480);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    IsCanClick = false;
                }
            }, 1500);
            mCamera.startPreview();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 停止预览
     */
    private void CloseCmera() {

        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        if (!IsRecording) {
            if (mCamera == null)
                OpenCamera();

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int i, int i1, int i2) {
        surfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceHolder = null;
        mediarecorder = null;
    }


    View.OnClickListener onClickListenermenu = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!menuView.getShow())
                mainview.addView(menuView.getMenuView());
        }
    };


    /**
     * 开始录音
     */
    private void startRecord() {

        records = 0;
        uuid = "";
        IsRecording = true;
//        PlaySound(true);
//            Camera mCamera= Camera.open(CammeraIndex);
//            mCamera.setDisplayOrientation(90);
//            mCamera.unlock();
//            mRecorder.setCamera(mCamera);
        mediarecorder = new MediaRecorder();// 创建mediarecorder对象
        // 设置录制视频源为Camera(相机)

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras(); // get cameras number
        int camera = 0;
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                camera = camIdx;
                break;
            }
            break;
        }
        mCamera = Camera.open(camera);
        mCamera.unlock();
        mediarecorder.setCamera(mCamera);
//
        // 这两项需要放在setOutputFormat之前
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Set output file format，输出格式
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//        mediarecorder.setProfile(cProfile);
        //必须在setEncoder之前
        mediarecorder.setVideoFrameRate(20);  //帧数  一分钟帧，15帧就够了
        mediarecorder.setVideoSize(640, 480);

        // 这两项需要放在setOutputFormat之后
        mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mediarecorder.setVideoEncodingBitRate(2 * 1024 * 512);// 设置帧频率，然后就清晰了
        mediarecorder.setOrientationHint(0);

        mediarecorder.setPreviewDisplay(surfaceHolder.getSurface());
        // 设置视频文件输出的路径
        uuid = getDTUUID();//UUID.randomUUID().toString();
        mediarecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/录像" + "/" + uuid + ".mp4");
        try {

            // 准备录制
            mediarecorder.prepare();
            // 开始录制

            record_state.setVisibility(View.VISIBLE);

            mediarecorder.start();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    records++;
                    handler.sendEmptyMessage(0);
//
                }
            }, 0, 1000);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    IsCanClick = false;
                }
            }, 1500);

        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            record_time.setText(String.valueOf(records) + " 秒");
        }
    };

    /**
     * 停止录像
     */
    private void StopRecord() {
        if (mediarecorder != null) {


            // 停止录制
            timer.cancel();
            timer = null;
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;
            IsRecording = false;
            mCamera.release();
            mCamera = null;
            record_state.setVisibility(View.GONE);

            PlaySound(false);


            //添加新的视频数据
            ContentValues cv = new ContentValues();
            cv.put("uuid", uuid);
            cv.put("state", 0);
            cv.put("duration", records);
            cv.put("createdt", GetSysTime());
            SuyDB.getSuyDB().getDb().insert("video", null, cv);

            //开始分离
            VideoSplit videoSplit = new VideoSplit(uuid, splitInterFace);
            videoSplit.StartSplit();
        }
    }

    VideoSplit.SplitInterFace splitInterFace = new VideoSplit.SplitInterFace() {
        @Override
        public void SplitState(boolean b) {
            if (b)
                analysis.setVisibility(View.VISIBLE);
            else
                analysis.setVisibility(View.GONE);
        }
    };

    /**
     * 播放提示音
     *
     * @param b
     */
    private void PlaySound(boolean b) {
        SoundPool soundPool;
        soundPool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
        soundPool.load(this, b ? R.raw.a1 : R.raw.a2, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPool.play(1, 0.06f, 0.06f, 1, 0, 1);
            }
        });

    }

    private void Vibrator(long time) {
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(time);
    }


    MenuView.MenuInterFace menuInterFace = new MenuView.MenuInterFace() {
        @Override
        public void OnMenuItem(int i) {
            mainview.removeView(menuView.getMenuView());
            menuView.setShow(false);
            switch (i) {
                case 1://播放

                    onKeyUp(131, null);
                    break;
                case 2:
                    onKeyUp(133, null);
                    break;
                case 3:
                    Intent intent = new Intent(MainActivity.this, ListActivity.class);
                    startActivity(intent);
                    break;
                case 4://设置
                    if (IsRecording) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("提示");
                        builder.setMessage("正在录像，需要停止吗");
                        builder.setPositiveButton("停止录制", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StopRecord();
                                OpenCamera();
                                dialogInterface.dismiss();
                                OpenSettingActivity();
                            }
                        });
                        builder.setNegativeButton("取消", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        return;
                    }
                    OpenSettingActivity();
                    break;
            }
        }
    };


    /**
     * 打开设置界面
     */
    private void OpenSettingActivity() {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    /**
     * 检查数据文件
     *
     * @return
     */
    private Boolean checkFileIsExits() {
        File file = new File(getFilesDir(), "db.sqlite");
        return file.exists();
    }


    /**
     * 拷贝数据文件
     */
    private void CopyRaw() {
        InputStream inputStream;
        try {
            inputStream = getResources().openRawResource(R.raw.db);
            byte[] bytebuff = new byte[inputStream.available()];
            inputStream.read(bytebuff);
            File file = new File(getFilesDir(), "db.sqlite");
            if (file.exists())
                file.delete();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytebuff);
            fileOutputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private String GetSysTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    private String getDTUUID() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd_HHmm_");
        String date = sDateFormat.format(new java.util.Date()) + String.valueOf(index++);
        return date;
    }
}
