package checkauto.camera.com;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kernal.demo.bucard.R;

import java.util.ArrayList;
import java.util.HashMap;

import checkauto.camera.com.util.AppManager;
import checkauto.camera.com.util.SharedPreferencesHelper;

//识别Activity调用类及结果显示页面
public class BucardRunner extends Activity {
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	public static final String TAG = "BucardRunner";
	public static final String PATH = Environment.getExternalStorageDirectory()
			.toString() + "/AndroidWT";
	private String cut_path;
	private String str = "";
	private EditText editResult;
	private int  bucardrunner, exception, exception1, exception2,
			exception6;
	private  int nCropType=0;
	private EditText et_recogPicture;
	private ImageView iv_recogPicture;
	private RelativeLayout re_et_recogPicture;
	private FrameLayout FrameLayout_activity_show_result,FrameLayout_toolbar_show_result;
	private TextView tv_set;
	private Button btn_ok;
	private TextView btn_back;
	private int srcWidth,srcHeight;
    private Handler handler;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		/*this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);*/// 去掉信息栏
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		srcWidth = displayMetrics.widthPixels;
		srcHeight = displayMetrics.heightPixels;
		bucardrunner = this.getResources().getIdentifier("bucardrunner",
				"layout", this.getPackageName());
		setContentView(bucardrunner);
		handler=new Handler();
		cut_path=getIntent().getStringExtra("enHancementPath");
		nCropType=getIntent().getIntExtra("nCropType",0);
		initViews();
	 AppManager.getAppManager().finishActivity(CameraActivity.class);
		readRecogResult();
/*handler.postDelayed(new Runnable() {
	@Override
	public void run() {
		Intent intent =new Intent(BucardRunner.this,CameraActivity.class);
		intent.putExtra("devcode", Devcode.DEVCODE);
		intent.putExtra("nCropType",nCropType);
		intent.putExtra("autocamera", true);
		startActivity(intent);
		BucardRunner.this.finish();
	}
},5000);*/
	}

	/**
	 * 读取识别结果
	 */
	private void readRecogResult() {
		if(cut_path!=null&&!"".equals(cut_path)){
			iv_recogPicture.setImageBitmap(BitmapFactory.decodeFile(cut_path));
		}
		// 读识别返回值
		Bundle bun = getIntent().getBundleExtra("RecogValue");
		if (bun != null) {
			int ReturnAuthority = bun.getInt("ReturnAuthority", -100000);// 取激活状态
			int ReturnInitBUCard = bun.getInt("ReturnInitBuCard", -100000);// 取初始化返回值
			int ReturnRecogBuCard = bun.getInt("RecogReturn", -100000);// 取识别的返回值
			if (ReturnAuthority == 0 && ReturnInitBUCard == 0
					&& ReturnRecogBuCard == 0) {
				// System.out.println("接收结果");
				int authNum = bun.getInt("ReturnAuthority");
				if (authNum == 0) {
					@SuppressWarnings("unchecked")
					ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) bun
							.getParcelableArrayList("list").get(0);
					for (int i = 0; i < list.size(); i++) {
						HashMap<String, String> map = list.get(i);
						str += map.get("Name") + ":" + map.get("Val") + "\n";
					}
				}
				et_recogPicture.setText(str);

			} else {
				String str = "";
				if (ReturnAuthority == -100000) {
					str = getString(R.string.exception) + ReturnAuthority;
				} else if (ReturnAuthority != 0) {
					str = getString(R.string.exception1) + ReturnAuthority;
				} else if (ReturnInitBUCard != 0) {
					str = getString(R.string.exception2) + ReturnInitBUCard;
				} else if (ReturnRecogBuCard != 0) {
					str = getString(R.string.exception6) + ReturnRecogBuCard;
				}
				et_recogPicture.setText("识别结果 :" + str + "\n");
			}
		}

	}

	/**
	 * 初始化布局
	 */
	private void initViews() {
		et_recogPicture = (EditText) findViewById(getResources().getIdentifier(
				"et_recogPicture", "id", getPackageName()));
		iv_recogPicture= (ImageView) findViewById(getResources().getIdentifier(
				"iv_recogPicture", "id", getPackageName()));
		re_et_recogPicture=(RelativeLayout) findViewById(getResources().getIdentifier(
				"re_et_recogPicture", "id", getPackageName()));
		FrameLayout_activity_show_result=(FrameLayout) findViewById(getResources().getIdentifier(
				"FrameLayout_activity_show_result", "id", getPackageName()));
		FrameLayout_toolbar_show_result=(FrameLayout) findViewById(getResources().getIdentifier(
				"FrameLayout_toolbar_show_result", "id", getPackageName()));
		btn_back = (TextView) findViewById(getResources()
				.getIdentifier("btn_back", "id", getPackageName()));
		btn_ok = (Button) findViewById(getResources()
				.getIdentifier("btn_ok", "id", getPackageName()));
		tv_set =(TextView) findViewById(getResources()
				.getIdentifier("tv_set", "id", getPackageName()));
	RelativeLayout.LayoutParams  params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), RelativeLayout.LayoutParams.WRAP_CONTENT);
		et_recogPicture.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), (int)(srcHeight*0.9-srcWidth*0.9*0.75));
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.topMargin= (int)(srcWidth*0.9*0.75)+(int)(srcHeight*0.06);
		re_et_recogPicture.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.5),
				(int) (srcWidth * 0.13));
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		params.addRule(RelativeLayout.BELOW,getResources().getIdentifier(
				"et_recogPicture", "id", getPackageName()));
		btn_ok.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams(
				(int)(srcWidth*0.9), (int)(srcWidth*0.9*0.75));
		params.topMargin=(int)(srcHeight*0.06);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		iv_recogPicture.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams(
				srcWidth, (int)(srcHeight*0.06));
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		FrameLayout_toolbar_show_result.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.145),
				(int) (srcWidth * 0.05));
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin=(int)(srcWidth*0.02);
		btn_back.setLayoutParams(params);

		params = new RelativeLayout.LayoutParams((int) (srcWidth * 0.14),
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.leftMargin=(int)(srcWidth*0.84);
		tv_set.setLayoutParams(params);

		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(BucardRunner.this, ImageChooser.class);
				BucardRunner.this.finish();
				startActivity(intent);
			}
		});
		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();
				intent.setClass(BucardRunner.this, CameraActivity.class);
				intent.putExtra("nCropType",nCropType);
				intent.putExtra("autocamera", SharedPreferencesHelper.getBoolean(
						getApplicationContext(), "isAutoRecog", false));
				BucardRunner.this.finish();
				startActivity(intent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 10 && resultCode == RESULT_OK) {
			// 读识别返回值
			Bundle bun = data.getBundleExtra("GetRecogResult");
			setContentView(bucardrunner);
			initViews();
			int ReturnAuthority = bun.getInt("ReturnAuthority", -100000);// 取激活状态
			int ReturnInitBUCard = bun.getInt("ReturnInitBuCard", -100000);// 取初始化返回值
			int ReturnRecogBuCard = bun.getInt("RecogReturn", -100000);// 取识别的返回值
			if (ReturnAuthority == 0 && ReturnInitBUCard == 0
					&& ReturnRecogBuCard == 0) {
				// System.out.println("接收结果");
				int authNum = bun.getInt("ReturnAuthority");
				if (authNum == 0) {
					ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) bun
							.getParcelableArrayList("list").get(0);
					for (int i = 0; i < list.size(); i++) {
						HashMap<String, String> map = list.get(i);

						str += map.get("Name") + ":" + map.get("Val") + "\n";
					}
				}
				editResult.setText(str);

			} else {
				String str = "";
				if (ReturnAuthority == -100000) {
					str = getString(R.string.exception) + ReturnAuthority;
				} else if (ReturnAuthority != 0) {
					str = getString(R.string.exception1) + ReturnAuthority;
				} else if (ReturnInitBUCard != 0) {
					str = getString(R.string.exception2) + ReturnInitBUCard;
				} else if (ReturnRecogBuCard != 0) {
					str = getString(R.string.exception6) + ReturnRecogBuCard;
				}
				editResult.setText("识别结果 :" + str + "\n");
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent intent = new Intent();
			intent.setClass(BucardRunner.this, CameraActivity.class);
			intent.putExtra("nCropType",nCropType);
			intent.putExtra("autocamera", SharedPreferencesHelper.getBoolean(
					getApplicationContext(), "isAutoRecog", false));
			BucardRunner.this.finish();
			startActivity(intent);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
}
