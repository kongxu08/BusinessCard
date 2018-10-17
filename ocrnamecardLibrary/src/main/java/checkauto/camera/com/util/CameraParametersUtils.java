package checkauto.camera.com.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class CameraParametersUtils {
	Camera.Parameters parameters;
	public int srcWidth, srcHeight;
	public int preWidth, preHeight;
	public int picWidth, picHeight;
	public int surfaceWidth, surfaceHeight;
	List<Camera.Size> list;
	private boolean isShowBorder = false;
    private Context context;
	public CameraParametersUtils(Context context) {
		this.context=context;
		setScreenSize(context);
	}
	/**
	 * 获取设备的拍照分辨率的宽和高
	 * @param camera
	 */
 public void getCameraPicParameters(Camera camera){
	 isShowBorder = false;
	 parameters = camera.getParameters();
	 list = parameters.getSupportedPictureSizes();
	 float ratioScreen = (float) srcWidth / srcHeight;
	 for (int i = 0; i < list.size(); i++) {
			float ratioPicture = (float) list.get(i).width / list.get(i).height;
			if (ratioScreen == ratioPicture) {// 判断屏幕宽高比是否与拍照宽高比一样，如果一样执行如下代码
				if (list.get(i).width >= 1600 || list.get(i).height >= 1200) {// 默认预览以1600*1200为标准
					if (picWidth == 0 && picHeight == 0) {// 初始值
						picWidth = list.get(i).width;
						picHeight = list.get(i).height;
					}
					if (list.get(0).width > list.get(list.size() - 1).width) {
						// 如果第一个值大于最后一个值
						if (picWidth > list.get(i).width
								|| picHeight > list.get(i).height) {
							// 当有大于1600*1200的分辨率但是小于之前记载的分辨率，我们取中间的分辨率
							picWidth = list.get(i).width;
							picHeight = list.get(i).height;
						}
					} else {
						// 如果第一个值小于最后一个值
						if (picWidth < list. get(i).width
								|| picHeight < list.get(i).height) {
							// 如果之前的宽度和高度大于等于1600*1200，就不需要再筛选了
							if (picWidth >= 1600 || picHeight >= 1200) {

							} else {
								// 为了找到合适的分辨率，如果picWidth和picHeight没有比1600*1200大的就继续过滤
								picWidth = list.get(i).width;
								picHeight = list.get(i).height;
							}
						}
					}
				}
			}
	 }
	  // 说明没有找到程序想要的分辨率
			if (picWidth == 0 || picHeight == 0) {
				isShowBorder = true;
				picWidth = list.get(0).width;
				picHeight = list.get(0).height;
				for (int i = 0; i < list.size(); i++) {
					
					if (list.get(0).width > list.get(list.size() - 1).width) {
						// 如果第一个值大于最后一个值
						if (picWidth >=list.get(i).width
								|| picHeight >= list.get(i).height) {
							// 当上一个选择的拍照分辨率宽或者高度大于本次的宽度和高度时，执行如下代码:
							if (list.get(i).width >= 1600) {
								// 当本次的预览宽度和高度大于1280*720时执行如下代码
								picWidth = list.get(i).width;
								picHeight = list.get(i).height;

							}
						}
					} else {
						if (picWidth <= list.get(i).width
								|| picHeight <= list.get(i).height) {
							if (picWidth >= 1600 || picHeight >= 1200) {

							} else {
								// 当上一个选择的预览分辨率宽或者高度大于本次的宽度和高度时，执行如下代码:
								if (list.get(i).width >= 1600) {
									// 当本次的预览宽度和高度大于1280*720时执行如下代码
									picWidth = list.get(i).width;
									picHeight = list.get(i).height;

								}
							}

						}
					}
				}
			}
			// 如果没有找到大于1280*720的分辨率的话，取集合中的最大值进行匹配
			if (picWidth == 0 || picHeight == 0) {
				isShowBorder = true;
				if (list.get(0).width > list.get(list.size() - 1).width) {
					picWidth = list.get(0).width;
					picHeight = list.get(0).height;
				} else {
					picWidth = list.get(list.size() - 1).width;
					picHeight = list.get(list.size() - 1).height;
				}
			}
			if (isShowBorder) {
				if (ratioScreen > (float) picWidth / picHeight) {
					float rp=ratioScreen-((float) preWidth / preHeight);
					//如果误差在0.02之内，可以忽略
					if(rp<=0.02){
						surfaceWidth = srcWidth;
						surfaceHeight = srcHeight;
					}else{
						surfaceWidth = (int) (((float) preWidth / preHeight) * srcHeight);
						surfaceHeight = srcHeight;
					}
				} else {
					surfaceWidth = srcWidth;
					surfaceHeight = (int) (((float) picWidth / picHeight) * srcHeight);
				}
			}else{
				surfaceWidth = srcWidth;
				surfaceHeight=srcHeight;
			}
 }
	/**
	 * 获取设备的预览分辨率的宽和高
	 * 
	 * @param camera
	 */
	public void getCameraPreParameters(Camera camera)

	{
		isShowBorder = false;
		// 荣耀七设备
		if ("PLK-TL01H".equals(Build.MODEL)) {
			preWidth = 1920;
			preHeight = 1080;
			surfaceWidth=1920;
			surfaceHeight=1080;
			return;
		}

		preWidth=0;
		preHeight=0;
		// 其他设备
		parameters = camera.getParameters();
		list = parameters.getSupportedPreviewSizes();
		float ratioScreen = (float) srcWidth / srcHeight;
		Collections.sort(list, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size lhs, Camera.Size rhs) {
				if(lhs.width>rhs.width){
					return -1;
				}else if(lhs.width==rhs.width&&lhs.height>rhs.height){
					return -1;
				}
				return 1;
			}
		});
		for (int i = 0; i < list.size(); i++) {
		//	Log.i("string","输出的数值为:"+list.get(i).width+"高度为:"+list.get(i).height);
			float ratioPreview = (float) list.get(i).width / list.get(i).height;
			if (ratioScreen == ratioPreview) {// 判断屏幕宽高比是否与预览宽高比一样，如果一样执行如下代码
				if (list.get(i).width >= 1600 || list.get(i).height >= 1200) {// 默认预览以1280*720为标准
					if (preWidth == 0 && preHeight == 0) {// 初始值
						preWidth = list.get(i).width;
						preHeight = list.get(i).height;
					}
					if (list.get(0).width > list.get(list.size() - 1).width) {
						// 如果第一个值大于最后一个值
						if (preWidth > list.get(i).width
								|| preHeight > list.get(i).height) {
							// 当有大于1280*720的分辨率但是小于之前记载的分辨率，我们取中间的分辨率
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;
						}
					} else {
						// 如果第一个值小于最后一个值
						if (preWidth < list.get(i).width
								|| preHeight <list.get(i).height) {
							// 如果之前的宽度和高度大于等于1280*720，就不需要再筛选了
							if (preWidth >= 1600 || preHeight >= 1200) {

							} else {
								// 为了找到合适的分辨率，如果preWidth和preHeight没有比1280*720大的就继续过滤
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;
							}
						}
					}
				}
			}
		}
		// 说明没有找到程序想要的分辨率
		if (preWidth == 0 || preHeight == 0) {
			isShowBorder = true;
			preWidth = list.get(0).width;
			preHeight = list.get(0).height;
			for (int i = 0; i < list.size(); i++) {
				
				if (list.get(0).width > list.get(list.size() - 1).width) {
					// 如果第一个值大于最后一个值
					if (preWidth >=list.get(i).width
							|| preHeight >=list.get(i).height) {
						// 当上一个选择的预览分辨率宽或者高度大于本次的宽度和高度时，执行如下代码:
						if (list.get(i).width >= 1600) {
							// 当本次的预览宽度和高度大于1280*720时执行如下代码
							preWidth = list.get(i).width;
							preHeight = list.get(i).height;

						}
					}
				} else {
					if (preWidth <= list.get(i).width
							|| preHeight <= list.get(i).height) {
						if (preWidth >= 1600 || preHeight >= 1200) {

						} else {
							// 当上一个选择的预览分辨率宽或者高度大于本次的宽度和高度时，执行如下代码:
							if (list.get(i).width >= 1600) {
								// 当本次的预览宽度和高度大于1280*720时执行如下代码
								preWidth = list.get(i).width;
								preHeight = list.get(i).height;

							}
						}

					}
				}
			}
		}
		
		// 如果没有找到大于1280*720的分辨率的话，取集合中的最大值进行匹配
		if (preWidth <= 640 || preHeight <=480) {
			isShowBorder = true;
			if (list.get(0).width > list.get(list.size() - 1).width) {
				preWidth = list.get(0).width;
				preHeight = list.get(0).height;
			} else {
				preWidth = list.get(list.size() - 1).width;
				preHeight = list.get(list.size() - 1).height;
			}
		}
		if (isShowBorder) {
			if (ratioScreen > (float) preWidth / preHeight) {
				surfaceWidth = (int) (((float) preWidth / preHeight) * srcHeight);
				surfaceHeight = srcHeight;
			} else {
				surfaceWidth = srcWidth;
				surfaceHeight = (int) (((float) preHeight / preWidth) * srcWidth);
			}
		}else{
			surfaceWidth = srcWidth;
			surfaceHeight=srcHeight;
		}

	}

	@SuppressLint("NewApi")
	private void setScreenSize(Context context) {
		int x, y;
		WindowManager wm = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Point screenSize = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				display.getRealSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			} else {
				display.getSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			}
		} else {
			x = display.getWidth();
			y = display.getHeight();
	 }

		srcWidth = x;
		srcHeight = y;

	}
	/**
     * @param mDecorView{tags} 设定文件
     * @return ${return_type}    返回类型
     * @throws
     * @Title: 沉寂模式
     * @Description: 隐藏虚拟按键
     */
    @TargetApi(19)
    public void hiddenVirtualButtons(View mDecorView) {
        if (Build.VERSION.SDK_INT >= 19) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }
}
