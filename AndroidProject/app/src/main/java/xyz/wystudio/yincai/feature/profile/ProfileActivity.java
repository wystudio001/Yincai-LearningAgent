package xyz.wystudio.yincai.feature.profile;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.ProfileApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

public final class ProfileActivity extends AppCompatActivity {
    private TextView state;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = LineUi.page(this);
        root.addView(LineUi.toolbar(this, "学习画像", v -> finish()));
        root.addView(LineUi.divider(this));
        state = LineUi.text(this, "正在读取画像...", LineTheme.FONT_MD, LineTheme.TEXT_SECONDARY, android.graphics.Typeface.NORMAL);
        state.setPadding(LineTheme.dp(this, 20), LineTheme.dp(this, 20), LineTheme.dp(this, 20), LineTheme.dp(this, 20));
        root.addView(state);
        setContentView(root);

        YinCaiApplication app = (YinCaiApplication) getApplication();
        String id = app.sessionManager().getUserId();
        NetworkCall.enqueue(app.apiClient().create(ProfileApi.class).current(id),
                new NetworkCall.Result<Map<String, Object>>() {
                    @Override public void success(Map<String, Object> data) {
                        state.setText(data == null || data.isEmpty()
                                ? "还没有学习画像，继续学习后系统会逐步建立。"
                                : "你的学习画像已建立，系统会据此调整资源和路径推荐。\n\n画像详情：\n" + data);
                    }
                    @Override public void failure(String message) {
                        state.setTextColor(LineTheme.DANGER);
                        state.setText("画像暂时无法读取：" + message);
                    }
                });
    }
}
