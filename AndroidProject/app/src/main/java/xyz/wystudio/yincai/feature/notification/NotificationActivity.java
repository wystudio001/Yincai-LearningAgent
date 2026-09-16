package xyz.wystudio.yincai.feature.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.NotificationApi;
import xyz.wystudio.yincai.ui.theme.LineUi;
import xyz.wystudio.yincai.ui.theme.LineTheme;

public final class NotificationActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView state;
    private NotificationApi api;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("通知中心");
        title.setTextSize(24);
        title.setTextColor(LineTheme.TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        state = new TextView(this);
        state.setTextColor(LineTheme.TEXT_SECONDARY);
        toolbar.addView(state, new LinearLayout.LayoutParams(0, -2, 1));
        Button readAll = new Button(this);
        readAll.setText("全部已读");
        readAll.setAllCaps(false);
        toolbar.addView(readAll);
        root.addView(toolbar);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LineUi.styleScreen(root);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(NotificationApi.class);
        readAll.setOnClickListener(v -> {
            readAll.setEnabled(false);
            NetworkCall.enqueue(api.markAllRead(), new NetworkCall.Result<Void>() {
                @Override public void success(Void ignored) { state.setText("所有通知已读"); load(); }
                @Override public void failure(String message) { readAll.setEnabled(true); state.setText("操作失败：" + message); }
            });
        });
        load();
    }

    private void load() {
        state.setText("正在加载通知...");
        NetworkCall.enqueue(api.list(1, 30), new NetworkCall.Result<CourseApi.PageData<Map<String, Object>>>() {
            @Override public void success(CourseApi.PageData<Map<String, Object>> data) {
                list.removeAllViews();
                List<Map<String, Object>> rows = data == null ? List.of() : data.getRecords();
                if (rows == null || rows.isEmpty()) {
                    state.setText("暂无通知");
                    return;
                }
                state.setText("共 " + rows.size() + " 条通知");
                for (Map<String, Object> row : rows) addNotification(row);
            }
            @Override public void failure(String message) { state.setText("通知加载失败：" + message); }
        });
    }

    private void addNotification(Map<String, Object> row) {
        TextView item = new TextView(this);
        String status = MapValues.text(row, "status", "UNREAD");
        item.setText(MapValues.text(row, "title", "学习提醒") + "\n"
                + MapValues.text(row, "content", "") + "\n"
                + ("UNREAD".equalsIgnoreCase(status) ? "未读" : "已读"));
        item.setTextColor(LineTheme.TEXT);
        item.setTextSize(LineTheme.FONT_MD);
        item.setPadding(LineTheme.dp(this, 16), LineTheme.dp(this, 16), LineTheme.dp(this, 16), LineTheme.dp(this, 16));
        item.setBackgroundResource(R.drawable.bg_line_row);
        item.setOnClickListener(v -> {
            String id = MapValues.text(row, "id", null);
            if (id == null || !"UNREAD".equalsIgnoreCase(status)) return;
            item.setEnabled(false);
            NetworkCall.enqueue(api.markRead(id), new NetworkCall.Result<Void>() {
                @Override public void success(Void ignored) { item.setText(item.getText().toString().replace("未读", "已读")); item.setBackgroundResource(R.drawable.bg_line_row); }
                @Override public void failure(String message) { item.setEnabled(true); state.setText("标记已读失败：" + message); }
            });
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, 10);
        list.addView(item, lp);
    }
}

