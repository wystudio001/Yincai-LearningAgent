package xyz.wystudio.yincai.ui.theme;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import com.google.android.material.textfield.TextInputEditText;

import xyz.wystudio.yincai.R;

/** Shared LineCode-style building blocks for screens outside the tutor. */
public final class LineUi {
    private LineUi() {}

    public static LinearLayout page(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(LineTheme.BG);
        return root;
    }

    public static ScrollView scrollPage(Context context) {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(LineTheme.BG);
        return scroll;
    }

    public static LinearLayout content(Context context) {
        LinearLayout content = page(context);
        content.setPadding(LineTheme.dp(context, 20), LineTheme.dp(context, 18),
                LineTheme.dp(context, 20), LineTheme.dp(context, 28));
        return content;
    }

    public static LinearLayout toolbar(Context context, String title, View.OnClickListener back) {
        LinearLayout bar = new LinearLayout(context);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(LineTheme.dp(context, 8), LineTheme.dp(context, 8),
                LineTheme.dp(context, 12), LineTheme.dp(context, 8));
        bar.setMinimumHeight(LineTheme.dp(context, 56));
        bar.setBackgroundColor(LineTheme.SURFACE);
        if (back != null) {
            TextView button = text(context, "‹", 32, LineTheme.TEXT, Typeface.NORMAL);
            button.setGravity(Gravity.CENTER);
            button.setContentDescription("返回");
            button.setBackground(null);
            button.setForeground(selectable(context));
            button.setOnClickListener(back);
            bar.addView(button, new LinearLayout.LayoutParams(LineTheme.dp(context, 42), LineTheme.dp(context, 42)));
        }
        TextView heading = text(context, title, LineTheme.FONT_LG, LineTheme.TEXT, Typeface.BOLD);
        heading.setPadding(LineTheme.dp(context, 8), 0, 0, 0);
        bar.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
        return bar;
    }

    public static View divider(Context context) {
        View divider = new View(context);
        divider.setBackgroundColor(LineTheme.BORDER);
        divider.setLayoutParams(new LinearLayout.LayoutParams(-1, LineTheme.dp(context, 1)));
        return divider;
    }

    public static TextView section(Context context, String title) {
        TextView label = text(context, title.toUpperCase(), LineTheme.FONT_XS, LineTheme.TEXT_SECONDARY, Typeface.BOLD);
        label.setLetterSpacing(0.06f);
        label.setPadding(LineTheme.dp(context, 4), LineTheme.dp(context, 22), LineTheme.dp(context, 4), LineTheme.dp(context, 8));
        return label;
    }

    public static LinearLayout group(Context context) {
        LinearLayout group = page(context);
        group.setBackground(LineTheme.rounded(context, LineTheme.SURFACE, 8));
        group.setClipToOutline(true);
        return group;
    }

    public static LinearLayout row(Context context, String title, String subtitle, View.OnClickListener click) {
        return row(context, title, subtitle, click, true);
    }

    public static LinearLayout row(Context context, String title, String subtitle, View.OnClickListener click, boolean arrow) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(LineTheme.dp(context, subtitle == null ? 54 : 68));
        row.setPadding(LineTheme.dp(context, 16), LineTheme.dp(context, 10), LineTheme.dp(context, 12), LineTheme.dp(context, 10));
        row.setBackgroundColor(LineTheme.SURFACE);
        if (click != null) {
            row.setClickable(true);
            row.setFocusable(true);
            row.setForeground(selectable(context));
            row.setOnClickListener(click);
        }
        LinearLayout labels = page(context);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        TextView main = text(context, title, LineTheme.FONT_MD, LineTheme.TEXT, Typeface.NORMAL);
        labels.addView(main);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(context, subtitle, LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY, Typeface.NORMAL);
            sub.setPadding(0, LineTheme.dp(context, 4), 0, 0);
            labels.addView(sub);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        if (arrow) {
            TextView chevron = text(context, "›", 25, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
            chevron.setGravity(Gravity.CENTER);
            row.addView(chevron, new LinearLayout.LayoutParams(LineTheme.dp(context, 24), -1));
        }
        return row;
    }

    public static LinearLayout rowWithValue(Context context, String title, String subtitle, String currentValue, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(LineTheme.dp(context, subtitle == null ? 54 : 68));
        row.setPadding(LineTheme.dp(context, 16), LineTheme.dp(context, 10), LineTheme.dp(context, 8), LineTheme.dp(context, 10));
        row.setBackgroundColor(LineTheme.SURFACE);
        row.setClickable(click != null);
        row.setFocusable(click != null);
        if (click != null) {
            row.setForeground(selectable(context));
            row.setOnClickListener(click);
        }
        LinearLayout labels = page(context);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.addView(text(context, title, LineTheme.FONT_MD, LineTheme.TEXT, Typeface.NORMAL));
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView sub = text(context, subtitle, LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY, Typeface.NORMAL);
            sub.setPadding(0, LineTheme.dp(context, 4), 0, 0);
            labels.addView(sub);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView current = value(context, currentValue);
        current.setMaxLines(2);
        row.addView(current, new LinearLayout.LayoutParams(LineTheme.dp(context, 128), -1));
        TextView chevron = text(context, "›", 25, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        chevron.setGravity(Gravity.CENTER);
        row.addView(chevron, new LinearLayout.LayoutParams(LineTheme.dp(context, 24), -1));
        return row;
    }

    public static TextView value(Context context, String value) {
        TextView view = text(context, value, LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY, Typeface.NORMAL);
        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        return view;
    }

    public static TextView text(Context context, String value, int size, int color, int style) {
        return LineTheme.text(context, value, size, color, style);
    }

    private static Drawable selectable(Context context) {
        return context.getDrawable(android.R.drawable.list_selector_background);
    }

    /** Applies the shared palette to legacy/programmatic screens without changing their behavior. */
    public static void styleScreen(View root) {
        if (root == null) return;
        root.setBackgroundColor(LineTheme.BG);
        styleChildren(root);
    }

    private static void styleChildren(View view) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            if (!(text instanceof Button)) text.setTextColor(LineTheme.TEXT);
            if (text instanceof TextInputEditText) {
                // Material TextInputLayout owns the field background, stroke and padding.
                text.setTextColor(LineTheme.TEXT);
                text.setHintTextColor(LineTheme.TEXT_SECONDARY);
            } else if (text instanceof EditText) {
                text.setTextColor(LineTheme.TEXT);
                text.setHintTextColor(LineTheme.TEXT_SECONDARY);
                text.setBackgroundResource(R.drawable.bg_line_input);
            } else if (text instanceof Button) {
                text.setTextColor(LineTheme.TEXT);
                text.setBackgroundResource(R.drawable.bg_line_row);
                text.setAllCaps(false);
                text.setMinHeight(LineTheme.dp(text.getContext(), 48));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) styleChildren(group.getChildAt(i));
        }
    }
}
