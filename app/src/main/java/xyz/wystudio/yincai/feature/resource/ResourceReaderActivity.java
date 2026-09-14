package xyz.wystudio.yincai.feature.resource;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.ResourceApi;

/** 文本和 Markdown 资源的轻量阅读器，复杂文件交给系统或下载地址打开。 */
public final class ResourceReaderActivity extends AppCompatActivity {
    private TextView state;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String resourceId = getIntent().getStringExtra("resourceId");
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        TextView title = new TextView(this);
        title.setText("资源阅读"); title.setTextSize(24); title.setTextColor(Color.rgb(27, 29, 42)); title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title);
        state = new TextView(this); state.setText("正在准备内容..."); state.setTextColor(Color.rgb(101, 105, 120)); root.addView(state);
        ScrollView scroll = new ScrollView(this);
        TextView content = new TextView(this); content.setTextSize(16); content.setTextColor(Color.rgb(27, 29, 42)); content.setTextIsSelectable(true); content.setPadding(0, 16, 0, 16);
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        if (resourceId == null || resourceId.trim().isEmpty()) { state.setText("资源信息不完整"); return; }
        ResourceApi api = ((YinCaiApplication) getApplication()).apiClient().create(ResourceApi.class);
        NetworkCall.enqueue(api.detail(resourceId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                String titleText = MapValues.text(data, "title", "学习资源");
                String raw = MapValues.text(data, "content", MapValues.text(data, "markdown", MapValues.text(data, "body", MapValues.text(data, "description", MapValues.text(data, "summary", "暂无可阅读内容")))));
                content.setText(Html.fromHtml(raw.replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT));
                state.setText(titleText + " · " + MapValues.text(data, "resourceType", "文档"));
            }
            @Override public void failure(String message) { state.setText("内容读取失败：" + message); }
        });
    }
}
