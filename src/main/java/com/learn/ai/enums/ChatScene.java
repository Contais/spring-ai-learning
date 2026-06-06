package com.learn.ai.enums;

/**
 * 聊天场景枚举
 * 用于路由到不同的 ChatClient，并标识会话类型
 */
public enum ChatScene {

    DEFAULT("chatClient", "chatMemory", "default", true),
    GAME("gameChatClient", "inMemoryChatMemory", "game", false),
    CUSTOMER_SERVICE("customerServiceChatClient", "chatMemory", "customer_service", true);

    private final String clientBeanName;
    private final String chatMemoryBeanName;
    private final String conversationType;
    private final boolean persistMessages;

    ChatScene(String clientBeanName, String chatMemoryBeanName, String conversationType, boolean persistMessages) {
        this.clientBeanName = clientBeanName;
        this.chatMemoryBeanName = chatMemoryBeanName;
        this.conversationType = conversationType;
        this.persistMessages = persistMessages;
    }

    public String getClientBeanName() {
        return clientBeanName;
    }

    public String getChatMemoryBeanName() {
        return chatMemoryBeanName;
    }

    public String getConversationType() {
        return conversationType;
    }

    public boolean isPersistMessages() {
        return persistMessages;
    }

    public static ChatScene fromConversationType(String conversationType) {
        for (ChatScene scene : values()) {
            if (scene.conversationType.equals(conversationType)) {
                return scene;
            }
        }
        return DEFAULT;
    }
}
