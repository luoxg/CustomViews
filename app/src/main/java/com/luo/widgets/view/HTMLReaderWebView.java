package com.luo.widgets.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.luo.widgets.R;
import com.luo.widgets.utils.DimenUtils;
import com.luo.widgets.utils.PageTurningDetector;
import com.luo.widgets.utils.PageTurningDirection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Administrator on 2018/8/12.
 */
public class HTMLReaderWebView extends WebView {

    public static final  int    PAGE_TURN_TYPE_VERTICAL  = 1;
    public static final  int    PAGE_TURN_TYPE_HORIZOTAL = 2;
    public static final  float  TEXT_ZOOM_VALUE          = 1.2f;
    private static final String TAG                      = HTMLReaderWebView.class.getSimpleName();
    private int mCurrentPage;
    private int mTotalPage;
    private int pageTurnType = PAGE_TURN_TYPE_VERTICAL;
    private float lastX, lastY;
    private boolean loadCssStyle = true;

    private int heightForSaveView = 50;
    private int pageTurnThreshold = 300;
    private int marginTop         = 10;

    private boolean callParentPageFinishedMethod = true;
    private int     mInternalScrollX             = 0;
    private int     mInternalScrollY             = 0;
    private OnPageChangedListener mOnPageChangedListener;

    /**
     * @param context
     */
    public HTMLReaderWebView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

        setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setScroll(0, 0);
                mCurrentPage = 0;
                if (callParentPageFinishedMethod) {
                    super.onPageFinished(view, url);
                }

                final HTMLReaderWebView myWebView = (HTMLReaderWebView) view;

