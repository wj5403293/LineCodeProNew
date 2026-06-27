package cn.lineai.tool;

import org.junit.Assert;
import org.junit.Test;

public final class ToolResultTest {
    @Test
    public void durationSurvivesCallDiffAndReviewCopies() {
        ToolResult result = new ToolResult("old", "old_tool", "ok", false)
                .withDurationMs(1234L)
                .withCall("call_1", "mcpx_demo")
                .withDiffId("diff_1")
                .withReview("accepted", "approved");

        Assert.assertEquals(1234L, result.getDurationMs());
        Assert.assertEquals("call_1", result.getToolCallId());
        Assert.assertEquals("mcpx_demo", result.getToolName());
        Assert.assertEquals("diff_1", result.getDiffId());
        Assert.assertEquals("accepted", result.getReviewState());
        Assert.assertEquals("approved", result.getReviewMessage());
    }
}
