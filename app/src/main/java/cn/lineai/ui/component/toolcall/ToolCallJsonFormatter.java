package cn.lineai.ui.component.toolcall;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 工具调用 JSON 格式化工具：将 JSONObject 转换为带缩进的可读字符串，用于卡片输入展示。
 */
final class ToolCallJsonFormatter {
    private ToolCallJsonFormatter() {
    }

    static String prettyJson(JSONObject input) {
        if (input == null) {
            return "{}";
        }
        try {
            return input.toString(2);
        } catch (Exception ignored) {
            return input.toString();
        }
    }

    static String prettyResult(String content) {
        String value = content == null ? "" : content.trim();
        if (value.length() == 0) {
            return "{}";
        }
        try {
            if (value.startsWith("{")) {
                return new JSONObject(value).toString(2);
            }
            if (value.startsWith("[")) {
                return new JSONArray(value).toString(2);
            }
        } catch (Exception ignored) {
        }
        try {
            return new JSONObject().put("text", content == null ? "" : content).toString(2);
        } catch (Exception ignored) {
            return "{\"text\":\"\"}";
        }
    }

    static String durationLabel(long durationMs) {
        long value = Math.max(0L, durationMs);
        if (value < 1000L) {
            return value + "ms";
        }
        long whole = value / 1000L;
        long hundredths = (value % 1000L) / 10L;
        return whole + "." + (hundredths < 10L ? "0" : "") + hundredths + "s";
    }
}
