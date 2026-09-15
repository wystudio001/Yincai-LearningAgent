package xyz.wystudio.yincai.feature.settings;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

public final class SettingsActivity extends AppCompatActivity {
    private YinCaiApplication app;
    private TextView backendUrl;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (YinCaiApplication) getApplication();
        LinearLayout root = LineUi.page(this);
        root.addView(LineUi.toolbar(this, "设置", v -> finish()));
        root.addView(LineUi.divider(this));
        android.widget.ScrollView scroll = LineUi.scrollPage(this);
        LinearLayout content = LineUi.content(this);

        content.addView(LineUi.section(this, "连接"));
        LinearLayout connection = LineUi.group(this);
        LinearLayout backend = LineUi.rowWithValue(this, "后端地址", "登录、学习路径和辅导请求使用的服务地址", app.sessionManager().getBackendUrl(), v -> showBackendUrlDialog());
        backendUrl = (TextView) backend.getChildAt(1);
        connection.addView(backend);
        content.addView(connection);

        content.addView(LineUi.section(this, "学习"));
        LinearLayout learning = LineUi.group(this);
        learning.addView(LineUi.row(this, "辅导对话", "管理会话并继续与因材导师交流", v -> finish()));
        learning.addView(LineUi.divider(this));
        learning.addView(LineUi.row(this, "数据同步", "学习路径、资源和通知从后端实时读取", null, false));
        content.addView(learning);

        content.addView(LineUi.section(this, "关于"));
        LinearLayout about = LineUi.group(this);
        about.addView(LineUi.row(this, "因材智学", "学生端 1.0", null, false));
        about.addView(LineUi.divider(this));
        about.addView(LineUi.row(this, "服务说明", "客户端仅连接 Java 后端，不保存上游模型配置", null, false));
        content.addView(about);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void refreshUrlText() {
        if (backendUrl != null) backendUrl.setText(app.sessionManager().getBackendUrl());
    }

    private void showBackendUrlDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setText(app.sessionManager().getBackendUrl());
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("后端地址").setView(input)
                .setNegativeButton("取消", null).setPositiveButton("保存", null).create()
                ;
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String value = input.getText() == null ? "" : input.getText().toString().trim();
                    String normalized = normalize(value);
                    if (normalized == null) { input.setError("请输入有效的 http 或 https 地址"); return; }
                    app.sessionManager().setBackendUrl(normalized);
                    refreshUrlText();
                    Toast.makeText(this, "后端地址已保存", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private String normalize(String value) {
        if (value.isEmpty()) return null;
        Uri uri = Uri.parse(value);
        String scheme = uri.getScheme();
        if (uri.getHost() == null || scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return null;
        if (uri.getQuery() != null || uri.getFragment() != null) return null;
        return value.endsWith("/") ? value : value + "/";
    }
}
