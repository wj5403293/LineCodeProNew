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
    public void retryNoticeContentAppendsToPartialAssistantText() {
        String content = ModelStreamRetryPolicy.retryNoticeContent(
                "现在修改 `YxAdsPluginDelegate` 的构造方法，让它只做基本初始化，不",
                ModelStreamRetryPolicy.retryNotice(1)
        );

        Assert.assertTrue(content.startsWith("现在修改 `YxAdsPluginDelegate`"));
        Assert.assertTrue(content.contains("正在自动重试连接"));
    }

    @Test
    public void failureContentAppendsToPartialAssistantTextAfterRetryStops() {
        String partial = ModelStreamRetryPolicy.retryNoticeContent("已有半截回答", ModelStreamRetryPolicy.retryNotice(3));

        String content = ModelStreamRetryPolicy.failureContent(partial, "模型通信失败：\nsocket reset");

        Assert.assertEquals("已有半截回答\n\n模型通信失败：\nsocket reset", content);
    }

    @Test
    public void mergeRetryTextKeepsPartialBaseWhileRestartedAttemptCatchesUp() {
        Assert.assertEquals(
                "abcdef",
                ModelStreamRetryPolicy.mergeRetryText("abcdef", "abc")
        );
    }

    @Test
    public void mergeRetryTextUsesRestartedAttemptAfterItExtendsBase() {
        Assert.assertEquals(
                "abcdefghi",
                ModelStreamRetryPolicy.mergeRetryText("abcdef", "abcdefghi")
        );
    }

    @Test
    public void mergeRetryTextAppendsResumedAttemptWithoutDuplicateOverlap() {
        Assert.assertEquals(
                "abcdefghi",
                ModelStreamRetryPolicy.mergeRetryText("abcdef", "defghi")
        );
    }

    @Test
    public void retriesTransientStreamFailuresOnlyUpToLimit() {
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("模型流式通信失败: socket reset", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("Connection timed out", 2));
        Assert.assertFalse(ModelStreamRetryPolicy.shouldRetry("模型流式通信失败: socket reset", 3));
        Assert.assertFalse(ModelStreamRetryPolicy.shouldRetry("HTTP 401: invalid api key", 0));
    }

    @Test
    public void retriesGatewayTimeoutMessagesShownToUser() {
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("504 Gateway Timeout", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("Gateway Timeout (504)", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("status=504 upstream timeout", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("HTTP/1.1 504 Gateway Time-out", 0));
        Assert.assertTrue(ModelStreamRetryPolicy.shouldRetry("模型通信失败：\n504", 0));
    }
}
