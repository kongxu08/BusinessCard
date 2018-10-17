package checkauto.camera.com.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.Time;

public class Utils {
	public static final String CPU_ARCHITECTURE_TYPE_32 = "32";  
	public static final String CPU_ARCHITECTURE_TYPE_64 = "64";
	private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";
	 private static long lastClickTime;  
	private static boolean LOGENABLE = false;
	/**
	 * Converts YUV420 NV21 to ARGB8888
	 * 
	 * @param data
	 *            byte array on YUV420 NV21 format.
	 * @param width
	 *            pixels width
	 * @param height
	 *            pixels height
	 * @return a ARGB8888 pixels int array. Where each int is a pixels ARGB.
	 */
	public static int[] convertYUV420_NV21toARGB8888(byte[] data, int width, int height) {
		int size = width * height;
		int offset = size;
		int[] pixels = new int[size];
		int u, v, y1, y2, y3, y4;

		// i along Y and the final pixels
		// k along pixels U and V
		for (int i = 0, k = 0; i < size; i += 2, k += 2) {
			y1 = data[i] & 0xff;
			y2 = data[i + 1] & 0xff;
			y3 = data[width + i] & 0xff;
			y4 = data[width + i + 1] & 0xff;

			u = data[offset + k] & 0xff;
			v = data[offset + k + 1] & 0xff;
			u = u - 128;
			v = v - 128;

			pixels[i] = convertYUVtoARGB(y1, u, v);
			pixels[i + 1] = convertYUVtoARGB(y2, u, v);
			pixels[width + i] = convertYUVtoARGB(y3, u, v);
			pixels[width + i + 1] = convertYUVtoARGB(y4, u, v);

			if (i != 0 && (i + 2) % width == 0)
				i += width;
		}

		return pixels;
	}

	private  static int convertYUVtoARGB(int y, int u, int v) {
		int r, g, b;

		r = y + (int) 1.402f * u;
		g = y - (int) (0.344f * v + 0.714f * u);
		b = y + (int) 1.772f * v;
		r = r > 255 ? 255 : r < 0 ? 0 : r;
		g = g > 255 ? 255 : g < 0 ? 0 : g;
		b = b > 255 ? 255 : b < 0 ? 0 : b;
		return 0xff000000 | (r << 16) | (g << 8) | b;
	}

