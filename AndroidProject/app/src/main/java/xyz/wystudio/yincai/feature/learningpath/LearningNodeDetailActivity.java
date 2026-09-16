package xyz.wystudio.yincai.feature.learningpath;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.LearningPathApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

public final class LearningNodeDetailActivity extends AppCompatActivity {
    private TextView state;
    private Button complete;
    private Button skip;
    private String nodeId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nodeId = getIntent().getStringExtra("nodeId");
        if (nodeId == null || nodeId.trim().isEmpty() || "null".equalsIgnoreCase(nodeId)) {
            Toast.makeText(this, "学习节点信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText(getIntent().getStringExtra("target") == null ? "学习节点" : getIntent().getStringExtra("target"));
        title.setTextSize(22);
        title.setTextColor(LineTheme.TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        state = new TextView(this);
        state.setText("查看节点学习要求并更新完成状态");
        state.setTextColor(LineTheme.TEXT_SECONDARY);
        state.setPadding(0, 12, 0, 18);
        root.addView(state);
        complete = action("标记为已完成");
        skip = action("暂时跳过");
        root.addView(complete);
        root.addView(skip);
        LineUi.styleScreen(root);
        setContentView(root);

        String initialStatus = getIntent().getStringExtra("status");
        String completionRule = getIntent().getStringExtra("completionRule");
        state.setText("当前状态：" + (initialStatus == null ? "PENDING" : initialStatus)
                + "\n完成规则：" + (completionRule == null ? "完成本节点学习内容" : completionRule));
        if ("COMPLETED".equalsIgnoreCase(initialStatus) || "DONE".equalsIgnoreCase(initialStatus)) {
            complete.setEnabled(false);
            skip.setEnabled(false);
        }
        LearningPathApi api = ((YinCaiApplication) getApplication()).apiClient().create(LearningPathApi.class);
        complete.setOnClickListener(v -> update(api, true));
        skip.setOnClickListener(v -> update(api, false));
    }

    private Button action(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        return button;
    }

    private void update(LearningPathApi api, boolean completed) {
        complete.setEnabled(false);
        skip.setEnabled(false);
        state.setText(completed ? "正在标记完成..." : "正在跳过节点...");
        NetworkCall.Result<Void> result = new NetworkCall.Result<>() {
            @Override public void success(Void ignored) { state.setText(completed ? "节点已完成" : "节点已跳过"); }
            @Override public void failure(String message) { state.setText("更新失败：" + message); complete.setEnabled(true); skip.setEnabled(true); }
        };
        if (completed) {
            NetworkCall.enqueue(api.complete(nodeId), result);
        } else {
            NetworkCall.enqueue(api.skip(nodeId), result);
        }
    }
}

