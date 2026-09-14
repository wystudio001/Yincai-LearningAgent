package xyz.wystudio.yincai.feature.main;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.feature.auth.AuthActivity;
public class MineFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_mine, c, false);
        YinCaiApplication app = (YinCaiApplication) requireActivity().getApplication();
        ((android.widget.TextView) v.findViewById(R.id.text_mine_username)).setText(app.sessionManager().getUsername());
        ((Button) v.findViewById(R.id.button_logout)).setOnClickListener(x -> {
            app.sessionManager().clear();
            startActivity(new Intent(requireContext(), AuthActivity.class));
            requireActivity().finish();
        });
        return v;
    }
}
