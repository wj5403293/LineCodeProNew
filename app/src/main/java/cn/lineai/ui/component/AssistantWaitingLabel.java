package cn.lineai.ui.component;

final class AssistantWaitingLabel {
    private AssistantWaitingLabel() {
    }

    static String format(long streamStartedAtMs, long nowMs) {
        long elapsedMs = streamStartedAtMs <= 0L ? 0L : Math.max(0L, nowMs - streamStartedAtMs);
        long seconds = elapsedMs / 1000L;
        return "正在思考中（用时 " + seconds + " 秒）";
    }
}
