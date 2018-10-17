package com.cjwsjy.app.businesscard;
/**
 * Project Name:IDCardScanCaller
 * File Name:RecogActivity.java
 * Package Name:com.intsig.idcardscancaller
 * Date:2016年3月15日下午3:58:29
 * Copyright (c) 2016, 上海合合信息 All Rights Reserved.
 *
 */


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.cjwsjy.app.commonui.MyZoomImageView;
import com.cjwsjy.app.commonui.ViewPagerAdapter;
import com.intsig.sdk.ContactInfo;
import com.intsig.sdk.ContactInfo.AddressItem;
import com.intsig.sdk.ContactInfo.CompanyItem;
import com.intsig.sdk.ContactInfo.NameItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecogResultActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.ac_recog);
		Intent intent = getIntent();


		ContactInfo result = (ContactInfo) intent
				.getSerializableExtra(PreviewActivity.EXTRA_KEY_RESULT_DATA);

		// 图片角度,如果有图片的话
		initPagerView(result.getOriImageUrl(), result.getTrimImageUrl());
	
		TextView layout_front_content = ((TextView) findViewById(R.id.layout_front_content));
		StringBuilder tmp = new StringBuilder();

		@SuppressWarnings("unchecked")
        ArrayList<ContactInfo.ContactItem> items = result.items;// .getItems();

		for (ContactInfo.ContactItem item : items) {
			if (item.type == ContactInfo.TYPE_COMPANY) {
				CompanyItem company = (CompanyItem) item;
				tmp.append(getLabel(item.type) + "    "
						+ company.getCompany() + "/"
						+ company.getDepartment() + "/"
						+ company.getTitle() + "\n");
			} else if (item.type == ContactInfo.TYPE_NAME) {
				NameItem name = (NameItem) item;
				tmp.append(getLabel(item.type) + "    "
						+ name.getFirstName() + "/"
						+ name.getMiddleName() + "/"
						+ name.getLastName() + "\n");
			} else if (item.type == ContactInfo.TYPE_ADDRESS) {
				AddressItem adrItem = (AddressItem) item;
				tmp.append(getLabel(item.type) + "    "
						+ adrItem.getCountry() + "/"
						+ adrItem.getProvince() + "/"
						+ adrItem.getCity() + "/"
						+ adrItem.getStreet() + "/"
						+ adrItem.getPostCode() + "\n");
			} else
				tmp.append(getLabel(item.type) + "    "
						+ item.getValue() + "\n");
		}
		
		Log.d("result", "result >>>>>>>>>>>>> "
				+ tmp.toString());
		layout_front_content.setText(tmp.toString());
	
	}
	String getLabel(int type) {
		switch (type) {
		case ContactInfo.TYPE_ADDRESS:
			return "Addr\t:";
		case ContactInfo.TYPE_ANNIVERSARY:
			return "Event\t:";
		case ContactInfo.TYPE_COMPANY:
			return "Company\t:";
		case ContactInfo.TYPE_EMAIL:
			return "Email\t:";
		case ContactInfo.TYPE_IM:
			return "IM\t:";
		case ContactInfo.TYPE_NAME:
			return "Name\t:";
		case ContactInfo.TYPE_NICKNAME:
			return "NickName\t:";
		case ContactInfo.TYPE_PHONE:
			return "Phone\t:";
		case ContactInfo.TYPE_SNS:
			return "SNS\t:";
		case ContactInfo.TYPE_WEBSITE:
			return "Web\t:";
		}
		return "";
	}
	public void onClick(View view) {
		if (!MainActivity.boolCheckAppKey) { 

			Intent intent = new Intent(this, MainActivity.class);

			startActivity(intent);
			finish();
		} else {
			finish();
		}

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent intent = new Intent(this, MainActivity.class);

		startActivity(intent);
		finish();
	}
	
	public static Bitmap loadBitmap(String pathName) {
		Bitmap b = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			b = BitmapFactory.decodeFile(pathName, opts);
		} catch (Exception e) {
			e.printStackTrace();
			b = null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			b = null;
		}
		return b;
	}
	public static Bitmap loadBitmapSample(String pathName) {
		return loadBitmap(pathName, 1200, 1600);
	}

	public static Bitmap loadBitmap(byte[] pathName) {
		return loadBitmap(pathName, 1200, 1600);
	}
	public static void saveBitmap(String copyfilename, Bitmap bm) {
		Log.e("saveBitmap", "保存图片");
		File f = new File(copyfilename);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			Log.i("saveBitmap", "已经保存");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
			}
		}

	}

	public static Bitmap loadBitmap(String pathName, float ww, float hh) {
		Bitmap b = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			b = BitmapFactory.decodeFile(pathName, opts);

			int originalWidth = opts.outWidth;
			int originalHeight = opts.outHeight;

			// float hh = 1280f;// 这里设置高度为800f
			// float ww = 720f;// 这里设置宽度为480f
			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
			int be = 1;// be=1表示不缩放
			if (originalWidth > originalHeight && originalWidth > ww) {// 如果宽度大的话根据宽度固定大小缩放
				be = (int) (originalWidth / ww);
			} else if (originalWidth < originalHeight && originalHeight > hh) {// 如果高度高的话根据宽度固定大小缩放
				be = (int) (originalHeight / hh);
			}
			if (be <= 0)
				be = 1;
			Log.d("decodeFile", "originalWidth:" + originalWidth
					+ ",originalHeight:" + originalHeight + ",be:" + be);

			BitmapFactory.Options optso = new BitmapFactory.Options();
			optso.inJustDecodeBounds = false;
			optso.inPreferredConfig = Bitmap.Config.RGB_565;
			optso.inSampleSize = be;
			b = BitmapFactory.decodeFile(pathName, optso);
		} catch (Exception e) {
			e.printStackTrace();
			b = null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			b = null;
		}
		return b;
	}

	public static Bitmap loadBitmap(byte[] data, float ww, float hh) {
		Bitmap b = null;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			b = BitmapFactory.decodeByteArray(data, 0, data.length, opts);

			int originalWidth = opts.outWidth;
			int originalHeight = opts.outHeight;

			// float hh = 1280f;// 这里设置高度为800f
			// float ww = 720f;// 这里设置宽度为480f
			// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
			int be = 1;// be=1表示不缩放
			if (originalWidth > originalHeight && originalWidth > ww) {// 如果宽度大的话根据宽度固定大小缩放
				be = (int) (originalWidth / ww);
			} else if (originalWidth < originalHeight && originalHeight > hh) {// 如果高度高的话根据宽度固定大小缩放
				be = (int) (originalHeight / hh);
			}
			if (be <= 0)
				be = 1;
			Log.d("decodeFile", "originalWidth:" + originalWidth
					+ ",originalHeight:" + originalHeight + ",be:" + be);

			BitmapFactory.Options optso = new BitmapFactory.Options();
			optso.inJustDecodeBounds = false;
			optso.inPreferredConfig = Bitmap.Config.RGB_565;
			optso.inSampleSize = be;
			b = BitmapFactory.decodeByteArray(data, 0, data.length, optso);
		} catch (Exception e) {
			e.printStackTrace();
			b = null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			b = null;
		}
		return b;
	}
	
	// ***********************************UI***************************************/
	ViewPager vp;
	RadioGroup group;
	ViewPagerAdapter vpAdapter;
	int currentItem;

	@SuppressWarnings("deprecation")
	public void initPagerView(String path1, String path2) {

		vp = (ViewPager) findViewById(R.id.viewpager);
		group = (RadioGroup) findViewById(R.id.RadioGroup);
		group.setOnCheckedChangeListener(listener);
		views = new ArrayList<View>();

		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mParams.setMargins(20, 20, 20, 20);
		if (!TextUtils.isEmpty(path2)) {
			pathList.add(path2);
			MyZoomImageView iv2 = new MyZoomImageView(this);
			iv2.setLayoutParams(mParams);

			Bitmap bmp = loadBitmap(path2);
			if (bmp != null) {
				iv2.setImageBitmap(bmp);
			}

			iv2.setScaleType(ScaleType.FIT_CENTER);
			iv2.initUI();
			views.add(iv2);
		} else {
			group.setVisibility(View.GONE);
		}
		if (!TextUtils.isEmpty(path1)) {
			pathList.add(path1);
			MyZoomImageView iv = new MyZoomImageView(this);
			iv.setLayoutParams(mParams);

			Bitmap bmp = loadBitmap(path1);
			if (bmp != null) {
				iv.setImageBitmap(bmp);
			}

			iv.setScaleType(ScaleType.FIT_CENTER);
			iv.initUI();
			views.add(iv);
		} else {
			group.setVisibility(View.GONE);
		}
		vpAdapter = new ViewPagerAdapter(views);
		vp.setAdapter(vpAdapter);
		vp.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				RadioButton radioButton = (RadioButton) group
						.getChildAt(position);
				radioButton.setChecked(true);
				currentItem = position;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		findViewById(R.id.rotation_title_id).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						rotation[currentItem] = rotation[currentItem] + 90;
						if (rotation[currentItem] < 360) {
							setRoateImageview(rotation[currentItem]);
						} else {
							rotation[currentItem] = 0;
							setRoateImageview(rotation[currentItem]);
						}
					}
				});
	}

	int[] rotation = new int[2];
	List<View> views;
	List<String> pathList = new ArrayList<String>();

	public void setRoateImageview(int progress) {

		MyZoomImageView imageView = (MyZoomImageView) views.get(currentItem);
		// imageView.setDrawingCacheEnabled(true);
		Bitmap bitmap = loadBitmap(pathList.get(currentItem));
		// 设置旋转角度
		Matrix matrix = new Matrix();
		matrix.setRotate(progress);
		// 重新绘制Bitmap
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		imageView.setImageBitmap(bitmap);
		// imageView.setDrawingCacheEnabled(false);
		imageView.setScaleType(ScaleType.FIT_CENTER);
		imageView.initUI();
	}

	OnCheckedChangeListener listener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkId) {
			switch (checkId) {
			case R.id.radio1:
				vp.setCurrentItem(0);
				currentItem = 0;
				break;
			case R.id.radio2:
				vp.setCurrentItem(1);
				currentItem = 1;
				break;

			default:
				break;
			}
		}
	};

	public void setBackgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		if (bgAlpha == 1) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// 不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// 此行代码主要是解决在华为手机上半透明效果无效的bug
		}
		lp.alpha = bgAlpha;

		RecogResultActivity.this.getWindow().setAttributes(lp);
	}

	String shotPathString;
	TextView tv_label_id;
	
}
