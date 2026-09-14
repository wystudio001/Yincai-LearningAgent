package xyz.wystudio.yincai.feature.tutor;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import xyz.wystudio.yincai.feature.main.TutorEntryFragment;

/** 独立打开时也复用首页的 LineCode 风格辅导工作台。 */
public final class TutorChatActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setId(View.generateViewId());
        setContentView(root);

        if (savedInstanceState == null) {
            String sessionId = getIntent().getStringExtra("sessionId");
            String draftMessage = getIntent().getStringExtra("draftMessage");
            getSupportFragmentManager().beginTransaction()
                    .replace(root.getId(), TutorEntryFragment.newInstance(sessionId, draftMessage))
                    .commit();
        }
    }
}
