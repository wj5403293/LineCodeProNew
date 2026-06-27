package cn.lineai.ui.component.toolcall;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.lineai.R;
import cn.lineai.tool.ToolCall;
import cn.lineai.tool.ToolResult;
import cn.lineai.ui.component.IconButtonView;
import cn.lineai.ui.theme.LineTheme;
import org.json.JSONObject;

public final class ToolCallGenericView extends BaseToolCallView {
    private final String label;
    private final LinearLayout header;
    private final IconButtonView icon;
    private final TextView labelView;
    private final TextView nameView;
    private final TextView metaView;
    private final ProgressBar progressBar;
    private final IconButtonView statusIcon;
    private final IconButtonView expandIcon;
    private final LinearLayout detailsContainer;

    private String lastToolCallId = "";
    private String lastSignature = "";
    private String pendingDetailSignature = "";
    private String lastMetaPrefix = "";
    private JSONObject lastInput = new JSONObject();
    private ToolResult lastResult;
    private boolean lastRunning;
    private boolean lastError;
    private boolean lastHasDetails;
    private boolean expanded;

    public ToolCallGenericView(Context context, String label) {
        super(context);
        this.label = label == null || label.length() == 0 ? getContext().getString(R.string.tool_call_generic_mcp) : label;

        header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setClickable(true);
        header.setOnClickListener(v -> {
            expanded = !expanded;
            updateExpandedState();
            if (expanded) {
                renderDetailsIfNeeded();
            }
        });
        LineTheme.padding(header, LineTheme.MD, LineTheme.SM, LineTheme.MD, LineTheme.SM);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        icon = new IconButtonView(context, IconButtonView.MCP);
        icon.setIconSizeDp(26, 13);
        icon.setClickable(false);
        icon.setBackground(LineTheme.roundedStroke(context, android.graphics.Color.TRANSPARENT, 8, LineTheme.CODE_BORDER));
        header.addView(icon, new LayoutParams(LineTheme.dp(context, 26), LineTheme.dp(context, 26)));

        LinearLayout titleBlock = new LinearLayout(context);
        titleBlock.setOrientation(VERTICAL);
        LayoutParams titleParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        titleParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        titleParams.rightMargin = LineTheme.dp(context, LineTheme.SM);
        header.addView(titleBlock, titleParams);

        labelView = LineTheme.text(context, this.label, 10, LineTheme.TEXT_TERTIARY, Typeface.BOLD);
        titleBlock.addView(labelView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        nameView = LineTheme.text(context, "", LineTheme.FONT_SM, LineTheme.TEXT, Typeface.NORMAL);
        nameView.setTypeface(Typeface.MONOSPACE);
        nameView.setSingleLine(true);
        nameView.setEllipsize(TextUtils.TruncateAt.END);
        titleBlock.addView(nameView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        metaView = LineTheme.text(context, "", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        metaView.setGravity(Gravity.END);
        LayoutParams metaParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        metaParams.rightMargin = LineTheme.dp(context, LineTheme.XS);
        header.addView(metaView, metaParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        header.addView(progressBar, new LayoutParams(LineTheme.dp(context, 18), LineTheme.dp(context, 18)));

        statusIcon = new IconButtonView(context, IconButtonView.CHECK);
        statusIcon.setIconSizeDp(18, 13);
        statusIcon.setClickable(false);
        header.addView(statusIcon, new LayoutParams(LineTheme.dp(context, 18), LineTheme.dp(context, 18)));

        expandIcon = new IconButtonView(context, IconButtonView.CHEVRON_RIGHT);
        expandIcon.setIconColor(LineTheme.TEXT_TERTIARY);
        expandIcon.setIconSizeDp(18, 13);
        expandIcon.setClickable(false);
        header.addView(expandIcon, new LayoutParams(LineTheme.dp(context, 18), LineTheme.dp(context, 18)));

        detailsContainer = new LinearLayout(context);
        detailsContainer.setOrientation(VERTICAL);
        addView(detailsContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void bind(ToolCall toolCall, ToolResult result) {
        String toolCallId = toolCall == null ? "" : toolCall.getId();
        if (!toolCallId.equals(lastToolCallId)) {
            expanded = false;
            lastToolCallId = toolCallId;
            lastSignature = "";
            detailsContainer.removeAllViews();
        }

        String name = toolCall == null ? "" : toolCall.getName();
        JSONObject input = ToolCallUtils.parseInput(toolCall);
        boolean running = result == null
                || "running".equals(result.getReviewState())
                || "pending".equals(result.getReviewState());
        boolean hasResult = result != null && result.getContent().length() > 0;
        boolean hasFinalResult = hasResult && !running;
        boolean error = result != null && result.isError();
        int statusColor = error ? LineTheme.DANGER : hasFinalResult ? LineTheme.SUCCESS : LineTheme.ACCENT;
        lastInput = input;
        lastResult = result;
        lastRunning = running;
        lastError = error;

        icon.setIconType(iconFor(name));
        icon.setIconColor(statusColor);
        nameView.setText(name);
        nameView.setTextColor(error ? LineTheme.DANGER : LineTheme.TEXT);
        labelView.setTextColor(LineTheme.TEXT_TERTIARY);
        progressBar.setVisibility(running ? VISIBLE : GONE);
        statusIcon.setVisibility(running ? GONE : VISIBLE);
        statusIcon.setIconType(error ? IconButtonView.CLOSE : IconButtonView.CHECK);
        statusIcon.setIconColor(statusColor);
        metaView.setText(metaText(result, input, hasResult));
        updateExpandedState();

        String signature = signature(toolCall, result, input);
        pendingDetailSignature = signature;
        if (expanded && !signature.equals(lastSignature)) {
            lastSignature = signature;
            renderDetails(input, result, running, error);
        } else if (!expanded) {
            detailsContainer.setVisibility(GONE);
        }
    }

    boolean hasLabel(String value) {
        return label.equals(value == null ? "" : value);
    }

    private String metaText(ToolResult result, JSONObject input, boolean hasResult) {
        StringBuilder builder = new StringBuilder();
        if (result != null && result.getDurationMs() > 0L) {
            builder.append(ToolCallJsonFormatter.durationLabel(result.getDurationMs()));
        }
        lastMetaPrefix = builder.toString();
        lastHasDetails = input.length() > 0 || hasResult;
        if (builder.length() > 0 && lastHasDetails) {
            builder.append(" · ");
        }
        if (lastHasDetails) {
            builder.append(expanded ? getContext().getString(R.string.tool_call_collapse) : getContext().getString(R.string.tool_call_expand));
        }
        return builder.toString();
    }

    private void updateExpandedState() {
        expandIcon.setVisibility(lastHasDetails ? VISIBLE : GONE);
        expandIcon.setIconType(expanded ? IconButtonView.CHEVRON_DOWN : IconButtonView.CHEVRON_RIGHT);
        detailsContainer.setVisibility(expanded ? VISIBLE : GONE);
        if (lastHasDetails) {
            String action = expanded ? getContext().getString(R.string.tool_call_collapse) : getContext().getString(R.string.tool_call_expand);
            metaView.setText(lastMetaPrefix.length() == 0 ? action : lastMetaPrefix + " · " + action);
        } else {
            metaView.setText(lastMetaPrefix);
        }
    }

    private void renderDetailsIfNeeded() {
        if (!pendingDetailSignature.equals(lastSignature)) {
            lastSignature = pendingDetailSignature;
            renderDetails(lastInput, lastResult, lastRunning, lastError);
        }
    }

    private void renderDetails(JSONObject input, ToolResult result, boolean running, boolean error) {
        detailsContainer.removeAllViews();
        String inputText = ToolCallUtils.prettyJson(input);
        if (!"{}".equals(inputText)) {
            addSection(getContext().getString(R.string.tool_call_input), inputText, LineTheme.TEXT_SECONDARY);
        }
        if (result != null && result.getContent().length() > 0) {
            addSection(
                    running ? getContext().getString(R.string.tool_call_progress) : getContext().getString(R.string.tool_call_output),
                    ToolCallJsonFormatter.prettyResult(result.getContent()),
                    error ? LineTheme.DANGER : LineTheme.TEXT_SECONDARY
            );
        }
    }

    private int iconFor(String name) {
        if (ToolCallUtils.isShellTool(name)) return IconButtonView.TERMINAL;
        if (ToolCallUtils.isHttpTool(name) || ToolCallUtils.isCustomMcpTool(name)) return IconButtonView.MCP;
        if (ToolCallUtils.isDeleteTool(name)) return IconButtonView.TRASH_2;
        return IconButtonView.MCP;
    }

    private void addSection(String title, String content, int color) {
        View divider = new View(getContext());
        divider.setBackgroundColor(LineTheme.CODE_BORDER);
        detailsContainer.addView(divider, new LayoutParams(LayoutParams.MATCH_PARENT, 1));

        LinearLayout section = new LinearLayout(getContext());
        section.setOrientation(VERTICAL);
        LineTheme.padding(section, LineTheme.MD, LineTheme.SM, LineTheme.MD, LineTheme.SM);
        section.addView(LineTheme.text(getContext(), title, LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.BOLD),
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView text = LineTheme.text(getContext(), content, LineTheme.FONT_XS, color, Typeface.NORMAL);
        text.setTypeface(Typeface.MONOSPACE);
        text.setTextIsSelectable(true);

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(false);
        scroll.setBackground(LineTheme.roundedStroke(getContext(), LineTheme.SURFACE, 8, LineTheme.CODE_BORDER));
        scroll.setOnTouchListener((view, event) -> {
            int action = event.getActionMasked();
            boolean active = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE;
            view.getParent().requestDisallowInterceptTouchEvent(active);
            if (action == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return false;
        });
        LineTheme.padding(scroll, LineTheme.SM, LineTheme.SM, LineTheme.SM, LineTheme.SM);
        scroll.addView(text, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutParams scrollParams = new LayoutParams(LayoutParams.MATCH_PARENT, LineTheme.dp(getContext(), 220));
        scrollParams.topMargin = LineTheme.dp(getContext(), 4);
        section.addView(scroll, scrollParams);
        detailsContainer.addView(section, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private String signature(ToolCall toolCall, ToolResult result, JSONObject input) {
        StringBuilder builder = new StringBuilder();
        if (toolCall != null) {
            builder.append(toolCall.getId()).append('|')
                    .append(toolCall.getName()).append('|')
                    .append(toolCall.getArguments());
        }
        builder.append('|').append(input == null ? "" : input.toString());
        if (result != null) {
            builder.append('|')
                    .append(result.getContent()).append('|')
                    .append(result.isError()).append('|')
                    .append(result.getReviewState()).append('|')
                    .append(result.getDurationMs());
        }
        return builder.toString();
    }
}
