package xyz.wystudio.yincai.feature.tutor;

import android.graphics.Color;
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
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.TutorApi;

/** 学生端辅导聊天，消息和模型请求均由 Java 后端托管。 */
public final class TutorChatActivity extends AppCompatActivity {
    private LinearLayout messages;
    private EditText input;
    private TextView state;
    private String sessionId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        TextView title = new TextView(this);
        title.setText("智能辅导");
        title.setTextSize(22);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        state = new TextView(this);
        state.setText("回答由 Java 后端提供");
        state.setTextColor(Color.DKGRAY);
        root.addView(state);
        ScrollView scroll = new ScrollView(this);
        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        input = new EditText(this);
        input.setHint("输入你的问题");
        bar.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        Button send = new Button(this);
        send.setText("发送");
        send.setOnClickListener(v -> send());
        bar.addView(send);
        root.addView(bar);
        setContentView(root);
        sessionId = getIntent().getStringExtra("sessionId");
        String draftMessage = getIntent().getStringExtra("draftMessage");
        if (draftMessage != null && !draftMessage.trim().isEmpty()) input.setText(draftMessage);
        if (sessionId == null || sessionId.trim().isEmpty()) create(); else loadHistory();
    }

    private TutorApi api() { return ((YinCaiApplication) getApplication()).apiClient().create(TutorApi.class); }

    private void create() {
        Map<String, Object> body = new HashMap<>(); body.put("title", "学习辅导"); state.setText("正在创建辅导会话...");
        NetworkCall.enqueue(api().createSession(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) { sessionId = data == null || data.get("id") == null ? null : String.valueOf(data.get("id")); state.setText(sessionId == null ? "会话创建失败，请重试" : "会话已创建"); }
            @Override public void failure(String message) { state.setText("辅导服务暂不可用：" + message); }
        });
    }

    private void loadHistory() {
        state.setText("正在读取历史消息...");
        NetworkCall.enqueue(api().messages(sessionId), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) { messages.removeAllViews(); if (data != null) for (Map<String, Object> row : data) { String role = String.valueOf(row.getOrDefault("role", "assistant")); add(String.valueOf(row.getOrDefault("content", "")), "user".equalsIgnoreCase(role), ""); } state.setText(data == null || data.isEmpty() ? "会话已打开，请输入问题" : "历史消息已恢复"); }
            @Override public void failure(String message) { state.setText("历史消息读取失败：" + message); }
        });
    }

    private void send() {
        String text = input.getText() == null ? "" : input.getText().toString().trim(); if (text.isEmpty()) return;
        if (sessionId == null) { state.setText("会话尚未准备好"); return; }
        add(text, true, ""); input.setText(""); state.setText("导师正在思考...");
        Map<String, Object> body = new HashMap<>(); body.put("content", text); body.put("message", text);
        NetworkCall.enqueue(api().sendMessage(sessionId, body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) { String reply = data == null ? "已收到问题，等待导师回答" : String.valueOf(data.getOrDefault("content", data.getOrDefault("answer", "导师已返回回答"))); String id = data == null ? "" : String.valueOf(data.getOrDefault("id", "")); add(reply, false, id); state.setText("回答由 Java 后端提供"); }
            @Override public void failure(String message) { add("暂时无法获取回答：" + message, false, ""); state.setText("辅导请求失败"); }
        });
    }

    private void add(String text, boolean mine, String messageId) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL);
        TextView bubble = new TextView(this); bubble.setText(text); bubble.setTextColor(mine ? Color.WHITE : Color.rgb(27, 29, 42)); bubble.setBackgroundColor(mine ? Color.rgb(74, 91, 213) : Color.rgb(239, 241, 247)); bubble.setPadding(14, 12, 14, 12); row.addView(bubble);
        if (!mine && messageId != null && !messageId.isEmpty()) { Button feedback = new Button(this); feedback.setText("有帮助"); feedback.setAllCaps(false); feedback.setOnClickListener(v -> { Map<String, Object> body = new HashMap<>(); body.put("helpful", true); feedback.setEnabled(false); NetworkCall.enqueue(api().feedback(messageId, body), new NetworkCall.Result<Void>() { @Override public void success(Void ignored) { feedback.setText("已反馈"); } @Override public void failure(String ignored) { feedback.setEnabled(true); } }); }); row.addView(feedback); }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 0, 0, 10); messages.addView(row, lp);
    }
}

