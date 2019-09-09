package com.onyx.jdread.tob.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.AlignmentSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.onyx.android.sdk.ui.utils.PageTurningDetector;
import com.onyx.android.sdk.ui.utils.PageTurningDirection;
import com.onyx.android.sdk.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lxg on 2018/6/6.
 */

public class JustifyPageTextView extends AppCompatTextView {
    private String skipIndexTextRegex = "( )*\\（\\d\\）";
    private OnPagingListener onPagingListener;
    private boolean canTouchPageTurning = true;
    private boolean pageTurningCycled = false;
    private boolean skipIndexText = true;
    private boolean justify = true;

    private CharSequence srcContent = null;
    private int currentPageNumber = 0;
    private int totalPageNumber = 0;
    private int mViewWidth;
    private int mLineY;
    private float lastX;
    private float lastY;
    private int page[];

    public interface OnPagingListener {
        void onPageChange(int currentPage, int totalPage);
    }

    public JustifyPageTextView(Context context) {
        super(context);
    }

    public JustifyPageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JustifyPageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getSkipIndexTextRegex() {
        return skipIndexTextRegex;
    }

    public void setSkipIndexTextRegex(String skipIndexTextRegex) {
        this.skipIndexTextRegex = skipIndexTextRegex;
    }

    public boolean isSkipIndexText() {
        return skipIndexText;
    }

    public boolean isPageTurningCycled() {
        return pageTurningCycled;
    }

    public void setPageTurningCycled(boolean pageTurningCycled) {
        this.pageTurningCycled = pageTurningCycled;
    }

    public void setOnPagingListener(OnPagingListener listener) {
        this.onPagingListener = listener;
    }

    public boolean isJustify() {
        return justify;
    }

    public void setJustify(boolean justify) {
        this.justify = justify;
    }

    public void setSkipIndexText(boolean skipIndexText) {
        this.skipIndexText = skipIndexText;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resize(changed);
    }

    public int resize(boolean changed) {
        CharSequence oldContent = getText();
        if (StringUtils.isNullOrEmpty(oldContent)) {
            this.srcContent = "";
            return -1;
        }
        if (srcContent == null || srcContent.length() <= 0) {
            getPage();
            srcContent = oldContent;
            gotoFirstPage();
        } else if (changed) {
            setText(srcContent);
            getPage();
            gotoFirstPage();
        }
        CharSequence newContent = oldContent.subSequence(0, getCharNum());
        return oldContent.length() - newContent.length();
    }

    public void reset(CharSequence srcContent) {
        getPage();
        this.srcContent = srcContent;
    }

    public int getCharNum() {
        return getLayout().getLineEnd(getLineNum());
    }

    public int getLineNum() {
        Layout layout = getLayout();
        int topOfLastLine = getHeight() - getPaddingTop() - getPaddingBottom() - getLineHeight();
        return layout.getLineForVertical(topOfLastLine);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                int direction = detectDirection(event);
                if (direction == PageTurningDirection.NEXT) {
                    if (canTouchPageTurning) {
                        nextPage();
                        return true;
                    }
                } else if (direction == PageTurningDirection.PREV) {
                    if (canTouchPageTurning) {
                        prevPage();
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void prevPage() {
        if (totalPageNumber <= 1) {
            return;
        }
        currentPageNumber--;
        if (currentPageNumber < 0) {
            if (pageTurningCycled) {
                currentPageNumber = totalPageNumber - 1;
            } else {
                currentPageNumber = 0;
            }
        }

        int start = page[currentPageNumber];
        int next = currentPageNumber + 1;
        int end = next < page.length ? page[next] : srcContent.length();
        setText(srcContent.subSequence(start, end));
        onPageChange(currentPageNumber);
    }

    private void gotoFirstPage() {
        int end = page.length > 1 ? page[1] : srcContent.length();
        setText(srcContent.subSequence(0, end));
        onPageChange(0);
    }

    public void nextPage() {
        if (totalPageNumber <= 1) {
            return;
        }
        currentPageNumber++;
        if (currentPageNumber >= totalPageNumber) {
            if (pageTurningCycled) {
                currentPageNumber = 0;
            } else {
                currentPageNumber = totalPageNumber - 1;
            }
        }
        int start = page[currentPageNumber];
        int next = currentPageNumber + 1;
        int end = next < page.length ? page[next] : srcContent.length();
        setText(srcContent.subSequence(start, end));
        onPageChange(currentPageNumber);
    }

    public int getCurrentPageNumber() {
        return currentPageNumber + 1;
    }

    public int getTotalPageNumber() {
        return totalPageNumber;
    }

    private void onPageChange(int currentPage) {
        if (onPagingListener != null) {
            onPagingListener.onPageChange(currentPage + 1, totalPageNumber);
        }
    }

    private int detectDirection(MotionEvent currentEvent) {
        return PageTurningDetector.detectBothAxisTuring(getContext(), (int) (currentEvent.getX() - lastX), (int) (currentEvent.getY() - lastY));
    }

    private int[] getPage() {
        int count = getLineCount();
        int pCount = getPageLineCount(this);
        totalPageNumber = count / pCount;
        if (count % pCount != 0) {
            totalPageNumber += 1;
        }
        if (totalPageNumber <= 0) {
            totalPageNumber = 1;
        }
        page = new int[totalPageNumber];
        page[0] = 0;
        for (int i = 0; i < totalPageNumber - 1; i++) {
            page[i + 1] = getLayout().getLineEnd((i + 1) * pCount - 1);
        }
        onPageChange(0);
        return page;
    }

    private int getPageLineCount(TextView view) {
        int h = view.getBottom() - view.getTop() - view.getPaddingTop();
        int firstH = getLineHeight(0, view);
        int otherH;
        if (getLineCount() > 1) {
            int line = findNotEmptyLine(1);
            if (line >= 1) {
                otherH = getLineHeight(line, view);
            } else {
                otherH = 1;
            }
        } else {
            otherH = 1;
        }
        int count = (h - firstH) / otherH + 1;
        if (count <= 0) {
            count = 1;
        }
        return count;
    }

    private int getLineHeight(int line, TextView view) {
        Rect rect = new Rect();
        view.getLineBounds(line, rect);
        return rect.bottom - rect.top;
    }

    private int findNotEmptyLine(int start) {
        Layout layout = getLayout();
        int line = getLineCount();
        if (start >= line) {
            return -1;
        }
        CharSequence temp;
        CharSequence text = getText();
        for (int i = start; i < line; i++) {
            temp = text.subSequence(layout.getLineStart(i), layout.getLineEnd(i));
            if (temp != null && temp.length() > 1) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth();
        CharSequence text = getText();
        mLineY = 0;
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
            Layout.Alignment alignment = Layout.Alignment.ALIGN_NORMAL;
            if (line instanceof AlignmentSpan) {
                alignment = ((AlignmentSpan) line).getAlignment();
            }
            StaticLayout staticLayout = new StaticLayout(line, paint, canvas.getWidth(), alignment, layout.getSpacingMultiplier(), layout.getSpacingAdd(), false);
            staticLayout.getHeight();
            if (isJustify() && ((currentPageNumber < totalPageNumber - 1) || (i < layout.getLineCount() - 1)) && needScale(line)) {
                drawScaledText(canvas, paint, lineStart, line, width, layout);
            } else {
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
            drawText(canvas, blanks, x, paint, layout);
            float bw = StaticLayout.getDesiredWidth(blanks, paint);
            x += bw;

            line = line.subSequence(blanks.length(), line.length());
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
