package cn.lineai.mvp;

import org.junit.Assert;
import org.junit.Test;

public final class ModelStreamRetryPolicyTest {
    @Test
    public void retryNoticeShowsAttemptOutOfThree() {
        Assert.assertEquals(
                "连接中断，正在自动重试连接（第 2/3 次）…",
                ModelStreamRetryPolicy.retryNotice(2)
        );
    }

    @Test
    public void retriesTransientStreamFailuresOnlyUpToLimit() {
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("模型流式通信失败: socket reset", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("Connection timed out", 2));
        Assert.assertFalse(ModelStreamRetryPolicy.shouldRetry("模型流式通信失败: socket reset", 3));
        Assert.assertFalse(ModelStreamRetryPolicy.shouldRetry("HTTP 401: invalid api key", 0));
    }
}
