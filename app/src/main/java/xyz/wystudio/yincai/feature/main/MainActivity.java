package xyz.wystudio.yincai.feature.main;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import xyz.wystudio.yincai.R;

public final class MainActivity extends AppCompatActivity {
    private Fragment activeFragment;
    private int activeItemId = R.id.nav_home;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnItemSelectedListener(this::selectTab);
        int restoredId = savedInstanceState == null
                ? R.id.nav_home : savedInstanceState.getInt("selected_tab", R.id.nav_home);
        MenuItem restoredItem = navigation.getMenu().findItem(restoredId);
        navigation.setSelectedItemId(restoredItem == null ? R.id.nav_home : restoredId);
    }
    private boolean selectTab(@NonNull MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.nav_path) {
            fragment = new LearningPathFragment();
        } else if (itemId == R.id.nav_resources) {
            fragment = new ResourceCenterFragment();
        } else if (itemId == R.id.nav_tutor) {
            fragment = new HomeFragment();
        } else if (itemId == R.id.nav_mine) {
            fragment = new MineFragment();
        } else {
            fragment = new TutorEntryFragment();
        }
        FragmentManager manager = getSupportFragmentManager();
        String tag = "main_tab_" + itemId;
        Fragment existing = manager.findFragmentByTag(tag);
        if (existing != null) fragment = existing;
        androidx.fragment.app.FragmentTransaction transaction = manager.beginTransaction();
        for (Fragment candidate : manager.getFragments()) {
            if (candidate != fragment && candidate != null && candidate.isAdded()) transaction.hide(candidate);
        }
        if (existing == null) transaction.add(R.id.main_container, fragment, tag);
        else transaction.show(fragment);
        transaction.commit();
        activeFragment = fragment;
        activeItemId = itemId;
        return true;
    }

    @Override protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("selected_tab", activeItemId);
        super.onSaveInstanceState(outState);
    }

    public void openTab(int itemId) {
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setSelectedItemId(itemId);
    }
}
