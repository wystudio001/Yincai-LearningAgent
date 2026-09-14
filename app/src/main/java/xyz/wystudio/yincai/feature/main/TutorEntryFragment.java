package xyz.wystudio.yincai.feature.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.TutorApi;
import xyz.wystudio.yincai.feature.tutor.TutorChatActivity;

public class TutorEntryFragment extends Fragment {
    private TextView state; private LinearLayout history; private YinCaiApplication app;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_tutor,container,false); app=(YinCaiApplication)requireActivity().getApplication(); state=view.findViewById(R.id.text_tutor_state); history=view.findViewById(R.id.tutor_history_content);
        view.findViewById(R.id.button_tutor_question_one).setOnClickListener(v -> createSession("帮我梳理一个知识点")); view.findViewById(R.id.button_tutor_question_two).setOnClickListener(v -> createSession("根据我的路径安排复习")); view.findViewById(R.id.button_tutor_new).setOnClickListener(v -> createSession("开始新的学习辅导")); loadSessions(); return view;
    }
    private void loadSessions(){state.setText("正在读取最近会话..."); NetworkCall.enqueue(app.apiClient().create(TutorApi.class).sessions(),new NetworkCall.Result<List<Map<String,Object>>>(){ @Override public void success(List<Map<String,Object>> data){if(!isAdded())return;history.removeAllViews();if(data==null||data.isEmpty()){state.setText("还没有辅导会话，选择一个推荐问题开始吧");return;}state.setText("选择会话继续学习");for(Map<String,Object> row:data)addSession(row);} @Override public void failure(String message){if(isAdded()){state.setText("暂时无法读取辅导会话："+message);}}});}
    private void addSession(Map<String,Object> row){
        TextView item=new TextView(requireContext());
        item.setText(MapValues.text(row,"title","学习辅导会话"));
        item.setTextColor(Color.rgb(27,29,42)); item.setTextSize(16); item.setPadding(18,18,18,18); item.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,10); history.addView(item,lp);
        String sessionId = MapValues.text(row, "id", null);
        if (sessionId == null) { item.setEnabled(false); return; }
        item.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TutorChatActivity.class);
            intent.putExtra("sessionId", sessionId);
            startActivity(intent);
        });
    }
    private void createSession(String prompt){state.setText("正在创建辅导会话...");Map<String,Object> body=new HashMap<>();body.put("title",prompt);NetworkCall.enqueue(app.apiClient().create(TutorApi.class).createSession(body),new NetworkCall.Result<Map<String,Object>>(){@Override public void success(Map<String,Object> data){if(isAdded()){Intent intent=new Intent(requireContext(), TutorChatActivity.class);if(data!=null&&data.get("id")!=null)intent.putExtra("sessionId",String.valueOf(data.get("id")));intent.putExtra("draftMessage",prompt);startActivity(intent);}}@Override public void failure(String message){if(isAdded())state.setText("辅导服务暂不可用："+message);}});}
}
