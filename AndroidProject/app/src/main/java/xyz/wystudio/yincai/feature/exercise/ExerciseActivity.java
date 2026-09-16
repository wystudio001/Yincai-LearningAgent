package xyz.wystudio.yincai.feature.exercise;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.ExerciseApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 学生练习的最小闭环：查看、开始、作答、提交和查看结果。 */
public final class ExerciseActivity extends AppCompatActivity {
    private TextView state;
    private TextView summary;
    private LinearLayout questions;
    private Button start;
    private Button submitAnswers;
    private Button finish;
    private String exerciseId;
    private final List<Map<String, Object>> questionRows = new ArrayList<>();
    private final List<EditText> answerInputs = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exerciseId = getIntent().getStringExtra("exerciseId");
        if (exerciseId == null || exerciseId.trim().isEmpty() || "null".equalsIgnoreCase(exerciseId)) {
            Toast.makeText(this, "练习信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("练习");
        title.setTextSize(LineTheme.FONT_XL);
        title.setTextColor(LineTheme.TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        summary = new TextView(this);
        summary.setText("正在读取练习...");
        summary.setTextColor(LineTheme.TEXT_SECONDARY);
        summary.setPadding(0, LineTheme.dp(this, 8), 0, LineTheme.dp(this, 8));
        root.addView(summary);
        state = new TextView(this);
        state.setTextColor(LineTheme.TEXT_SECONDARY);
        root.addView(state);

        ScrollView scroll = new ScrollView(this);
        questions = new LinearLayout(this);
        questions.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(questions);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        start = actionButton("开始练习");
        submitAnswers = actionButton("保存答案");
        finish = actionButton("提交练习并查看结果");
        submitAnswers.setVisibility(View.GONE);
        finish.setVisibility(View.GONE);
        root.addView(start);
        root.addView(submitAnswers);
        root.addView(finish);
        LineUi.styleScreen(root);
        setContentView(root);

        ExerciseApi api = ((YinCaiApplication) getApplication()).apiClient().create(ExerciseApi.class);
        NetworkCall.enqueue(api.detail(exerciseId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                if (data == null || data.isEmpty()) {
                    summary.setText("未找到练习内容");
                    start.setEnabled(false);
                    return;
                }
                String titleText = MapValues.text(data, "title", "未命名练习");
                String description = MapValues.text(data, "description", "暂无说明");
                String count = MapValues.text(data, "questionCount", "未标注");
                String limit = MapValues.text(data, "timeLimitMin", "不限时");
                summary.setText(titleText + "\n" + description + "\n题目：" + count + "  ·  时长：" + limit + "分钟");
                state.setText("准备开始");
            }
            @Override public void failure(String message) {
                summary.setText("练习加载失败：" + message);
                start.setEnabled(false);
            }
        });
        start.setOnClickListener(v -> begin(api));
        submitAnswers.setOnClickListener(v -> saveAnswers(api, false));
        finish.setOnClickListener(v -> saveAnswers(api, true));
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        return button;
    }

    private void begin(ExerciseApi api) {
        start.setEnabled(false);
        state.setText("正在开始练习...");
        NetworkCall.enqueue(api.start(exerciseId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> ignored) {
                state.setText("练习已开始");
                start.setVisibility(View.GONE);
                submitAnswers.setVisibility(View.VISIBLE);
                finish.setVisibility(View.VISIBLE);
                loadQuestions(api);
            }
            @Override public void failure(String message) {
                state.setText("开始失败：" + message);
                start.setEnabled(true);
            }
        });
    }

