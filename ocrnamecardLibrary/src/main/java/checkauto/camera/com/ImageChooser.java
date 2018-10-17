package checkauto.camera.com;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import checkauto.camera.com.util.AppManager;
import checkauto.camera.com.util.CheckPermission;
import checkauto.camera.com.util.PermissionActivity;
import checkauto.camera.com.util.Utils;
import kernel.BusinessCard.android.AuthParameterMessage;
import kernel.BusinessCard.android.AuthService;
import kernel.BusinessCard.android.RecogService;

//首页
public class ImageChooser extends Activity implements View.OnClickListener {

    public static final String PATH = Environment.getExternalStorageDirectory()
            .toString() + "/AndroidWT";
    public static final String TAG = "ImageChooser";
    private Button mbutsel;
    private Button handTakePic;
    /**
     * 自动拍照
     */
    private Button autoTakePic;
    private Button mbutquit;
    /**
     * 激活
     */
    private Button mbutCode,ButtonAutoCheckLine;
    private String selectPath;
    private EditText editText;
    private AuthService.authBinder authBinder;
    private String editsString = "";
    private int ReturnAuthority = -1;
    private int meijihuoTextView, license_verification_failed,
            toast_please_retake, butcode, HandTakePic, AutoTakePic,
            serialdialog, serialdialogEdittext, online_activation, zoomin,
            zoomout, offline_activation, dialog_alert,
            dialog_message_send_admin, confirm, butlog, butlog3;
    /**
     *   项目授权开发码
     */
    private String devcode = Devcode.DEVCODE;
    private CheckPermission checkPermission;
    private String cutPicturePath="";
    // 授权验证service 时间授权等参数在BucardRunner.java类中更改
    public ServiceConnection authConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            authBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            authBinder = (AuthService.authBinder) service;
            try {

                AuthParameterMessage apm = new AuthParameterMessage();
                // apm.datefile = "";//预留
                // apm.devcode = "";//预留
                apm.devcode = devcode;// 5YYX5LQS5PAH6YC
                apm.sn = editsString;// WU9H5VSSDVXYB6KYYI52YYICW
                // WUB7RVSN1JVYHFBYY7P9YYC37
                apm.authfile = "";// /mnt/sdcard/auth/A1000038AB08A2_zj.txt
                // apm.isCheckDevType=true;//强制验证设备型号开关
                ReturnAuthority = authBinder.getBuCardAuth(apm);
                TextView textView = (TextView) findViewById(meijihuoTextView);
                if (ReturnAuthority != 0) {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(license_verification_failed) + ":"
                                    + ReturnAuthority, Toast.LENGTH_LONG)
                            .show();
                    textView.setVisibility(View.VISIBLE);
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        getString(license_verification_failed),
                        Toast.LENGTH_LONG).show();

            } finally {
                if (authBinder != null) {
                    unbindService(authConn);
                }
            }
        }

    };
    public static final String[] PERMISSION = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
            Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE, Manifest.permission.INTERNET
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int imagechooser = this.getResources().getIdentifier("imagechooser",
                "layout", this.getPackageName());
        super.onCreate(savedInstanceState);
        setContentView(imagechooser);
        checkPermission = new CheckPermission(ImageChooser.this);
      if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission.permissionSet(ImageChooser.PERMISSION)) {
                PermissionActivity.startActivityForResult(ImageChooser.this, 0, devcode, true, false, ImageChooser.PERMISSION);
            }
        }
        AppManager.getAppManager().finishActivity(CameraActivity.class);

        findView();
    }
