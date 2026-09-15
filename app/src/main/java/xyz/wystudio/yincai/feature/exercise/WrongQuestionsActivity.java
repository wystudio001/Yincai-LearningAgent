package xyz.wystudio.yincai.feature.exercise;

import android.os.Bundle;
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
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.ExerciseApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 学生错题本，支持从服务端加载并标记题目已复习。 */
public final class WrongQuestionsActivity extends AppCompatActivity {
    private LinearLayout list;
    private TextView state;
    private ExerciseApi api;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("错题本"));
        state = body("正在加载错题...");
        root.addView(state);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LineUi.styleScreen(root);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(ExerciseApi.class);
        load();
    }

    private void load() {
        NetworkCall.enqueue(api.wrongQuestions(1, 50), new NetworkCall.Result<CourseApi.PageData<Map<String, Object>>>() {
            @Override public void success(CourseApi.PageData<Map<String, Object>> data) {
                list.removeAllViews();
                List<Map<String, Object>> rows = data == null ? List.of() : data.getRecords();
                if (rows == null || rows.isEmpty()) {
                    state.setText("还没有错题记录");
                    return;
                }
                state.setText("共 " + rows.size() + " 道错题");
                for (Map<String, Object> row : rows) addWrongQuestion(row);
            }
            @Override public void failure(String message) { state.setText("错题加载失败：" + message); }
        });
    }

    private void addWrongQuestion(Map<String, Object> row) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 14, 14, 14);
        box.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row);
        TextView text = body("题目编号：" + MapValues.text(row, "questionId", "未标注")
                + "\n练习编号：" + MapValues.text(row, "exerciseId", "未标注")
                + "\n错误次数：" + MapValues.text(row, "wrongCount", "1")
                + "\n最近错误：" + MapValues.text(row, "lastWrongAt", "未标注"));
        text.setTextColor(LineTheme.TEXT);
        box.addView(text);
        Button reviewed = new Button(this);
        boolean done = "true".equalsIgnoreCase(MapValues.text(row, "reviewStatus", "false"));
        reviewed.setText(done ? "已复习" : "标记为已复习");
        reviewed.setAllCaps(false);
        reviewed.setEnabled(!done);
        box.addView(reviewed);
        String questionId = MapValues.text(row, "questionId", null);
        reviewed.setOnClickListener(v -> {
            if (questionId == null) return;
            reviewed.setEnabled(false);
            NetworkCall.enqueue(api.markWrongReviewed(questionId), new NetworkCall.Result<Void>() {
                @Override public void success(Void ignored) { reviewed.setText("已复习"); state.setText("已更新复习状态"); }
                @Override public void failure(String message) { reviewed.setEnabled(true); state.setText("更新失败：" + message); }
            });
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, 10);
        list.addView(box, lp);
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
}
