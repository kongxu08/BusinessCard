package com.cjwsjy.app.businesscard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intsig.sdk.BCRSDK;
import com.intsig.sdk.ContactInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class CameraRecognizeUtil {
	Activity activity;
	ImageView textView;

	public CameraRecognizeUtil(Activity activity, ImageView textView) {
		this.activity = activity;
		this.textView = textView;

	}

	/**
	 * 做识别工作放这里
	 */
	public void doRecogWork(final String imgPath) {
		popupHandler.sendEmptyMessageDelayed(MSG_POPUP, 10);
		new AsyncTask<Void, Void, ContactInfo[]>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected ContactInfo[] doInBackground(Void... params) {
				final ContactInfo[] tempResult = new ContactInfo[1];

				BCRSDK.getInstance().RecognizeCard(
						imgPath,
						new int[] { BCRSDK.ISLangOpt_English,
								BCRSDK.ISLangOpt_Chinese_Simp,
								BCRSDK.ISLangOpt_Chinese_Trad }, true, true,

						new BCRSDK.ResultCallback() {

							@Override
							public boolean onRecognize(int code,
									ContactInfo result) {
								tempResult[0] = result;
								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (popupWindow != null)
											popupWindow.dismiss();
									}
								});
								return true;
							}

							@Override
							public boolean onReceivePrecisedResult(int code,
									ContactInfo result) {

								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (popupWindow != null)
											popupWindow.dismiss();
									}
								});

								tempResult[0] = result;
								return false;
							}

							@Override
							public boolean onImageProcessed(int code,
									String path) {
								System.out.println("onImageProcessed " + code
										+ " " + path);
								if (code >= 0) {
									final Bitmap bmp = BitmapFactory
											.decodeFile(path);
									String cameraPathString = MainActivity.DIR_IMG_RESULT
											+ "trim.jpg";

									saveTemp(bmp, cameraPathString);

									final ContactInfo contactInfo = tempResult[0];
									contactInfo.setOriImageUrl(imgPath);
									contactInfo
											.setTrimImageUrl(cameraPathString);
									activity.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											if (popupWindow != null)
												popupWindow.dismiss();
											showResult(contactInfo);

										}
									});
								}
								return true;
							}
						});
				return tempResult;
			}

		}.execute();
	}

	PopupWindow popupWindow;

	boolean boolpopupWindow = true;

	public void popupwindowShowProgress() {
		RelativeLayout layout = (RelativeLayout) LayoutInflater.from(activity)
				.inflate(R.layout.cui_view_loadingdialog, null);
		popupWindow = new PopupWindow(layout,
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT, true);

		popupWindow.showAtLocation(textView, Gravity.CENTER, 0, 0);

		loading_text = (TextView) layout.findViewById(R.id.loading_text);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				popupHandler.sendEmptyMessage(MSG_Loading);
			}
		}, 0, 500);
		if (!boolpopupWindow) {
			popupWindow.dismiss();
			boolpopupWindow = true;
		}
	}

	private static final int MSG_Loading = 0;
	private static final int MSG_POPUP = 1;
	int count = 0;
	TextView loading_text;

	@SuppressLint("HandlerLeak") private Handler popupHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_POPUP:
				popupwindowShowProgress();
				break;

			case MSG_Loading:
				count++;
				if (count > 3) {
					count = 1;
				}
				switch (count) {
				case 1:
					loading_text.setText("识别中.  ");
					break;
				case 2:
					loading_text.setText("识别中.. ");
					break;
				case 3:
					loading_text.setText("识别中...");
					break;
				}
				break;
			}
		}

	};

	/**
	 * 临时储存
	 * 
	 * @param bitmap
	 * @param imageName
	 * @throws IOException
	 */
	public String saveTemp(Bitmap bitmap, String cameraPathString) {

		File file = new File(cameraPathString);
		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			if (!bitmap.isRecycled()) {
				bitmap.recycle(); // 回收图片所占的内存
				System.gc(); // 提醒系统及时回收
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return cameraPathString;
	}

	private void showResult(ContactInfo result) {
		Intent data = new Intent();
		data.putExtra(PreviewActivity.EXTRA_KEY_RESULT_DATA, result);

		if (result.getOriImageUrl() != null) {
			data.putExtra(PreviewActivity.EXTRA_KEY_RESULT_IMAGE,
					result.getOriImageUrl());
		} else if (result.getTrimImageUrl() != null) {
			data.putExtra(PreviewActivity.EXTRA_KEY_RESULT_IMAGE,
					result.getTrimImageUrl());
		}

		activity.setResult(Activity.RESULT_OK, data);
		activity.finish();
	}
	public static void copyTmpData(String path, String fileName, Context context) {
		File file = new File(path);
		long nsize = 0;

		if (!file.exists() || (file.length() != nsize)) {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				is = context.getResources().getAssets().open(fileName);
				fos = new FileOutputStream(file);
				byte[] buf = new byte[1024];
				int size;
				while ((size = is.read(buf)) != -1) {
					fos.write(buf, 0, size);
				}
				fos.close();
				is.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (fos != null)
						fos.close();
					if (is != null)
						is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}


	public static void showFailedDialogAndFinish(final Activity context) {
		new AlertDialog.Builder(context)
				.setMessage(R.string.fail_to_contect_camcard)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								context.finish();
							}
						}).create().show();
	}
}
