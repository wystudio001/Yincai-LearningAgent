package xyz.wystudio.yincai.feature.course;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.LearningPathApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 选择课程并为当前学生生成学习路径。 */
public final class CourseSelectActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView state;
    private YinCaiApplication app;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (YinCaiApplication) getApplication();

        LinearLayout root = LineUi.page(this);
        root.addView(LineUi.toolbar(this, "选择课程", v -> finish()));
        root.addView(LineUi.divider(this));

        ScrollView scroll = LineUi.scrollPage(this);
        LinearLayout content = LineUi.content(this);
        state = LineUi.text(this, "正在加载课程...", LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY, android.graphics.Typeface.NORMAL);
        content.addView(state);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list, new LinearLayout.LayoutParams(-1, -2));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        load();
    }

    private void load() {
        NetworkCall.enqueue(
                app.apiClient().create(CourseApi.class).listCourses(1, 30, null, "PUBLISHED"),
                new NetworkCall.Result<CourseApi.PageData<Map<String, Object>>>() {
                    @Override public void success(CourseApi.PageData<Map<String, Object>> data) {
                        List<Map<String, Object>> rows = data == null ? List.of() : data.getRecords();
                        if (rows == null || rows.isEmpty()) {
                            state.setText("暂时没有可选课程");
                            return;
                        }
                        state.setText("选择一门课程生成学习路径");
                        for (Map<String, Object> row : rows) addCourse(row);
                    }
                    @Override public void failure(String message) { state.setText("课程加载失败：" + message); }
                });
    }

    private void addCourse(Map<String, Object> row) {
        Button button = new Button(this);
        button.setText(MapValues.text(row, "name", MapValues.text(row, "title", "未命名课程"))
                + "\n" + MapValues.text(row, "description", "进入课程后生成专属学习路径"));
        button.setTextColor(LineTheme.TEXT);
        button.setAllCaps(false);
        button.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
        button.setPadding(LineTheme.dp(this, 16), LineTheme.dp(this, 12), LineTheme.dp(this, 16), LineTheme.dp(this, 12));
        button.setBackgroundResource(R.drawable.bg_line_row);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, LineTheme.dp(this, 8));
        list.addView(button, params);
        button.setOnClickListener(v -> generate(row));
    }

    private void generate(Map<String, Object> row) {
        String id = MapValues.text(row, "id", null);
        if (id == null) { state.setText("课程信息不完整"); return; }
        state.setText("正在生成学习路径...");
        Map<String, Object> body = new HashMap<>();
        body.put("studentId", app.sessionManager().getUserId());
        body.put("courseId", id);
        NetworkCall.enqueue(app.apiClient().create(LearningPathApi.class).generate(body),
                new NetworkCall.Result<Map<String, Object>>() {
                    @Override public void success(Map<String, Object> ignored) {
                        state.setText("学习路径已准备好");
                        setResult(RESULT_OK);
                    }
                    @Override public void failure(String message) { state.setText("生成失败：" + message); }
                });
    }
}
