package com.yd.ibhs.dp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.yd.ibhs.R;
import com.yd.ibhs.project.Item;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ItemAdapter extends BaseAdapter {
    private final Context context;
    private List<Item> itemList;
    private final items dbHelper; // 数据库帮助类实例
    private final RefreshCallback callback; // 刷新回调接口

    public interface RefreshCallback {
        void refreshData();
    }

    public ItemAdapter(Context context, List<Item> itemList, items dbHelper, RefreshCallback callback) {
        this.context = context;
        this.itemList = itemList;
        this.dbHelper = dbHelper;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return itemList == null ? 0 : itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList == null ? null : itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList == null ? -1 : itemList.get(position).getId();
    }

    public void updateList(List<Item> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvReminder = convertView.findViewById(R.id.tvReminder);
            holder.btnComplete = convertView.findViewById(R.id.btnComplete);
            holder.currentStageText = convertView.findViewById(R.id.currentStageText);
            holder.tvDaysUntilReview = convertView.findViewById(R.id.tvDaysUntilReview);
            
            // 初始化阶段指示器
            holder.stageIndicators = new ImageView[6]; // 更新为6个阶段
            holder.stageIndicators[0] = convertView.findViewById(R.id.stage1);
            holder.stageIndicators[1] = convertView.findViewById(R.id.stage2);
            holder.stageIndicators[2] = convertView.findViewById(R.id.stage3);
            holder.stageIndicators[3] = convertView.findViewById(R.id.stage4);
            holder.stageIndicators[4] = convertView.findViewById(R.id.stage5);
            holder.stageIndicators[5] = convertView.findViewById(R.id.stage6);
            
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Item item = itemList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvReminder.setText(formatReminderText(item));
        
        // 设置距离下次复习的天数显示
        setDaysUntilReview(holder.tvDaysUntilReview, item);
        
        // 设置当前阶段文本 - 从0开始显示，不再加1
        holder.currentStageText.setText(String.format(Locale.getDefault(), 
            "%d/6", item.getCurrentStage()));

        // 更新阶段指示器
        updateStageIndicators(holder.stageIndicators, item.getCurrentStage());

        // 根据阶段设置按钮文本
        if (item.isFinalStage()) {
            holder.btnComplete.setText("完成");
        } else {
            holder.btnComplete.setText("标记完成 (" + getNextStageLabel(item) + ")");
        }
        
        Log.d("ADAPTER_DEBUG", "Position: " + position
                + " Title: " + item.getTitle()
                + " Reminder: " + item.getNextReminderDate()
                + " Stage: " + item.getCurrentStage());
                
        // 按钮点击事件
        holder.btnComplete.setOnClickListener(v -> handleCompleteClick(item));

        return convertView;
    }

    /**
     * 更新阶段指示器状态
     * @param indicators 指示器ImageView数组
     * @param currentStage 当前阶段（0-6）
     */
    private void updateStageIndicators(ImageView[] indicators, int currentStage) {
        // 设置阶段指示器状态
        for (int i = 0; i < indicators.length; i++) {
            if (i < currentStage) {
                // 已完成阶段为绿色实心
                indicators[i].setBackgroundResource(R.drawable.circle_green_filled);
            } else if (i == currentStage) {
                // 当前阶段为黄色实心
                indicators[i].setBackgroundResource(R.drawable.circle_yellow_filled);
            } else {
                // 未完成阶段为红色空心
                indicators[i].setBackgroundResource(R.drawable.circle_red_outline);
            }
        }
    }

    private String formatReminderText(Item item) {
        String baseDate = item.getBaseDate();
        String nextDate = item.getNextReminderDate();
        
        // 增加详细日志记录
        Log.d("ItemAdapter", "格式化提醒文本 - ID:" + item.getId() + 
              ", 标题:" + item.getTitle() + 
              ", 基础日期:" + baseDate + 
              ", 下次提醒:" + nextDate + 
              ", 日期数组:" + formatDatesArray(item.getDates()));
              
        // 确保创建日期显示
        if (baseDate == null || baseDate.isEmpty()) {
            baseDate = "未设置";
        }
        
        if (nextDate == null || nextDate.isEmpty()) {
            nextDate = "未设置";
        }
        
        return String.format(Locale.getDefault(),
                "创建日期：%s\n下次提醒：%s",
                baseDate,
                nextDate
        );
    }

    /**
     * 安全地格式化日期数组为字符串
     */
    private String formatDatesArray(String[] dates) {
        if (dates == null) return "null";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dates.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(dates[i] == null ? "null" : dates[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String getNextStageLabel(Item item) {
        int nextStage = item.getCurrentStage() + 1;
        if (nextStage < items.ReminderStages.STAGE_DAYS.length) {
            return items.ReminderStages.STAGE_LABELS[nextStage];
        }
        return "完成";
    }

    private void handleCompleteClick(Item item) {
        Log.d("ItemAdapter", "开始处理完成点击 - 项目ID=" + item.getId() + ", 标题=" + item.getTitle() + ", 当前阶段=" + item.getCurrentStage());
        
        if (item.isFinalStage()) {
            // 完成项目
            dbHelper.completeItem(item.getId());
            Log.d("ItemAdapter", "项目已完成(最终阶段) - ID=" + item.getId());
        } else {
            // 更新项目到下一阶段
            dbHelper.updateItemStage(item.getId());
            Log.d("ItemAdapter", "项目更新至下一阶段 - ID=" + item.getId() + ", 新阶段=" + (item.getCurrentStage() + 1));
        }
        
        // 首先从当前列表中移除该项目
        int position = -1;
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getId() == item.getId()) {
                position = i;
                break;
            }
        }
        
        if (position != -1) {
            itemList.remove(position);
            notifyDataSetChanged();
            Log.d("ItemAdapter", "项目已从列表中移除 - ID=" + item.getId());
        }
        
        // 通知Activity刷新数据
        // 这将重新查询数据库并更新主页显示
        callback.refreshData();
        
        // 记录刷新后的项目数量
        Log.d("ItemAdapter", "刷新后列表中的项目数量: " + itemList.size());
    }

    /**
     * 设置距离下次复习的天数显示
     */
    private void setDaysUntilReview(TextView textView, Item item) {
        try {
            // 获取当前日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            // 获取下次复习日期
            String nextReviewDateStr = item.getNextReminderDate();
            if (nextReviewDateStr.equals("完成阶段") || nextReviewDateStr.equals("已完成")) {
                textView.setText("已完成");
                textView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                return;
            }
            
            Calendar nextReview = Calendar.getInstance();
            nextReview.setTime(sdf.parse(nextReviewDateStr));
            nextReview.set(Calendar.HOUR_OF_DAY, 0);
            nextReview.set(Calendar.MINUTE, 0);
            nextReview.set(Calendar.SECOND, 0);
            nextReview.set(Calendar.MILLISECOND, 0);
            
            // 计算日期差
            long diffMillis = nextReview.getTimeInMillis() - today.getTimeInMillis();
            int diffDays = (int)(diffMillis / (24 * 60 * 60 * 1000));
            
            // 设置显示
            if (diffDays < 0) {
                textView.setText("已过期");
                textView.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (diffDays == 0) {
                textView.setText("今天");
                textView.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            } else if (diffDays == 1) {
                textView.setText("明天");
                textView.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                textView.setText("还有" + diffDays + "天");
                textView.setTextColor(context.getResources().getColor(android.R.color.holo_purple));
            }
        } catch (Exception e) {
            Log.e("ItemAdapter", "计算复习天数出错: " + e.getMessage());
            textView.setText("未知");
        }
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvReminder;
        TextView currentStageText;
        TextView tvDaysUntilReview;
        Button btnComplete;
        ImageView[] stageIndicators; // 阶段指示器数组
    }
}
