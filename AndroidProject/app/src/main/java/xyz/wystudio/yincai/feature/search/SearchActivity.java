package xyz.wystudio.yincai.feature.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.ResourceApi;
import xyz.wystudio.yincai.feature.resource.ResourceDetailActivity;
import xyz.wystudio.yincai.ui.theme.LineTheme;
import xyz.wystudio.yincai.ui.theme.LineUi;

/** 独立资源搜索页，搜索请求仍然只发往 Java 后端。 */
public final class SearchActivity extends AppCompatActivity {
    private EditText input;
    private TextView state;
    private LinearLayout content;
    private ResourceApi api;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.addView(title("搜索学习资源"));
        LinearLayout toolbar = new LinearLayout(this);
        input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("输入关键词，例如 Java 集合");
        String keyword = getIntent().getStringExtra("keyword");
        if (keyword != null) input.setText(keyword);
        toolbar.addView(input, new LinearLayout.LayoutParams(0, -2, 1));
        Button search = action("搜索");
        toolbar.addView(search);
        root.addView(toolbar);
        state = body("输入关键词开始搜索");
        root.addView(state);
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LineUi.styleScreen(root);
        setContentView(root);
        api = ((YinCaiApplication) getApplication()).apiClient().create(ResourceApi.class);
        search.setOnClickListener(v -> load());
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                load();
                return true;
            }
            return false;
        });
        if (keyword != null && !keyword.trim().isEmpty()) load();
    }

    private void load() {
        String keyword = input.getText() == null ? "" : input.getText().toString().trim();
        if (keyword.isEmpty()) {
            state.setText("请输入搜索关键词");
            return;
        }
        state.setText("正在搜索...");
        content.removeAllViews();
        NetworkCall.enqueue(api.search(keyword, 1, 30, null, null), new NetworkCall.Result<CourseApi.PageData<Map<String, Object>>>() {
            @Override public void success(CourseApi.PageData<Map<String, Object>> data) {
                List<Map<String, Object>> rows = data == null ? List.of() : data.getRecords();
                if (rows == null || rows.isEmpty()) {
                    state.setText("没有找到匹配资源");
                    addText("试试更换关键词，或使用课程名称搜索。", LineTheme.TEXT_SECONDARY);
                    return;
                }
                state.setText("找到 " + rows.size() + " 项资源");
                for (Map<String, Object> row : rows) addResource(row);
            }

            @Override public void failure(String message) {
                state.setText("搜索失败：" + message);
            }
        });
    }

    private void addResource(Map<String, Object> row) {
        TextView item = body(MapValues.text(row, "title", "学习资源") + "\n"
                + MapValues.text(row, "resourceType", "资料") + " · 难度 " + MapValues.text(row, "difficulty", "未标注")
                + "\n" + MapValues.text(row, "summary", "暂无简介"));
        item.setTextColor(LineTheme.TEXT);
        item.setTextSize(LineTheme.FONT_MD);
        item.setBackgroundResource(xyz.wystudio.yincai.R.drawable.bg_line_row);
        item.setPadding(LineTheme.dp(this, 16), LineTheme.dp(this, 16), LineTheme.dp(this, 16), LineTheme.dp(this, 16));
        item.setOnClickListener(v -> {
            String id = MapValues.text(row, "id", null);
            if (id != null) startActivity(new Intent(this, ResourceDetailActivity.class).putExtra("resourceId", id));
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, 10);
        content.addView(item, lp);
    }

    private void addText(String value, int color) {
        TextView view = body(value);
        view.setTextColor(color);
        content.addView(view);
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
