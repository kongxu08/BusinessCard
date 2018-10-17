/**
 * Project Name:IDCardScanCaller
 * File Name:PreviewActivity.java
 * Package Name:com.intsig.idcardscancaller
 * Date:2016年3月15日下午2:14:46
 * Copyright (c) 2016, 上海合合信息 All Rights Reserved.
 *
 */

package com.cjwsjy.app.businesscard;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.intsig.sdk.BCRSDK;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * ClassName:PreviewActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年3月15日 下午2:14:46 <br/>
 * 
 * @author guohua_xu
 */
public class PreviewActivity extends Activity implements Camera.PreviewCallback {

	public static final String EXTRA_KEY_APP_KEY = "EXTRA_KEY_APP_KEY";
	public static final String EXTRA_KEY_IMG_CAMERA_PATH = "EXTRA_KEY_IMG_CAMERA_PATH";
	public static final String EXTRA_KEY_RESULT_DATA = "EXTRA_KEY_RESULT_DATA";

	public static final String EXTRA_KEY_RESULT_IMAGE = "EXTRA_KEY_RESULT_IMAGE";

	private Preview mPreview = null;
	private Camera mCamera = null;
	private int numberOfCameras;
	private int defaultCameraId;

	/**
	 * 用于引擎指针，用于辅助检测切边区域是否合法
	 */
	private DetectThread mDetectThread = null;

	RelativeLayout rootView;

