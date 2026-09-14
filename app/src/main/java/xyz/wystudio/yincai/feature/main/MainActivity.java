package xyz.wystudio.yincai.feature.main;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import xyz.wystudio.yincai.R;

public final class MainActivity extends AppCompatActivity {
    private TextView titleView;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        titleView = findViewById(R.id.text_toolbar_title);
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnItemSelectedListener(this::selectTab);
        if (savedInstanceState == null) navigation.setSelectedItemId(R.id.nav_home);
    }
    private boolean selectTab(@NonNull MenuItem item) {
        Fragment fragment;
        String title;
        switch (item.getItemId()) {
            case R.id.nav_path: fragment = new LearningPathFragment(); title = "学习路径"; break;
            case R.id.nav_resources: fragment = new ResourceCenterFragment(); title = "资源中心"; break;
            case R.id.nav_tutor: fragment = new TutorEntryFragment(); title = "智能辅导"; break;
            case R.id.nav_mine: fragment = new MineFragment(); title = "我的"; break;
            default: fragment = new HomeFragment(); title = "首页";
        }
        titleView.setText(title);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, fragment).commit();
        return true;
    }
}
