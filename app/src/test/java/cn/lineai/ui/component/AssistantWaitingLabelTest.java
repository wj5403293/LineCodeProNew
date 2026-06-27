package cn.lineai.ui.component;

import org.junit.Assert;
import org.junit.Test;

public final class AssistantWaitingLabelTest {
    @Test
    public void formatsThinkingElapsedSeconds() {
        Assert.assertEquals("正在思考中（用时 0 秒）", AssistantWaitingLabel.format(1000L, 1999L));
        Assert.assertEquals("正在思考中（用时 2 秒）", AssistantWaitingLabel.format(1000L, 3100L));
    }

    @Test
    public void unknownStartTimeUsesZeroSeconds() {
        Assert.assertEquals("正在思考中（用时 0 秒）", AssistantWaitingLabel.format(0L, 3100L));
    }
}
