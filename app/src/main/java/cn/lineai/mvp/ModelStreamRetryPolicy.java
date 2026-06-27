package cn.lineai.mvp;

import java.util.Locale;
import java.util.regex.Pattern;

final class ModelStreamRetryPolicy {
    static final int MAX_RETRIES = 3;
    private static final String RETRY_NOTICE_PREFIX = "连接中断，正在自动重试连接";
    private static final Pattern TRANSIENT_HTTP_STATUS = Pattern.compile(
            "(^|[^0-9])(408|409|425|429|500|502|503|504)([^0-9]|$)"
    );

    private ModelStreamRetryPolicy() {
    }

    static boolean shouldRetry(String message, int completedRetries) {
        if (completedRetries >= MAX_RETRIES) {
            return false;
        }
        String value = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (value.contains("http 400")
                || value.contains("http 401")
                || value.contains("http 403")
                || value.contains("invalid api key")
                || value.contains("unauthorized")
                || value.contains("forbidden")
                || value.contains("内容安全")
                || value.contains("本地 gguf")) {
            return false;
        }
        return value.contains("模型通信失败")
                || value.contains("模型流式通信失败")
                || value.contains("timeout")
                || value.contains("timed out")
                || value.contains("socket")
                || value.contains("connection")
                || value.contains("reset")
                || value.contains("refused")
                || value.contains("gateway timeout")
                || value.contains("gateway time-out")
                || value.contains("upstream timeout")
                || TRANSIENT_HTTP_STATUS.matcher(value).find();
    }

    static String retryNotice(int retryAttempt) {
        int attempt = Math.max(1, Math.min(MAX_RETRIES, retryAttempt));
        return "连接中断，正在自动重试连接（第 " + attempt + "/" + MAX_RETRIES + " 次）…";
    }

    static String retryNoticeContent(String currentContent, String notice) {
        String base = stripRetryNotice(currentContent);
        String retryNotice = notice == null ? "" : notice;
        if (base.trim().length() == 0) {
            return retryNotice;
        }
        if (retryNotice.length() == 0) {
            return base;
        }
        return base + "\n\n" + retryNotice;
    }

    static String visibleTextBeforeRetryNotice(String content) {
        return stripRetryNotice(content);
    }

    static String mergeRetryText(String previousVisibleText, String retryText) {
        String previous = stripRetryNotice(previousVisibleText);
        String retry = retryText == null ? "" : retryText;
        if (previous.length() == 0) {
            return retry;
        }
        if (retry.length() == 0) {
            return previous;
        }
        if (retry.startsWith(previous)) {
            return retry;
        }
        if (previous.startsWith(retry)) {
            return previous;
        }
        int overlap = suffixPrefixOverlap(previous, retry);
        if (overlap > 0) {
            return previous + retry.substring(overlap);
        }
        return previous + retry;
    }

    static String failureContent(String currentContent, String failureText) {
        String base = stripRetryNotice(currentContent);
        String failure = failureText == null ? "" : failureText;
        if (base.trim().length() == 0) {
            return failure;
        }
        if (failure.trim().length() == 0) {
            return base;
        }
        return base + "\n\n" + failure;
    }

    private static String stripRetryNotice(String content) {
        String value = content == null ? "" : content;
        int separatedNoticeIndex = value.lastIndexOf("\n\n" + RETRY_NOTICE_PREFIX);
        if (separatedNoticeIndex >= 0) {
            return value.substring(0, separatedNoticeIndex);
        }
        return value.startsWith(RETRY_NOTICE_PREFIX) ? "" : value;
    }

    private static int suffixPrefixOverlap(String left, String right) {
        int max = Math.min(left.length(), right.length());
        for (int size = max; size > 0; size--) {
            if (left.regionMatches(left.length() - size, right, 0, size)) {
                return size;
            }
        }
        return 0;
    }
}
