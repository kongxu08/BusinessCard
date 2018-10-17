package checkauto.camera.com.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesHelper {
    private static String name = "config";

    /**
     * 获取SharedPreferences实例对象
     * 
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 保存一个Boolean类型的值！
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putBoolean(Context context, String key, Boolean value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * 保存一个int类型的值！
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putInt(Context context, String key, int value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putInt(key, value);
        return editor.commit();
    }


    /**
     * 保存一个float类型的值！
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putFloat(Context context, String key, float value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * 保存一个long类型的值！
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putLong(Context context, String key, long value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 保存一个String类型的值！
     * 
     * @param context
     * @param key
     * @param value
     * @return
     */
    public static boolean putString(Context context, String key, String value) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.putString(key, value);
        return editor.commit();
    }

   

    /**
     * 获取String的value
     * 
     * @param context
     * @param key
     *            名字
     * @param defValue
     *            默认值
     * @return
     */
    public static String getString(Context context, String key, String defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getString(key, defValue);
    }

    /**
     * 获取int的value
     * 
     * @param context
     * @param key
     *            名字
     * @param defValue
     *            默认值
     * @return
     */
    public static int getInt(Context context, String key, int defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getInt(key, defValue);
    }

    /**
     * 获取float的value
     * 
     * @param context
     * @param key
     *            名字
     * @param defValue
     *            默认值
     * @return
     */
    public static float getFloat(Context context, String key, Float defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getFloat(key, defValue);
    }

    /**
     * 获取boolean的value
     * 
     * @param context
     * @param key
     *            名字
     * @param defValue
     *            默认值
     * @return
     */
    public static boolean getBoolean(Context context, String key,
            Boolean defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getBoolean(key, defValue);
    }

    /**
     * 获取long的value
     * 
     * @param context
     * @param key
     *            名字
     * @param defValue
     *            默认值
     * @return
     */
    public static long getLong(Context context, String key, long defValue) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        return sharedPreference.getLong(key, defValue);
    }

    /**
     * 删除Key对应的内容
     * 
     * @param context
     * @param key
     *            名字

     * @return
     */
    public static boolean remove(Context context, String key) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.remove(key);
        return editor.commit();
    }

    /**
     * 清除SharedPreferences中的所有值！
     * 
     * @param context
     * @return
     */
    public static boolean clear(Context context) {
        SharedPreferences sharedPreference = getSharedPreference(context);
        Editor editor = sharedPreference.edit();
        editor.clear();
        return editor.commit();
    }
}
