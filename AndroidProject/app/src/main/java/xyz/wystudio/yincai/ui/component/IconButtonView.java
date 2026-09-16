package xyz.wystudio.yincai.ui.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageButton;

import xyz.wystudio.yincai.R;

/** Markdown 代码块使用的轻量图标按钮。 */
public final class IconButtonView extends ImageButton {
    public static final int COPY = 1;

    private int iconColor = Color.WHITE;

    public IconButtonView(Context context, int iconType) {
        super(context);
        setScaleType(ScaleType.FIT_CENTER);
        setAdjustViewBounds(false);
        setPadding(0, 0, 0, 0);
        setBackgroundColor(Color.TRANSPARENT);
        setClickable(true);
        setIconType(iconType);
    }

    public void setIconType(int iconType) {
        setImageResource(iconType == COPY ? R.drawable.ic_line_copy : R.drawable.ic_line_copy);
        applyColor();
    }

    public void setIconColor(int color) {
        iconColor = color;
        applyColor();
    }

    public void setIconPaddingDp(int left, int top, int right, int bottom) {
        float density = getResources().getDisplayMetrics().density;
        setPadding(Math.round(left * density), Math.round(top * density),
                Math.round(right * density), Math.round(bottom * density));
    }

    private void applyColor() {
        setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
    }
}
