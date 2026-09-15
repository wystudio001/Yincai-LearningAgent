package xyz.wystudio.yincai.feature.resource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.ResourceApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

public final class ResourceDetailActivity extends AppCompatActivity {
    private TextView state;
    private String resourceId;
    private boolean favorited;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resourceId = getIntent().getStringExtra("resourceId");
        if (resourceId == null || resourceId.trim().isEmpty() || "null".equalsIgnoreCase(resourceId)) {
            Toast.makeText(this, "资源信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("资源详情");
        title.setTextSize(24);
        title.setTextColor(LineTheme.TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        state = new TextView(this);
        state.setText("正在读取资源...");
        state.setTextSize(16);
        state.setTextColor(LineTheme.TEXT_SECONDARY);
        state.setPadding(0, 16, 0, 16);
        root.addView(state);
        Button favorite = new Button(this);
        favorite.setText("收藏资源");
        favorite.setAllCaps(false);
        root.addView(favorite);
        Button read = new Button(this);
        read.setText("开始阅读");
        read.setAllCaps(false);
        root.addView(read);
        Button download = new Button(this);
        download.setText("打开下载地址");
        download.setAllCaps(false);
        root.addView(download);
        Button feedback = new Button(this);
        feedback.setText("反馈这份资源");
        feedback.setAllCaps(false);
        root.addView(feedback);
        LineUi.styleScreen(root);
        setContentView(root);

        ResourceApi api = ((YinCaiApplication) getApplication()).apiClient().create(ResourceApi.class);
        NetworkCall.enqueue(api.detail(resourceId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                state.setText(MapValues.text(data, "title", "学习资源") + "\n\n"
                        + MapValues.text(data, "summary", MapValues.text(data, "description", "暂无简介"))
                        + "\n\n类型：" + MapValues.text(data, "resourceType", "未标注")
                        + "\n难度：" + MapValues.text(data, "difficulty", "未标注"));
            }
            @Override public void failure(String message) { state.setText("资源加载失败：" + message); }
        });
        favorite.setOnClickListener(v -> {
            favorite.setEnabled(false);
            NetworkCall.Result<Void> result = new NetworkCall.Result<>() {
                @Override public void success(Void ignored) { favorited = !favorited; favorite.setText(favorited ? "取消收藏" : "收藏资源"); favorite.setEnabled(true); }
                @Override public void failure(String message) { favorite.setEnabled(true); state.append("\n操作失败：" + message); }
            };
            if (favorited) NetworkCall.enqueue(api.unfavorite(resourceId), result); else NetworkCall.enqueue(api.favorite(resourceId), result);
        });
        read.setOnClickListener(v -> startActivity(new Intent(this, ResourceReaderActivity.class).putExtra("resourceId", resourceId)));
        download.setOnClickListener(v -> NetworkCall.enqueue(api.downloadUrl(resourceId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                String url = MapValues.text(data, "downloadUrl", "");
                if (url.isEmpty()) { state.append("\n暂时没有下载地址"); return; }
                if (url.startsWith("/")) {
                    String base = ((YinCaiApplication) getApplication()).sessionManager().getBackendUrl();
                    url = base + url.substring(1);
                }
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { state.append("\n无法打开下载地址：" + url); }
            }
            @Override public void failure(String message) { state.append("\n下载地址读取失败：" + message); }
        }));
        feedback.setOnClickListener(v -> submitFeedback(api));
    }

    private void submitFeedback(ResourceApi api) {
        EditText comment = new EditText(this);
        comment.setHint("写下你的建议（可选）");
        comment.setTextColor(LineTheme.TEXT);
        comment.setHintTextColor(LineTheme.TEXT_SECONDARY);
        comment.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_input);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("资源反馈")
                .setView(comment)
                .setPositiveButton("提交", (dialog, which) -> {
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("rating", 5); body.put("comment", comment.getText().toString().trim());
                    NetworkCall.enqueue(api.feedback(resourceId, body), new NetworkCall.Result<Map<String, Object>>() {
                        @Override public void success(Map<String, Object> ignored) { Toast.makeText(ResourceDetailActivity.this, "感谢你的反馈", Toast.LENGTH_SHORT).show(); }
                        @Override public void failure(String message) { state.append("\n反馈提交失败：" + message); }
                    });
                })
                .setNegativeButton("取消", null).show();
    }
}

