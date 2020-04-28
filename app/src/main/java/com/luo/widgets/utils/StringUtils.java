package com.luo.widgets.utils;


import android.graphics.Paint;
import android.util.Log;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lxg on 2020/4/28.
 */
public class StringUtils {

    static public final String UTF16LE = "UTF-16LE";
    static public final String UTF16BE = "UTF-16BE";
    static public final String UTF16 = "UTF-16";

    static public String punctuation="[_\\-`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

    static public boolean isNullOrEmpty(final String string) {
        return (string == null || string.trim().length() <= 0);
    }

    static public boolean isNullOrEmpty(final CharSequence string) {
        return (string == null || string.length() <= 0);
    }

    static public boolean isNotBlank(final String string) {
        return (string != null && string.trim().length() > 0);
    }

    static public boolean isBlank(final String string) {
        return !isNotBlank(string);
    }

    static public boolean isInteger(final String string) {
        if (isNullOrEmpty(string)) {
            return false;
        }
        String str = string;
        if (string.charAt(0) == '-') {
            if (string.length() <= 1) {
                return false;
            }
            str = string.substring(1, string.length() - 1);
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static public String utf16le(final byte [] data) {
        String string = "";
        if (data == null) {
            return string;
        }
        try {
            string = new String(data, UTF16LE);
        } catch (Exception e) {
            Log.w("", e);
        }
        return string;
    }

    static public String utf16(final byte [] data) {
        String string = "";
        try {
            string = new String(data, UTF16);
        } catch (Exception e) {
        }
        return string;
    }

    static public byte[] utf16leBuffer(final String text) {
        byte [] buffer = null;
        try {
            buffer = text.getBytes(UTF16LE);
        } catch (Exception e) {
        }
        return buffer;
    }

    public static String join(Iterable<?> elements, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object e : elements) {
            if (sb.length() > 0)
                sb.append(delimiter);
            sb.append(e);
        }
        return sb.toString();
    }

    public static List<String> split(final String string, final String delimiter) {
        if (isNullOrEmpty(string)) {
            return new ArrayList<String>();
        }
        final String[] result = string.split(delimiter);
        return Arrays.asList(result);
    }

    public static String deleteNewlineSymbol(String content){
        if (!isNullOrEmpty(content)){
            content = content.replaceAll("\r\n"," ").replaceAll("\n", " ");
        }
        return content;
    }

    public static String leftTrim(String content) {
        int start = 0, last = content.length() - 1;
        while ((start <= last) && (content.charAt(start) <= ' ')) {
            start++;
        }
        if (start == 0) {
            return content;
        }
        return content.substring(start, last + 1);
    }

    public static String rightTrim(String content) {
        int start = 0, last = content.length() - 1;
        int end = last;
        while ((end >= start) && (content.charAt(end) <= ' ')) {
            end--;
        }
        if (end == last) {
            return content;
        }
        return content.substring(start, end + 1);
    }

    public static String substring(String content, int beginIndex, int endIndex) {
        if (StringUtils.isNullOrEmpty(content)) {
            return "";
        }
        int count = content.codePointCount(0, content.length());
        if (endIndex > count) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            builder.appendCodePoint(content.codePointAt(i));
        }
        return builder.toString();
    }

    public static String trim(String input) {
        if (StringUtils.isNotBlank(input)) {
            input = input.trim();
            input = input.replace("\u0000", "");
            input = input.replace("\\u0000", "");
            input = input.replaceAll("\\u0000", ""); // removes NUL chars
            input = input.replaceAll("\\\\u0000", ""); // removes backslash+u0000
        }
        return input;
    }

    public static String trimTwo(String input) {
        if (StringUtils.isNotBlank(input)) {
            input = input.trim();
            input = input.replace("\u3000", "");
            input = input.replace("\u0000", "");
            input = input.replace("\\u0000", "");
            input = input.replaceAll("\\u0000", ""); // removes NUL chars
            input = input.replaceAll("\\\\u0000", ""); // removes backslash+u0000
        }
        return input;
    }