	/**
	 * bitmap 缩放
	 */
	public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidth = ((float) width / w);
		float scaleHeight = ((float) height / h);
		matrix.reset();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
		return newbmp;
	}
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	/**
	 * 
	 * @Title: pictureName
	 * @Description: 将文件命名
	 * @param @return 设定文件
	 * @return String 文件以时间命的名字
	 * @throws
	 */
	public static String pictureName() {
		String str = "";
		Time t = new Time();
		t.setToNow(); // 取得系统时间。
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		if (month < 10)
			str = String.valueOf(year) + "0" + String.valueOf(month);
		else {
			str = String.valueOf(year) + String.valueOf(month);
		}
		if (date < 10)
			str = str + "0" + String.valueOf(date);
		else {
			str = str + String.valueOf(date);
		}
		if (hour < 10)
			str = str + "0" + String.valueOf(hour);
		else {
			str = str + String.valueOf(hour);
		}
		if (minute < 10)
			str = str + "0" + String.valueOf(minute);
		else {
			str = str + String.valueOf(minute);
		}
		if (second < 10)
			str = str + "0" + String.valueOf(second);
		else {
			str = str + String.valueOf(second);
		}
		return str;
	}
	public static ArrayList<Size> splitSize(String str, Camera camera) {
		if (str == null)
			return null;
		StringTokenizer tokenizer = new StringTokenizer(str, ",");
		ArrayList<Size> sizeList = new ArrayList<Size>();
		while (tokenizer.hasMoreElements()) {
			Size size = strToSize(tokenizer.nextToken(), camera);
			if (size != null)
				sizeList.add(size);
		}
		if (sizeList.size() == 0)
			return null;
		return sizeList;
	}

	public static Size strToSize(String str, Camera camera) {
		if (str == null)
			return null;
		int pos = str.indexOf('x');
		if (pos != -1) {
			String width = str.substring(0, pos);
			String height = str.substring(pos + 1);
			return camera.new Size(Integer.parseInt(width),
					Integer.parseInt(height));
		}
		return null;
	}
	/**
	 * 
	 * @Title: getBitmapIntArray
	 * @Description: 根据bitmap值转换成int[]数组
	 * @param @param bitmap
	 * @param @return 设定文件
	 * @return int[] 返回类型
	 * @throws
	 */
	public static int[] getBitmapIntArray(Bitmap bitmap) {
		int mWidth = bitmap.getWidth();
		int mHeight = bitmap.getHeight();
		int[] mIntArray = new int[mWidth * mHeight];
		bitmap.getPixels(mIntArray, 0, mWidth, 0, 0, mWidth, mHeight);
		return mIntArray;
	}

	/**
	 * 
	 * @Title: freeFileLock
	 * @Description: 释放文件锁
	 * @param @param fl
	 * @param @param file 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public static void freeFileLock(FileLock fl, File file) {
		if (file != null)
			file.delete();

		if (fl == null || !fl.isValid())
			return;

		try {
			fl.release();

		} catch (IOException e) {
		}
	}
	
	//版本比较：是否是4.4及以上版本  	
	  public final static boolean mIsKitKat = Build.VERSION.SDK_INT >= 19; 
	    /**  
	     * <br>功能简述:4.4及以上获取图片的方法 
	     * <br>功能详细描述: 
	     * <br>注意: 
	     * @param context 
	     * @param uri 
	     * @return 
	     */  
	    @TargetApi(19)  
	    public static String getPath(final Context context, final Uri uri) {  
	      
	        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;    
	        // DocumentProvider  
	        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {  
	            // ExternalStorageProvider  
	            if (isExternalStorageDocument(uri)) {  
	                final String docId = DocumentsContract.getDocumentId(uri);  
	                final String[] split = docId.split(":");  
	                final String type = split[0];  
	      
	                if ("primary".equalsIgnoreCase(type)) {  
	                    return Environment.getExternalStorageDirectory() + "/" + split[1];  
	                }  
	            }  
	            // DownloadsProvider  
	            else if (isDownloadsDocument(uri)) {  
	      
	                final String id = DocumentsContract.getDocumentId(uri);  
	                final Uri contentUri = ContentUris.withAppendedId(  
	                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
	      
	                return getDataColumn(context, contentUri, null, null);  
	            }  
	            // MediaProvider  
	            else if (isMediaDocument(uri)) {  
	                final String docId = DocumentsContract.getDocumentId(uri);  
	                final String[] split = docId.split(":");  
	                final String type = split[0];  
	      
	                Uri contentUri = null;  
	                if ("image".equals(type)) {  
	                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
	                } else if ("video".equals(type)) {  
	                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	                } else if ("audio".equals(type)) {  
	                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;  
	                }  
	      
	                final String selection = "_id=?";  
	                final String[] selectionArgs = new String[] { split[1] };  
	      
	                return getDataColumn(context, contentUri, selection, selectionArgs);  
	            }  
	        }  
	        // MediaStore (and general)  
	        else if ("content".equalsIgnoreCase(uri.getScheme())) {  
	      
	            // Return the remote address  
	            if (isGooglePhotosUri(uri))  
	                return uri.getLastPathSegment();  
	      
	            return getDataColumn(context, uri, null, null);  
	        }  
	        // File  
	        else if ("file".equalsIgnoreCase(uri.getScheme())) {  
	            return uri.getPath();  
	        }  
	      
	        return null;  
	    }  
	    public static String getDataColumn(Context context, Uri uri, String selection,  
	            String[] selectionArgs) {  
	      
	        Cursor cursor = null;  
	        final String column = "_data";  
	        final String[] projection = { column };  
	      
	        try {  
	            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,  
	                    null);  
	            if (cursor != null && cursor.moveToFirst()) {  
	                final int index = cursor.getColumnIndexOrThrow(column);  
	                return cursor.getString(index);  
	            }  
	        } finally {  
	            if (cursor != null)  
	                cursor.close();  
	        }  
	        return null;  
	    }  
	      
	    /** 
	     * @param uri The Uri to check. 
	     * @return Whether the Uri authority is ExternalStorageProvider. 
	     */  
	    public static boolean isExternalStorageDocument(Uri uri) {  
	        return "com.android.externalstorage.documents".equals(uri.getAuthority());  
	    }  
	      
	    /** 
	     * @param uri The Uri to check. 
	     * @return Whether the Uri authority is DownloadsProvider. 
	     */  
	    public static boolean isDownloadsDocument(Uri uri) {  
	        return "com.android.providers.downloads.documents".equals(uri.getAuthority());  
	    }  
	      
	    /** 
	     * @param uri The Uri to check. 
	     * @return Whether the Uri authority is MediaProvider. 
	     */  
	    public static boolean isMediaDocument(Uri uri) {  
	        return "com.android.providers.media.documents".equals(uri.getAuthority());  
	    }  
	      
	    /** 
	     * @param uri The Uri to check. 
	     * @return Whether the Uri authority is Google Photos. 
	     */  
	    public static boolean isGooglePhotosUri(Uri uri) {  
	        return "com.google.android.apps.photos.content".equals(uri.getAuthority());  
	    }
	    public static String getPathBefore(Context context, Uri uri) {
	        if ("content".equalsIgnoreCase(uri.getScheme())) {
	            String[] projection = { "_data" };
	            Cursor cursor = null;
	            try {
	                cursor = context.getContentResolver().query(uri, projection,
	                        null, null, null);
	                int column_index = cursor.getColumnIndexOrThrow("_data");
	                if (cursor.moveToFirst()) {
	                    return cursor.getString(column_index);
	                }
	            } catch (Exception e) {
	            }
	        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
	            return uri.getPath();
	        }
	        return null;
	    }
	    /*
	     * 将数据流转换成字节数组
	     */
	    public static byte[] Stream2Byte(String infile) {
	        BufferedInputStream in = null;
	        ByteArrayOutputStream out = null;
	        try {
	            in = new BufferedInputStream(new FileInputStream(infile));
	            out = new ByteArrayOutputStream(1024);
	            byte[] temp = new byte[1024];
	            int size = 0;
	            while ((size = in.read(temp)) != -1) {
	                out.write(temp, 0, size);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                in.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }

	        byte[] content = out.toByteArray();
	        return content;
	    }
	    public void savePic(String path,byte[] list_bytes)
	    {  Bitmap bitmap;
	    	
			 BitmapFactory.Options opts = new BitmapFactory.Options();

	         bitmap = BitmapFactory.decodeByteArray(list_bytes, 0, list_bytes.length, opts);
	         BufferedOutputStream bos;
			try {
				bos = new BufferedOutputStream(
				         new FileOutputStream(path));
				 /* 采用压缩转档方法 */
		         bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		         /* 调用flush()方法，更新BufferStream */
		         bos.flush();
		         /* 结束OutputStream */
		         bos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(bitmap!=null)
			{
				if(!bitmap.isRecycled())
				{
					bitmap.recycle();
					bitmap=null;
				}
			}
	    }
	    /** 
	     * Read the first line of "/proc/cpuinfo" file, and check if it is 64 bit. 
	     */  
	    public static boolean isCPUInfo64() {  
	        File cpuInfo = new File(PROC_CPU_INFO_PATH);  
	        if (cpuInfo != null && cpuInfo.exists()) {  
	            InputStream inputStream = null;  
	            BufferedReader bufferedReader = null;  
	            try {  
	                inputStream = new FileInputStream(cpuInfo);  
	                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);  
	                String line = bufferedReader.readLine();  
	                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64")) {  
	                     
	                    return true;  
	                } 
	            } catch (Throwable t) {  
	                
	            } finally {  
	                try {  
	                    if (bufferedReader != null) {  
	                        bufferedReader.close();  
	                    }  
	                } catch (Exception e) {  
	                    e.printStackTrace();  
	                }  
	                  
	                try {  
	                    if (inputStream != null) {  
	                        inputStream.close();  
	                    }  
	                } catch (Exception e) {  
	                    e.printStackTrace();  
	                }  
	            }  
	        }  
	        return false;  
	    }

	/**
	 * NV.21数据源旋转90度
	 * @param data
	 * @param imageWidth
	 * @param imageHeight
     * @return
     */

	public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight){
		byte[] yuv =new byte[imageWidth*imageHeight*3/2];
// Rotate the Y luma
		int i =0;
		for(int x =0;x < imageWidth;x++){
			for(int y = imageHeight-1;y >=0;y--){
				yuv[i]= data[y*imageWidth+x];
				i++;}

		}
// Rotate the U and V color components
		i = imageWidth*imageHeight*3/2-1;for(int x = imageWidth-1;x >0;x=x-2){for(int y =0;y < imageHeight/2;y++){
			yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+x];
			i--;
			yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
			i--;}}return yuv;}
	/**
	 * NV21预览数据旋转180度
	 * @param data
	 * @param imageWidth
	 * @param imageHeight
     * @return
     */
	public static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
		byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
		int i = 0;
		int count = 0;
		for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
			yuv[count] = data[i];
			count++;
		}
		i = imageWidth * imageHeight * 3 / 2 - 1;
		for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
				* imageHeight; i -= 2) {
			yuv[count++] = data[i - 1];
			yuv[count++] = data[i];
		}
		return yuv;
	}

	/**
	 * NV.21旋转270
	 * @param data
	 * @param imageWidth
	 * @param imageHeight
     * @return
     */
	public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth, int imageHeight){
		byte[] yuv =new byte[imageWidth*imageHeight*3/2];
		// Rotate the Y luma
		int i =0;
		for(int x = imageWidth-1;x >=0;x--){
			for(int y =0;y < imageHeight;y++){
				yuv[i]= data[y*imageWidth+x];
				i++;
			}
		}// Rotate the U and V color components
		i = imageWidth*imageHeight;
		for(int x = imageWidth-1;x >0;x=x-2){
			for(int y =0;y < imageHeight/2;y++){
				yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
				i++;
				yuv[i]= data[(imageWidth*imageHeight)+(y*imageWidth)+x];
				i++;
			}
		}
		return yuv;
	}

	private static int R = 0;
	private static int G = 1;
	private static int B = 2;

	/**
	 * YUV格式的数据转换为RGB格式
	 * @param src
	 * @param width
	 * @param height
     * @return
     */
	public static int[] YV12ToRGB(byte[] src, int width, int height){
		int numOfPixel = width * height;
		int positionOfV = numOfPixel;
		int positionOfU = numOfPixel/4 + numOfPixel;
		int[] rgb = new int[numOfPixel*3];

		for(int i=0; i<height; i++){
			int startY = i*width;
			int step = (i/2)*(width/2);
			int startV = positionOfV + step;
			int startU = positionOfU + step;
			for(int j = 0; j < width; j++){
				int Y = startY + j;
				int V = startV + j/2;
				int U = startU + j/2;
				int index = Y*3;
				RGB tmp = yuvTorgb(src[Y], src[U], src[V]);
				rgb[index+R] = tmp.r;
				rgb[index+G] = tmp.g;
				rgb[index+B] = tmp.b;
			}
		}
		return rgb;
	}
	private static class RGB{
		public int r, g, b;
	}

	private static RGB yuvTorgb(byte Y, byte U, byte V){
		RGB rgb = new RGB();
		rgb.r = (int)((Y&0xff) + 1.4075 * ((V&0xff)-128));
		rgb.g = (int)((Y&0xff) - 0.3455 * ((U&0xff)-128) - 0.7169*((V&0xff)-128));
		rgb.b = (int)((Y&0xff) + 1.779 * ((U&0xff)-128));
		rgb.r =(rgb.r<0? 0: rgb.r>255? 255 : rgb.r);
		rgb.g =(rgb.g<0? 0: rgb.g>255? 255 : rgb.g);
		rgb.b =(rgb.b<0? 0: rgb.b>255? 255 : rgb.b);
		return rgb;
	}

}
