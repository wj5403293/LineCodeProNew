package cn.lineai.ui.component.toolcall;

import org.junit.Assert;
import org.junit.Test;
import org.json.JSONObject;

public final class ToolCallJsonFormatterTest {
    @Test
    public void prettyResultFormatsJsonObjectText() throws Exception {
        String formatted = ToolCallJsonFormatter.prettyResult("{\"ok\":true,\"count\":2}");
        JSONObject parsed = new JSONObject(formatted);

        Assert.assertTrue(parsed.getBoolean("ok"));
        Assert.assertEquals(2, parsed.getInt("count"));
        Assert.assertTrue(formatted.contains("\n"));
    }

    @Test
    public void prettyResultWrapsPlainTextAsJsonText() throws Exception {
        JSONObject parsed = new JSONObject(ToolCallJsonFormatter.prettyResult("plain output"));

        Assert.assertEquals("plain output", parsed.getString("text"));
    }

    @Test
    public void prettyResultUnwrapsMcpTextContentJsonString() throws Exception {
        String raw = "[{\"type\":\"text\",\"text\":\"{\\\"ok\\\":true,\\\"data\\\":{\\\"workspaceId\\\":\\\"3uqxvkm3\\\",\\\"editSessionId\\\":\\\"edab3860\\\"},\\\"error\\\":null,\\\"nextActions\\\":[]}\"}]";

        String formatted = ToolCallJsonFormatter.prettyResult(raw);
        JSONObject parsed = new JSONObject(formatted);

        Assert.assertTrue(parsed.getBoolean("ok"));
        Assert.assertEquals("3uqxvkm3", parsed.getJSONObject("data").getString("workspaceId"));
        Assert.assertEquals("edab3860", parsed.getJSONObject("data").getString("editSessionId"));
        Assert.assertTrue(parsed.isNull("error"));
        Assert.assertEquals(0, parsed.getJSONArray("nextActions").length());
        Assert.assertFalse(formatted.contains("\\\""));
    }

    @Test
    public void prettyResultDoesNotEscapePathSlashesInNestedMcpText() throws Exception {
        String raw = "[{\"type\":\"text\",\"text\":\"{\\\"ok\\\":true,\\\"prefix\\\":\\\"Lcom/dream/yx_ads/\\\"}\"}]";

        String formatted = ToolCallJsonFormatter.prettyResult(raw);
        JSONObject parsed = new JSONObject(formatted);

        Assert.assertEquals("Lcom/dream/yx_ads/", parsed.getString("prefix"));
        Assert.assertTrue(formatted.contains("Lcom/dream/yx_ads/"));
        Assert.assertFalse(formatted.contains("\\/"));
    }

    @Test
    public void prettyJsonNormalizesNestedJsonStringValues() throws Exception {
        JSONObject input = new JSONObject()
                .put("payload", "{\"name\":\"demo\",\"items\":[1,2]}")
                .put("plain", "value");

        String formatted = ToolCallJsonFormatter.prettyJson(input);
        JSONObject parsed = new JSONObject(formatted);

        Assert.assertEquals("demo", parsed.getJSONObject("payload").getString("name"));
        Assert.assertEquals(2, parsed.getJSONObject("payload").getJSONArray("items").length());
        Assert.assertEquals("value", parsed.getString("plain"));
        Assert.assertFalse(formatted.contains("\\\"name\\\""));
    }

    @Test
    public void prettyJsonDoesNotEscapePathSlashes() throws Exception {
        JSONObject input = new JSONObject()
                .put("prefix", "Lcom/dream/yx_ads/")
                .put("path", "/storage/emulated/0/Download");

        String formatted = ToolCallJsonFormatter.prettyJson(input);
        JSONObject parsed = new JSONObject(formatted);

        Assert.assertEquals("Lcom/dream/yx_ads/", parsed.getString("prefix"));
        Assert.assertEquals("/storage/emulated/0/Download", parsed.getString("path"));
        Assert.assertFalse(formatted.contains("\\/"));
    }

    @Test
    public void durationLabelFormatsMillisecondsAndSeconds() {
        Assert.assertEquals("12ms", ToolCallJsonFormatter.durationLabel(12L));
        Assert.assertEquals("1.23s", ToolCallJsonFormatter.durationLabel(1234L));
    }
}
