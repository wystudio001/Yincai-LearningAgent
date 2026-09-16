package xyz.wystudio.yincai.feature.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.feature.auth.AuthActivity;
import xyz.wystudio.yincai.feature.profile.ProfileActivity;
import xyz.wystudio.yincai.feature.notification.NotificationActivity;
import xyz.wystudio.yincai.feature.settings.SettingsActivity;
import xyz.wystudio.yincai.feature.assessment.AssessmentReportActivity;
import xyz.wystudio.yincai.feature.exercise.WrongQuestionsActivity;
import xyz.wystudio.yincai.feature.profile.ProfileDialogueActivity;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.AuthApi;

public class MineFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mine,container,false); YinCaiApplication app=(YinCaiApplication)requireActivity().getApplication(); String username=app.sessionManager().getUsername(); ((TextView)view.findViewById(R.id.text_mine_username)).setText(username==null?"学生":username);
        view.findViewById(R.id.button_mine_profile).setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileActivity.class)));
        view.findViewById(R.id.button_mine_profile_dialogue).setOnClickListener(v -> startActivity(new Intent(requireContext(), ProfileDialogueActivity.class)));
        view.findViewById(R.id.button_mine_assessment).setOnClickListener(v -> startActivity(new Intent(requireContext(), AssessmentReportActivity.class)));
        view.findViewById(R.id.button_mine_wrong_questions).setOnClickListener(v -> startActivity(new Intent(requireContext(), WrongQuestionsActivity.class)));
        view.findViewById(R.id.button_mine_notifications).setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationActivity.class)));
        view.findViewById(R.id.button_mine_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
        Button logout = view.findViewById(R.id.button_logout);
        logout.setOnClickListener(v -> {
            logout.setEnabled(false);
            NetworkCall.enqueue(app.apiClient().authApi().logout(), new NetworkCall.Result<Void>() {
                @Override public void success(Void ignored) { completeLogout(app); }
                @Override public void failure(String ignored) { completeLogout(app); }
            });
        });
        return view;
    }
    private void completeLogout(YinCaiApplication app) {
        if (!isAdded()) return;
        app.sessionManager().clear();
        startActivity(new Intent(requireContext(), AuthActivity.class));
        requireActivity().finish();
    }
    private void showMessage(String message){android.widget.Toast.makeText(requireContext(),message,android.widget.Toast.LENGTH_SHORT).show();}
}