    public static String trimPunctuation(String input) {
        input = StringUtils.trim(input);
        if (StringUtils.isNullOrEmpty(input)) {
            return input;
        }

        int start = 0;
        while (start < input.length() - 1) {
            if (!punctuation.contains(String.valueOf(input.charAt(start)))) {
                break;
            }
            ++start;
        }

        int end = input.length() - 1;
        while (end >= 0) {
            if (!punctuation.contains(String.valueOf(input.charAt(end)))) {
                break;
            }
            --end;
        }
        input = input.substring(start, end + 1);
        return input;
    }

    public static boolean isAlpha(char ch) {
        /**
         * The following defines which characters are included in these sets. The values are Unicode code points.
         * - ALPHA
         *		- 0x0041 - 0x007A Basic Latin
         *		- 0x00C0 - 0x00D6 Latin-1 Supplement
         *		- 0x00D8 - 0x00F6 Latin-1 Supplement
         *		- 0x00F8 - 0x00FF Latin-1 Supplement
         *		- 0x0100 - 0x017F Latin Extended-A
         *		- 0x0180 - 0x024F Latin Extended-B
         *		- 0x0386          Greek
         *		- 0x0388 - 0x03FF Greek
         *		- 0x0400 - 0x0481 Cyrillic
         *		- 0x048A - 0x04FF Cyrillic
         *		- 0x0500 - 0x052F Cyrillic Supplement
         *		- 0x1E00 - 0x1EFF Latin Extended Additional
         */
        int codepoint = (int)ch;
        return (0x0041 <= codepoint && codepoint <= 0x007A) ||
                (0x00C0 <= codepoint && codepoint <= 0x00D6) ||
                (0x00D8 <= codepoint && codepoint <= 0x00F6) ||
                (0x00F8 <= codepoint && codepoint <= 0x00FF) ||
                (0x0100 <= codepoint && codepoint <= 0x017F) ||
                (0x0180 <= codepoint && codepoint <= 0x024F) ||
                (0x0386 == codepoint) ||
                (0x0388 <= codepoint && codepoint <= 0x03FF) ||
                (0x0400 <= codepoint && codepoint <= 0x0481) ||
                (0x048A <= codepoint && codepoint <= 0x04FF) ||
                (0x0500 <= codepoint && codepoint <= 0x052F) ||
                (0x1E00 <= codepoint && codepoint <= 0x1EFF);

    }

    public static boolean isUrl(String url) {
        return !isNullOrEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }

    public static String getBlankStr(String origin) {
        if (StringUtils.isNullOrEmpty(origin)) {
            return "";
        }
        return origin;
    }

    public static int getTextWidth(Paint paint, String str) {
        int resultWidth = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                resultWidth += (int) Math.ceil(widths[j]);
            }
        }
        return resultWidth;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
    }

    public static boolean isChinese(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (isChinese(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String getHtmlFormatString(String content) {
        if (content == null) {
            return null;
        }
        return content.replaceAll("\\<.*?>|\\n", "");
    }

    public static String removeBlank(String content) {
        if (isNullOrEmpty(content)) {
            return content;
        }
        return content.replaceAll("\\s*|\t|\r|\n", "");
    }

    public static String removeBlankCompatibleWithEnglish(String content) {
        if (content == null || content.length() == 0) {
            return content;
        }
        String replace = content.replaceAll("([a-zA-Z0-9])([\\s\\t\\r\\n]+)([a-zA-Z0-9])", "$1 $3");
        replace = replace.replaceAll("([^a-zA-Z0-9])([\\s\\t\\r\\n]+)([^a-zA-Z0-9])", "$1$3");
        return replace;
    }

    public static boolean isNull(String content) {
        return null == content;
    }

    public static boolean isPureSymbol(String content) {
        content = StringUtils.trim(content);
        if (StringUtils.isNullOrEmpty(content)) {
            return false;
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (!punctuation.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    public static int length(String content) {
        if (isNullOrEmpty(content)) {
            return 0;
        }
        return content.length();
    }

    public static String joinWithUnderLine(Object... args) {
        return join("_", args);
    }

    public static String join(String delimiter, Object... args) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1) {
                builder.append(delimiter);
            }
        }
        return builder.toString();
    }
}
