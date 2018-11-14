package com.lbrong.rumusic.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.lbrong.rumusic.application.AppContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author lbRoNG
 * @since 2018/1/15
 */

public final class SystemUtils {

    private SystemUtils(){}

    public static boolean addToCopy(String text){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) AppContext.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        if(ObjectHelper.requireNonNull(cm)){
            cm.setPrimaryClip(mClipData);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前本地apk的版本
     */
    public static int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = AppContext.getContext().getPackageManager().
                    getPackageInfo(AppContext.getContext().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取当前本地apk的版本
     */
    public static String getVersionName() {
        String versionName = "";
        try {
            versionName = AppContext.getContext().getPackageManager().
                    getPackageInfo(AppContext.getContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }

    /**
     * 检测某Activity是否在当前Task的栈顶
     */
    private boolean isTopActivity(String activityName){
        ActivityManager manager = (ActivityManager) AppContext.getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if(ObjectHelper.requireNonNull(manager)){
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            String cmpNameTemp = null;
            if(runningTaskInfos != null){
                cmpNameTemp = runningTaskInfos.get(0).topActivity.toString();
            }
            if(cmpNameTemp == null){
                return false;
            }
            return cmpNameTemp.equals(activityName);
        }
       return false;
    }

    /**
     * 获取联系人内容
     */
    public static String[] getPhoneContacts(Activity context, Uri uri) {
        String[] contact = new String[2];
        //得到ContentResolver对象
        ContentResolver cr = context.getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);

        if(cursor != null){
            cursor.moveToFirst();
            //取得联系人名字
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            contact[0] = cursor.getString(nameFieldColumnIndex);

            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);

            if(phone != null){
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phone.close();
            }
            cursor.close();
        }
        return contact;
    }

    /**
     * 判断MainActivity是否活动
     * @param context 一个context
     * @param activityName 要判断Activity
     * @return boolean
     */
    public static boolean isMainActivityAlive(Context context, String activityName){
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        for (ActivityManager.RunningTaskInfo info : list) {
            // 注意这里的 topActivity 包含 packageName和className，可以打印出来看看
            if (info.topActivity.getClassName().equals(activityName) ||
                    info.baseActivity.getClassName().equals(activityName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断应用是否已经启动
     * @param context 一个context
     * @param packageName 要判断应用的包名
     * @return boolean
     */
    public static boolean isAppAlive(Context context, String packageName){
        ActivityManager activityManager =
                (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos
                = activityManager.getRunningAppProcesses();
        for(int i = 0; i < processInfos.size(); i++){
            if(processInfos.get(i).processName.equals(packageName)){
                return true;
            }
        }
        return false;
    }
}
