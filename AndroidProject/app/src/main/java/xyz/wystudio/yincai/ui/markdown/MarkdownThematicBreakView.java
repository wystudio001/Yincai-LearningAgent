package xyz.wystudio.yincai.ui.markdown;

import android.content.Context;
import android.view.View;
import xyz.wystudio.yincai.ui.theme.LineTheme;

public final class MarkdownThematicBreakView extends View {
    public MarkdownThematicBreakView(Context context) {
        super(context);
        setBackgroundColor(LineTheme.BORDER_LIGHT);
        setMinimumHeight(Math.max(1, LineTheme.dp(context, 1)));
    }
}
