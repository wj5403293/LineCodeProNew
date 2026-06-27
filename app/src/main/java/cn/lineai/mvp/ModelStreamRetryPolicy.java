package cn.lineai.mvp;

import java.util.Locale;

final class ModelStreamRetryPolicy {
    static final int MAX_RETRIES = 3;

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
                || value.contains("http 408")
                || value.contains("http 409")
                || value.contains("http 425")
                || value.contains("http 429")
                || value.contains("http 500")
                || value.contains("http 502")
                || value.contains("http 503")
                || value.contains("http 504");
    }

    static String retryNotice(int retryAttempt) {
        int attempt = Math.max(1, Math.min(MAX_RETRIES, retryAttempt));
        return "连接中断，正在自动重试连接（第 " + attempt + "/" + MAX_RETRIES + " 次）…";
    }
}
