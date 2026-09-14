package xyz.wystudio.yincai.feature.main;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Map;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.LearningPathApi;
import xyz.wystudio.yincai.data.remote.ProfileApi;
import xyz.wystudio.yincai.feature.profile.ProfileDialogueActivity;
import xyz.wystudio.yincai.feature.resource.ResourceGenerationActivity;

public class HomeFragment extends Fragment {
    private TextView greeting, profile, profileHint, path, pathHint;
    private View retry;
    private YinCaiApplication app;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        app = (YinCaiApplication) requireActivity().getApplication();
        greeting = view.findViewById(R.id.text_home_greeting); profile = view.findViewById(R.id.text_home_profile); profileHint = view.findViewById(R.id.text_home_profile_hint); path = view.findViewById(R.id.text_home_path); pathHint = view.findViewById(R.id.text_home_path_hint); retry = view.findViewById(R.id.button_home_retry);
        String name = app.sessionManager().getUsername(); greeting.setText(name == null ? "你好，开始今天的学习" : "你好，" + name);
        view.findViewById(R.id.button_home_path).setOnClickListener(v -> ((MainActivity) requireActivity()).openTab(R.id.nav_path));
        view.findViewById(R.id.button_home_tutor).setOnClickListener(v -> ((MainActivity) requireActivity()).openTab(R.id.nav_tutor));
        view.findViewById(R.id.button_home_profile).setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileDialogueActivity.class)));
        view.findViewById(R.id.button_home_generate).setOnClickListener(v -> startActivity(new Intent(requireContext(), ResourceGenerationActivity.class)));
        retry.setOnClickListener(v -> load()); load(); return view;
    }
    private void load() {
        retry.setVisibility(View.GONE); profile.setText("正在读取你的学习状态..."); path.setText("正在读取学习路径...");
        String studentId = app.sessionManager().getUserId(); if (studentId == null) { showError("会话信息不完整，请重新登录"); return; }
        NetworkCall.enqueue(app.apiClient().create(ProfileApi.class).current(studentId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) { if (!isAdded()) return; if (data == null || data.isEmpty()) { profile.setText("还没有学习画像"); profileHint.setText("完成一次学习后，系统会逐步了解你的特点"); } else { profile.setText("学习画像已建立"); profileHint.setText("系统会根据你的学习记录调整推荐内容"); } }
            @Override public void failure(String message) { if (isAdded()) profileHint.setText("学习画像暂时无法读取"); }
        });
        NetworkCall.enqueue(app.apiClient().create(LearningPathApi.class).current(studentId), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) { if (!isAdded()) return; if (data == null || data.isEmpty()) { path.setText("还没有学习路径"); pathHint.setText("进入学习路径页查看推荐内容"); } else { path.setText("继续你的学习路径"); pathHint.setText("当前路径已准备好，去完成下一个节点吧"); } }
            @Override public void failure(String message) {
                if (!isAdded()) return;
                if (message != null && message.contains("不存在")) {
                    path.setText("还没有学习路径");
                    pathHint.setText("进入学习路径页完成课程选择");
                } else {
                    path.setText("学习路径暂时无法读取");
                    pathHint.setText(message);
                    retry.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void showError(String message) { profile.setText(message); path.setText("暂时无法加载首页数据"); retry.setVisibility(View.VISIBLE); }
}
