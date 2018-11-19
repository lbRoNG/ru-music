package com.lbrong.rumusic.common.db;

/**
 * 数据库管理
 */
public class DBHelper {
    private static DBHelper dbHelper;

    private DBHelper() {}

    public static DBHelper build() {
        if (dbHelper == null) {
            synchronized (DBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new DBHelper();
                }
            }
        }
        return dbHelper;
    }

}