	String cameraPathString = "";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 隐藏当前Activity界面的导航栏, 隐藏后,点击屏幕又会显示出来.
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower
																		// api
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			// for new api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
		// 设置为半透明
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}

		/*************************** set a SurfaceView as the content of our activity.******START ***********************/
		mPreview = new Preview(this);
		RelativeLayout root = new RelativeLayout(this);
		root.setBackgroundColor(Color.TRANSPARENT);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		root.addView(mPreview, lp);

		lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		setContentView(root);
		rootView = root;
		// 初始化预览界面左边按钮组
		initButtonGroup();
		/*************************** Find the ID of the default camera******END ***********************/

		/*************************** Find the ID of the default camera******START ***********************/
		// Find the total number of cameras available
		numberOfCameras = Camera.getNumberOfCameras();
		// Find the ID of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				defaultCameraId = i;
			}
		}
		/*************************** Find the ID of the default camera******END ***********************/

		/*************************** Add mPreview Touch Listener******START ***********************/

		mPreview.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(!isRecognize){
				if (mCamera != null) {
					mCamera.autoFocus(null);
				}
				}
				return false;
			}
		});
		/*************************** Add mPreview Touch Listener******END ***********************/
		cameraPathString = getIntent()
				.getStringExtra(EXTRA_KEY_IMG_CAMERA_PATH);
		/*************************** init recog appkey ******START ***********************/
		// mScannerSDK = new ScannerSDK();
		Intent intent = getIntent();



		//第一步：初始化名片引擎
		final String appkey = intent.getStringExtra(EXTRA_KEY_APP_KEY);
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				File tmpDir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/bcrsdk/");
				tmpDir.mkdir();
				// copy IS_BCRAllTemplete.dat to certain folder.
				CameraRecognizeUtil.copyTmpData(
						tmpDir.getAbsolutePath() + "/IS_BCRAllTemplete.dat",
						"IS_BCRAllTemplete.dat",PreviewActivity.this);
				CameraRecognizeUtil.copyTmpData(tmpDir.getAbsolutePath()
						+ "/IS_BCRTemplete_AddressParse.dat",
						"IS_BCRTemplete_AddressParse.dat",PreviewActivity.this);

				final int ret = BCRSDK.getInstance().InitEngine(
						getApplicationContext(),
						tmpDir,
						tmpDir.getAbsolutePath() + "/IS_BCRAllTemplete.dat",
						tmpDir.getAbsolutePath()
								+ "/IS_BCRTemplete_AddressParse.dat", appkey,
						new BCRSDK.OnUpdateCallback() {

							@Override
							public void onEngineUpdate(final String enginePath,
									final String dbpath) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(
												PreviewActivity.this,
												"onEngineUpdate " + enginePath
														+ " \n" + dbpath,
												Toast.LENGTH_SHORT).show();
									}
								});
							}
						});
				return ret;
			}

			@Override
			protected void onPostExecute(Integer result) {
				// mResultLabel.setText(" InitEngine " + result);
				Log.e(" InitEngine ", result + "");
				super.onPostExecute(result);
				if (result != 0) {// 初始化引擎失败返回错误码
					Intent data = new Intent();
					data.putExtra(EXTRA_KEY_RESULT_ERROR_CODE, result);
					setResult(RESULT_CANCELED, data);
					finish();
				}
			}

		}.execute();

	}

	// -------change----
	public static final String EXTRA_KEY_RESULT_ERROR_CODE = "EXTRA_KEY_RESULT_ERROR_CODE";

	boolean mNeedInitCameraInResume = false;

	@Override
	protected void onResume() {
		super.onResume();
		try {
			mCamera = Camera.open(defaultCameraId);// open the default camera
		} catch (Exception e) {
			e.printStackTrace();
			CameraRecognizeUtil.showFailedDialogAndFinish(PreviewActivity.this);
			return;
		}
		/********************************* preview是自定义的viewgroup 继承了surfaceview,将相机和surfaceview 通过holder关联 ***********************/
		mPreview.setCamera(mCamera);
		/********************************* 设置显示的图片和预览角度一致 ***********************/
		setDisplayOrientation();
		try {

			/********************************* 对surfaceview的PreviewCallback的 callback监听，回调onPreviewFrame ***********************/
			mCamera.setOneShotPreviewCallback(this);
		} catch (Exception e) {
			e.printStackTrace();

		}
		/*************************** 当按power键后,再回到程序,surface 不会调用created/changed,所以需要主动初始化相机参数******START ***********************/
		if (mNeedInitCameraInResume) {
			mPreview.surfaceCreated(mPreview.mHolder);
			mPreview.surfaceChanged(mPreview.mHolder, 0,
					mPreview.mSurfaceView.getWidth(),
					mPreview.mSurfaceView.getHeight());
			mHandler.sendEmptyMessageDelayed(100, 100);
		}
		mNeedInitCameraInResume = true;
		/********************************* END ***********************/

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mCamera != null) {
			Camera camera = mCamera;
			mCamera = null;
			camera.setOneShotPreviewCallback(null);
			mPreview.setCamera(null);
			camera.release();
			camera = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHandler.removeMessages(MSG_AUTO_FOCUS);

		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}

	}

	Map<String, Float> mapValueMap = null;
	boolean isRecognize = false;// 判断是否在找边，如果进入识别则停止找边

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Size size = camera.getParameters().getPreviewSize();
		if (mDetectThread == null) {
			int newWidth = size.height;
			int newHeight = size.width;

			mapValueMap = getPositionWithArea(newWidth, newHeight, 1, 1);
			mDetectThread = new DetectThread();
			mDetectThread.start();
			/********************************* 自动对焦的核心 启动handler 来进行循环对焦 ***********************/
			mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 100);

		}
		/********************************* 向预览线程队列中 加入预览的 data 分析是否ismatch ***********************/

		if (!isRecognize) {
			mDetectThread.addDetect(data, size.width, size.height);
		} else {
			resumePreviewCallback();
		}

	}

	/**
	 * 功能：继续预览识别的回调注册
	 */
	private void resumePreviewCallback() {
		if (mCamera != null) {
			mCamera.setOneShotPreviewCallback(this);
		}
	}

	/**
	 * 功能：将显示的照片和预览的方向一致
	 */
	private void setDisplayOrientation() {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(defaultCameraId, info);
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		int result = (info.orientation - degrees + 360) % 360;
		mCamera.setDisplayOrientation(result);
	}

	public boolean isSupported(String value, List<String> supported) {
		return supported == null ? false : supported.indexOf(value) >= 0;
	}

	private static final int MSG_AUTO_FOCUS = 100;
	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_AUTO_FOCUS) {
				if (!isRecognize) {
					autoFocus();
				}
			}
		};
	};

	private void autoFocus() {
		if (mCamera != null) {
			try {
				mCamera.autoFocus(focusCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	boolean isFocus = false;

	/**
	 * 功能：对焦后的回调，每次返回bool值，如果对焦成功延时2秒对焦，如果失败继续对焦
	 */
	AutoFocusCallback focusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.d("lz", "success==" + success);

			if (success) {
				if (camera != null) {
					isFocus = true;
				}
			} else {
				if (camera != null) {
					isFocus = false;
				}
			}

			mHandler.sendEmptyMessageDelayed(MSG_AUTO_FOCUS, 2000L);

		}
	};

	boolean isVertical = false;

	/**
	 * @CN: 功能：根据当前屏幕的方向还有 宽高，还有证件的比例比如身份证高宽比 0.618来算出
	 *      预览框的位置和大小，可以更改此处来更改预览框的方向位置还有大小
	 * @EN :Function: according to the current screen orientation and high
	 *     width, and the proportion of documents such as identity card with
	 *     high aspect ratio the size and location of the 0.618 to calculate the
	 *     preview box, you can change here to change the direction of the
	 *     location and size of the preview box
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	public Map<String, Float> getPositionWithArea(int newWidth, int newHeight,
                                                  float scaleW, float scaleH) {
		float left, top, right, bottom;
		Map<String, Float> map = new HashMap<String, Float>();
		if (isVertical) {// vertical
			float dis = 1 / 20f;
			left = newWidth * dis;
			right = newWidth - left;
			top = 200f * scaleH;
			bottom = top + (newWidth - left - left) * 0.618f;

		} else {// horizental
			float dis = 1 / 10f;// 10

			left = newWidth * dis;
			right = newWidth - left;
			top = (newHeight - (newWidth - left - left) / 0.618f) / 2;
			bottom = newHeight - top;

		}
		map.put("left", left);
		map.put("right", right);
		map.put("top", top);
		map.put("bottom", bottom);
		return map;

	}

	/**
	 * A simple wrapper around a Camera and a SurfaceView that renders a
	 * centered preview of the Camera to the surface. We need to center the
	 * SurfaceView because not all devices have cameras that support preview
	 * sizes at the same aspect ratio as the device's display.
	 */
	private class Preview extends ViewGroup implements SurfaceHolder.Callback {
		private final String TAG = "Preview";
		private SurfaceView mSurfaceView = null;
		private SurfaceHolder mHolder = null;
		private Size mPreviewSize = null;
		private List<Size> mSupportedPreviewSizes = null;
		private Camera mCamera = null;
		private DetectView mDetectView = null;
		private TextView mInfoView = null;

		public Preview(Context context) {
			super(context);
			/*********************************
			 * 自定义viewgrop上添加SurfaceView 然后对应的其他ui DetectView是自定义的预览框
			 *
			 * ***********************/

			mSurfaceView = new SurfaceView(context);
			addView(mSurfaceView);

			mInfoView = new TextView(context);
			addView(mInfoView);

			mDetectView = new DetectView(context);
			addView(mDetectView);

			mHolder = mSurfaceView.getHolder();
			mHolder.addCallback(this);
		}

		public void setCamera(Camera camera) {
			mCamera = camera;
			if (mCamera != null) {
				mSupportedPreviewSizes = mCamera.getParameters()
						.getSupportedPreviewSizes();
				requestLayout();
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

			final int width = resolveSize(getSuggestedMinimumWidth(),
					widthMeasureSpec);
			final int height = resolveSize(getSuggestedMinimumHeight(),
					heightMeasureSpec);
			setMeasuredDimension(width, height);
			Log.e(TAG, "xxxx onMesaure " + width + " " + height);
			if (mSupportedPreviewSizes != null) {
				int targetHeight = 720;
				if (width > targetHeight && width <= 1080)
					targetHeight = width;
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
						height, width, targetHeight);// 竖屏模式，寬高颠倒

				Log.e(TAG, "xxxx mPreviewSize " + mPreviewSize.width + " "
						+ mPreviewSize.height);

			}
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			if (changed && getChildCount() > 0) {
				final View child = getChildAt(0);

				final int width = r - l;
				final int height = b - t;

				int previewWidth = width;
				int previewHeight = height;
				if (mPreviewSize != null) {
					previewWidth = mPreviewSize.height;
					previewHeight = mPreviewSize.width;
				}

				if (width * previewHeight > height * previewWidth) {
					final int scaledChildWidth = previewWidth * height
							/ previewHeight;
					child.layout((width - scaledChildWidth) / 2, 0,
							(width + scaledChildWidth) / 2, height);
					mDetectView.layout((width - scaledChildWidth) / 2, 0,
							(width + scaledChildWidth) / 2, height);
				} else {
					final int scaledChildHeight = previewHeight * width
							/ previewWidth;
					child.layout(0, (height - scaledChildHeight) / 2, width,
							(height + scaledChildHeight) / 2);
					mDetectView.layout(0, (height - scaledChildHeight) / 2,
							width, (height + scaledChildHeight) / 2);
				}
				getChildAt(1).layout(l, t, r, b);
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {

			try {
				if (mCamera != null) {
					mCamera.setPreviewDisplay(holder);
				}
			} catch (IOException exception) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()",
						exception);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		}

		/**
		 * 功能：获取预览的相机合适分辨率size
		 *
		 * @param sizes
		 * @param w
		 * @param h
		 * @param targetHeight
		 * @return
		 */
		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h,
                                           int targetHeight) {
			final double ASPECT_TOLERANCE = 0.2;
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;
			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff&& Math.abs(ratio - 1.77f) < 0.02) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}
			return optimalSize;
		}

		/**
		 * 功能：获取相机的拍照合适size
		 *
		 * @param sizes
		 * @param th
		 * @return
		 */
		private Size getOptimalPictureSize(List<Size> sizes, int th) {
			Collections.sort(sizes, sizeComparator);

			int i = 0;
			for (Size s : sizes) {
				if ((s.width >= th) && equalRate(s, 1.33f)) {
					break;
				}
				i++;
			}
			return sizes.get(i);
		}

		public boolean equalRate(Size s, float rate) {
			float r = (float) (s.width) / (float) (s.height);
			if (Math.abs(r - rate) <= 0.2) {
				return true;
			} else {
				return false;
			}
		}

		private CameraSizeComparator sizeComparator = new CameraSizeComparator();

		public class CameraSizeComparator implements Comparator<Size> {
			// 按升序排列
			public int compare(Size lhs, Size rhs) {
				if (lhs.width == rhs.width) {
					return 0;
				} else if (lhs.width > rhs.width) {
					return 1;
				} else {
					return -1;
				}
			}

		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
			if (mCamera != null) {

				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setPreviewSize(mPreviewSize.width,
						mPreviewSize.height);
				parameters.setPreviewFormat(ImageFormat.NV21);
				parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片格式
				requestLayout();
				mDetectView.setPreviewSize(mPreviewSize.width,
						mPreviewSize.height);
				List<Size> mSupportedPictureSizes = mCamera.getParameters()
						.getSupportedPictureSizes();

				Size picSize = getOptimalPictureSize(mSupportedPictureSizes,
						1600);
				Log.e(TAG, "picSize:width:" + picSize.width + ",height:"
						+ picSize.height);
				parameters.setPictureSize(picSize.width, picSize.height); // 设置保存的图片尺寸

				parameters.setJpegQuality(100); // 设置照片质量

				mCamera.setParameters(parameters);

				mCamera.startPreview();
			}
		}

		public void showBorder(int[] border, boolean match) {
			mDetectView.showBorder(border, match);
		}

	}

	/**
	 * the view show bank card border.
	 */

	private class DetectView extends View {
		private Paint paint = null;
		private boolean match = false;
		private int previewWidth;
		private int previewHeight;
		private int  mColorMatch = 0xff2A7DF3;
		private int mColorNormal = 0xff01d2ff;

		// 蒙层位置路径
		Path mClipPath = new Path();
		RectF mClipRect = new RectF();
		float mRadius = 12;
		float cornerSize = 80;// 4个角的大小
		float cornerStrokeWidth = 8;

		public void showBorder(int[] border, boolean match) {
			this.match = match;
			postInvalidate();
		}

		public DetectView(Context context) {
			super(context);
			paint = new Paint();
			paint.setColor(0xffff0000);
		}

		public void setPreviewSize(int width, int height) {
			this.previewWidth = width;
			this.previewHeight = height;
		}

		// 计算蒙层位置
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public void upateClipRegion(float scale, float scaleH) {
			float left, top, right, bottom;
			float density = getResources().getDisplayMetrics().density;
			mRadius = 0;
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
				setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
			cornerStrokeWidth = 4 * density;

			Map<String, Float> map = getPositionWithArea(getWidth(),
					getHeight(), scale, scaleH);
			left = map.get("left");
			right = map.get("right");
			top = map.get("top");
			bottom = map.get("bottom");

			mClipPath.reset();
			mClipRect.set(left, top, right, bottom);
			mClipPath.addRoundRect(mClipRect, mRadius, mRadius, Direction.CW);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			float scaleW;
			float scaleH;

			scaleW = getWidth() / (float) previewHeight;
			scaleH = getHeight() / (float) previewWidth;
			upateClipRegion(scaleW, scaleH);
		}

		@Override
		public void onDraw(Canvas c) {

			/**
			 * 绘制 灰色蒙层
			 */
			/********************************
			 * START*************************************
			 * 绘制预览框的四个角，根据预览是否匹配改变角的颜色
			 */
			if (match) {// 设置颜色
				paint.setColor(mColorMatch);
			} else {
				paint.setColor(mColorNormal);
			}
			float len = cornerSize;
			float strokeWidth = cornerStrokeWidth;
			paint.setStrokeWidth(strokeWidth);
			// 左上
			c.drawLine(mClipRect.left, mClipRect.top + strokeWidth / 2,
					mClipRect.left + len + strokeWidth / 2, mClipRect.top
							+ strokeWidth / 2, paint);
			c.drawLine(mClipRect.left + strokeWidth / 2, mClipRect.top
					+ strokeWidth / 2, mClipRect.left + strokeWidth / 2,
					mClipRect.top + len + strokeWidth / 2, paint);
			// 右上
			c.drawLine(mClipRect.right - len - strokeWidth / 2, mClipRect.top
					+ strokeWidth / 2, mClipRect.right, mClipRect.top
					+ strokeWidth / 2, paint);
			c.drawLine(mClipRect.right - strokeWidth / 2, mClipRect.top
					+ strokeWidth / 2, mClipRect.right - strokeWidth / 2,
					mClipRect.top + len + strokeWidth / 2, paint);
			// 右下
			c.drawLine(mClipRect.right - len - strokeWidth / 2,
					mClipRect.bottom - strokeWidth / 2, mClipRect.right,
					mClipRect.bottom - strokeWidth / 2, paint);
			c.drawLine(mClipRect.right - strokeWidth / 2, mClipRect.bottom
					- len - strokeWidth / 2, mClipRect.right - strokeWidth / 2,
					mClipRect.bottom - strokeWidth / 2, paint);
			// 左下
			c.drawLine(mClipRect.left, mClipRect.bottom - strokeWidth / 2,
					mClipRect.left + len + strokeWidth / 2, mClipRect.bottom
							- strokeWidth / 2, paint);
			c.drawLine(mClipRect.left + strokeWidth / 2, mClipRect.bottom - len
					- strokeWidth / 2, mClipRect.left + strokeWidth / 2,
					mClipRect.bottom - strokeWidth / 2, paint);

		}
	}

	/********************************* 预览框的UI自定义 START *********************************************/

	private class DetectThread extends Thread {
		private ArrayBlockingQueue<byte[]> mPreviewQueue = new ArrayBlockingQueue<byte[]>(
				1);
		private int width;
		private int height;

		@SuppressWarnings("unused")
		public void stopRun() {
			addDetect(new byte[] { 0 }, -1, -1);
		}

		@Override
		public void run() {
			try {
				while (true) {
					byte[] data = mPreviewQueue.take();// block here, if no data
														// in the queue.
					if (data.length == 1) {// quit the thread, if we got special
											// byte array put by stopRun().
						return;
					}
					float left, top, right, bottom;
					Map<String, Float> map = mapValueMap;
					left = map.get("left");
					right = map.get("right");
					top = map.get("top");
					bottom = map.get("bottom");
					/********************************* 通过底册api 将预览的数据 还有证件的坐标位置 获取当前一帧证件的4个点坐标的数组 ***********************/
					int[] out = BCRSDK.getInstance().DetectCardEdge(data,
							width, height);
					if (out != null) {// find border

						for (int i = 0; i < 4; i++) {
							int tmp = out[0 + i * 2];
							out[0 + i * 2] = height - out[1 + i * 2];
							out[1 + i * 2] = tmp;
						}

						boolean match = false;

						match = isMatch((int) left, (int) top, (int) right,
								(int) bottom, out);
						/********************************* 实时画出预览 证件的虚拟边框，用来辅助 将证件 与预览框重合 更好识别 ***********************/

						mPreview.showBorder(out, match);

						/********************************* 当前预览帧的 证件四个点的坐标 和 预览框的证件4个点的坐标 校验，在一定范围内认定校验成功 ***********************/
						if (match) {

							takepictrueCameraTake();
						}
					} else {// no find border, continue to preview;
						mPreview.showBorder(null, false);
					}
					// continue to preview;
					resumePreviewCallback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 当前预览帧的 证件四个点的坐标 和 预览框的证件4个点的坐标 校验，在一定范围内认定校验成功
		 * 注意点：其中120是多次验证的值，没有其他理由校验比较稳定，这个值可以自己尝试改变 注意：判断远近 稍有误差 可以作为适当辅助预览
		 * 
		 * @param left
		 * @param top
		 * @param right
		 * @param bottom
		 * @param qua
		 * @return
		 */
		int continue_match_time = 0;

		public boolean isMatch(int left, int top, int right, int bottom,
				int[] qua) {
			int dif = 90;
			int num = 0;

			if (Math.abs(left - qua[6]) < dif && Math.abs(top - qua[7]) < dif) {
				num++;
			}
			if (Math.abs(right - qua[0]) < dif && Math.abs(top - qua[1]) < dif) {
				num++;
			}
			if (Math.abs(right - qua[2]) < dif
					&& Math.abs(bottom - qua[3]) < dif) {
				num++;
			}
			if (Math.abs(left - qua[4]) < dif
					&& Math.abs(bottom - qua[5]) < dif) {
				num++;
			}
			if (num > 2) {
				continue_match_time++;
				if (continue_match_time > 1)
					return true;
			} else {
				continue_match_time = 0;
			}
			return false;
		}

		public void addDetect(byte[] data, int width, int height) {
			if (mPreviewQueue.size() == 1) {
				mPreviewQueue.clear();
			}
			mPreviewQueue.add(data);
			this.width = width;
			this.height = height;
		}

	}

	/**
	 * 初始化预览界面左边按钮组，可以选择正反面识别 正面识别 反面识别 注：如果客户想要自定义预览界面，可以参考
	 * initButtonGroup中的添加方式
	 */
	ImageView take_photo_id;

	private void initButtonGroup() {
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		// **********************************添加动态的布局----相机页面自定义布局
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.camera, null);
		take_photo_id = (ImageView) view.findViewById(R.id.take_photo_id);
		take_photo_id.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					break;
				case MotionEvent.ACTION_UP:
					if (!isRecognize) {
						takepictrueCameraTake();
					} else {
						Log.e("takepictrueCameraTake", "正在预览识别");
					}
					break;
				default:
					break;
				}
				return true;
			}
		});
		rootView.addView(view, lp);
	}

	PictureCallback pictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(final byte[] data, Camera camera) {

			Bitmap bm = RecogResultActivity.loadBitmap(data);

			String copyfilename = MainActivity.DIR_IMG_RESULT + "ori.jpg";
			File file = new File(MainActivity.DIR_IMG_RESULT);
			if (!file.exists()) {
				file.mkdir();
			}

		
			RecogResultActivity.saveBitmap(copyfilename, bm);

			new CameraRecognizeUtil(PreviewActivity.this, take_photo_id)
					.doRecogWork(copyfilename);

		}
	};
	ToneGenerator tone;

	public void takepictrueCameraTake() {
		isRecognize=true;//已经开始识别则不允许继续预览找边操作

		mCamera.autoFocus(new AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean arg0, Camera arg1) {
				

				mCamera.takePicture(new ShutterCallback() {
					@Override
					public void onShutter() {
						if (tone == null) {
							// 发出提示用户的声音
							tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
									ToneGenerator.MAX_VOLUME);
						}
						tone.startTone(ToneGenerator.TONE_PROP_BEEP);
					}
				}, null, pictureCallback);
			}
		});
	}

}
