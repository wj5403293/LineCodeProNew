package cn.lineai.tool;

public final class ToolResult {
    private final String toolCallId;
    private final String toolName;
    private final String content;
    private final boolean error;
    private final String diffId;
    private final String reviewState;
    private final String reviewMessage;
    private final long durationMs;

    public ToolResult(String toolCallId, String toolName, String content, boolean error) {
        this(toolCallId, toolName, content, error, "", "", "");
    }

    public ToolResult(
            String toolCallId,
            String toolName,
            String content,
            boolean error,
            String diffId,
            String reviewState,
            String reviewMessage
    ) {
        this(toolCallId, toolName, content, error, diffId, reviewState, reviewMessage, 0L);
    }

    public ToolResult(
            String toolCallId,
            String toolName,
            String content,
            boolean error,
            String diffId,
            String reviewState,
            String reviewMessage,
            long durationMs
    ) {
        this.toolCallId = toolCallId == null ? "" : toolCallId;
        this.toolName = toolName == null ? "" : toolName;
        this.content = content == null ? "" : content;
        this.error = error;
        this.diffId = diffId == null ? "" : diffId;
        this.reviewState = reviewState == null ? "" : reviewState;
        this.reviewMessage = reviewMessage == null ? "" : reviewMessage;
        this.durationMs = Math.max(0L, durationMs);
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public String getToolName() {
        return toolName;
    }

    public String getContent() {
        return content;
    }

    public boolean isError() {
        return error;
    }

    public String getDiffId() {
        return diffId;
    }

    public String getReviewState() {
        return reviewState;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public ToolResult withCall(String nextToolCallId, String nextToolName) {
        return new ToolResult(nextToolCallId, nextToolName, content, error, diffId, reviewState, reviewMessage, durationMs);
    }

    public ToolResult withDiffId(String nextDiffId) {
        return new ToolResult(toolCallId, toolName, content, error, nextDiffId, reviewState, reviewMessage, durationMs);
    }

    public ToolResult withReview(String nextReviewState, String nextReviewMessage) {
        return new ToolResult(toolCallId, toolName, content, error, diffId, nextReviewState, nextReviewMessage, durationMs);
    }

    public ToolResult withDurationMs(long nextDurationMs) {
        return new ToolResult(toolCallId, toolName, content, error, diffId, reviewState, reviewMessage, nextDurationMs);
    }
}
