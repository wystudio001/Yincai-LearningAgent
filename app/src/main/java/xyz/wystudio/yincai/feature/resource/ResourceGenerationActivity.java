package xyz.wystudio.yincai.feature.resource;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.GenerationApi;

/** 发起个性化资源生成任务。任务内容由 Java 后端调度，不在客户端保存模型配置。 */
public final class ResourceGenerationActivity extends AppCompatActivity {
    private TextView state;
    private EditText taskType;
    private EditText courseId;
    private EditText knowledgePointId;
    private Button create;
    private GenerationApi api;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("生成个性化资源"));
        root.addView(body("选择任务类型并填写可选的课程或知识点，生成过程可以在进度页查看。"));
        taskType = input("任务类型，例如 STUDY_MATERIAL");
        taskType.setText("STUDY_MATERIAL");
        root.addView(taskType);
        courseId = input("课程 ID（可选）");
        String presetCourse = getIntent().getStringExtra("courseId");
        if (presetCourse != null) courseId.setText(presetCourse);
        root.addView(courseId);
        knowledgePointId = input("知识点 ID（可选）");
        root.addView(knowledgePointId);
        state = body("");
        root.addView(state);
        create = action("开始生成");
        create.setOnClickListener(v -> submit());
        root.addView(create);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(GenerationApi.class);
    }

    private void submit() {
        String type = taskType.getText() == null ? "" : taskType.getText().toString().trim();
        if (type.isEmpty()) { state.setText("请填写任务类型"); return; }
        create.setEnabled(false);
        state.setText("正在创建生成任务...");
        Map<String, Object> body = new HashMap<>();
        body.put("taskType", type);
        if (!putNumber(body, "courseId", courseId.getText().toString())
                || !putNumber(body, "knowledgePointId", knowledgePointId.getText().toString())) {
            create.setEnabled(true);
            return;
        }
        NetworkCall.enqueue(api.create(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                String id = MapValues.text(data, "id", null);
                if (id == null) { state.setText("任务已创建，但没有返回任务编号"); create.setEnabled(true); return; }
                state.setText("任务已创建，正在打开进度页");
                startActivity(new android.content.Intent(ResourceGenerationActivity.this, GenerationProgressActivity.class).putExtra("taskId", id));
                create.setEnabled(true);
            }
            @Override public void failure(String message) { state.setText("创建任务失败：" + message); create.setEnabled(true); }
        });
    }

    private boolean putNumber(Map<String, Object> body, String key, String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return true;
        try { body.put(key, Long.parseLong(value)); return true; }
        catch (NumberFormatException ignored) { state.setText("请输入有效的 " + key); return false; }
    }

    private EditText input(String hint) { EditText input = new EditText(this); input.setHint(hint); input.setSingleLine(true); input.setPadding(0, 10, 0, 10); return input; }
    private TextView title(String value) { TextView view = body(value); view.setTextSize(24); view.setTextColor(Color.rgb(27, 29, 42)); view.setTypeface(null, android.graphics.Typeface.BOLD); return view; }
    private TextView body(String value) { TextView view = new TextView(this); view.setText(value); view.setTextSize(14); view.setTextColor(Color.rgb(101, 105, 120)); view.setPadding(0, 8, 0, 8); return view; }
    private Button action(String value) { Button button = new Button(this); button.setText(value); button.setAllCaps(false); return button; }
}
