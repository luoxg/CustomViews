package com.luo.widgets.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;

/**
 * Created by lxg on 2018/6/9.
 */

public class JustifyTextView extends AppCompatTextView {

    private int mViewWidth;
    private int mLineY;


    public JustifyTextView(Context context) {
        super(context);
    }

    public JustifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JustifyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth();
        CharSequence text = getText();
        mLineY = 0;
        //mLineY += getTextSize();
        Layout layout = getLayout();
        // layout.getLayout() occur NullPointerException in 4.4.3
        if (layout == null) {
            return;
        }
        Paint.FontMetrics fm = paint.getFontMetrics();

        int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
        textHeight = (int) (textHeight * layout.getSpacingMultiplier() + layout.getSpacingAdd());
        //Solved the problem of excessive space between the last line of text
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            CharSequence line = text.subSequence(lineStart, lineEnd);
            StaticLayout staticLayout = new StaticLayout(line, paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            staticLayout.getHeight();
            if ((i < layout.getLineCount() - 1) && needScale(line)) {
                drawScaledText(canvas, paint, lineStart, line, width, layout);
            } else {
                // canvas.drawText(line, 0, mLineY, paint); 不考虑 Spannable可以使用canvas直接drawText
                canvas.translate(0, mLineY);
                staticLayout.draw(canvas);
                canvas.translate(0, -mLineY);
            }
            mLineY += textHeight;
        }
    }

    private void drawScaledText(Canvas canvas, TextPaint paint, int lineStart, CharSequence line, float lineWidth, Layout layout) {
        float x = 0;
        if (isFirstLineOfParagraph(lineStart, line)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.subSequence(3, line.length());
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288 && line.charAt(1) == 12288) {
            CharSequence substring = line.subSequence(0, 2);
            StaticLayout staticLayout = new StaticLayout(substring, paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            float cw = StaticLayout.getDesiredWidth(substring, paint);
            canvas.translate(x, mLineY);
            staticLayout.draw(canvas);
            canvas.translate(-x, -mLineY);
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            CharSequence subSequence = line.subSequence(i, i + 1);
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            StaticLayout staticLayout = new StaticLayout(subSequence, paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            canvas.translate(x, mLineY);
            staticLayout.draw(canvas);
            canvas.translate(-x, -mLineY);
            x += cw + d;
        }
    }

    private boolean isFirstLineOfParagraph(int lineStart, CharSequence line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    private boolean needScale(CharSequence line) {
        if (line == null || line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }
}
