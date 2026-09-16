package xyz.wystudio.yincai.feature.main;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.ResourceApi;
import xyz.wystudio.yincai.feature.search.SearchActivity;
import xyz.wystudio.yincai.ui.theme.LineTheme;

public class ResourceCenterFragment extends Fragment {
    private EditText search; private TextView state; private LinearLayout content; private View retry; private YinCaiApplication app;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_resources,container,false); app=(YinCaiApplication)requireActivity().getApplication(); search=view.findViewById(R.id.input_resource_search); state=view.findViewById(R.id.text_resource_state); content=view.findViewById(R.id.resource_content); retry=view.findViewById(R.id.button_resource_retry);
        view.findViewById(R.id.button_resource_search).setOnClickListener(v -> load());
        view.findViewById(R.id.button_resource_advanced_search).setOnClickListener(v -> {
            String keyword = search.getText() == null ? "" : search.getText().toString().trim();
            startActivity(new Intent(requireContext(), SearchActivity.class).putExtra("keyword", keyword));
        });
        retry.setOnClickListener(v -> load()); load(); return view;
    }
    private void load() {
        state.setText("正在加载资源..."); retry.setVisibility(View.GONE); content.removeAllViews(); String keyword=search.getText()==null?"":search.getText().toString().trim();
        NetworkCall.Result<CourseApi.PageData<Map<String,Object>>> result=new NetworkCall.Result<>() {
            @Override public void success(CourseApi.PageData<Map<String,Object>> data) { if (!isAdded()) return; List<Map<String,Object>> rows=data==null?List.of():data.getRecords(); if(rows==null||rows.isEmpty()){state.setText(keyword.isEmpty()?"还没有可用资源":"没有找到匹配的资源"); addText("试试更换关键词，或稍后再来看看",Color.DKGRAY);return;} state.setText("找到 " + rows.size() + " 项资源"); for(Map<String,Object> row:rows)addResource(row); }
            @Override public void failure(String message){if(isAdded())error(message);}
        };
        ResourceApi api=app.apiClient().create(ResourceApi.class); if(keyword.isEmpty()) NetworkCall.enqueue(api.list(1,20,null,null,"PUBLISHED"),result); else NetworkCall.enqueue(api.search(keyword,1,20,null,null),result);
    }
    private void addResource(Map<String,Object> row){LinearLayout box=new LinearLayout(requireContext());box.setOrientation(LinearLayout.VERTICAL);box.setPadding(LineTheme.dp(requireContext(),16),LineTheme.dp(requireContext(),15),LineTheme.dp(requireContext(),12),LineTheme.dp(requireContext(),15));box.setBackgroundResource(R.drawable.bg_line_row);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,8);box.setLayoutParams(lp);TextView t=new TextView(requireContext());t.setText(MapValues.text(row,"title","学习资源"));t.setTextColor(LineTheme.TEXT);t.setTextSize(LineTheme.FONT_LG);t.setTypeface(null, android.graphics.Typeface.BOLD);box.addView(t);TextView m=new TextView(requireContext());m.setText(MapValues.text(row,"resourceType","资料")+" · 难度 "+MapValues.text(row,"difficulty","未标注")+"\n"+MapValues.text(row,"summary","暂无简介"));m.setTextColor(LineTheme.TEXT_SECONDARY);m.setTextSize(LineTheme.FONT_SM);LinearLayout.LayoutParams mlp=new LinearLayout.LayoutParams(-1,-2);mlp.setMargins(0,LineTheme.dp(requireContext(),6),0,0);box.addView(m,mlp);box.setOnClickListener(v -> {Intent intent=new Intent(requireContext(),xyz.wystudio.yincai.feature.resource.ResourceDetailActivity.class);intent.putExtra("resourceId",MapValues.text(row,"id",null));startActivity(intent);});content.addView(box);}
    private void addText(String value,int color){TextView t=new TextView(requireContext());t.setText(value);t.setTextColor(color == Color.DKGRAY ? LineTheme.TEXT_SECONDARY : color);t.setTextSize(LineTheme.FONT_MD);t.setPadding(LineTheme.dp(requireContext(),4),LineTheme.dp(requireContext(),12),LineTheme.dp(requireContext(),4),LineTheme.dp(requireContext(),12));content.addView(t);}
    private void error(String message){state.setText("资源加载失败");addText(message,LineTheme.DANGER);retry.setVisibility(View.VISIBLE);}
}
