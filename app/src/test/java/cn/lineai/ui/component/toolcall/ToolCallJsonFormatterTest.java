package cn.lineai.ui.component.toolcall;

import org.junit.Assert;
import org.junit.Test;

public final class ToolCallJsonFormatterTest {
    @Test
    public void prettyResultFormatsJsonObjectText() {
        Assert.assertEquals(
                "{\n  \"ok\": true,\n  \"count\": 2\n}",
                ToolCallJsonFormatter.prettyResult("{\"ok\":true,\"count\":2}")
        );
    }

    @Test
    public void prettyResultWrapsPlainTextAsJsonText() {
        Assert.assertEquals(
                "{\n  \"text\": \"plain output\"\n}",
                ToolCallJsonFormatter.prettyResult("plain output")
        );
    }

    @Test
    public void durationLabelFormatsMillisecondsAndSeconds() {
        Assert.assertEquals("12ms", ToolCallJsonFormatter.durationLabel(12L));
        Assert.assertEquals("1.23s", ToolCallJsonFormatter.durationLabel(1234L));
    }
}