    private void loadQuestions(ExerciseApi api) {
        state.setText("正在读取题目...");
        NetworkCall.enqueue(api.exerciseQuestions(exerciseId), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) {
                questionRows.clear();
                answerInputs.clear();
                questions.removeAllViews();
                if (data != null) questionRows.addAll(data);
                if (questionRows.isEmpty()) {
                    addMessage("当前练习还没有可作答的题目，仍可以提交练习查看服务端记录。");
                    state.setText("暂无题目");
                    return;
                }
                int number = 1;
                for (Map<String, Object> row : questionRows) {
                    addQuestion(number++, row);
                }
                state.setText("已加载 " + questionRows.size() + " 道题目");
            }
            @Override public void failure(String message) {
                addMessage("题目加载失败：" + message);
                state.setText("题目暂时无法读取");
            }
        });
    }

    private void addQuestion(int number, Map<String, Object> row) {
        TextView prompt = new TextView(this);
        prompt.setText(number + ". " + MapValues.text(row, "title", MapValues.text(row, "content", "未命名题目")));
        prompt.setTextColor(LineTheme.TEXT);
        prompt.setTextSize(LineTheme.FONT_LG);
        prompt.setTypeface(null, android.graphics.Typeface.BOLD);
        prompt.setPadding(0, LineTheme.dp(this, 14), 0, LineTheme.dp(this, 6));
        questions.addView(prompt);
        String options = MapValues.text(row, "options", "");
        if (!options.isEmpty()) {
            TextView optionText = new TextView(this);
            optionText.setText(options);
            optionText.setTextColor(LineTheme.TEXT_SECONDARY);
            optionText.setTextSize(LineTheme.FONT_SM);
            optionText.setPadding(LineTheme.dp(this, 0), LineTheme.dp(this, 4), LineTheme.dp(this, 0), LineTheme.dp(this, 8));
            questions.addView(optionText);
        }
        EditText answer = new EditText(this);
        answer.setHint("填写你的答案");
        answer.setTextColor(LineTheme.TEXT);
        answer.setHintTextColor(LineTheme.TEXT_SECONDARY);
        answer.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_input);
        answer.setPadding(LineTheme.dp(this, 12), LineTheme.dp(this, 10), LineTheme.dp(this, 12), LineTheme.dp(this, 10));
        answer.setMinLines(2);
        answer.setGravity(android.view.Gravity.TOP);
        questions.addView(answer);
        answerInputs.add(answer);
    }

    private void saveAnswers(ExerciseApi api, boolean submit) {
        List<Map<String, Object>> answers = new ArrayList<>();
        for (int i = 0; i < questionRows.size() && i < answerInputs.size(); i++) {
            Map<String, Object> answer = new HashMap<>();
            String questionId = MapValues.text(questionRows.get(i), "id", null);
            if (questionId == null) continue;
            answer.put("questionId", questionId);
            answer.put("exerciseId", exerciseId);
            answer.put("answerContent", answerInputs.get(i).getText().toString().trim());
            answers.add(answer);
        }
        state.setText(submit ? "正在提交练习..." : "正在保存答案...");
        NetworkCall.enqueue(api.answer(exerciseId, answers), new NetworkCall.Result<Void>() {
            @Override public void success(Void ignored) {
                if (!submit) {
                    state.setText("答案已保存");
                    return;
                }
                NetworkCall.enqueue(api.submit(exerciseId), new NetworkCall.Result<Map<String, Object>>() {
                    @Override public void success(Map<String, Object> ignored) {
                        startActivity(new Intent(ExerciseActivity.this, ExerciseResultActivity.class)
                                .putExtra("exerciseId", exerciseId));
                        state.setText("练习已提交");
                        submitAnswers.setEnabled(false);
                        finish.setEnabled(false);
                    }
                    @Override public void failure(String message) { state.setText("提交练习失败：" + message); }
                });
            }
            @Override public void failure(String message) { state.setText("答案保存失败：" + message); }
        });
    }

    private void loadResult(ExerciseApi api) {
        state.setText("正在读取练习结果...");
        NetworkCall.enqueue(api.result(exerciseId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                String count = MapValues.text(data, "answerCount", "0");
                addMessage("练习已提交\n已记录答案：" + count + " 条\n继续保持学习节奏，稍后可以在错题和学习报告中复盘。");
                state.setText("练习完成");
                submitAnswers.setEnabled(false);
                finish.setEnabled(false);
            }
            @Override public void failure(String message) { state.setText("结果读取失败：" + message); }
        });
    }

    private void addMessage(String message) {
        TextView view = new TextView(this);
        view.setText(message);
        view.setTextColor(LineTheme.TEXT_SECONDARY);
        view.setPadding(0, LineTheme.dp(this, 18), 0, LineTheme.dp(this, 18));
        questions.addView(view);
    }
}

