package xyz.wystudio.yincai.feature.resource;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.GenerationApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 通过任务状态接口轮询生成进度，同时展示步骤和已产出资源。 */
public final class GenerationProgressActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout content;
    private TextView state;
    private Button cancel;
    private Button retry;
    private String taskId;
    private GenerationApi api;
    private boolean terminal;

    private final Runnable poll = new Runnable() {
        @Override public void run() {
            if (!isFinishing() && !terminal) { refresh(); handler.postDelayed(this, 3000); }
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskId = getIntent().getStringExtra("taskId");
        if (taskId == null || taskId.trim().isEmpty()) { finish(); return; }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("资源生成进度"));
        state = body("正在读取任务...");
        root.addView(state);
        LinearLayout actions = new LinearLayout(this);
        cancel = action("取消任务");
        retry = action("重试任务");
        retry.setVisibility(View.GONE);
        actions.addView(cancel, new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(retry, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(actions);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LineUi.styleScreen(root);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(GenerationApi.class);
        cancel.setOnClickListener(v -> changeTask(false));
        retry.setOnClickListener(v -> changeTask(true));
        refresh();
    }

    private void refresh() {
        NetworkCall.enqueue(api.detail(taskId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> task) {
                if (task == null) { state.setText("任务不存在"); terminal = true; return; }
                String status = MapValues.text(task, "status", "UNKNOWN");
                String progress = MapValues.text(task, "progress", "0");
                String error = MapValues.text(task, "errorMessage", "");
                state.setText("状态：" + status + "\n进度：" + progress + "%" + (error.isEmpty() ? "" : "\n说明：" + error));
                terminal = "COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status);
                cancel.setEnabled(!terminal);
                retry.setVisibility("FAILED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) ? View.VISIBLE : View.GONE);
                loadStepsAndOutputs();
            }
            @Override public void failure(String message) { state.setText("任务状态读取失败：" + message); }
        });
    }

    private void loadStepsAndOutputs() {
        NetworkCall.enqueue(api.steps(taskId), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) {
                content.removeAllViews();
                addHeading("生成步骤");
                if (data == null || data.isEmpty()) addText("后端尚未返回步骤，任务仍可能正在排队。", LineTheme.TEXT_SECONDARY);
                else for (Map<String, Object> row : data) addText(MapValues.text(row, "agentCode", "生成器") + " · " + MapValues.text(row, "stepType", "处理") + "\n状态：" + MapValues.text(row, "status", "等待中") + "\n" + MapValues.text(row, "inputSummary", ""), LineTheme.TEXT);
                NetworkCall.enqueue(api.outputs(taskId), new NetworkCall.Result<List<Map<String, Object>>>() {
                    @Override public void success(List<Map<String, Object>> outputs) {
                        addHeading("已生成资源");
                        if (outputs == null || outputs.isEmpty()) addText("暂时还没有资源输出。", LineTheme.TEXT_SECONDARY);
                        else for (Map<String, Object> row : outputs) addOutput(row);
                    }
                    @Override public void failure(String message) { addText("输出读取失败：" + message, LineTheme.DANGER); }
                });
            }
            @Override public void failure(String message) { content.removeAllViews(); addText("步骤读取失败：" + message, LineTheme.DANGER); }
        });
    }

    private void changeTask(boolean shouldRetry) {
        cancel.setEnabled(false); retry.setEnabled(false); state.setText(shouldRetry ? "正在重试任务..." : "正在取消任务...");
        NetworkCall.Result<Map<String, Object>> result = new NetworkCall.Result<>() {
            @Override public void success(Map<String, Object> data) { terminal = false; retry.setVisibility(View.GONE); cancel.setEnabled(true); retry.setEnabled(true); refresh(); }
            @Override public void failure(String message) { state.setText("操作失败：" + message); cancel.setEnabled(true); retry.setEnabled(true); }
        };
        if (shouldRetry) NetworkCall.enqueue(api.retry(taskId), result); else NetworkCall.enqueue(api.cancel(taskId), result);
    }

    private void addOutput(Map<String, Object> row) {
        TextView item = new TextView(this);
        item.setText(MapValues.text(row, "title", "学习资源") + "\n" + MapValues.text(row, "summary", "生成完成，点击查看详情"));
        item.setTextColor(LineTheme.TEXT); item.setPadding(LineTheme.dp(this, 14), LineTheme.dp(this, 14), LineTheme.dp(this, 14), LineTheme.dp(this, 14)); item.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row);
        item.setOnClickListener(v -> startActivity(new android.content.Intent(this, ResourceDetailActivity.class).putExtra("resourceId", MapValues.text(row, "id", ""))));
        content.addView(item, marginParams());
    }
    private void addHeading(String text) { TextView heading = body(text); heading.setTextSize(LineTheme.FONT_LG); heading.setTextColor(LineTheme.TEXT); heading.setTypeface(null, android.graphics.Typeface.BOLD); content.addView(heading); }
    private void addText(String text, int color) { TextView view = body(text); view.setTextColor(color); view.setPadding(LineTheme.dp(this, 14), LineTheme.dp(this, 12), LineTheme.dp(this, 14), LineTheme.dp(this, 12)); view.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row); content.addView(view, marginParams()); }
    private LinearLayout.LayoutParams marginParams() { LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 0, 0, 10); return lp; }
    private TextView title(String value) { TextView view = body(value); view.setTextSize(LineTheme.FONT_XL); view.setTextColor(LineTheme.TEXT); view.setTypeface(null, android.graphics.Typeface.BOLD); return view; }
    private TextView body(String value) { TextView view = new TextView(this); view.setText(value); view.setTextSize(LineTheme.FONT_SM); view.setTextColor(LineTheme.TEXT_SECONDARY); view.setPadding(0, LineTheme.dp(this, 8), 0, LineTheme.dp(this, 8)); return view; }
    private Button action(String value) { Button button = new Button(this); button.setText(value); button.setAllCaps(false); return button; }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }
}