                applyCSS(myWebView);

            }
        });

        getSettings().setJavaScriptEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setDefaultTextEncodingName("UTF-8");
        getSettings().setBlockNetworkImage(false);
        int textZoom = getSettings().getTextZoom();
        int newTextZoom = (int) (textZoom * TEXT_ZOOM_VALUE);
        getSettings().setTextZoom(newTextZoom);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        setLongClickable(false);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getX();
                        lastY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        return true;
                    case MotionEvent.ACTION_UP:
                        int direction = detectDirection(event);
                        if (direction == PageTurningDirection.NEXT) {
                            nextPage();
                            return true;
                        } else if (direction == PageTurningDirection.PREV) {
                            prevPage();
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void setScroll(int l, int t) {
        mInternalScrollX = l;
        mInternalScrollY = t;
    }

    private void applyCSS(WebView webView) {
        if (!loadCssStyle) {
            return;
        }
        String varMySheet = "var mySheet = document.styleSheets[0];";

        String addCSSRule = "function addCSSRule(selector, newRule) {"
                + "ruleIndex = mySheet.cssRules.length;"
                + "mySheet.insertRule(selector + '{' + newRule + ';}', ruleIndex);"

                + "}";

        int width = webView.getMeasuredWidth();
        float fontSize = DimenUtils.getCssPx(getContext(), getResources().getDimension(R.dimen.control_panel_floating_tittle_text_size));
        String insertRule1 = "addCSSRule('html', '"
                + " -webkit-column-gap: 0px; -webkit-column-width: "
                + width + "px; margin-top:" + marginTop + "px;"
                + " line-height:130%; letter-spacing:2px; text-align:justify; font-size: " + fontSize + "px')";


        String css = varMySheet + addCSSRule + insertRule1;

        webView.loadUrl("javascript:" + css);
    }

    private int detectDirection(MotionEvent currentEvent) {
        return PageTurningDetector.detectBothAxisTuring(getContext(), (int) (currentEvent.getX() - lastX), (int) (currentEvent.getY() - lastY));
    }

    public void nextPage() {
        if (mCurrentPage < mTotalPage) {
            // EpdController.invalidate(webView, UpdateMode.GC);
            mCurrentPage++;
            setScroll(getScrollX() + getScrollWidth(), 0);
            scrollBy(getScrollWidth(), 0);
            if (mOnPageChangedListener != null)
                mOnPageChangedListener.onPageChanged(mTotalPage, mCurrentPage);
        }
    }

    public void prevPage() {
        if (mCurrentPage > 1) {
            // EpdController.invalidate(webView, UpdateMode.GC);
            mCurrentPage--;
            setScroll(getScrollX() - getScrollWidth(), 0);
            scrollBy(getScrollWidth(), 0);
            if (mOnPageChangedListener != null)
                mOnPageChangedListener.onPageChanged(mTotalPage, mCurrentPage);
        }
    }

    private int getScrollWidth() {
        return getMeasuredWidth();
    }

    /**
     * @param context
     * @param attrs
     */
    public HTMLReaderWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public HTMLReaderWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setPageTurnType(int pageTurnType) {
        this.pageTurnType = pageTurnType;
    }

    public void setHeightForSaveView(int heightForSaveView) {
        this.heightForSaveView = heightForSaveView;
    }

    public void setPageTurnThreshold(int pageTurnThreshold) {
        this.pageTurnThreshold = pageTurnThreshold;
    }

    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    public void computeScroll() {
        scrollTo(mInternalScrollX, mInternalScrollY);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(mInternalScrollX, mInternalScrollY);
    }

    @Override
    public void draw(Canvas canvas) {
        scrollTo(mInternalScrollX, mInternalScrollY);
        super.draw(canvas);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            nextPage();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            prevPage();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (refreshWebViewSize() || !loadCssStyle) {
            Log.d(TAG, "onDraw: ");
            super.onDraw(canvas);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        scrollTo(mInternalScrollX, mInternalScrollY);
        super.onScrollChanged(mInternalScrollX, mInternalScrollY, oldl, oldt);
    }

    private boolean refreshWebViewSize() {
        int width = getWidth();
        int scrollWidth = computeHorizontalScrollRange();
        if (width == 0) {
            return true;
        }

        mTotalPage = (scrollWidth + width) / width - 1;
        if (mTotalPage <= 0) {
            mTotalPage = 1;
        }
        if (mCurrentPage > mTotalPage) {
            mCurrentPage = mTotalPage;
        }

        if (mCurrentPage <= 0) {
            mCurrentPage = 1;
        }

        if (mOnPageChangedListener != null) {
            mOnPageChangedListener.onPageChanged(mTotalPage, mCurrentPage);
        }

        Log.e(TAG, "page: " + mCurrentPage + " / " + mTotalPage + " " + getContentHeight() + " " + scrollWidth + " view: " + getHeight());

        return mTotalPage >= 1 && getContentHeight() <= getHeight();
    }

    public void setLoadCssStyle(boolean loadCssStyle) {
        this.loadCssStyle = loadCssStyle;
    }

    public void setCallParentPageFinishedMethod(boolean call) {
        this.callParentPageFinishedMethod = call;
    }

    public void registerOnOnPageChangedListener(OnPageChangedListener l) {
        mOnPageChangedListener = l;
    }

    public void unRegisterOnOnPageChangedListener() {
        mOnPageChangedListener = null;
    }

    public String saveWebContentToFile(Context context, String expString) {

        String saveTempFile = getHtmlCacheDir(context) + "/result.html";

        OutputStreamWriter outputStreamWriter = null;
        File dirFile = new File(getHtmlCacheDir(context));
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File saveTemp = new File(saveTempFile);
        if (saveTemp.exists()) {
            saveTemp.delete();
        }
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(saveTemp));
            if (expString != null) {
                saveTemp.createNewFile();
                outputStreamWriter.write(expString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return saveTempFile;
    }

    public static String getHtmlCacheDir(Context context) {
        return "/data/data/" + context.getPackageName() + "/html";
    }

    interface OnPageChangedListener {
        void onPageChanged(int totalPage, int curPage);
    }

}