package com.yd.ibhs.project;

import com.yd.ibhs.dp.items;

public class Item {
    private final int id;
    private final String title;
    private final int currentStage;
    private final String[] dates;
    private boolean completed = false; // 是否已完成

    // 完整构造函数
    public Item(int id, String title, int currentStage, String[] dates) {
        this.id = id;
        this.title = title;
        this.currentStage = currentStage;
        this.dates = dates;
    }

    // 简化构造函数（用于测试）
    public Item(int id, String title, String baseDate) {
        this.id = id;
        this.title = title;
        this.currentStage = 0;
        this.dates = generateStageDates(baseDate);
    }

    // 生成阶段日期数组
    private String[] generateStageDates(String baseDate) {
        String[] stages = new String[items.ReminderStages.STAGE_DAYS.length];
        // 这里需要实现具体的日期计算逻辑
        // 示例伪代码：
         for (int i=0; i<stages.length; i++) {
             stages[i] = baseDate + items.ReminderStages.STAGE_DAYS[i] + "天";
         }
        return stages;
    }

    // 获取下一个提醒日期
    public String getNextReminderDate() {
        // 如果已完成，直接返回已完成状态
        if (completed) {
            return "已完成";
        }
        
        // 检查日期数组是否有效
        if (dates == null || dates.length <= currentStage + 1) {
            android.util.Log.e("Item", "无法获取下一个提醒日期，ID=" + id + 
                    ", 标题=" + title + 
                    ", 阶段=" + currentStage + 
                    ", 日期数组=" + (dates != null ? "长度" + dates.length : "null"));
            return "未设置";
        }
        
        int nextStage = currentStage + 1;
        String nextDate = dates[nextStage];
        
        // 检查下一个日期是否为空
        if (nextDate == null || nextDate.isEmpty()) {
            android.util.Log.e("Item", "下一个提醒日期为空，ID=" + id + 
                    ", 标题=" + title + 
                    ", 下一阶段=" + nextStage);
            return "未设置";
        }
        
        return nextDate;
    }

    // 判断是否最终阶段
    public boolean isFinalStage() {
        return currentStage >= 6; // 支持到第6阶段 (30天)
    }

    // 设置完成状态
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    // 判断是否已完成
    public boolean isCompleted() {
        return completed;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getCurrentStage() { return currentStage; }
    public String[] getDates() { return dates; }
    
    public String getBaseDate() { 
        if (dates == null || dates.length == 0) {
            android.util.Log.e("Item", "日期数组为空，ID=" + id + ", 标题=" + title);
            return "未设置";
        }
        
        // 检查第一个日期是否为空
        if (dates[0] == null || dates[0].isEmpty()) {
            android.util.Log.e("Item", "基础日期为空，ID=" + id + ", 标题=" + title);
            return "未设置";
        }
        
        android.util.Log.d("Item", "获取基础日期 - ID:" + id + 
                ", 标题:" + title + 
                ", 首个日期:" + dates[0]);
        
        return dates[0]; // 第一个日期为基准日期
    }
}
