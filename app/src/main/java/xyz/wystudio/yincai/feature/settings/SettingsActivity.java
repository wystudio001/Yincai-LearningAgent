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

public final class SettingsActivity extends AppCompatActivity {
    private YinCaiApplication app;
    private TextView backendUrl;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        app = (YinCaiApplication) getApplication();
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("设置");
        title.setTextSize(24);
        root.addView(title);

        backendUrl = new TextView(this);
        backendUrl.setTextSize(16);
        backendUrl.setPadding(0, 24, 0, 24);
        root.addView(backendUrl);
        TextView edit = new TextView(this);
        edit.setText("修改后端地址");
        edit.setTextSize(16);
        edit.setPadding(0, 14, 0, 14);
        edit.setOnClickListener(v -> showBackendUrlDialog());
        root.addView(edit);

        TextView info = new TextView(this);
        info.setText("学生端版本 1.0\n\n客户端仅连接 Java 后端，不保存上游模型配置。");
        info.setPadding(0, 20, 0, 0);
        root.addView(info);
        setContentView(root);
        refreshUrlText();
    }

    private void refreshUrlText() {
        backendUrl.setText("后端地址\n" + app.sessionManager().getBackendUrl());
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
