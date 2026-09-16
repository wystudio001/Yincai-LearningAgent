package xyz.wystudio.yincai.feature.assessment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.AssessmentApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 学生端阶段性评估报告，报告生成由 Java 后端完成。 */
public final class AssessmentReportActivity extends AppCompatActivity {
    private TextView state;
    private LinearLayout content;
    private Button refresh;
    private Button generate;
    private AssessmentApi api;
    private String studentId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        YinCaiApplication app = (YinCaiApplication) getApplication();
        studentId = app.sessionManager().getUserId();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("评估报告"));
        state = body("正在读取最近一次评估...");
        root.addView(state);
        LinearLayout actions = new LinearLayout(this);
        refresh = action("刷新报告");
        generate = action("生成阶段报告");
        actions.addView(refresh, new LinearLayout.LayoutParams(0, -2, 1));
        actions.addView(generate, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(actions);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LineUi.styleScreen(root);
        setContentView(root);
        api = app.apiClient().create(AssessmentApi.class);
        refresh.setOnClickListener(v -> load());
        generate.setOnClickListener(v -> generateReport());
        load();
    }

    private void load() {
        refresh.setEnabled(false);
        state.setText("正在读取最近一次评估...");
        NetworkCall.enqueue(api.latest(), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                refresh.setEnabled(true);
                render(data);
            }
            @Override public void failure(String message) {
                refresh.setEnabled(true);
                state.setText("评估报告读取失败：" + message);
            }
        });
    }

    private void generateReport() {
        generate.setEnabled(false);
        state.setText("正在生成阶段性报告...");
        Map<String, Object> body = new HashMap<>();
        if (studentId != null && !studentId.trim().isEmpty()) body.put("studentId", studentId);
        String courseId = getIntent().getStringExtra("courseId");
        if (courseId != null && !courseId.trim().isEmpty()) body.put("courseId", courseId.trim());
        body.put("reportType", "STUDENT_SELF");
        NetworkCall.enqueue(api.generate(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                generate.setEnabled(true);
                render(data);
            }
            @Override public void failure(String message) {
                generate.setEnabled(true);
                state.setText("报告生成失败：" + message);
            }
        });
    }

    private void render(Map<String, Object> data) {
        content.removeAllViews();
        if (data == null || data.isEmpty()) {
            state.setText("还没有评估报告");
            addText("完成一些练习后，可以生成阶段性学习报告。", LineTheme.TEXT_SECONDARY);
            return;
        }
        state.setText("最近报告：" + MapValues.text(data, "generatedAt", "刚刚生成"));
        addSection("报告概览", MapValues.text(data, "summary", "暂无概览"));
        addSection("需要加强", MapValues.text(data, "weaknesses", "暂无明确薄弱项"));
        addSection("学习建议", MapValues.text(data, "suggestions", "建议继续完成练习并复盘错题"));
        addSection("分数明细", MapValues.text(data, "scoreDetail", "暂无分数明细"));
        addText("报告编号：" + MapValues.text(data, "id", "未标注") + "\n类型：" + MapValues.text(data, "reportType", "阶段评估"), LineTheme.TEXT_SECONDARY);
    }

    private void addSection(String heading, String value) {
        TextView section = title(heading);
        section.setTextSize(18);
        content.addView(section);
        addText(value, LineTheme.TEXT);
    }

    private void addText(String value, int color) {
        TextView view = body(value);
        view.setTextColor(color);
        view.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row);
        view.setPadding(14, 14, 14, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, 14);
        content.addView(view, lp);
    }

    private TextView title(String value) {
        TextView view = body(value);
        view.setTextSize(23);
        view.setTextColor(LineTheme.TEXT);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        return view;
    }

    private TextView body(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(14);
        view.setTextColor(LineTheme.TEXT_SECONDARY);
        view.setPadding(0, 8, 0, 8);
        return view;
    }

    private Button action(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setAllCaps(false);
        return button;
    }
}
