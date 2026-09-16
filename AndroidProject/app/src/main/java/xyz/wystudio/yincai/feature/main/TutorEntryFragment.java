package xyz.wystudio.yincai.feature.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.wystudio.yincai.R;
import xyz.wystudio.yincai.YinCaiApplication;
import xyz.wystudio.yincai.core.common.ApiResponse;
import xyz.wystudio.yincai.core.common.MapValues;
import xyz.wystudio.yincai.core.network.NetworkCall;
import xyz.wystudio.yincai.data.remote.TutorApi;
import xyz.wystudio.yincai.ui.markdown.MarkdownLinkHandler;
import xyz.wystudio.yincai.ui.markdown.MarkdownView;
import xyz.wystudio.yincai.ui.theme.LineTheme;

/** LineCode 风格的学生端学习导师工作台。 */
public final class TutorEntryFragment extends Fragment {
    private static final int AI_SURFACE = LineTheme.AI_BUBBLE;
    private static final int USER_BLUE = LineTheme.USER_BUBBLE;
    private static final int TEXT = LineTheme.TEXT;
    private static final int SECONDARY = LineTheme.TEXT_SECONDARY;
    private static final int TERTIARY = LineTheme.TEXT_TERTIARY;
    private static final int ACCENT = LineTheme.ACCENT;
    private static final int DANGER = LineTheme.DANGER;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService streamExecutor = Executors.newCachedThreadPool();
    private final Runnable thinkingAnimator = new Runnable() {
        @Override public void run() {
            if (thinkingLabel == null || !waitingForReply) return;
            thinkingPhase = (thinkingPhase + 1) % 4;
            StringBuilder dots = new StringBuilder("思考中");
            for (int i = 0; i < thinkingPhase; i++) dots.append('.');
            thinkingLabel.setText("✦  " + dots);
            mainHandler.postDelayed(this, 420);
        }
    };

    private LinearLayout messages;
    private ScrollView scroll;
    private EditText input;
    private TextView state;
    private TextView conversationTitle;
    private TextView thinkingLabel;
    private View thinkingRow;
    private ImageButton sendButton;
    private YinCaiApplication app;
    private String sessionId;
    private Call<ResponseBody> activeCall;
    private PopupWindow conversationDrawer;
    private boolean waitingForReply;
    private int thinkingPhase;
    private LinearLayout streamingRow;
    private MarkdownView streamingMarkdown;
    private StringBuilder streamingContent;
    private StringBuilder streamingReasoning;
    private String streamingMessageId;
    private boolean streamingActionsAdded;
    private volatile boolean userStopped;
    private volatile boolean streamFinished;
    private TextView thinkingPreview;
    private TextView streamingThinkingContent;
    private TextView streamingThinkingHeader;
    private TextView streamingThinkingChevron;
    private View streamingThinkingBlock;

    public static TutorEntryFragment newInstance(String sessionId, String draftMessage) {
        TutorEntryFragment fragment = new TutorEntryFragment();
        Bundle args = new Bundle();
        if (sessionId != null) args.putString("sessionId", sessionId);
        if (draftMessage != null) args.putString("draftMessage", draftMessage);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutor, container, false);
        app = (YinCaiApplication) requireActivity().getApplication();
        messages = view.findViewById(R.id.tutor_history_content);
        scroll = view.findViewById(R.id.tutor_scroll);
        input = view.findViewById(R.id.input_tutor_message);
        state = view.findViewById(R.id.text_tutor_state);
        conversationTitle = view.findViewById(R.id.text_tutor_title);
        sendButton = view.findViewById(R.id.button_tutor_send);

