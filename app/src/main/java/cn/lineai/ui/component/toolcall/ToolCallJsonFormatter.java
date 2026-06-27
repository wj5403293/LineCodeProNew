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
            return cleanDisplayJson(normalizeObject(input).toString(2));
        } catch (Exception ignored) {
            return cleanDisplayJson(input.toString());
        }
    }

    static String prettyResult(String content) {
        String value = content == null ? "" : content.trim();
        if (value.length() == 0) {
            return "{}";
        }
        try {
            if (value.startsWith("{")) {
                return cleanDisplayJson(normalizeObject(new JSONObject(value)).toString(2));
            }
            if (value.startsWith("[")) {
                Object unwrapped = unwrapMcpContentArray(new JSONArray(value));
                if (unwrapped instanceof JSONObject) {
                    return cleanDisplayJson(((JSONObject) unwrapped).toString(2));
                }
                if (unwrapped instanceof JSONArray) {
                    return cleanDisplayJson(((JSONArray) unwrapped).toString(2));
                }
                return cleanDisplayJson(normalizeArray(new JSONArray(value)).toString(2));
            }
        } catch (Exception ignored) {
        }
        try {
            return cleanDisplayJson(new JSONObject().put("text", content == null ? "" : content).toString(2));
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

    private static Object unwrapMcpContentArray(JSONArray array) throws Exception {
        if (array.length() != 1) {
            return normalizeArray(array);
        }
        JSONObject item = array.optJSONObject(0);
        if (item == null || !"text".equals(item.optString("type"))) {
            return normalizeArray(array);
        }
        String text = item.optString("text").trim();
        Object parsed = parseJsonText(text);
        if (parsed instanceof JSONObject) {
            return normalizeObject((JSONObject) parsed);
        }
        if (parsed instanceof JSONArray) {
            return normalizeArray((JSONArray) parsed);
        }
        return normalizeArray(array);
    }

    private static JSONObject normalizeObject(JSONObject source) throws Exception {
        JSONObject normalized = new JSONObject();
        JSONArray names = source.names();
        if (names == null) {
            return normalized;
        }
        for (int i = 0; i < names.length(); i++) {
            String key = names.getString(i);
            normalized.put(key, normalizeValue(source.opt(key)));
        }
        return normalized;
    }

    private static JSONArray normalizeArray(JSONArray source) throws Exception {
        JSONArray normalized = new JSONArray();
        for (int i = 0; i < source.length(); i++) {
            normalized.put(normalizeValue(source.opt(i)));
        }
        return normalized;
    }

    private static Object normalizeValue(Object value) throws Exception {
        if (value instanceof JSONObject) {
            return normalizeObject((JSONObject) value);
        }
        if (value instanceof JSONArray) {
            return normalizeArray((JSONArray) value);
        }
        if (value instanceof String) {
            Object parsed = parseJsonText(((String) value).trim());
            if (parsed instanceof JSONObject) {
                return normalizeObject((JSONObject) parsed);
            }
            if (parsed instanceof JSONArray) {
                return normalizeArray((JSONArray) parsed);
            }
        }
        return value;
    }

    private static Object parseJsonText(String text) {
        if (text == null || text.length() == 0) {
            return null;
        }
        try {
            if (text.startsWith("{")) {
                return new JSONObject(text);
            }
            if (text.startsWith("[")) {
                return new JSONArray(text);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String cleanDisplayJson(String json) {
        return json == null ? "" : json.replace("\\/", "/");
    }
}
