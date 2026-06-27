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
    public void durationLabelFormatsMillisecondsAndSeconds() {
        Assert.assertEquals("12ms", ToolCallJsonFormatter.durationLabel(12L));
        Assert.assertEquals("1.23s", ToolCallJsonFormatter.durationLabel(1234L));
    }
}
