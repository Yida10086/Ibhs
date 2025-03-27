package com.yd.ibhs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.GravityCompat;

import androidx.drawerlayout.widget.DrawerLayout;

import com.yd.ibhs.Activitys.AddActivity;
import com.yd.ibhs.Activitys.ShowAllActivity;
import com.yd.ibhs.dp.ItemAdapter;
import com.yd.ibhs.dp.items;
import com.yd.ibhs.project.Item;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
    implements ItemAdapter.RefreshCallback { // 实现回调接口

        private items dbHelper; // 数据库帮助类实例
        private ItemAdapter adapter;
        private List<Item> itemList;
        private ListView listView;
        private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化数据库
        dbHelper = new items(this);

        // 初始化列表
        itemList = dbHelper.queryAllItems();
        listView = findViewById(R.id.listView);
        emptyView = findViewById(R.id.emptyView);

        // 正确初始化适配器（传递四个参数）
        adapter = new ItemAdapter(
                this,       // Context
                itemList,   // 数据列表
                dbHelper,   // 数据库实例
                this        // 实现RefreshCallback接口的Activity
        );

        listView.setAdapter(adapter);
        
        // 检查列表是否为空，更新UI显示
        checkEmptyState();
    }

    private void refreshList() {
        List<Item> itemList = dbHelper.queryAllItems();
        adapter = new ItemAdapter(
                this,       // Context
                itemList,   // 数据列表
                dbHelper,   // 数据库实例
                this        // 实现RefreshCallback接口的Activity
        );
        listView.setAdapter(adapter);
        
        // 检查列表是否为空，更新UI显示
        checkEmptyState();
    }
    
    @Override
    public void refreshData() {
        Log.d("MainActivity", "开始刷新数据...");
        // 获取刷新前的项目数量
        int oldItemCount = itemList != null ? itemList.size() : 0;
        
        // 确保数据库连接可用
        if (dbHelper == null) {
            dbHelper = new items(this);
            Log.d("MainActivity", "数据库连接已重新初始化");
        }
        
        try {
            // 重新从数据库查询所有项目
            itemList = dbHelper.queryAllItems();
            
            // 更新适配器数据
            if (adapter != null) {
                adapter.updateList(itemList);
            } else {
                Log.w("MainActivity", "适配器为空，重新初始化");
                adapter = new ItemAdapter(
                    this,       // Context
                    itemList,   // 数据列表
                    dbHelper,   // 数据库实例
                    this        // 实现RefreshCallback接口的Activity
                );
                if (listView != null) {
                    listView.setAdapter(adapter);
                }
            }
            
            // 检查列表是否为空，更新UI显示
            checkEmptyState();
            
            // 记录刷新结果
            int newItemCount = itemList != null ? itemList.size() : 0;
            Log.d("MainActivity", "数据刷新完成 - 刷新前: " + oldItemCount + " 项, 刷新后: " + newItemCount + " 项");
        } catch (Exception e) {
            Log.e("MainActivity", "刷新数据时出错: " + e.getMessage(), e);
            
            // 尝试修复数据库连接
            try {
                if (dbHelper != null) {
                    dbHelper.close();
                }
                dbHelper = new items(this);
                
                // 再次尝试查询
                itemList = dbHelper.queryAllItems();
                if (adapter != null) {
                    adapter.updateList(itemList);
                }
                
                // 检查列表是否为空，更新UI显示
                checkEmptyState();
                
                Log.d("MainActivity", "恢复数据库连接并重新查询成功");
            } catch (Exception e2) {
                Log.e("MainActivity", "重试刷新数据仍然失败: " + e2.getMessage(), e2);
                // 显示错误提示
                Toast.makeText(this, "加载数据失败，请重启应用", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 检查列表是否为空，并更新UI状态
     */
    private void checkEmptyState() {
        if (itemList == null || itemList.isEmpty()) {
            // 列表为空，显示空视图提示
            Log.d("MainActivity", "列表为空，显示空视图提示");
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            // 列表有内容，显示列表视图
            Log.d("MainActivity", "列表有 " + itemList.size() + " 个项目，显示列表视图");
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_add, null);

        EditText etTitle = view.findViewById(R.id.texttille);
        EditText etContent = view.findViewById(R.id.textcontent);
        TextView tvDate = view.findViewById(R.id.datetext);

        // 日期选择逻辑
        builder.setView(view)
                .setPositiveButton("添加", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String content = etContent.getText().toString();
                    String date = tvDate.getText().toString();

                    dbHelper.insertData(title, content, date);
                    refreshList();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    @Override
    protected void onStart() {
        super.onStart();
        
        // 每次回到主页时重新查询数据库
        Log.d("MainActivity", "onStart: 重新查询数据库...");
        refreshData();
        
        Button buttonBack = findViewById(R.id.b1);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        Button openDrawerButton = findViewById(R.id.openDrawerButton);
        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // 关闭抽屉
        Button closeDrawerButton = findViewById(R.id.closeDrawerButton);
        closeDrawerButton.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        
        // 查看所有按钮点击事件
        Button showAllButton = findViewById(R.id.setmain);
        showAllButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, ShowAllActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);  // 关闭侧边栏
            } catch (Exception e) {
                Log.e("MainActivity", "跳转到ShowAllActivity失败: " + e.getMessage());
                Toast.makeText(MainActivity.this, "页面跳转失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // 每次恢复活动时重新查询数据库
        Log.d("MainActivity", "onResume: 重新查询数据库...");
        refreshData();
    }
    
    @Override
    protected void onRestart() {
        super.onRestart();
        
        // 每次从其他界面返回时重新查询数据库
        Log.d("MainActivity", "onRestart: 从其他界面返回，重新查询数据库...");
        refreshData();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MainActivity", "onStop: 主页被暂停");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 关闭数据库连接
        if (dbHelper != null) {
            dbHelper.close();
            Log.d("MainActivity", "onDestroy: 关闭数据库连接");
        }
    }

}