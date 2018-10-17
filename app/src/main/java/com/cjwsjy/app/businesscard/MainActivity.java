package com.cjwsjy.app.businesscard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

import com.intsig.sdk.CommonUtil;
import com.intsig.sdk.ContactInfo;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";
	private static final String APP_KEY = "2dXPgMPWeJ0U5BbA7JUE3rD5";// 替换您申请的合合信息授权提供的APP_KEY;
																				// 58959e29b5acfde38059004551-Vagfvt
	
	public static boolean boolCheckAppKey = false;
	private static final int REQ_CODE_CAPTURE = 100;

	public static final String EXTRA_KEY_RESULT_IS_ALL_TIME = "EXTRA_KEY_RESULT_IS_ALL_TIME";

	public static final String DIR_IMG_RESULT = Environment.getExternalStorageDirectory()+"/bcrscan/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		
			Intent intent = new Intent(this, PreviewActivity.class);
			// 合合信息授权提供的APP_KEY
			intent.putExtra(PreviewActivity.EXTRA_KEY_APP_KEY, APP_KEY);
			// IDCardScanSDK.OPEN_COMOLETE_CHECK
			// 表示完整性判断，IDCardScanSDK.CLOSE_COMOLETE_CHECK或其它值表示关闭完整判断

			startActivityForResult(intent, REQ_CODE_CAPTURE);
		
	}

	

	@Override
	public void onClick(View view) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == REQ_CODE_CAPTURE) {

//
			ContactInfo result = (ContactInfo) data
					.getSerializableExtra(PreviewActivity.EXTRA_KEY_RESULT_DATA);
			Intent intent = new Intent(this, RecogResultActivity.class);

			intent.putExtra(PreviewActivity.EXTRA_KEY_RESULT_DATA, result);

			startActivity(intent);
//
			this.overridePendingTransition(R.anim.cui_fade_in, 0);
//
				finish();

		} else if (resultCode == RESULT_CANCELED
				&& requestCode == REQ_CODE_CAPTURE) {
			// 识别失败或取消
			Log.d(TAG, "识别失败或取消");
			if (data != null) {
				/**
				 * 101 包名错误 102 appKey错误 103 超过时间限制 104 达到设备上限 201 签名错误 202 其他错误
				 * 203 服务器错误 204 网络错误 205 包名/签名错误
				 */
				int error_code = data.getIntExtra(
						PreviewActivity.EXTRA_KEY_RESULT_ERROR_CODE, 0);
				Toast.makeText(
						this,
						"Error:"
								+ error_code
								+ "\nMsg:"
								+ CommonUtil.getPkgSigKeyLog(MainActivity.this,
										APP_KEY), Toast.LENGTH_LONG).show();
				
				Toast.makeText(
						this,
						"初始化过程中如果报错，报错日志会自动存储在/sdcard/IntsigLog/IntsigLog.txt文件中，请前往拷贝发送给合合信息技术支持分析解决问题，谢谢"
								, Toast.LENGTH_LONG).show();
			}
				finish();

		}
	}
}
