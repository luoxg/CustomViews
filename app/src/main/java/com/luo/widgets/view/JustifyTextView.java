package com.luo.widgets.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lxg on 2018/6/9.
 */

public class JustifyTextView extends AppCompatTextView {
    private String skipIndexTextRegex = "\\（[a-zA-Z\\d]\\）|\\([a-zA-Z\\d]\\)";
    private boolean skipIndexText = true;
    private boolean justify = true;
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

    public boolean isJustify() {
        return justify;
    }

    public void setJustify(boolean justify) {
        this.justify = justify;
    }

    public boolean isSkipIndexText() {
        return skipIndexText;
    }

    public void setSkipIndexText(boolean skipIndexText) {
        this.skipIndexText = skipIndexText;
    }

    public String getSkipIndexTextRegex() {
        return skipIndexTextRegex;
    }

    public void setSkipIndexTextRegex(String skipIndexTextRegex) {
        this.skipIndexTextRegex = skipIndexTextRegex;
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
            Layout.Alignment alignment = getTextAlignment(line);
            StaticLayout staticLayout = new StaticLayout(line, paint, canvas.getWidth(), alignment, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            staticLayout.getHeight();
            if (isJustify() && (i < layout.getLineCount() - 1) && needScale(line)) {
                drawScaledText(canvas, paint, lineStart, line, width, layout);
            } else {
                // canvas.drawText(line, 0, mLineY, paint); 不考虑 Spannable可以使用canvas直接drawText
                float dx = getDxOfLineByAlignment(alignment, staticLayout.getWidth());
                canvas.translate(dx, mLineY);
                staticLayout.draw(canvas);
                canvas.translate(-dx, -mLineY);
            }
            mLineY += textHeight;
        }
    }
    /**
     * @param line
     * @return
     */
    private Layout.Alignment getTextAlignment(CharSequence line) {
        Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
        if (line == null || line.length() == 0) {
            return alignment;
        }
        if (line instanceof Spanned) {
            Spanned spanned = (Spanned) line;
            AlignmentSpan[] alignmentSpans = spanned.getSpans(0, line.length(), AlignmentSpan.class);
            for (AlignmentSpan span : alignmentSpans) {
                alignment = span.getAlignment();
            }
        }
        return alignment;
    }

    private float getDxOfLineByAlignment(Layout.Alignment alignment, int lineWidth) {
        float dx = 0;
        if (alignment == Layout.Alignment.ALIGN_CENTER) {
            dx = (mViewWidth - lineWidth) * 1.0f / 2;
        } else if (alignment == Layout.Alignment.ALIGN_OPPOSITE) {
            dx = mViewWidth - lineWidth;
        }
        return dx;
    }

    private void drawScaledText(Canvas canvas, TextPaint paint, int lineStart, CharSequence line, float lineWidth, Layout layout) {
        float x = 0;
        if (isFirstLineOfParagraph(lineStart, line)) {
            String blanks = "  ";
            drawText(canvas, blanks, x, paint, layout);
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.subSequence(3, line.length());
        }

        if (isSkipIndexText() && !TextUtils.isEmpty(getSkipIndexTextRegex())) {
            CharSequence skipIndexText = findSkipIndexText(line);
            if (!TextUtils.isEmpty(skipIndexText)) {
                int length = skipIndexText.length();
                drawText(canvas, skipIndexText, x, paint, layout);
                float sw = StaticLayout.getDesiredWidth(skipIndexText, paint);
                x += sw;
                line = line.subSequence(length, line.length());
            }
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288 && line.charAt(1) == 12288) {
            CharSequence subSequence = line.subSequence(0, 2);
            drawText(canvas, subSequence, x, paint, layout);
            float cw = StaticLayout.getDesiredWidth(subSequence, paint);
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            CharSequence subSequence = line.subSequence(i, i + 1);
            String c = String.valueOf(line.charAt(i));
            drawText(canvas, subSequence, x, paint, layout);
            float cw = StaticLayout.getDesiredWidth(c, paint);
            x += cw + d;
        }
    }

    private void drawText(Canvas canvas, CharSequence content, float x, TextPaint paint, Layout layout) {
        StaticLayout staticLayout = new StaticLayout(content, paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
        canvas.translate(x, mLineY);
        staticLayout.draw(canvas);
        canvas.translate(-x, -mLineY);
    }

    private CharSequence findSkipIndexText(CharSequence line) {
        if (line.length() == 0) {
            return null;
        }
        Pattern pattern = Pattern.compile(getSkipIndexTextRegex());
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return line.subSequence(0, matcher.end());
        }
        return null;
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
