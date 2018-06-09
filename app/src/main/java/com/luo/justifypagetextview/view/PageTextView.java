package com.luo.justifypagetextview.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.luo.justifypagetextview.utils.PageTurningDetector;
import com.luo.justifypagetextview.utils.PageTurningDirection;

/**
 * Created by lxg on 2018/6/8.
 */


public class PageTextView extends AppCompatTextView {
    private OnPagingListener onPagingListener;
    private boolean canTouchPageTurning = true;
    private boolean pageTurningCycled = false;
    private CharSequence srcContent = null;
    private int currentPageNumber = 0;
    private int totalPageNumber = 0;
    private float lastX;
    private float lastY;
    private int page[];

    public interface OnPagingListener {
        void onPageChange(int currentPage, int totalPage);
    }

    public PageTextView(Context context) {
        super(context);
    }

    public PageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resize();
    }

    private int resize() {
        CharSequence oldContent = getText();
        if (srcContent == null || srcContent.length() <= 0) {
            getPage();
            srcContent = oldContent;
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
}
