package xyz.wystudio.yincai.feature.profile;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.ProfileApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 用轻量聊天流完成学生画像建档，画像抽取仍由 Java 后端负责。 */
public final class ProfileDialogueActivity extends AppCompatActivity {
    private LinearLayout messages;
    private EditText input;
    private TextView state;
    private ProfileApi api;
    private String studentId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        studentId = ((YinCaiApplication) getApplication()).sessionManager().getUserId();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        TextView title = title("对话构建学习画像");
        root.addView(title);
        state = body("回答几个问题，系统会逐步了解你的学习目标和习惯");
        root.addView(state);
        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(this);
        input.setHint("例如：我想提高 Java 基础");
        input.setMaxLines(3);
        bar.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        Button send = action("发送");
        send.setOnClickListener(v -> send());
        bar.addView(send);
        root.addView(bar);
        Button init = action("生成我的学习画像");
        init.setOnClickListener(v -> initialize(init));
        root.addView(init);
        LineUi.styleScreen(root);
        setContentView(root);

        api = ((YinCaiApplication) getApplication()).apiClient().create(ProfileApi.class);
        loadHistory();
    }

    private void loadHistory() {
        if (studentId == null) {
            state.setText("会话信息不完整，请重新登录");
            return;
        }
        NetworkCall.enqueue(api.dialogues(studentId, null, 50), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) {
                messages.removeAllViews();
                if (data != null) for (Map<String, Object> row : data) {
                    boolean mine = "STUDENT".equalsIgnoreCase(MapValues.text(row, "role", ""));
                    addMessage(MapValues.text(row, "content", ""), mine);
                }
                if (data == null || data.isEmpty()) addMessage("你好，我会根据你的目标、基础和时间安排，帮你建立学习画像。你最近最想解决什么问题？", false);
                state.setText("可以继续补充你的学习情况");
            }
            @Override public void failure(String message) { state.setText("历史对话暂时无法读取：" + message); }
        });
    }

    private void send() {
        String content = input.getText() == null ? "" : input.getText().toString().trim();
        if (content.isEmpty() || studentId == null) return;
        input.setText("");
        addMessage(content, true);
        state.setText("正在记录你的回答...");
        Map<String, Object> body = new HashMap<>();
        body.put("studentId", studentId);
        body.put("role", "STUDENT");
        body.put("content", content);
        NetworkCall.enqueue(api.dialogue(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) { state.setText("已记录，可以继续回答或生成画像"); }
            @Override public void failure(String message) { state.setText("回答保存失败：" + message); }
        });
    }

    private void initialize(Button button) {
        if (studentId == null) return;
        button.setEnabled(false);
        state.setText("正在初始化学习画像...");
        Map<String, Object> body = new HashMap<>();
        body.put("studentId", studentId);
        String courseId = getIntent().getStringExtra("courseId");
        if (courseId != null && !courseId.trim().isEmpty()) body.put("courseId", courseId.trim());
        body.put("summary", "画像正在根据对话内容初始化");
        NetworkCall.enqueue(api.initialize(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                state.setText("学习画像已初始化，可以到画像页查看");
                addMessage("画像初始化完成：" + MapValues.text(data, "summary", "系统会继续根据学习行为更新画像"), false);
                button.setEnabled(true);
            }
            @Override public void failure(String message) { state.setText("画像初始化失败：" + message); button.setEnabled(true); }
        });
    }

    private void addMessage(String text, boolean mine) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(15);
        bubble.setTextColor(mine ? LineTheme.TEXT : LineTheme.TEXT);
        bubble.setBackground(LineTheme.rounded(this, mine ? LineTheme.USER_BUBBLE : LineTheme.AI_BUBBLE, 16));
        bubble.setPadding(LineTheme.dp(this, 14), LineTheme.dp(this, 12), LineTheme.dp(this, 14), LineTheme.dp(this, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(mine ? 48 : 0, 0, mine ? 0 : 48, 10);
        messages.addView(bubble, lp);
    }

    private TextView title(String value) { TextView view = body(value); view.setTextSize(LineTheme.FONT_XL); view.setTextColor(LineTheme.TEXT); view.setTypeface(null, android.graphics.Typeface.BOLD); return view; }
    private TextView body(String value) { TextView view = new TextView(this); view.setText(value); view.setTextSize(LineTheme.FONT_SM); view.setTextColor(LineTheme.TEXT_SECONDARY); view.setPadding(0, LineTheme.dp(this, 8), 0, LineTheme.dp(this, 8)); return view; }
    private Button action(String value) { Button button = new Button(this); button.setText(value); button.setAllCaps(false); return button; }
}
