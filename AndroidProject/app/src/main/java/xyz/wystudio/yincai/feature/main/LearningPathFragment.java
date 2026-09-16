package xyz.wystudio.yincai.feature.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.List;
import java.util.Map;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.LearningPathApi;
import xyz.wystudio.yincai.ui.theme.LineTheme;

public class LearningPathFragment extends Fragment {
    private TextView status, title; private LinearLayout content; private View retry; private YinCaiApplication app;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning_path, container, false); app = (YinCaiApplication) requireActivity().getApplication();
        status=view.findViewById(R.id.text_path_status); title=view.findViewById(R.id.text_path_title); content=view.findViewById(R.id.path_content); retry=view.findViewById(R.id.button_path_retry); retry.setOnClickListener(v -> load()); load(); return view;
    }
    private void load() {
        status.setText("正在加载学习路径..."); retry.setVisibility(View.GONE); content.removeAllViews(); String studentId=app.sessionManager().getUserId();
        if (studentId == null) { error("会话信息不完整，请重新登录"); return; }
        NetworkCall.enqueue(app.apiClient().create(LearningPathApi.class).current(studentId), new NetworkCall.Result<Map<String,Object>>() {
            @Override public void success(Map<String,Object> data) { if (!isAdded()) return; List<Map<String,Object>> nodes=MapValues.maps(data,"nodes"); title.setText("我的学习路径"); if (nodes.isEmpty()) { status.setText("还没有可展示的学习节点"); addText("完成课程选择后，系统会生成适合你的学习路径", LineTheme.TEXT_SECONDARY); return; } status.setText("共 " + nodes.size() + " 个学习节点"); for (Map<String,Object> node:nodes) addNode(node); }
            @Override public void failure(String message) {
                if (!isAdded()) return;
                if (message != null && message.contains("不存在")) {
                    title.setText("我的学习路径");
                    status.setText("还没有可展示的学习路径");
                    addText("完成课程选择后，系统会生成适合你的学习路径", LineTheme.TEXT_SECONDARY);
                } else {
                    error(message);
                }
            }
        });
    }
    private void addNode(Map<String,Object> node) {
        LinearLayout box=new LinearLayout(requireContext()); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(LineTheme.dp(requireContext(),16),LineTheme.dp(requireContext(),15),LineTheme.dp(requireContext(),12),LineTheme.dp(requireContext(),15)); box.setBackgroundResource(R.drawable.bg_line_row); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,8); box.setLayoutParams(lp);
        TextView heading=new TextView(requireContext()); heading.setText(MapValues.text(node,"target","学习节点")); heading.setTextColor(LineTheme.TEXT); heading.setTextSize(LineTheme.FONT_LG); heading.setTypeface(null, android.graphics.Typeface.BOLD); box.addView(heading);
        TextView meta=new TextView(requireContext()); meta.setText("状态：" + MapValues.text(node,"status","PENDING") + "\n完成规则：" + MapValues.text(node,"completionRule","完成本节点学习内容")); meta.setTextColor(LineTheme.TEXT_SECONDARY); meta.setTextSize(LineTheme.FONT_SM); LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2); mlp.setMargins(0,LineTheme.dp(requireContext(),6),0,0); box.addView(meta,mlp);
        box.setOnClickListener(v -> {
            String nodeId = MapValues.text(node, "id", null);
            if (nodeId == null) {
                addText("该节点缺少有效标识，暂时无法打开", LineTheme.DANGER);
                return;
            }
            Intent intent=new Intent(requireContext(), xyz.wystudio.yincai.feature.learningpath.LearningNodeDetailActivity.class);
            intent.putExtra("nodeId", nodeId);
            intent.putExtra("target", MapValues.text(node,"target","学习节点"));
            intent.putExtra("status", MapValues.text(node,"status","PENDING"));
            intent.putExtra("completionRule", MapValues.text(node,"completionRule","完成本节点学习内容"));
            startActivity(intent);
        }); content.addView(box);
    }
    private void addText(String text,int color) { TextView v=new TextView(requireContext()); v.setText(text); v.setTextColor(color); v.setTextSize(LineTheme.FONT_MD); v.setPadding(LineTheme.dp(requireContext(),4),LineTheme.dp(requireContext(),12),LineTheme.dp(requireContext(),4),LineTheme.dp(requireContext(),12)); content.addView(v); }
    private void error(String message) { status.setText("学习路径加载失败"); addText(message, LineTheme.DANGER); retry.setVisibility(View.VISIBLE); }
}
