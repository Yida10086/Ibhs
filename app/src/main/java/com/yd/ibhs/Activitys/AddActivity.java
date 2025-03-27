package com.yd.ibhs.Activitys;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yd.ibhs.MainActivity;
import com.yd.ibhs.R;
import com.yd.ibhs.dp.items;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddActivity extends AppCompatActivity {
    private items dp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);
        // 初始化数据库操作对象
        dp = new items(this);
        
        // 设置默认日期为当前日期
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date(System.currentTimeMillis());
        String defaultDate = formatter.format(currentDate);
        TextView dateText = findViewById(R.id.datetext);
        dateText.setText(defaultDate);
        Log.d("AddActivity", "初始化默认日期: " + defaultDate);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Button buttonBack = findViewById(R.id.btback);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    finish();
                } catch (Exception e) {
                    Log.e("AddActivity", "返回时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    // 如果finish失败，尝试使用其他方式返回
                    Intent intent = new Intent(AddActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
        Button btdate = findViewById(R.id.select_date);
        btdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 弹出对话框
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this,
                        null,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

                // 确认按钮
                datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View dialogView) { // 将参数名改为 dialogView，避免冲突
                        // 获取选择的日期
                        int year = datePickerDialog.getDatePicker().getYear();
                        int month = datePickerDialog.getDatePicker().getMonth() + 1; // 月份从 0 开始，需要加 1
                        int day = datePickerDialog.getDatePicker().getDayOfMonth();
                        String selectedDate = year + "-" + month + "-" + day;
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-M-d");
                        try {
                            Date ndate = inputFormat.parse(selectedDate);
                            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                            selectedDate = outputFormat.format(ndate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        // 在这里处理选择的日期
                        TextView tv = findViewById(R.id.datetext);
                        tv.setText(selectedDate);
                        // 增加视觉反馈
                        tv.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                        Log.d("AddActivity", "用户选择的日期: " + selectedDate);

                        // 关闭对话框
                        datePickerDialog.dismiss();
                    }
                });
            }
        });
        Button sbumitBt = findViewById(R.id.sbumitBt);
        sbumitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText t = findViewById(R.id.texttille);
                String title = t.getText() != null ? t.getText().toString().trim() : "";
                
                // 检查标题是否为空
                if (title.isEmpty()) {
                    // 显示错误提示
                    t.setError("标题不能为空");
                    return; // 终止后续操作
                }
                
                EditText c = findViewById(R.id.textcontent);
                String content = c.getText() != null ? c.getText().toString().trim() : "";
                // 内容可以为空，所以不需要检查
                
                TextView d = findViewById(R.id.datetext);
                String date = d.getText() != null ? d.getText().toString().trim() : "";
                
                // 如果日期为空，则使用当前日期
                if (date.isEmpty()) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date currentDate = new Date(System.currentTimeMillis());
                    date = formatter.format(currentDate);
                    Log.d("AddActivity", "使用当前日期: " + date);
                }

                try {
                    // 插入数据
                    dp.insertData(title, content, date);
                    Log.d("AddActivity", "添加项目 - 标题:" + title + ", 内容:" + content + ", 日期:" + date);
                    
                    // 返回上一页
                    finish();
                } catch (Exception e) {
                    Log.e("AddActivity", "添加项目失败: " + e.getMessage(), e);
                    // 显示错误提示
                    android.widget.Toast.makeText(AddActivity.this, 
                        "添加失败，请检查日期格式", 
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dp != null) {
            dp.close();
        }
    }
}