@Override
    protected void onResume() {
        super.onResume();
       // startAuthService();

    }

    private void startAuthService() {
        Intent authIntent = new Intent(ImageChooser.this, AuthService.class);
        bindService(authIntent, authConn, Service.BIND_AUTO_CREATE);
    }

    public AlertDialog.Builder createAlertDialog(String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.create();
        return dialog;
    }

    /**
     * 离线激活方式 创建dev文件
     */
    public void createDevFile() {
        String sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            File file = new File(PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            sdDir = PATH + "/bucard.dev";
            String deviceId;
            String androId;
            TelephonyManager telephonyManager;
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            StringBuilder sb = new StringBuilder();
            sb.append(telephonyManager.getDeviceId());
            deviceId = sb.toString();// 由15位数字组成
            StringBuilder sb1 = new StringBuilder();
            sb1.append(Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID));
            androId = sb1.toString();
            File newFile = new File(sdDir);
            String idString = deviceId + ";" + androId;
            try {
                newFile.delete();
                newFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newFile);
                StringBuffer sBuffer = new StringBuffer();
                sBuffer.append(idString);
                fos.write(sBuffer.toString().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 在线激活方式 创建sn文件
     */
    public void createSnFile(String editsString) {
        if (editsString != null && !"".equals(editsString)) {
            File file = new File(PATH);
            if (!file.exists()) {
                file.mkdir();
            }
            String filePATH = PATH + "/bucard.sn";
            File newFile = new File(filePATH);
            try {
                newFile.delete();
                newFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(newFile);
                StringBuffer sBuffer = new StringBuffer();
                sBuffer.append(editsString);
                fos.write(sBuffer.toString().toUpperCase().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9 && resultCode == Activity.RESULT_OK) {
            // 选择后识别
            Bitmap bitmap;
            String zipPicPath = "";
            Uri uri = data.getData();
            selectPath = Utils.getPath(getApplicationContext(), uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeFile(selectPath, options);
            //System.out.println("选择的图片的路径为:" + selectPath);
            if (bitmap == null||selectPath.endsWith(".png")) {
                Toast.makeText(getApplicationContext(), "图片损坏或格式不正确!", Toast.LENGTH_SHORT).show();
            } else {
              //  System.out.println("选择图片的的宽度为:" + bitmap.getWidth() + "选择图片的高度为:" + bitmap.getHeight());
                if (bitmap.getWidth() < bitmap.getHeight()) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    bitmap = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                    bitmap.getHeight(), matrix, true);
                    /* 创建文件 */
                    File dir = new File(CameraActivity.path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    BufferedOutputStream bos;
                    zipPicPath = CameraActivity.path + Utils.pictureName()
                            + ".jpg";
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(
                                zipPicPath));
						/* 采用压缩转档方法 */
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
						/* 调用flush()方法，更新BufferStream */
                        bos.flush();
						/* 结束OutputStream */
                        bos.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                } else {
                    zipPicPath = selectPath;
                }
                if (bitmap != null) {
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                        System.gc();
                    }
                    bitmap = null;
                }
                RecogService.nLightValue=50;
                cutPicturePath = CameraActivity.path + Utils.pictureName() + "_cut.jpg";
                CameraActivity.enHancementPath=cutPicturePath;
                RecogOpera recogUtils = new RecogOpera(1, ImageChooser.this);
                recogUtils.startActivityRecog(null, null, 0, 0, zipPicPath,cutPicturePath);
            }
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // 拍照后存图片
            boolean go = false;
            if (selectPath == null) {
                selectPath = getLatestImage();
                File file = new File(selectPath);
                if (file.exists()) {
                    go = true;
                } else {
                    Toast.makeText(getApplicationContext(),
                            getString(toast_please_retake), 0).show();
                }
            } else {
                go = true;
            }
            if (go) {
                // 跳转
                File file = new File(selectPath);
                Bitmap source = BitmapFactory.decodeFile(selectPath);
                Bitmap bmp = Bitmap.createScaledBitmap(source, 1280, 960, true);
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    bmp.compress(Bitmap.CompressFormat.JPEG, 75, bos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(ImageChooser.this,
                        BucardRunner.class);
                intent.putExtra("path", selectPath);
                this.startActivity(intent);
                overridePendingTransition(zoomin, zoomout);
            }

        }
        if (requestCode == 8 && resultCode == RESULT_OK) {
            // 读识别返回值
            Bundle bun = data.getBundleExtra("GetRecogResult");
            Intent intent = new Intent(ImageChooser.this, BucardRunner.class);
            intent.putExtra("RecogValue", bun);
           intent.putExtra("cutpath",cutPicturePath);
            intent.putExtra("enHancementPath",CameraActivity.enHancementPath);
            startActivity(intent);
            ImageChooser.this.finish();
            overridePendingTransition(zoomin, zoomout);
        }
    }

    @SuppressWarnings("unused")
    protected String getLatestImage() {
        String latestImage = null;
        String[] items = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, items, null,
                null, MediaStore.Images.Media._ID + " desc");
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                    .moveToNext()) {
                latestImage = cursor.getString(1);
                break;
            }
        }
        return latestImage;
    }

    public void findView() {
        meijihuoTextView = this.getResources().getIdentifier(
                "meijihuoTextView", "id", this.getPackageName());
        butcode = this.getResources().getIdentifier("butcode", "id",
                this.getPackageName());
        HandTakePic = this.getResources().getIdentifier("handTakePic", "id",
                this.getPackageName());
        AutoTakePic = this.getResources().getIdentifier("autoTakePic", "id",
                this.getPackageName());
        serialdialog = this.getResources().getIdentifier("serialdialog",
                "layout", this.getPackageName());
        serialdialogEdittext = this.getResources().getIdentifier(
                "serialdialogEdittext", "id", this.getPackageName());
        license_verification_failed = this.getResources().getIdentifier(
                "license_verification_failed", "string", this.getPackageName());
        toast_please_retake = this.getResources().getIdentifier(
                "toast_please_retake", "string", this.getPackageName());
        zoomin = this.getResources().getIdentifier("zoomin", "anim",
                this.getPackageName());
        zoomout = this.getResources().getIdentifier("zoomout", "anim",
                this.getPackageName());
        online_activation = this.getResources().getIdentifier(
                "online_activation", "string", this.getPackageName());
        offline_activation = this.getResources().getIdentifier(
                "offline_activation", "string", this.getPackageName());

        dialog_alert = this.getResources().getIdentifier("dialog_alert",
                "string", this.getPackageName());
        dialog_message_send_admin = this.getResources().getIdentifier(
                "dialog_message_send_admin", "string", this.getPackageName());

        confirm = this.getResources().getIdentifier("confirm", "string",
                this.getPackageName());
        butlog = this.getResources().getIdentifier("butlog", "id",
                this.getPackageName());
        butlog3 = this.getResources().getIdentifier("butlog3", "id",
                this.getPackageName());
        mbutCode = (Button) findViewById(butcode);
        handTakePic = (Button) findViewById(HandTakePic);
        autoTakePic = (Button) findViewById(AutoTakePic);
        mbutsel = (Button) this.findViewById(butlog);
        mbutquit = (Button) this.findViewById(butlog3);
        ButtonAutoCheckLine=(Button)findViewById(getResources().getIdentifier("autoCheckLine","id",getPackageName()));
        mbutCode.setOnClickListener(this);
        handTakePic.setOnClickListener(this);
        autoTakePic.setOnClickListener(this);
        mbutquit.setOnClickListener(this);
        mbutsel.setOnClickListener(this);
        ButtonAutoCheckLine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mbutCode) {
            startAuthService();
            View view = getLayoutInflater().inflate(serialdialog, null);
            editText = (EditText) view.findViewById(serialdialogEdittext);
            AlertDialog dialog = new AlertDialog.Builder(ImageChooser.this)
                    .setView(view)
                    .setPositiveButton(getString(online_activation),
                            new DialogInterface.OnClickListener() {
@Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm.isActive()) {
                                        imm.toggleSoftInput(
                                                InputMethodManager.SHOW_IMPLICIT,
                                                InputMethodManager.HIDE_NOT_ALWAYS);
                                    }
                                    editsString = editText.getText()
                                            .toString().toUpperCase();
                                    createSnFile(editsString);
                                    startAuthService();
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(getString(offline_activation),
                            new DialogInterface.OnClickListener() {
@Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if (imm.isActive()) {
                                        imm.toggleSoftInput(
                                                InputMethodManager.SHOW_IMPLICIT,
                                                InputMethodManager.HIDE_NOT_ALWAYS);
                                    }
                                    createDevFile();
                                    dialog.dismiss();
                                    AlertDialog.Builder dialogSend = createAlertDialog(
                                            getString(dialog_alert),
                                            getString(dialog_message_send_admin));
                                    dialogSend
                                            .setPositiveButton(
                                                    getString(confirm),
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(
                                                                DialogInterface dialog,
                                                                int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                    dialogSend.show();

                                }

                            }).create();
            dialog.show();
        } else if (v == mbutquit) {

            ImageChooser.this.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        } else if (v == mbutsel) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission.permissionSet(ImageChooser.PERMISSION)) {
                    PermissionActivity.startActivityForResult(ImageChooser.this, 0, devcode, true, false, ImageChooser.PERMISSION);
                } else {
                    Intent innerIntentX = new Intent(Intent.ACTION_GET_CONTENT);
                    innerIntentX.addCategory(Intent.CATEGORY_OPENABLE);
                    innerIntentX.setType("image/jpg");
                    innerIntentX.setType("image/jpeg");
                    Intent wrapperIntentX = Intent.createChooser(innerIntentX,
                            "Select Picture");
                    startActivityForResult(wrapperIntentX, 9);
                    overridePendingTransition(zoomin, zoomout);
                }
            } else {
                Intent innerIntentX = new Intent(Intent.ACTION_GET_CONTENT);
                innerIntentX.addCategory(Intent.CATEGORY_OPENABLE);
                innerIntentX.setType("image/*");
                Intent wrapperIntentX = Intent.createChooser(innerIntentX,
                        "Select Picture");
                startActivityForResult(wrapperIntentX, 9);
                overridePendingTransition(zoomin, zoomout);
            }

        } else if (v == autoTakePic) {
            StartActivity(true,0);
        } else if (v == handTakePic) {
            StartActivity(false,0);
        }else if(v==ButtonAutoCheckLine){
            StartActivity(true,1);
        }

    }

    void StartActivity(boolean type,int n) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission.permissionSet(ImageChooser.PERMISSION)) {
                PermissionActivity.startActivityForResult(ImageChooser.this, 0, devcode, true, true, ImageChooser.PERMISSION);
            } else {
                Intent intent = new Intent();
                intent.setClass(ImageChooser.this, CameraActivity.class);
                intent.putExtra("devcode", devcode);
                intent.putExtra("autocamera", type);
                intent.putExtra("nCropType",n);
                ImageChooser.this.finish();
                startActivity(intent);
                overridePendingTransition(zoomin, zoomout);
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(ImageChooser.this, CameraActivity.class);
            intent.putExtra("devcode", devcode);
            intent.putExtra("autocamera", type);
            intent.putExtra("nCropType",n);
            ImageChooser.this.finish();
            startActivity(intent);
            overridePendingTransition(zoomin, zoomout);
        }
    }
}