        sendButton.setOnClickListener(v -> {
            if (waitingForReply) stopReply(); else sendMessage();
        });
        view.findViewById(R.id.button_tutor_attach).setOnClickListener(v ->
                Toast.makeText(requireContext(), "可以直接描述你想学习的内容", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.button_tutor_menu).setOnClickListener(v -> showConversationPicker());
        view.findViewById(R.id.button_tutor_new).setOnClickListener(v -> newConversation());
        view.findViewById(R.id.button_tutor_more).setOnClickListener(v -> showMoreActions());
        view.findViewById(R.id.button_tutor_title).setOnClickListener(v -> showConversationPicker());
        view.findViewById(R.id.button_tutor_question_one).setOnClickListener(v -> useSuggestion("帮我梳理一个知识点"));
        view.findViewById(R.id.button_tutor_question_two).setOnClickListener(v -> useSuggestion("根据我的学习路径安排复习"));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || isPlainEnterDown(event)) {
                if (!waitingForReply) sendMessage();
                return true;
            }
            return false;
        });
        input.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && isPlainEnterDown(event)) {
                if (!waitingForReply) sendMessage();
                return true;
            }
            return false;
        });
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateSendButton(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        showWelcome();
        updateSendButton();
        Bundle args = getArguments();
        sessionId = args == null ? null : args.getString("sessionId");
        String draft = args == null ? null : args.getString("draftMessage");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            createSession(draft);
        } else {
            state.setText("在线");
            loadHistory();
            if (draft != null && !draft.trim().isEmpty()) {
                input.setText(draft);
                input.setSelection(input.length());
            }
        }
        return view;
    }

    @Override public void onDestroyView() {
        userStopped = true;
        mainHandler.removeCallbacks(thinkingAnimator);
        if (activeCall != null) activeCall.cancel();
        if (conversationDrawer != null) conversationDrawer.dismiss();
        super.onDestroyView();
    }

    @Override public void onDestroy() {
        streamExecutor.shutdownNow();
        super.onDestroy();
    }

    private TutorApi api() { return app.apiClient().create(TutorApi.class); }

    private void showWelcome() {
        messages.removeAllViews();
        LinearLayout welcome = new LinearLayout(requireContext());
        welcome.setOrientation(LinearLayout.VERTICAL);
        welcome.setGravity(Gravity.CENTER_HORIZONTAL);
        welcome.setPadding(dp(20), dp(54), dp(20), dp(28));
        TextView mark = text("✦", 34, ACCENT, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        welcome.addView(mark, new LinearLayout.LayoutParams(-1, -2));
        TextView title = text("今天想学点什么？", 24, TEXT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        welcome.addView(title, new LinearLayout.LayoutParams(-1, -2));
        TextView desc = text("把问题交给因材导师，一起拆解、练习并找到下一步。", 14, SECONDARY, Typeface.NORMAL);
        desc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(10);
        welcome.addView(desc, descParams);
        messages.addView(welcome, new LinearLayout.LayoutParams(-1, -2));
    }

    private void createSession(String draft) {
        state.setText("连接中");
        Map<String, Object> body = new HashMap<>();
        body.put("title", "学习辅导");
        NetworkCall.enqueue(api().createSession(body), new NetworkCall.Result<Map<String, Object>>() {
            @Override public void success(Map<String, Object> data) {
                if (!isAdded()) return;
                sessionId = data == null ? null : MapValues.text(data, "id", null);
                if (conversationTitle != null) conversationTitle.setText(MapValues.text(data, "title", "因材导师"));
                state.setText(sessionId == null ? "暂时离线" : "在线");
                if (draft != null && !draft.trim().isEmpty()) {
                    input.setText(draft);
                    input.setSelection(input.length());
                    sendMessage();
                }
            }
            @Override public void failure(String message) { if (isAdded()) state.setText("暂时离线"); }
        });
    }

    private void newConversation() {
        if (waitingForReply) stopReply();
        sessionId = null;
        if (conversationTitle != null) conversationTitle.setText("因材导师");
        showWelcome();
        input.setText("");
        createSession(null);
    }

    private void useSuggestion(String suggestion) {
        input.setText(suggestion);
        input.setSelection(input.length());
        input.requestFocus();
    }

    private void sendMessage() {
        String value = input.getText() == null ? "" : input.getText().toString().trim();
        if (value.isEmpty() || waitingForReply) return;
        if (sessionId == null) {
            state.setText("正在连接导师...");
            return;
        }
        addBubble(value, true, "");
        input.setText("");
        userStopped = false;
        streamingRow = null;
        streamingMarkdown = null;
        streamingContent = new StringBuilder();
        streamingReasoning = new StringBuilder();
        streamingMessageId = null;
        streamingActionsAdded = false;
        streamFinished = false;
        streamingThinkingContent = null;
        streamingThinkingHeader = null;
        setWaiting(true);
        Map<String, Object> body = new HashMap<>();
        body.put("content", value);
        body.put("message", value);
        activeCall = api().streamMessage(sessionId, body);
        activeCall.enqueue(new Callback<ResponseBody>() {
            @Override public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded() || call.isCanceled()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> finishStreamError("暂时无法获取回答，请稍后再试。"));
                    return;
                }
                try {
                    streamExecutor.execute(() -> consumeSse(call, response.body()));
                } catch (Exception error) {
                    mainHandler.post(() -> finishStreamError("暂时无法获取回答，请稍后再试。"));
                }
            }

            @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                if (!isAdded() || call.isCanceled() || userStopped) return;
                mainHandler.post(() -> finishStreamError("暂时无法获取回答，请稍后再试。"));
            }
        });
    }

    private void stopReply() {
        userStopped = true;
        if (activeCall != null) activeCall.cancel();
        setWaiting(false);
        if (streamingContent == null || streamingContent.length() == 0) {
            addBubble("已停止生成。", false, "");
        } else if (streamingRow != null && !streamingActionsAdded && streamingMessageId != null) {
            addActionBar(streamingRow, streamingMessageId, streamingContent.toString());
            streamingActionsAdded = true;
        }
        state.setText("在线");
    }

    private void consumeSse(Call<ResponseBody> call, ResponseBody body) {
        String eventType = "";
        StringBuilder data = new StringBuilder();
        try (ResponseBody ignored = body;
             BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean terminalEvent = false;
            while (!call.isCanceled() && !userStopped && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (data.length() > 0) terminalEvent = processSseEvent(eventType, data.toString());
                    data.setLength(0);
                    eventType = "";
                    if (terminalEvent) break;
                    continue;
                }
                if (line.startsWith("event:")) {
                    eventType = line.substring("event:".length()).trim();
                } else if (line.startsWith("data:")) {
                    if (data.length() > 0) data.append('\n');
                    String value = line.substring("data:".length());
                    data.append(value.startsWith(" ") ? value.substring(1) : value);
                }
            }
            if (!call.isCanceled() && !userStopped && !terminalEvent && data.length() > 0) {
                terminalEvent = processSseEvent(eventType, data.toString());
            }
            if (!call.isCanceled() && !userStopped && !terminalEvent) {
                mainHandler.post(() -> finishStreamDone(null, null, null));
            }
        } catch (Exception error) {
            if (!call.isCanceled() && !userStopped) {
                mainHandler.post(() -> finishStreamError("流式连接中断，请稍后重试。"));
            }
        }
    }

    private boolean processSseEvent(String eventType, String rawData) {
        String data = rawData == null ? "" : rawData.trim();
        if (data.isEmpty() || "[DONE]".equals(data)) return "[DONE]".equals(data);
        try {
            JsonObject object = JsonParser.parseString(data).getAsJsonObject();
            String type = eventType == null || eventType.isEmpty() ? object.has("type") ? object.get("type").getAsString() : "" : eventType;
            if ("start".equals(type)) {
                String id = object.has("messageId") ? object.get("messageId").getAsString() : "";
                mainHandler.post(() -> streamingMessageId = id);
            } else if ("delta".equals(type)) {
                String delta = object.has("delta") ? object.get("delta").getAsString() : "";
                if (!delta.isEmpty()) mainHandler.post(() -> appendStreamingDelta(delta));
            } else if ("reasoning".equals(type)) {
                String delta = object.has("delta") ? object.get("delta").getAsString() : "";
                if (!delta.isEmpty()) mainHandler.post(() -> appendStreamingReasoning(delta));
            } else if ("done".equals(type)) {
                String id = object.has("messageId") ? object.get("messageId").getAsString() : null;
                String content = object.has("content") ? object.get("content").getAsString() : null;
                String reasoning = object.has("reasoning") ? object.get("reasoning").getAsString() : null;
                mainHandler.post(() -> finishStreamDone(id, content, reasoning));
                return true;
            } else if ("error".equals(type)) {
                String message = object.has("message") ? object.get("message").getAsString() : "暂时无法获取回答，请稍后再试。";
                mainHandler.post(() -> finishStreamError(message));
                return true;
            }
        } catch (Exception ignored) {
            // 忽略无法解析的 SSE 注释或供应商扩展事件，继续读取后续增量。
        }
        return false;
    }

    private void appendStreamingDelta(String delta) {
        if (!isAdded() || getView() == null || userStopped) return;
        if (streamingContent == null) streamingContent = new StringBuilder();
        if (streamingRow == null) createStreamingBubble();
        streamingContent.append(delta);
        if (streamingMarkdown != null) streamingMarkdown.setMarkdown(streamingContent.toString());
        if (scroll != null) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void appendStreamingReasoning(String delta) {
        if (!isAdded() || getView() == null || userStopped) return;
        if (streamingReasoning == null) streamingReasoning = new StringBuilder();
        streamingReasoning.append(delta);
        if (thinkingPreview != null) thinkingPreview.setText(streamingReasoning.toString());
        if (streamingRow == null) createStreamingBubble();
        ensureStreamingThinkingBlock();
        if (streamingThinkingContent != null) streamingThinkingContent.setText(streamingReasoning.toString());
        if (streamingThinkingHeader != null) streamingThinkingHeader.setText("思考中");
        if (scroll != null) scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void finishStreamDone(String messageId, String finalContent, String finalReasoning) {
        if (!isAdded() || getView() == null || userStopped || streamFinished) return;
        if (finalReasoning != null && !finalReasoning.isEmpty()
                && (streamingReasoning == null || !finalReasoning.equals(streamingReasoning.toString()))) {
            if (streamingReasoning == null) streamingReasoning = new StringBuilder();
            streamingReasoning.setLength(0);
            streamingReasoning.append(finalReasoning);
            ensureStreamingThinkingBlock();
            if (streamingThinkingContent != null) streamingThinkingContent.setText(finalReasoning);
        }
        boolean hasExistingContent = streamingContent != null && streamingContent.length() > 0;
        boolean emptyFallback = "暂时没有生成回答".equals(finalContent);
        if ((!hasExistingContent && (finalContent == null || finalContent.trim().isEmpty())) || emptyFallback) {
            finishStreamError("智能体没有返回内容，请检查后端大模型配置或稍后重试。");
            return;
        }
        streamFinished = true;
        if (messageId != null && !messageId.isEmpty()) streamingMessageId = messageId;
        if (finalContent != null && !finalContent.isEmpty()
                && (streamingContent == null || !finalContent.equals(streamingContent.toString()))) {
            if (streamingContent == null) streamingContent = new StringBuilder();
            streamingContent.setLength(0);
            if (streamingRow == null) createStreamingBubble();
            streamingContent.append(finalContent);
            if (streamingMarkdown != null) streamingMarkdown.setMarkdown(streamingContent.toString());
        }
        if (streamingThinkingHeader != null && streamingReasoning != null && streamingReasoning.length() > 0) {
            streamingThinkingHeader.setText("思考过程");
        }
        if (streamingRow != null && !streamingActionsAdded && streamingMessageId != null && !streamingMessageId.isEmpty()) {
            addActionBar(streamingRow, streamingMessageId, streamingContent.toString());
            streamingActionsAdded = true;
        }
        setWaiting(false);
        activeCall = null;
        state.setText("在线");
    }

    private void finishStreamError(String message) {
        if (!isAdded() || getView() == null || userStopped || streamFinished) return;
        streamFinished = true;
        setWaiting(false);
        if (streamingContent == null || streamingContent.length() == 0) {
            addBubble(message, false, "");
        } else if (streamingRow != null && !streamingActionsAdded
                && streamingMessageId != null && !streamingMessageId.isEmpty()) {
            addActionBar(streamingRow, streamingMessageId, streamingContent.toString());
            streamingActionsAdded = true;
        }
        activeCall = null;
        state.setText("连接异常");
    }

    private void createStreamingBubble() {
        hideThinkingRow();
        streamingRow = new LinearLayout(requireContext());
        streamingRow.setOrientation(LinearLayout.VERTICAL);
        streamingRow.setGravity(Gravity.START);
        streamingRow.setPadding(0, dp(4), 0, dp(4));
        streamingMarkdown = assistantMarkdown();
        streamingMarkdown.setPadding(dp(16), dp(12), dp(16), dp(12));
        streamingMarkdown.setBackground(bubbleBackground(false));
        int maxBubbleWidth = (int) (getResources().getDisplayMetrics().widthPixels * .84f);
        if (streamingReasoning != null && streamingReasoning.length() > 0) {
            ensureStreamingThinkingBlock();
        }
        streamingRow.addView(streamingMarkdown, new LinearLayout.LayoutParams(maxBubbleWidth, -2));
        messages.addView(streamingRow, new LinearLayout.LayoutParams(-1, -2));
    }

    /** Creates the compact, collapsed-by-default LineCode thinking block above the answer. */
    private void ensureStreamingThinkingBlock() {
        if (streamingRow == null || streamingThinkingBlock != null
                || streamingReasoning == null || streamingReasoning.length() == 0) return;
        LinearLayout block = createThinkingBlock(streamingReasoning.toString(), true);
        streamingThinkingBlock = block;
        streamingThinkingHeader = (TextView) block.getTag(R.id.tag_thinking_header);
        streamingThinkingContent = (TextView) block.getTag(R.id.tag_thinking_content);
        streamingThinkingChevron = (TextView) block.getTag(R.id.tag_thinking_chevron);
        streamingRow.addView(block, 0, new LinearLayout.LayoutParams(-1, -2));
    }

    private LinearLayout createThinkingBlock(String reasoning, boolean streaming) {
        LinearLayout block = new LinearLayout(requireContext());
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(16), dp(4), dp(16), dp(4));

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setClickable(true);
        TextView mark = text("✦", 11, TERTIARY, Typeface.NORMAL);
        header.addView(mark, new LinearLayout.LayoutParams(-2, -2));
        TextView label = text(streaming ? "思考中" : "思考过程", 12, TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-2, -2);
        labelParams.leftMargin = dp(4);
        header.addView(label, labelParams);
        TextView chevron = text("›", 18, TERTIARY, Typeface.NORMAL);
        chevron.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        chevronParams.leftMargin = dp(3);
        header.addView(chevron, chevronParams);
        TextView content = text(reasoning == null ? "" : reasoning, 12, TERTIARY, Typeface.NORMAL);
        content.setLineSpacing(dp(3), 1f);
        content.setVisibility(View.GONE);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(-1, -2);
        contentParams.topMargin = dp(6);

        header.setOnClickListener(v -> {
            boolean expanded = content.getVisibility() != View.VISIBLE;
            content.setVisibility(expanded ? View.VISIBLE : View.GONE);
            chevron.setText(expanded ? "⌄" : "›");
        });
        block.addView(header, new LinearLayout.LayoutParams(-1, -2));
        block.addView(content, contentParams);
        block.setTag(R.id.tag_thinking_header, label);
        block.setTag(R.id.tag_thinking_content, content);
        block.setTag(R.id.tag_thinking_chevron, chevron);
        return block;
    }

    private void setWaiting(boolean waiting) {
        waitingForReply = waiting;
        input.setEnabled(!waiting);
        requireView().findViewById(R.id.button_tutor_attach).setEnabled(!waiting);
        if (waiting) {
            state.setText("思考中");
            showThinkingRow();
            mainHandler.removeCallbacks(thinkingAnimator);
            thinkingPhase = 0;
            mainHandler.post(thinkingAnimator);
        } else {
            hideThinkingRow();
            mainHandler.removeCallbacks(thinkingAnimator);
        }
        updateSendButton();
    }

    private void updateSendButton() {
        if (sendButton == null || input == null) return;
        boolean hasContent = input.getText() != null && input.getText().toString().trim().length() > 0;
        if (waitingForReply) {
            sendButton.setImageResource(R.drawable.ic_line_stop);
            sendButton.setBackground(round(DANGER, 20));
            sendButton.setColorFilter(TEXT);
            sendButton.setEnabled(true);
            sendButton.setAlpha(1f);
            return;
        }
        sendButton.setImageResource(R.drawable.ic_line_arrow_up);
        sendButton.setBackground(round(hasContent ? ACCENT : AI_SURFACE, 20));
        sendButton.setColorFilter(hasContent ? TEXT : TERTIARY);
        sendButton.setEnabled(hasContent);
        sendButton.setAlpha(hasContent ? 1f : 0.72f);
    }

    private void showThinkingRow() {
        if (thinkingRow != null) return;
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(4), dp(8), dp(4), dp(12));
        thinkingLabel = text("✦  思考中", 12, TERTIARY, Typeface.NORMAL);
        row.addView(thinkingLabel, new LinearLayout.LayoutParams(-1, -2));
        thinkingPreview = text("…", 16, TEXT, Typeface.NORMAL);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-2, -2);
        previewParams.topMargin = dp(4);
        row.addView(thinkingPreview, previewParams);
        thinkingRow = row;
        messages.addView(row, new LinearLayout.LayoutParams(-1, -2));
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void hideThinkingRow() {
        if (thinkingRow != null && thinkingRow.getParent() instanceof ViewGroup) {
            ((ViewGroup) thinkingRow.getParent()).removeView(thinkingRow);
        }
        thinkingRow = null;
        thinkingLabel = null;
        thinkingPreview = null;
    }

    private void loadHistory() {
        if (sessionId == null) return;
        NetworkCall.enqueue(api().messages(sessionId), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) {
                if (!isAdded() || data == null || data.isEmpty()) return;
                messages.removeAllViews();
                for (Map<String, Object> row : data) {
                    String role = MapValues.text(row, "role", "assistant");
                    addBubble(MapValues.text(row, "content", ""), "user".equalsIgnoreCase(role),
                            MapValues.text(row, "id", ""), MapValues.text(row, "reasoningContent",
                                    MapValues.text(row, "reasoning", "")));
                }
                state.setText("在线");
            }
            @Override public void failure(String message) { if (isAdded()) state.setText("历史消息读取失败"); }
        });
    }

    private void showConversationPicker() {
        state.setText("读取会话列表");
        NetworkCall.enqueue(api().sessions(), new NetworkCall.Result<List<Map<String, Object>>>() {
            @Override public void success(List<Map<String, Object>> data) {
                if (!isAdded()) return;
                showConversationDrawer(data);
                state.setText("在线");
            }
            @Override public void failure(String message) { if (isAdded()) Toast.makeText(requireContext(), "会话列表暂时无法读取", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void showConversationDrawer(List<Map<String, Object>> data) {
        if (conversationDrawer != null) conversationDrawer.dismiss();
        LinearLayout drawer = new LinearLayout(requireContext());
        drawer.setOrientation(LinearLayout.VERTICAL);
        drawer.setPadding(dp(18), dp(18), dp(14), dp(18));
        drawer.setBackgroundColor(AI_SURFACE);

        LinearLayout header = new LinearLayout(requireContext());
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("对话", 22, TEXT, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView close = text("×", 30, SECONDARY, Typeface.NORMAL);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(v -> conversationDrawer.dismiss());
        header.addView(close, new LinearLayout.LayoutParams(dp(36), dp(36)));
        drawer.addView(header, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView create = text("＋  新建对话", 15, LineTheme.TEXT_ON_COLOR, Typeface.BOLD);
        create.setGravity(Gravity.CENTER_VERTICAL);
        create.setPadding(dp(14), 0, dp(14), 0);
        create.setBackground(round(ACCENT, 18));
        create.setOnClickListener(v -> {
            conversationDrawer.dismiss();
            newConversation();
        });
        LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(-1, dp(44));
        createParams.topMargin = dp(14);
        drawer.addView(create, createParams);

        View divider = new View(requireContext());
        divider.setBackgroundColor(LineTheme.BORDER);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dp(1));
        dividerParams.topMargin = dp(18);
        drawer.addView(divider, dividerParams);

        ScrollView listScroll = new ScrollView(requireContext());
        LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL);
        List<Map<String, Object>> rows = data == null ? java.util.Collections.emptyList() : data;
        if (rows.isEmpty()) {
            TextView empty = text("还没有其他对话", 14, SECONDARY, Typeface.NORMAL);
            empty.setPadding(dp(4), dp(20), dp(4), dp(20));
            list.addView(empty);
        } else {
            for (Map<String, Object> row : rows) {
                String id = MapValues.text(row, "id", null);
                if (id == null) continue;
                TextView item = text(MapValues.text(row, "title", "学习辅导"), 15, TEXT, Typeface.NORMAL);
                item.setGravity(Gravity.CENTER_VERTICAL);
                item.setSingleLine(true);
                item.setEllipsize(android.text.TextUtils.TruncateAt.END);
                item.setPadding(dp(12), 0, dp(12), 0);
                item.setBackground(round(id.equals(sessionId) ? LineTheme.SURFACE_LIGHT : Color.TRANSPARENT, 12));
                item.setOnClickListener(v -> {
                    conversationDrawer.dismiss();
                    if (conversationTitle != null) conversationTitle.setText(MapValues.text(row, "title", "学习辅导"));
                    openConversation(id);
                });
                LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(-1, dp(48));
                itemParams.topMargin = dp(4);
                list.addView(item, itemParams);
            }
        }
        listScroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        drawer.addView(listScroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        conversationDrawer = new PopupWindow(drawer, dp(318), -1, true);
        conversationDrawer.setBackgroundDrawable(new ColorDrawable(AI_SURFACE));
        conversationDrawer.setOutsideTouchable(true);
        conversationDrawer.setFocusable(true);
        conversationDrawer.setElevation(dp(12));
        conversationDrawer.setAnimationStyle(android.R.style.Animation_Dialog);
        conversationDrawer.showAtLocation(requireView(), Gravity.START, 0, 0);
    }

    private void openConversation(String id) {
        if (waitingForReply) stopReply();
        sessionId = id;
        if (conversationTitle != null && conversationTitle.getText().toString().trim().isEmpty()) {
            conversationTitle.setText("学习辅导");
        }
        showWelcome();
        loadHistory();
    }

    private void showMoreActions() {
        String[] actions = {"新建对话", "重新加载当前对话", "清空当前显示"};
        new AlertDialog.Builder(requireContext()).setTitle("更多").setItems(actions, (dialog, which) -> {
            if (which == 0) newConversation();
            else if (which == 1) { showWelcome(); loadHistory(); }
            else if (which == 2) showWelcome();
        }).show();
    }

    private void addBubble(String value, boolean mine, String messageId) {
        addBubble(value, mine, messageId, "");
    }

    private void addBubble(String value, boolean mine, String messageId, String reasoning) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(mine ? Gravity.END : Gravity.START);
        row.setPadding(0, dp(4), 0, dp(4));
        View bubble;
        if (mine) {
            TextView userBubble = text(value, 15, TEXT, Typeface.NORMAL);
            userBubble.setPadding(dp(16), dp(12), dp(16), dp(12));
            userBubble.setBackground(bubbleBackground(true));
            bubble = userBubble;
        } else {
            if (reasoning != null && !reasoning.trim().isEmpty()) {
                row.addView(createThinkingBlock(reasoning, false), new LinearLayout.LayoutParams(-1, -2));
            }
            MarkdownView markdown = assistantMarkdown();
            markdown.setPadding(dp(16), dp(12), dp(16), dp(12));
            markdown.setBackground(bubbleBackground(false));
            markdown.setMarkdown(value == null ? "" : value);
            bubble = markdown;
        }
        int maxBubbleWidth = (int) (getResources().getDisplayMetrics().widthPixels * .84f);
        if (bubble instanceof TextView) {
            ((TextView) bubble).setMaxWidth(maxBubbleWidth);
        }
        row.addView(bubble, new LinearLayout.LayoutParams(
                bubble instanceof TextView ? -2 : maxBubbleWidth,
                -2));
        if (!mine && messageId != null && !messageId.isEmpty()) addActionBar(row, messageId, value);
        messages.addView(row, new LinearLayout.LayoutParams(-1, -2));
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private MarkdownView assistantMarkdown() {
        MarkdownView markdown = new MarkdownView(requireContext());
        markdown.setCodeWrapEnabled(true);
        markdown.setLinkHandler(new MarkdownLinkHandler() {
            @Override public void onOpenUrl(String url) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                    Toast.makeText(requireContext(), "无法打开链接", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return markdown;
    }

    private void addActionBar(LinearLayout row, String messageId, String value) {
        LinearLayout actions = new LinearLayout(requireContext());
        actions.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        TextView copy = text("复制", 11, TERTIARY, Typeface.NORMAL);
        copy.setPadding(0, dp(5), dp(12), dp(4));
        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("assistant", value));
            Toast.makeText(requireContext(), "已复制", Toast.LENGTH_SHORT).show();
        });
        TextView helpful = text("有帮助", 11, TERTIARY, Typeface.NORMAL);
        helpful.setPadding(0, dp(5), 0, dp(4));
        helpful.setOnClickListener(v -> {
            Map<String, Object> body = new HashMap<>();
            body.put("helpful", true);
            helpful.setText("已反馈");
            helpful.setEnabled(false);
            NetworkCall.enqueue(api().feedback(messageId, body), new NetworkCall.Result<Void>() {
                @Override public void success(Void ignored) { }
                @Override public void failure(String message) { helpful.setText("有帮助"); helpful.setEnabled(true); }
            });
        });
        actions.addView(copy);
        actions.addView(helpful);
        row.addView(actions, new LinearLayout.LayoutParams(-2, -2));
    }

    private GradientDrawable bubbleBackground(boolean mine) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(mine ? USER_BLUE : AI_SURFACE);
        float large = dp(18), small = dp(5);
        drawable.setCornerRadii(mine ? new float[]{large, large, large, large, small, small, large, large}
                : new float[]{large, large, large, large, large, large, small, small});
        return drawable;
    }

    private GradientDrawable round(int color, float radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp((int) radius));
        return drawable;
    }

    private TextView text(String value, int size, int color, int style) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        view.setIncludeFontPadding(false);
        view.setLineSpacing(dp(2), 1f);
        return view;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private boolean isPlainEnterDown(KeyEvent event) {
        return event != null
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                && !event.isShiftPressed();
    }
}
