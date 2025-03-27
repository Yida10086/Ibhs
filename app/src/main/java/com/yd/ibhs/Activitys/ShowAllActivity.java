package com.yd.ibhs.Activitys;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yd.ibhs.R;
import com.yd.ibhs.dp.ItemAdapter;
import com.yd.ibhs.dp.items;
import com.yd.ibhs.project.Item;

import java.util.ArrayList;
import java.util.List;

public class ShowAllActivity extends AppCompatActivity implements ItemAdapter.RefreshCallback {
    private static final String TAG = "ShowAllActivity";
    private items dbHelper;
    private ItemAdapter adapter;
    private List<Item> allItemList;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showall);

        try {
            Log.d(TAG, "开始初始化 ShowAllActivity");
            
            // 初始化数据库
            dbHelper = new items(this);
            Log.d(TAG, "数据库初始化完成");

            // 初始化列表视图
            listView = findViewById(R.id.showAllListView);
            
            // 尝试打开数据库并检查表是否存在
            try {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + items.TABLE_NAME + "'", null);
                boolean tableExists = cursor != null && cursor.moveToFirst();
                if (cursor != null) {
                    cursor.close();
                }
                
                if (!tableExists) {
                    Log.e(TAG, "数据表不存在！");
                    Toast.makeText(this, "数据库表结构不正确", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "数据表存在，继续查询");
                }
            } catch (Exception e) {
                Log.e(TAG, "检查数据表时出错: " + e.getMessage());
            }
            
            // 查询所有数据（不限日期），采用安全模式
            try {
                allItemList = dbHelper.queryAllItemsNoDateFilter();
                Log.d(TAG, "查询数据成功，共有 " + allItemList.size() + " 条记录");
                
                if (allItemList.isEmpty()) {
                    Log.d(TAG, "数据列表为空，可能是数据库中没有记录");
                    Toast.makeText(this, "暂无记录", Toast.LENGTH_SHORT).show();
                } else {
                    // 显示第一条数据的信息用于调试
                    Item firstItem = allItemList.get(0);
                    Log.d(TAG, "第一条数据：ID=" + firstItem.getId() + ", 标题=" + firstItem.getTitle() + 
                          ", 阶段=" + firstItem.getCurrentStage() + ", 基础日期=" + firstItem.getBaseDate());
                }
            } catch (Exception e) {
                Log.e(TAG, "查询数据时出错: " + e.getMessage());
                e.printStackTrace(); // 打印详细堆栈信息
                allItemList = new ArrayList<>(); // 使用空列表防止崩溃
                Toast.makeText(this, "加载数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            
            Log.d(TAG, "开始初始化适配器");
            // 初始化适配器
            adapter = new ItemAdapter(
                    this,         // Context
                    allItemList,  // 数据列表
                    dbHelper,     // 数据库实例
                    this          // 刷新回调
            );
            
            listView.setAdapter(adapter);
            Log.d(TAG, "适配器设置完成");
            
            // 设置返回按钮
            Button backButton = findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // 返回上一个界面
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "初始化ShowAllActivity时发生错误: " + e.getMessage());
            e.printStackTrace(); // 打印详细堆栈信息
            Toast.makeText(this, "页面加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // 关闭当前页面，返回上一页
        }
    }

    @Override
    public void refreshData() {
        try {
            // 刷新数据
            allItemList = dbHelper.queryAllItemsNoDateFilter();
            adapter.updateList(allItemList);
        } catch (Exception e) {
            Log.e(TAG, "刷新数据时出错: " + e.getMessage());
            Toast.makeText(this, "刷新数据失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 