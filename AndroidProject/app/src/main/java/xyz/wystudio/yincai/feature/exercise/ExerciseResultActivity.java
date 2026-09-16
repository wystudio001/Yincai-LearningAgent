package xyz.wystudio.yincai.feature.exercise;

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
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.ExerciseApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 独立展示练习提交后的记录，便于从练习页和通知入口重复查看。 */
public final class ExerciseResultActivity extends AppCompatActivity {
    private TextView state;
    private LinearLayout content;
    private Button retry;
    private String exerciseId;
    private ExerciseApi api;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseId = getIntent().getStringExtra("exerciseId");
        if (exerciseId == null || exerciseId.trim().isEmpty() || "null".equalsIgnoreCase(exerciseId)) {
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("练习结果"));
        state = body("正在读取结果...");
        root.addView(state);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        retry = action("重新读取");
        retry.setVisibility(View.GONE);
        retry.setOnClickListener(v -> load());
        root.addView(retry);
        LineUi.styleScreen(root);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(ExerciseApi.class);
        load();
    }

    private void load() {
        state.setText("正在读取结果...");
        retry.setVisibility(View.GONE);
        NetworkCall.enqueue(api.result(exerciseId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                content.removeAllViews();
                if (data == null || data.isEmpty()) {
                    state.setText("暂时没有练习结果");
                    addText("完成提交后，系统会在这里保留你的作答记录。", LineTheme.TEXT_SECONDARY);
                    return;
                }
                state.setText("练习已提交");
                addText("练习编号：" + MapValues.text(data, "exerciseId", exerciseId)
                        + "\n已记录答案：" + MapValues.text(data, "answerCount", "0") + " 条", LineTheme.TEXT);
                List<Map<String, Object>> records = MapValues.maps(data, "records");
                if (records.isEmpty()) {
                    addText("服务端暂未返回逐题记录。", LineTheme.TEXT_SECONDARY);
                    return;
                }
                addHeading("逐题记录");
                int number = 1;
                for (Map<String, Object> record : records) {
                    addText(number++ + ". 题目：" + MapValues.text(record, "questionId", "未标注")
                            + "\n回答：" + MapValues.text(record, "answerContent", "未填写")
                            + "\n提交时间：" + MapValues.text(record, "submittedAt", "未标注"), LineTheme.TEXT);
                }
            }

            @Override public void failure(String message) {
                state.setText("结果读取失败：" + message);
                retry.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addHeading(String value) {
        TextView view = title(value);
        view.setTextSize(18);
        content.addView(view);
    }

    private void addText(String value, int color) {
        TextView view = body(value);
        view.setTextColor(color);
        view.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row);
        view.setPadding(14, 14, 14, 14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, 10);
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
