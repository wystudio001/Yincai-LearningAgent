package xyz.wystudio.yincai.ui.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.graphics.text.LineBreaker;
import android.view.View;
import android.widget.TextView;

/** Markdown 组件使用的 LineCode 视觉令牌。 */
public final class LineTheme {
    public static final int BG = Color.BLACK;
    public static final int SURFACE = Color.parseColor("#0A0A0A");
    public static final int SURFACE_ELEVATED = Color.parseColor("#141414");
    public static final int SURFACE_LIGHT = Color.parseColor("#1C1C1E");
    public static final int ACCENT = Color.parseColor("#30D158");
    public static final int USER_BUBBLE = Color.parseColor("#0A84FF");
    public static final int AI_BUBBLE = Color.parseColor("#1C1C1E");
    public static final int TEXT = Color.WHITE;
    public static final int TEXT_SECONDARY = Color.parseColor("#8E8E93");
    public static final int TEXT_TERTIARY = Color.parseColor("#636366");
    public static final int BORDER_LIGHT = Color.parseColor("#2C2C2E");
    public static final int BORDER = Color.parseColor("#1C1C1E");
    public static final int INPUT_BG = Color.parseColor("#1C1C1E");
    public static final int CODE_BG = Color.parseColor("#151515");
    public static final int CODE_BORDER = Color.parseColor("#2C2C2E");
    public static final int DANGER = Color.parseColor("#F85149");

    public static final int SM = 8;
    public static final int MD = 12;
    public static final int FONT_XS = 11;
    public static final int FONT_SM = 13;
    public static final int FONT_MD = 15;
    public static final int FONT_LG = 17;
    public static final int FONT_XL = 20;

    private LineTheme() { }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static GradientDrawable rounded(Context context, int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable roundedStroke(Context context, int color, float radiusDp, int strokeColor) {
        GradientDrawable drawable = rounded(context, color, radiusDp);
        drawable.setStroke(Math.max(1, dp(context, 1)), strokeColor);
        return drawable;
    }

    public static void padding(View view, int left, int top, int right, int bottom) {
        Context context = view.getContext();
        view.setPadding(dp(context, left), dp(context, top), dp(context, right), dp(context, bottom));
    }

    public static TextView text(Context context, String value, int sizeSp, int color, int style) {
        TextView textView = new TextView(context);
        textView.setText(value);
        textView.setTextColor(color);
        textView.setTextSize(sizeSp);
        textView.setIncludeFontPadding(false);
        textView.setLineSpacing(dp(context, 2), 1.0f);
        if (style != Typeface.NORMAL) textView.setTypeface(Typeface.DEFAULT, style);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            textView.setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE);
        }
        return textView;
    }
}
