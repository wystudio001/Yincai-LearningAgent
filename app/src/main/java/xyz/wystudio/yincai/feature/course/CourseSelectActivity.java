package xyz.wystudio.yincai.feature.course;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.CourseApi;
import xyz.wystudio.yincai.data.remote.LearningPathApi;

public final class CourseSelectActivity extends AppCompatActivity {
    private LinearLayout list; private TextView state; private YinCaiApplication app;
    @Override protected void onCreate(Bundle b){super.onCreate(b); app=(YinCaiApplication)getApplication(); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(20,20,20,20); TextView title=title("选择课程");root.addView(title);state=body("正在加载课程...");root.addView(state); ScrollView scroll=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);load();}
    private void load(){NetworkCall.enqueue(app.apiClient().create(CourseApi.class).listCourses(1,30,null,"PUBLISHED"),new NetworkCall.Result<CourseApi.PageData<Map<String,Object>>>(){@Override public void success(CourseApi.PageData<Map<String,Object>> data){List<Map<String,Object>> rows=data==null?List.of():data.getRecords();if(rows==null||rows.isEmpty()){state.setText("暂时没有可选课程");return;}state.setText("选择一门课程生成学习路径");for(Map<String,Object> row:rows)addCourse(row);}@Override public void failure(String m){state.setText("课程加载失败："+m);}});}
    private void addCourse(Map<String,Object> row){Button button=new Button(this);button.setText(MapValues.text(row,"name",MapValues.text(row,"title","未命名课程"))+"\n"+MapValues.text(row,"description","进入课程后生成专属学习路径"));button.setTextColor(Color.rgb(27,29,42));button.setAllCaps(false);button.setGravity(17);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,12);list.addView(button,lp);button.setOnClickListener(v->generate(row));}
    private void generate(Map<String,Object> row){String id=MapValues.text(row,"id",null);if(id==null){state.setText("课程信息不完整");return;}state.setText("正在生成学习路径...");Map<String,Object> body=new HashMap<>();body.put("studentId",app.sessionManager().getUserId());body.put("courseId",id);NetworkCall.enqueue(app.apiClient().create(LearningPathApi.class).generate(body),new NetworkCall.Result<Map<String,Object>>(){@Override public void success(Map<String,Object> d){state.setText("学习路径已准备好");setResult(RESULT_OK);}@Override public void failure(String m){state.setText("生成失败："+m);}});}
    private TextView title(String s){TextView v=body(s);v.setTextSize(24);v.setTypeface(null, android.graphics.Typeface.BOLD);v.setTextColor(Color.rgb(27,29,42));return v;} private TextView body(String s){TextView v=new TextView(this);v.setText(s);v.setTextSize(14);v.setPadding(0,8,0,8);return v;}
}


