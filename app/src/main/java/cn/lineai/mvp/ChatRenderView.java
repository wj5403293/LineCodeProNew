package cn.lineai.mvp;

import cn.lineai.model.ChatUiState;
import cn.lineai.model.ChatMessage;
import cn.lineai.model.InputAttachment;
import java.util.List;

public interface ChatRenderView {
    void render(ChatUiState state);

    boolean renderStreamingMessage(ChatMessage message);

    void setComposerDraft(String text);

    void setComposerDraft(String text, List<InputAttachment> attachments);
}
