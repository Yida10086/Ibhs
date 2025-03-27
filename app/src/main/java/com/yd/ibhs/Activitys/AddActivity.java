package com.yd.ibhs.Activitys;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
                        Log.d("SelectedDate", selectedDate);

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
                TextView t = findViewById(R.id.texttille);
                String title = t.getText() != null ? t.getText().toString() : "";
                TextView c = findViewById(R.id.textcontent);
                String content = c.getText() != null ? c.getText().toString() : "";
                TextView d = findViewById(R.id.datetext);
                String date = d.getText() != null ? d.getText().toString() : "";

                dp.insertData(title,content,date);
                Log.d("Debug",title+content+date);
                finish();
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