package checkauto.camera.com;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import checkauto.camera.com.util.Utils;
import kernel.BusinessCard.android.RecogParameterMessage;
import kernel.BusinessCard.android.RecogService;
import kernel.BusinessCard.android.ResultMessage;

/**
 * @author zouxh
 * 识别辅助类，识别程序接口，主要分两种识别方式Activity和Service 这里推荐用Activity识别方式
 * 根据识别数据源（nv21格式和GBK格式）的不同分为两个识别接口
 *
 */
public class RecogOpera {
	private Context context;
	public static String path = Environment.getExternalStorageDirectory()
			.toString() + "/wtimage/";
	private String lpFileName = "";
	private int width, height;

	public RecogOpera(int byteDataType, Context context) {
		RecogService.byteDataType = byteDataType;
		this.context = context;
	}

	private RecogService.recogBinder recogBinder;
	/**
	 * 识别Service服务
	 */
	public ServiceConnection recogConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			recogConn = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				RecogParameterMessage rpm = new RecogParameterMessage();
				rpm.devcode = Devcode.DEVCODE;
				rpm.sn = "";
				rpm.lpFileName = lpFileName;// 拍照识别的图片识别路径，扫描识别时可为""
				rpm.recogBytes_width = width;// 预览图像的宽
				rpm.recogBytes_height = height;// 预览图像的高
				rpm.isSaveCut = false;// 是否保存裁切图片
				recogBinder = (RecogService.recogBinder) service;

				if (recogBinder != null) {
					ResultMessage result = recogBinder
							.getVicardRecogResult(rpm);
					Bundle bundle = new Bundle();
					if (result.ReturnAuthority == 0) {
						Intent intent=new Intent(context, BucardRunner.class);
						ArrayList<HashMap<String, String>> Data = result.data;
						bundle.putString("ReturnTime", result.time);
						bundle.putInt("RecogReturn", result.RecogReturn);
						bundle.putInt("ReturnInitBuCard", result.ReturnInitBuCard);
						bundle.putInt("ReturnAuthority", result.ReturnAuthority);
						bundle.putString("ReturnGetVersionInfo",
								result.ReturnGetVersionInfo);
						context.startActivity(intent);
						
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (recogBinder != null) {
					context.unbindService(recogConn);
				}

			}

		}
	};

	/**
	 * 描述：Service识别方式识别程序接口（不推荐）
	 * 
	 * @param nv21data
	 *            预览数据格式，通常用于扫描识别中，如果是拍照识别，可以将此参数置空
	 * @param size
	 *            相机的预览分辨率的集合，通常用于扫描识别中，如果是拍照识别，可以将此参数置空
	 * @param width
	 *            预览图片的宽度 拍照识别时设置成0
	 * @param height
	 *            预览图片的高度 拍照识别时设置成0
	 * @param recogPicturePath
	 *            拍照识别方式的识别图片的路径，扫描识别时可为空
	 */
	public void startServiceRecog(byte[] nv21data, Size size, int width,
			int height, String recogPicturePath) {
		String picPathString = "";
		int noFoundProgram = context.getResources().getIdentifier(
				"noFoundProgram", "string", context.getPackageName());
		try {
			if (RecogService.byteDataType == 0) {
				// 存取测试图片 start
				picPathString = path + "card_full.jpg";
				File file = new File(path);
				if (!file.exists()){
					file.mkdirs();
				}
				File file1 = new File(picPathString);
				Utils.freeFileLock(new FileOutputStream(picPathString)
						.getChannel().tryLock(), file1);
				YuvImage yuvimage = new YuvImage(nv21data, ImageFormat.NV21,
						size.width, size.height, null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				yuvimage.compressToJpeg(new Rect((int) (size.width * 0.15),
						(int) (size.height - 0.41004673 * size.width) / 2,
						(int) (size.width * 0.8),
						(int) (size.height + 0.41004673 * size.width) / 2), 80,
						baos);
				FileOutputStream outStream;
				outStream = new FileOutputStream(picPathString);
				outStream.write(baos.toByteArray());
				outStream.close();
				baos.close();
				// 存取测试图片 end
				RecogService.recogBytes = nv21data;
				this.width = width;
				this.height = height;
			} else if (RecogService.byteDataType == 1) {
				RecogService.byteDataType = 1;
				this.lpFileName = recogPicturePath;
			}
			// else if (RecogService.byteDataType == 2) {
			// RecogService.byteDataType = 2;
			// this.lpFileName = recogPicturePath;
			// RecogService.recogInts=rgbData;
			// this.width=width;
			// this.height=height;
			// }
			Intent recogIntent = new Intent(context, RecogService.class);
			context.bindService(recogIntent, recogConn,
					Service.BIND_AUTO_CREATE);
		} catch (Exception e) {
			Toast.makeText(
					context,
					context.getString(noFoundProgram)
							+ "kernel.bucard", 0).show();
			e.printStackTrace();
		}

	}

	/**
	 * 描述：Activity识别方式识别程序接口（推荐）
	 * 
	 * @param nv21data
	 *            预览数据格式，通常用于扫描识别中，如果是拍照识别，可以将此参数置空
	 * @param size
	 *            相机的预览分辨率的集合，通常用于扫描识别中，如果是拍照识别，可以将此参数置空
	 * @param width
	 *            预览图片的宽度 拍照识别时设置成0
	 * @param height
	 *            预览图片的高度 拍照识别时设置成0
	 * @param picturePath
	 *            拍照识别方式的识别图片的路径，扫描识别时可为空
	 */
	public void startActivityRecog(byte[] nv21data, Size size, int width,
			int height, String picturePath,String cutPicturePath) {
		String picPathString = "";
		int noFoundProgram = context.getResources().getIdentifier(
				"noFoundProgram", "string", context.getPackageName());
		try {
			if (RecogService.byteDataType == 0) {
				picPathString=picturePath;
				RecogService.recogBytes = nv21data;
			} else if (RecogService.byteDataType == 1) {
				RecogService.byteDataType = 1;
				picPathString = picturePath;
			}
			// else if (RecogService.byteDataType == 2) {
			// RecogService.byteDataType = 2;
			// picPathString = picturePath;
			// }


			Intent intent = new Intent("kernel.bucard");
			Bundle bundle = new Bundle();
			bundle.putString("lpFileName", picPathString);// 指定的图像路径，预览扫描识别时，该参数代表动画图片路径
			bundle.putString("devcode", Devcode.DEVCODE);
			bundle.putInt("recogBytes_width", width);// 预览图像的宽
			bundle.putInt("recogBytes_height", height);// 预览图像的高
			bundle.putString("returntype", "withvalue");// 返回值传递方式withvalue带参数的传值方式（onActivityResult方式返回识别结果）
		//	bundle.putBoolean("isSaveCut", true);// 是否保存裁切图片
			bundle.putString("cutPicturePath", cutPicturePath);// 指定的图像路径，预览扫描识别时，该参数代表动画图片路径
			bundle.putString("enhancementpath",CameraActivity.enHancementPath);
			intent.putExtras(bundle);
			((Activity) context).startActivityForResult(intent, 8);
		} catch (Exception e) {
			Toast.makeText(
					context,
					context.getString(noFoundProgram)
							+ "kernel.bucard", 0).show();
			e.printStackTrace();
		}
	}
}
