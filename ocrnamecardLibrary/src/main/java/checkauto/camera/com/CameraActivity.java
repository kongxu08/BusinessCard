package checkauto.camera.com;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import checkauto.camera.com.util.CameraParametersUtils;
import checkauto.camera.com.util.FileLog;
import checkauto.camera.com.util.SharedPreferencesHelper;
import checkauto.camera.com.util.Utils;
import checkauto.camera.com.view.ViewfinderView;
import kernel.BusinessCard.android.Frame;
import kernel.BusinessCard.android.RecogService;

/**
 * 
 * 项目名称：ViCard 类名称：
 * CameraActivity 类描述： 相机界面
 * 创建时间：2015-9-16
 * 上午9:10:57
 * 修改人：huangzhen 修改时间：2015-9-16 上午9:10:57
 * 修改备注：
 * @author  huangzhen
 * @version
 * 
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback,
		Camera.PreviewCallback, OnClickListener {
	private SurfaceView surfaceView;
	private ViewfinderView myViewfinderView;
	private CameraParametersUtils cameraParametersutils;
	private int width, height,preWidth, preHeight;
	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private ImageView iv_kernel_camera_back, iv_kernel_camera_flash,
			iv_kernel_camera;
	private ToneGenerator tone;
	public static String path = Environment.getExternalStorageDirectory()
			.toString() + "/wtimage/";
	private boolean isRecoging = false;
	private boolean isInitBusinessCard = false;
	private RecogService.recogBinder recogBinder;
	private List<String> focusModes;
	private static String selectPath = "";
	private Size size;
	private Parameters parameters;
	public static byte[] recogBytes;
	public boolean isAutoRecog = false;
	private RecogOpera recogUtils;
	private boolean isFirstCreate = true;
	private Message msg;
	private boolean isOpenFlash = false;
	private boolean isTouched = false;
	private boolean isTackePic=false;
	private int sum=0;
	private int nCropType=0;
	private Toast mtoast=null;
	private  String cutPicturePath="";

	public static String enHancementPath="";
	private int SurfaceView,MyViewfinderView, zoomin, zoomout,
			Iv_kernel_camera, Iv_kernel_camera_flash, Iv_kernel_camera_back,
			unsupport_auto_focus, toast_autofocus_failure, noFoundProgram,
			not_support_scan, unsupportflash, flash_off, flash_on,
			openCameraPermission_Id;
	private float scale;
	private  final  int mAutoMessageWhat=100;
	private final int findViewMessageWhat=101;
	private final int buildVesionSdk=18;
	private final int nameCardDetectSideSuccess=1010;
	private final int minPreviewWidth=1920;
	private final String deviceModel="Nexus 5X";
	private  final int cameraRequestCode=8;
    private long totaltime;
    private long checkeTime;

	private Handler mAutoFocusHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == mAutoMessageWhat) {
			  autoCameraFocus();
			}

		}

		;
	};
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case findViewMessageWhat:
					findView();
					break;
				case -145:
						if(mtoast==null){
							mtoast=Toast.makeText(getApplicationContext(),"距离太远",Toast.LENGTH_SHORT);
						}else{
							mtoast.setText("距离太远");
						}
						mtoast.show();
					break;
              default:
	               break;
			}


		};
	};
	public ServiceConnection recogConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			recogConn = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {

				recogBinder = (RecogService.recogBinder) service;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if (Build.VERSION.SDK_INT > buildVesionSdk) {
			Window window = getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
    	this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int autoCameraActivity = getResources().getIdentifier(
				"bucard_camera_activity", "layout", this.getPackageName());
		setContentView(autoCameraActivity);
		cameraParametersutils = new CameraParametersUtils(
				getApplicationContext());
		width = cameraParametersutils.srcWidth;
		height = cameraParametersutils.srcHeight;
		initResouce();
	}

	private void initResouce() {
		// TODO Auto-generated method stub
		flash_off = getResources().getIdentifier("flash_off", "drawable",
				this.getPackageName());
		flash_on = getResources().getIdentifier("flash_on", "drawable",
				this.getPackageName());
		unsupportflash = getResources().getIdentifier("unsupportflash",
				"string", this.getPackageName());
		not_support_scan = getResources().getIdentifier("not_support_scan",
				"string", this.getPackageName());
		openCameraPermission_Id = getResources().getIdentifier(
				"openCameraPermission", "string", this.getPackageName());
		SurfaceView = getResources().getIdentifier("surfaceView", "id",
				this.getPackageName());
		unsupport_auto_focus = getResources().getIdentifier(
				"unsupport_auto_focus", "string", this.getPackageName());
		toast_autofocus_failure = getResources().getIdentifier(
				"toast_autofocus_failure", "string", this.getPackageName());
		noFoundProgram = getResources().getIdentifier("noFoundProgram",
				"string", this.getPackageName());
		MyViewfinderView = getResources().getIdentifier("myViewfinderView",
				"id", this.getPackageName());
		Iv_kernel_camera_back = getResources().getIdentifier(
				"iv_kernel_camera_back", "id", this.getPackageName());
		Iv_kernel_camera_flash = getResources().getIdentifier(
				"iv_kernel_camera_flash", "id", this.getPackageName());
		Iv_kernel_camera = getResources().getIdentifier("iv_kernel_camera",
				"id", this.getPackageName());
		zoomin = getResources().getIdentifier("zoomin", "anim",
				this.getPackageName());
		zoomout = getResources().getIdentifier("zoomout", "anim",
				this.getPackageName());
		surfaceView = (SurfaceView) this.findViewById(SurfaceView);
		myViewfinderView = (ViewfinderView) this.findViewById(MyViewfinderView);
		iv_kernel_camera_back = (ImageView) this
				.findViewById(Iv_kernel_camera_back);
		iv_kernel_camera_flash = (ImageView) this
				.findViewById(Iv_kernel_camera_flash);
		iv_kernel_camera = (ImageView) this.findViewById(Iv_kernel_camera);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		isInitBusinessCard = false;
		Intent intent = getIntent();
		isAutoRecog = intent.getBooleanExtra("autocamera", false);
		nCropType=intent.getIntExtra("nCropType",0);
		SharedPreferencesHelper.putBoolean(getApplicationContext(),
				"isAutoRecog", isAutoRecog);
		myViewfinderView.setnCropType(nCropType);
		cameraParametersutils.hiddenVirtualButtons(getWindow().getDecorView());
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(CameraActivity.this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		startCamera();
		if (!isFirstCreate) {
			setParams();
		}
	}

	/**
	 * 相机的开启
	 */
	private void startCamera() {
		// TODO Auto-generated method stub
		try {
			try {
				if (null == camera) {
					camera = Camera.open();
				}
			} catch (Exception e) {
				// 禁止使用相机权限后防止布局混乱

				Toast.makeText(getApplicationContext(),
						getString(openCameraPermission_Id), Toast.LENGTH_SHORT)
						.show();
				msg = new Message();
				msg.what=findViewMessageWhat;
				handler.sendMessage(msg);
			}
			isRecoging = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @Title: findView
	 * @Description: 相机界面UI布局
	 * @param :设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void findView() {
		// TODO Auto-generated method stub
		iv_kernel_camera.setOnClickListener(this);
		iv_kernel_camera_back.setOnClickListener(this);
		iv_kernel_camera_flash.setOnClickListener(this);

		// 返回按钮布局
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				(int) (width * 0.05), (int) (width * 0.05));
		params.leftMargin = (int) (width * 0.03);
		params.topMargin = (int) (height * 0.83);
		iv_kernel_camera_back.setLayoutParams(params);

		// 闪光灯布局
		params = new RelativeLayout.LayoutParams((int) (width * 0.05),
				(int) (width * 0.05));
		params.leftMargin = (int) (width * 0.03);
		params.topMargin = (int) (height * 0.09);
		iv_kernel_camera_flash.setLayoutParams(params);

		int surfaceWidth = cameraParametersutils.surfaceWidth;
		int surfaceHeight = cameraParametersutils.surfaceHeight;

		if (width == surfaceView.getWidth() || surfaceView.getWidth() == 0) {
			// 预览界面
			params = new RelativeLayout.LayoutParams(width, height);
			surfaceView.setLayoutParams(params);
			// 扫描框
			params = new RelativeLayout.LayoutParams(width, height);
			myViewfinderView.setLayoutParams(params);
			// 拍照按钮布局
			params = new RelativeLayout.LayoutParams((int) (width * 0.08),
					(int) (width * 0.08));
			params.leftMargin = (int) (width * 0.85);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			iv_kernel_camera.setLayoutParams(params);
		} else if (width > surfaceView.getWidth()) {
			// 如果将虚拟硬件弹出则执行如下布局代码，相机预览分辨率不变压缩屏幕的高度
			int surfaceViewHeight = (surfaceView.getWidth() * height) / width;
			params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, surfaceViewHeight);
			params.topMargin = (height - surfaceViewHeight) / 2;
			surfaceView.setLayoutParams(params);
			// 拍照按钮布局
			params = new RelativeLayout.LayoutParams((int) (width * 0.1),
					(int) (width * 0.1));
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.leftMargin = (int) (width * 0.83);
			iv_kernel_camera.setLayoutParams(params);
		}
		if (surfaceWidth < width || surfaceHeight < height) {

			// 预览界面
			params = new RelativeLayout.LayoutParams(surfaceWidth,
					surfaceHeight);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			surfaceView.setLayoutParams(params);
			// 扫描框
			params = new RelativeLayout.LayoutParams(surfaceWidth,
					surfaceHeight);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			myViewfinderView.setLayoutParams(params);
			// 拍照按钮布局
			params = new RelativeLayout.LayoutParams((int) (width * 0.08),
					(int) (width * 0.08));
			params.leftMargin = (int) (width * 0.8);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			iv_kernel_camera.setLayoutParams(params);
		}

		if (isAutoRecog) {
			// 视频流模式 必须最先设置，因为他决定调用哪个初始化函数
			RecogService.byteDataType = 0;
		} else {
			// 拍照模式 必须最先设置，因为他决定调用哪个初始化函数
			RecogService.byteDataType = 1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		// 返回按钮触发事件
		if (id == Iv_kernel_camera_back) {
			isTouched = true;
			sum = -1;
			mAutoFocusHandler.removeCallbacksAndMessages(null);
			Intent intent = new Intent(CameraActivity.this, ImageChooser.class);
			CameraActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(zoomin, zoomout);
			// 拍照按钮触发事件
		} else if (id == Iv_kernel_camera) {
			if (camera!= null) {
				mAutoFocusHandler.removeCallbacksAndMessages(null);
				isAutoRecog=false;
				isTackePic=true;
				RecogService.byteDataType = 1;
			}
			// 闪光灯按钮触发事件
		} else if (id == Iv_kernel_camera_flash) {
			try {
				if (camera == null){
					camera = Camera.open();
				}
				Parameters parameters = camera.getParameters();
				List<String> flashList = parameters.getSupportedFlashModes();
				if (flashList != null
						&& flashList
								.contains(Parameters.FLASH_MODE_TORCH)) {
					if (!isOpenFlash) {
						iv_kernel_camera_flash.setBackgroundResource(flash_on);
						isOpenFlash = true;
						parameters
								.setFlashMode(Parameters.FLASH_MODE_TORCH);
						camera.setParameters(parameters);
					} else {
						iv_kernel_camera_flash.setBackgroundResource(flash_off);
						isOpenFlash = false;
						parameters
								.setFlashMode(Parameters.FLASH_MODE_OFF);
						camera.setParameters(parameters);
					}
				} else {
					Toast.makeText(getApplicationContext(),
							getString(unsupportflash), Toast.LENGTH_SHORT)
							.show();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[],
	 * android.hardware.Camera)
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		if(isTouched){
			return;
		}else {
			if (sum == 0) {
				recogBytes = data;
		        MyThread thread = new MyThread();
				thread.start();
			}
		}

      //  Log.i("string","数值"+i);
	}

	/**
	 * 预览识别线程
	 */
	class MyThread extends Thread {
		public MyThread() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void run() {
			recogOpera();
		}
	}
	private synchronized void recogOpera(){
		sum = sum + 1;
		if (isTouched) {
			return;
		}
		if (!isInitBusinessCard) {
			/**
			 * 图片增亮
			 */
			RecogService.nLightValue=50;
			Intent recogIntent = new Intent(getApplicationContext(),
					RecogService.class);
			bindService(recogIntent, recogConn, Service.BIND_AUTO_CREATE);
			isInitBusinessCard = true;
			size = camera.getParameters().getPreviewSize();
		}
		if (isAutoRecog) {
		int detectSideLineEx=-1;
			if (recogBinder != null) {
				recogBinder.SetCardROI((int) (size.width * 0.15),
						(int) (size.height - 0.41004673 * size.width) / 2,
						(int) (size.width * 0.8),
						(int) (size.height + 0.41004673 * size.width) / 2);
				recogBinder.SetVideoStreamCropTypeBC(nCropType);
				recogBinder.SetBCAreaRatio( 0.25f);
			    recogBinder.SetPixClear(120);
			    long  checkeTime1=System.currentTimeMillis();
				detectSideLineEx=recogBinder.ReturnDetectSideLineEx(recogBytes,preWidth,preHeight,12);
				checkeTime=System.currentTimeMillis()-checkeTime1;
				FileLog.writeLog(path+"2017.12.7.txt","recorged namecard deectSideLineTime:"+checkeTime+"ms");
				Frame frame= Frame.getInstance();
				recogBinder.GetSideLinePosRT(frame,scale);
			    myViewfinderView.setFocues(nCropType,frame);
				if (detectSideLineEx==nameCardDetectSideSuccess&&!isRecoging) {
					checkeTime=System.currentTimeMillis()-checkeTime1;
					FileLog.writeLog(path+"2017.12.7.txt","recorged namecard startRecogTime:"+checkeTime+"ms");
					isRecoging=true;
					// 样本保存功能start
					savePicFullPath();
					// end
						myViewfinderView.setCheckLeftFrame(1);
						myViewfinderView.setCheckTopFrame(1);
						myViewfinderView.setCheckRightFrame(1);
						myViewfinderView.setCheckBottomFrame(1);
						myViewfinderView.setFocues(nCropType,frame);
					camera.setPreviewCallback(null);
					camera.stopPreview();
					mAutoFocusHandler.removeCallbacksAndMessages(null);
					// 开始识别
					 cutPicturePath = path + Utils.pictureName() + "_cut.jpg";
					 enHancementPath=path +  Utils.pictureName()+"enHancementPath.jpg";

					recogUtils = new RecogOpera(0, CameraActivity.this);
					recogUtils.startActivityRecog(recogBytes, size, preWidth, preHeight,
							selectPath,cutPicturePath);
					totaltime=System.currentTimeMillis();
				}else{
                    msg=new Message();
					msg.what=detectSideLineEx;
					handler.sendMessage(msg);
				}
			}

		}else{
			if (!isRecoging&&isTackePic) {
				isRecoging=true;
				isTackePic=false;
				mAutoFocusHandler.removeCallbacksAndMessages(null);
				camera.takePicture(null, null, pictureCallback);
			}
		}
		sum = sum - 1;
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
     mAutoFocusHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onDestroy() {

		// TODO Auto-generated method stub
		super.onDestroy();
        mAutoFocusHandler.removeCallbacksAndMessages(null);
		closeCamera();
		if (recogBinder != null) {
			unbindService(recogConn);
			recogBinder = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		if (camera != null) {
			try {
				setParams();
				if (isFirstCreate) {
					isFirstCreate = false;
					CameraActivity.this.runOnUiThread(updateUI);
				} else {
					msg = new Message();
					msg.what=findViewMessageWhat;
					handler.sendMessage(msg);
				}
			}catch (Exception e){

			}
			msg=new Message();
			msg.what=mAutoMessageWhat;
			mAutoFocusHandler.sendMessage(msg);
		}
	}

	/**
	 * 相机参数的配置
	 */
	private void setParams() {
		// TODO Auto-generated method stub
		if (camera != null) {
			cameraParametersutils.getCameraPreParameters(camera);
			preWidth = cameraParametersutils.preWidth;
			preHeight = cameraParametersutils.preHeight;
			scale=(float) width/preWidth;
			parameters = camera.getParameters();

			if (parameters.getSupportedFocusModes().contains(
					Parameters.FOCUS_MODE_AUTO)) {
				parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
			}
			parameters.setPictureFormat(PixelFormat.JPEG);

			if (preWidth >= minPreviewWidth) {
				parameters.setPreviewSize(preWidth, preHeight);
			} else {
				parameters.setPreviewSize(preWidth, preHeight);

				iv_kernel_camera.setVisibility(View.VISIBLE);
				RecogService.byteDataType = 1;
				isAutoRecog = false;
				Toast.makeText(this, not_support_scan, Toast.LENGTH_SHORT)
						.show();
			}
			try {
				camera.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.setPreviewCallback(CameraActivity.this);
			camera.setParameters(parameters);
			camera.startPreview();
		}
	}

	/**
	 *进行界面的布局
	 */
	private Runnable updateUI = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			findView();
		}
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		synchronized (this) {
			try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				Log.i("TAG", e.getMessage());
			}
		}
	}

	/**
	 * 自动对焦
	 */
	public void autoCameraFocus() {
		if (camera != null) {
			synchronized (camera) {
				try {
					if (camera.getParameters().getSupportedFocusModes() != null
							&& camera
									.getParameters()
									.getSupportedFocusModes()
									.contains(Parameters.FOCUS_MODE_AUTO)) {
						camera.autoFocus(new AutoFocusCallback() {
							@Override
							public void onAutoFocus(boolean success,
									Camera camera) {
								if (success) {
									isRecoging = false;
								} else {
								}
							}
						});
					} else {
						Toast.makeText(getBaseContext(),
								getString(unsupport_auto_focus),
								Toast.LENGTH_LONG).show();
					}
					mAutoFocusHandler.sendEmptyMessageDelayed(
							mAutoMessageWhat, 2500);
				} catch (Exception e) {
					Toast.makeText(this, toast_autofocus_failure,
							Toast.LENGTH_SHORT).show();
					isRecoging=false;
				}
			}
		}
	}

	/**
	 * 释放相机
	 */
	public void closeCamera() {
		synchronized (this) {
			try {
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}
			} catch (Exception e) {
				Log.i("TAG", e.getMessage());
			}
		}
	}

	/**
	 * 点击返回的方法
	 * @param keyCode
	 * @param event
	 * @return
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			isTouched = true;
			sum = -1;
			mAutoFocusHandler.removeCallbacksAndMessages(null);
			Intent intent = new Intent(CameraActivity.this, ImageChooser.class);
			CameraActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(zoomin, zoomout);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 *点击拍照按钮调用的callback
	 */
	private PictureCallback pictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			camera.setPreviewCallback(null);
			camera.stopPreview();

			savePicFullPath();
			/**
			 * 调用识别并回传数据
			 */
			try {
				cutPicturePath = path + Utils.pictureName() + "_cut.jpg";
				enHancementPath=cutPicturePath;
				recogUtils = new RecogOpera(1, CameraActivity.this);
			    recogUtils.startActivityRecog(null, size, preWidth, preHeight,
						selectPath,cutPicturePath);
			}catch (Exception e){
				Toast.makeText(getApplicationContext(),
						getString(noFoundProgram) + "kernel.bucard", 0).show();
				e.printStackTrace();
			}

		}



	};
	/**
	 * 快门按下的时候onShutter()被回调拍照声音
	 */
	private ShutterCallback shutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			if (tone == null){
				tone = new ToneGenerator(1,
						ToneGenerator.MIN_VOLUME);
			}
				// 发出提示用户的声音
			tone.startTone(ToneGenerator.TONE_PROP_BEEP);
		}
	};

	/**
	 * 拍照函數
	 */
	public void takePicture() {

		if (camera != null) {
			try {
				if (focusModes != null
						&& focusModes
								.contains(Parameters.FOCUS_MODE_AUTO)) {
					camera.autoFocus(new AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							if (success) {

								camera.takePicture(shutterCallback, null,
										pictureCallback);
							} else {
								camera.takePicture(shutterCallback, null,
										pictureCallback);
							}

						}
					});
				} else {
					camera.takePicture(shutterCallback, null, pictureCallback);
				}
			} catch (Exception e) {

				camera.stopPreview();
				camera.startPreview();
				Toast.makeText(this, toast_autofocus_failure,
						Toast.LENGTH_SHORT).show();

			}
		}
	}

	/**
	 * 获取返回结果进行界面跳转传递数值显示操作
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode ==cameraRequestCode && resultCode == RESULT_OK) {
			totaltime=System.currentTimeMillis()-totaltime;
			FileLog.writeLog(path+"2017.12.7.txt","recorged namecard totaltime:"+totaltime+"ms");
			// 读识别返回值
			Bundle bun = data.getBundleExtra("GetRecogResult");
			Intent intent = new Intent(CameraActivity.this, BucardRunner.class);
			intent.putExtra("RecogValue", bun);
			intent.putExtra("camera", isAutoRecog);
			intent.putExtra("nCropType",nCropType);
			intent.putExtra("cutpath",cutPicturePath);
			intent.putExtra("enHancementPath",enHancementPath);
		    CameraActivity.this.finish();
			startActivity(intent);
			overridePendingTransition(zoomin, zoomout);
		}
	}

	/**
	 *保存图片的操作
	 */
   		private  void savePicFullPath(){
			YuvImage yuvimage=null;
			try {
				selectPath = path + Utils.pictureName() + ".jpg";
				File file = new File(path);
				if (!file.exists()){
					file.mkdirs();
				}
				File file1 = new File(selectPath);
				Utils.freeFileLock(new FileOutputStream(selectPath)
						.getChannel().tryLock(), file1);

				if(deviceModel.equals(Build.MODEL)){
					byte[] nv21_new=Utils.rotateYUV420Degree180(recogBytes,size.width,size.height);
					yuvimage = new YuvImage(nv21_new, ImageFormat.NV21,
							size.width, size.height, null);
				}else{
					yuvimage = new YuvImage(recogBytes, ImageFormat.NV21,
							size.width, size.height, null);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				yuvimage.compressToJpeg(new Rect(0,
								0,
								size.width,
								size.height), 100,
						baos);
				FileOutputStream outStream;
				outStream = new FileOutputStream(selectPath);
				outStream.write(baos.toByteArray());
				outStream.close();
				baos.close();

		  } catch (Exception e) {

		  }
	}
}
