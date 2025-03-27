package com.yd.ibhs.dp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.ParseException;
import android.util.Log;

import com.yd.ibhs.project.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class items extends SQLiteOpenHelper {
    private static final String TAG = "items";
    private static final String DB_NAME = "items.db"; // 数据库的名称
    private static final int DB_VERSION = 7; // 数据库的版本号已更新
    private static items mHelper = null; // 数据库帮助器的实例
    private SQLiteDatabase mDB = null; // 数据库的实例
    public static final String TABLE_NAME = "items"; // 表的名称
    public items(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    private items(Context context, int version) {
        super(context, DB_NAME, null, version);
    }
    // 利用单例模式获取数据库帮助器的唯一实例
    public static items getInstance(Context context, int version) {
        if (version > 0 && mHelper == null) {
            mHelper = new items(context, version);
        } else if (mHelper == null) {
            mHelper = new items(context);
        }
        return mHelper;
    }
    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getReadableDatabase();
        }
        return mDB;
    }
    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mDB == null || !mDB.isOpen()) {
            mDB = mHelper.getWritableDatabase();
        }
        return mDB;
    }
    // 关闭数据库连接
    public void closeLink() {
        if (mDB != null && mDB.isOpen()) {
            mDB.close();
            mDB = null;
        }
    }
    // 创建数据库，执行建表语句
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");

        // 删除已存在的表
        String drop_sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
        Log.d(TAG, "drop_sql:" + drop_sql);
        db.execSQL(drop_sql);

        // 创建新表
        // 修改后的建表语句
        String create_sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "title VARCHAR NOT NULL,"
                + "content VARCHAR NOT NULL,"
                + "date VARCHAR NOT NULL," // 基础日期字段
                + "current_stage INTEGER DEFAULT 0 NOT NULL,"
                + "date_day_1 VARCHAR NOT NULL," // 基础日期+1天
                + "date_day_3 VARCHAR NOT NULL," // 基础日期+3天
                + "date_day_5 VARCHAR NOT NULL," // 基础日期+5天
                + "date_day_7 VARCHAR NOT NULL," // 基础日期+7天
                + "date_day_15 VARCHAR NOT NULL," // 基础日期+15天
                + "date_day_30 VARCHAR NOT NULL" // 基础日期+30天
                + ");";

        Log.d(TAG, "create_sql:" + create_sql);
        db.execSQL(create_sql); // 执行SQL语句
    }
    public void insertData(String title, String content, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 规范化日期格式，确保使用yyyy-MM-dd格式
        String normalizedDate = date;
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            SimpleDateFormat outputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            java.util.Date parsedDate = inputSdf.parse(date);
            if (parsedDate != null) {
                normalizedDate = outputSdf.format(parsedDate);
            }
            
            Log.d(TAG, "输入日期: " + date + ", 规范化后: " + normalizedDate);
        } catch (Exception e) {
            Log.e(TAG, "日期格式转换失败: " + e.getMessage());
        }

        // 计算日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(normalizedDate));
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        
        // 记录原始日期和计算用日期
        Log.d(TAG, "插入数据 - 标题: " + title + ", 内容: " + content + 
              ", 原始日期: " + date + ", 计算用日期: " + normalizedDate);

        // 计算每个阶段日期（直接基于基础日期计算）
        Calendar baseCalendar = Calendar.getInstance();
        try {
            baseCalendar.setTime(sdf.parse(normalizedDate));
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        
        // 保存基础日期副本，用于计算各阶段日期
        Calendar stageCalendar = (Calendar) baseCalendar.clone();
        
        // 计算 date_plus_1（基础日期+1天）
        stageCalendar.add(Calendar.DAY_OF_YEAR, 1);
        String date_plus_1 = sdf.format(stageCalendar.getTime());
        
        // 重置为基础日期，计算 date_plus_3（基础日期+3天）
        stageCalendar = (Calendar) baseCalendar.clone();
        stageCalendar.add(Calendar.DAY_OF_YEAR, 3);
        String date_plus_3 = sdf.format(stageCalendar.getTime());
        
        // 重置为基础日期，计算 date_plus_5（基础日期+5天）
        stageCalendar = (Calendar) baseCalendar.clone();
        stageCalendar.add(Calendar.DAY_OF_YEAR, 5);
        String date_plus_5 = sdf.format(stageCalendar.getTime());
        
        // 重置为基础日期，计算 date_plus_7（基础日期+7天）
        stageCalendar = (Calendar) baseCalendar.clone();
        stageCalendar.add(Calendar.DAY_OF_YEAR, 7);
        String date_plus_7 = sdf.format(stageCalendar.getTime());
        
        // 重置为基础日期，计算 date_plus_15（基础日期+15天）
        stageCalendar = (Calendar) baseCalendar.clone();
        stageCalendar.add(Calendar.DAY_OF_YEAR, 15);
        String date_plus_15 = sdf.format(stageCalendar.getTime());
        
        // 重置为基础日期，计算 date_plus_30（基础日期+30天）
        stageCalendar = (Calendar) baseCalendar.clone();
        stageCalendar.add(Calendar.DAY_OF_YEAR, 30);
        String date_plus_30 = sdf.format(stageCalendar.getTime());

        // 插入数据
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("date", normalizedDate); // 使用规范化后的日期
        values.put("current_stage", 0); // 确保设置初始阶段为0
        values.put("date_day_1", date_plus_1);
        values.put("date_day_3", date_plus_3);
        values.put("date_day_5", date_plus_5);
        values.put("date_day_7", date_plus_7);
        values.put("date_day_15", date_plus_15);
        values.put("date_day_30", date_plus_30);

        db.insert(TABLE_NAME, null, values);
        db.close(); // 关闭数据库连接
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "数据库版本从 " + oldVersion + " 升级到 " + newVersion);
        
        if (oldVersion < 7) {
            // 版本7引入了新的字段命名方案
            try {
                // 创建临时表
                db.execSQL("CREATE TABLE temp_items AS SELECT * FROM " + TABLE_NAME);
                
                // 删除旧表
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                
                // 创建新表
                String create_sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "title VARCHAR NOT NULL,"
                    + "content VARCHAR NOT NULL,"
                    + "date VARCHAR NOT NULL," 
                    + "current_stage INTEGER DEFAULT 0 NOT NULL,"
                    + "date_day_1 VARCHAR NOT NULL,"
                    + "date_day_3 VARCHAR NOT NULL,"
                    + "date_day_5 VARCHAR NOT NULL,"
                    + "date_day_7 VARCHAR NOT NULL,"
                    + "date_day_15 VARCHAR NOT NULL,"
                    + "date_day_30 VARCHAR NOT NULL"
                    + ");";
                db.execSQL(create_sql);
                
                // 迁移数据
                db.execSQL("INSERT INTO " + TABLE_NAME + " (_id, title, content, date, current_stage, "
                        + "date_day_1, date_day_3, date_day_5, date_day_7, date_day_15, date_day_30) "
                        + "SELECT _id, title, content, date, current_stage, "
                        + "date_plus_1, date_plus_3, date_plus_5, date_plus_7, date_plus_15, date_plus_30 "
                        + "FROM temp_items");
                
                // 删除临时表
                db.execSQL("DROP TABLE IF EXISTS temp_items");
                
                Log.d(TAG, "数据库迁移完成，新字段命名方案已应用");
            } catch (Exception e) {
                Log.e(TAG, "数据库迁移失败: " + e.getMessage());
                // 如果迁移失败，重新创建表
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
            }
        } else {
            // 其他版本升级处理
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 处理数据库版本降级情况
        Log.w(TAG, "数据库版本从 " + oldVersion + " 降级到 " + newVersion);
        // 删除表并重建以避免兼容性问题
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 在items类中添加以下方法
    public List<Item> queryAllItems() {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // 获取当前日期格式化为yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String currentDate = sdf.format(calendar.getTime());
        
        Log.d(TAG, "查询项目 - 当前日期: " + currentDate);

        // 读取所有项目的SQL
        String allItemsQuery = "SELECT * FROM " + TABLE_NAME;
        Cursor debugCursor = db.rawQuery(allItemsQuery, null);
        Log.d(TAG, "数据库中所有项目数量: " + debugCursor.getCount());
        
        // 输出数据库中的所有记录进行调试
        if (debugCursor.moveToFirst()) {
            do {
                String id = debugCursor.getString(debugCursor.getColumnIndex("_id"));
                String title = debugCursor.getString(debugCursor.getColumnIndex("title"));
                String dateValue = debugCursor.getString(debugCursor.getColumnIndex("date"));
                int stage = debugCursor.getInt(debugCursor.getColumnIndex("current_stage"));
                String dateDay1 = debugCursor.getString(debugCursor.getColumnIndex("date_day_1"));
                
                Log.d(TAG, "数据库记录 - ID: " + id + ", 标题: " + title + 
                      ", 日期: " + dateValue + ", 阶段: " + stage + 
                      ", 第1天日期: " + dateDay1 + ", 当前日期: " + currentDate);
            } while (debugCursor.moveToNext());
        }
        debugCursor.close();
        
        // 创建查询条件，明确排除当前日期等于创建日期的项目
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                      "(CASE current_stage " +
                      "WHEN 0 THEN date_day_1 " +  // 第0阶段：等待第1天复习日期
                      "WHEN 1 THEN date_day_3 " +  // 第1阶段：等待第3天复习日期
                      "WHEN 2 THEN date_day_5 " +  // 第2阶段：等待第5天复习日期
                      "WHEN 3 THEN date_day_7 " +  // 第3阶段：等待第7天复习日期
                      "WHEN 4 THEN date_day_15 " + // 第4阶段：等待第15天复习日期
                      "WHEN 5 THEN date_day_30 " + // 第5阶段：等待第30天复习日期
                      "WHEN 6 THEN date_day_30 " + // 第6阶段：最后一次复习
                      "END) = ? AND date != ?"; // 确保不显示创建日期是今天的项目

        String[] selectionArgs = new String[]{
            currentDate, currentDate
        };
        
        // 添加调试日志
        Log.d(TAG, "SQL 查询: " + query);
        Log.d(TAG, "查询参数: currentDate=" + currentDate);
        
        try (Cursor cursor = db.rawQuery(query, selectionArgs)) {
            // 安全获取字段索引
            int idIndex = getColumnIndexSafe(cursor, "_id");
            int titleIndex = getColumnIndexSafe(cursor, "title");
            int stageIndex = getColumnIndexSafe(cursor, "current_stage");
            int dateIndex = getColumnIndexSafe(cursor, "date");
            int date1Index = getColumnIndexSafe(cursor, "date_day_1");
            int date3Index = getColumnIndexSafe(cursor, "date_day_3");
            int date5Index = getColumnIndexSafe(cursor, "date_day_5");
            int date7Index = getColumnIndexSafe(cursor, "date_day_7");
            int date15Index = getColumnIndexSafe(cursor, "date_day_15");
            int date30Index = getColumnIndexSafe(cursor, "date_day_30");

            Log.d(TAG, "查询结果数: " + cursor.getCount());
            
            while (cursor.moveToNext()) {
                // 记录当前查询到的项目
                int itemId = cursor.getInt(idIndex);
                String itemTitle = cursor.getString(titleIndex);
                int itemStage = cursor.getInt(stageIndex);
                String itemDate = cursor.getString(dateIndex);
                String itemNextDate = "";
                
                // 根据阶段获取下一次复习日期
                switch (itemStage) {
                    case 0:
                        itemNextDate = cursor.getString(date1Index);
                        break;
                    case 1:
                        itemNextDate = cursor.getString(date3Index);
                        break;
                    case 2:
                        itemNextDate = cursor.getString(date5Index);
                        break;
                    case 3:
                        itemNextDate = cursor.getString(date7Index);
                        break;
                    case 4:
                        itemNextDate = cursor.getString(date15Index);
                        break;
                    case 5:
                    case 6:
                        itemNextDate = cursor.getString(date30Index);
                        break;
                }
                
                Log.d(TAG, "查询到项目: ID=" + itemId + ", 标题=" + itemTitle + 
                        ", 阶段=" + itemStage + ", 创建日期=" + itemDate + 
                        ", 下次复习日期=" + itemNextDate);
                
                // 构建日期数组（包含基础日期）
                String[] dates = new String[]{
                        cursor.getString(dateIndex),     // 基础日期
                        cursor.getString(date1Index),    // +1
                        cursor.getString(date3Index),    // +3
                        cursor.getString(date5Index),    // +5
                        cursor.getString(date7Index),    // +7
                        cursor.getString(date15Index),   // +15
                        cursor.getString(date30Index)    // +30
                };
                
                // 记录所有日期用于调试
                Log.d(TAG, "项目ID=" + itemId + " 的日期数组：" + 
                         "基础=" + dates[0] + ", " +
                         "1天=" + dates[1] + ", " +
                         "3天=" + dates[2] + ", " +
                         "5天=" + dates[3] + ", " +
                         "7天=" + dates[4] + ", " +
                         "15天=" + dates[5] + ", " +
                         "30天=" + dates[6]);

                itemList.add(new Item(
                        cursor.getInt(idIndex),
                        cursor.getString(titleIndex),
                        cursor.getInt(stageIndex),
                        dates
                ));
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Database schema mismatch: " + e.getMessage());
        }
        
        Log.d(TAG, "queryAllItems 完成，返回 " + itemList.size() + " 个项目");

        // 如果没有数据，添加调试信息
        if (itemList.isEmpty()) {
            Log.d(TAG, "今天没有复习任务！所有项目都已完成或未到复习时间");
        }
        
        return itemList;
    }

    /**
     * 安全获取列索引，避免字段不存在导致崩溃
     */
    private int getColumnIndexSafe(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index == -1) {
            throw new IllegalStateException("COLUMN '" + columnName + "' NOT FOUND");
        }
        return index;
    }


    public void deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
    }
    public void updateItemStage(int id) {
        SQLiteDatabase db = getWritableDatabase();
        
        try {
            // 首先获取当前项目的阶段
            Cursor cursor = db.rawQuery(
                "SELECT current_stage FROM " + TABLE_NAME + " WHERE _id = ?",
                new String[]{String.valueOf(id)});
                
            if (cursor != null && cursor.moveToFirst()) {
                int currentStage = cursor.getInt(0);
                cursor.close();
                
                // 计算新阶段
                int newStage = currentStage + 1;
                
                // 只有当新阶段在有效范围内时才更新
                if (newStage < ReminderStages.STAGE_DAYS.length) {
                    // 获取当前日期
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar today = Calendar.getInstance();
                    
                    // 计算下一个复习日期：当前日期 + 下一阶段的天数
                    int nextStageDays = ReminderStages.STAGE_DAYS[newStage];
                    Calendar nextReviewDate = (Calendar) today.clone();
                    nextReviewDate.add(Calendar.DAY_OF_YEAR, nextStageDays);
                    String nextReviewDateStr = sdf.format(nextReviewDate.getTime());
                    
                    // 根据新阶段更新相应的日期字段
                    ContentValues values = new ContentValues();
                    values.put("current_stage", newStage);
                    
                    // 更新对应的日期字段，确保项目不会在今天再次显示
                    switch (newStage) {
                        case 1:
                            values.put("date_day_1", nextReviewDateStr);
                            break;
                        case 2:
                            values.put("date_day_3", nextReviewDateStr);
                            break;
                        case 3:
                            values.put("date_day_5", nextReviewDateStr);
                            break;
                        case 4:
                            values.put("date_day_7", nextReviewDateStr);
                            break;
                        case 5:
                            values.put("date_day_15", nextReviewDateStr);
                            break;
                        case 6:
                            values.put("date_day_30", nextReviewDateStr);
                            break;
                    }
                    
                    // 更新数据库
                    db.update(TABLE_NAME, values, "_id = ?", new String[]{String.valueOf(id)});
                    
                    Log.d(TAG, "项目ID=" + id + " 已更新到阶段 " + newStage + 
                          "，下次复习日期: " + nextReviewDateStr);
                } else {
                    // 如果已经是最后阶段，直接完成项目
                    completeItem(id);
                    Log.d(TAG, "项目ID=" + id + " 已完成所有阶段，已从数据库删除");
                }
            } else {
                // 如果找不到项目，简单地增加阶段（旧行为）
                Log.w(TAG, "找不到项目ID=" + id + "，使用简单的阶段递增");
                db.execSQL("UPDATE " + TABLE_NAME +
                        " SET current_stage = current_stage + 1 WHERE _id = ?",
                        new Object[]{id});
            }
        } catch (Exception e) {
            Log.e(TAG, "更新项目阶段时出错: " + e.getMessage());
            // 出错时回退到简单的阶段递增
            db.execSQL("UPDATE " + TABLE_NAME +
                    " SET current_stage = current_stage + 1 WHERE _id = ?",
                    new Object[]{id});
        }
    }

    // 完成项目
    public void completeItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
    }

    // 新增方法：查询所有项目（不受日期限制）
    public List<Item> queryAllItemsNoDateFilter() {
        List<Item> itemList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_NAME;
        
        try (Cursor cursor = db.rawQuery(query, null)) {
            // 获取列索引
            int idIndex = cursor.getColumnIndex("_id");
            int titleIndex = cursor.getColumnIndex("title");
            int stageIndex = cursor.getColumnIndex("current_stage");
            int dateIndex = cursor.getColumnIndex("date");
            int date1Index = cursor.getColumnIndex("date_day_1");
            int date3Index = cursor.getColumnIndex("date_day_3");
            int date5Index = cursor.getColumnIndex("date_day_5");
            int date7Index = cursor.getColumnIndex("date_day_7");
            int date15Index = cursor.getColumnIndex("date_day_15");
            int date30Index = cursor.getColumnIndex("date_day_30");
            
            // 迭代结果集
            while (cursor.moveToNext()) {
                String[] dates = new String[7];
                
                // 安全地获取日期值
                dates[0] = cursor.getString(dateIndex);
                dates[1] = cursor.getString(date1Index); 
                dates[2] = cursor.getString(date3Index);
                dates[3] = cursor.getString(date5Index);
                dates[4] = cursor.getString(date7Index);
                dates[5] = cursor.getString(date15Index);
                dates[6] = cursor.getString(date30Index);
                
                // 创建并添加Item对象
                Item item = new Item(
                    cursor.getInt(idIndex),
                    cursor.getString(titleIndex),
                    cursor.getInt(stageIndex),
                    dates
                );
                itemList.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "查询所有项目时出错 (不过滤日期): " + e.getMessage());
            e.printStackTrace();
        }
        
        return itemList;
    }

    // 获取阶段配置
    public static class ReminderStages {
        public static final int[] STAGE_DAYS = {1, 3, 5, 7, 15, 30};
        public static final String[] STAGE_LABELS = {
                "阶段0(1天)", "阶段1(3天)", "阶段2(5天)", "阶段3(7天)", "阶段4(15天)", "阶段5(30天)"
        };
    }

    // 获取下一阶段日期
    public String getNextStageDate(int id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT current_stage, date_day_1, date_day_3, date_day_5, " +
                        "date_day_7, date_day_15, date_day_30 FROM " + TABLE_NAME + " WHERE _id=?",
                new String[]{String.valueOf(id)}
        )) {
            if (cursor.moveToFirst()) {
                int stage = cursor.getInt(0);
                if (stage < 6) { // 0-5对应1,3,5,7,15,30天
                    return cursor.getString(stage + 1);
                }
            }
        }
        return null;
    }


}