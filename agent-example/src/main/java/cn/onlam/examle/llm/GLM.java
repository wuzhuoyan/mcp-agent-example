package cn.onlam.examle.llm;


import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;


@Component
public class GLM implements LLM {

    public static Logger logger = LoggerFactory.getLogger(GLM.class);

    public static Map<String, List<ChatMessage>> contextMap = new HashMap<>();

    @Value("${ai.bridge.zhipu.api_key}")
    private String API_KEY;
    private static final String requestIdTemplate = "circlelog-%d";


    private volatile ClientV4 client;
    private ClientV4 getClient() {
        if (this.client == null) {
            synchronized (this) {
                if (this.client == null) {
                    this.client = new ClientV4.Builder(API_KEY)
                            .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
                            .connectionPool(new okhttp3.ConnectionPool(16, 1, TimeUnit.SECONDS))
                            .build();
                }
            }
        }
        return this.client;
    }

    public String chat(String message){
        String contextId = UUID.randomUUID().toString();
        String response = chat(UUID.randomUUID().toString(), message);
        contextMap.remove(contextId);
        return response;
    }

    public String chat(String contextId, String message) {

        List<ChatMessage> context = contextMap.get(contextId);
        if (context == null) {
            context = new ArrayList<>();
            context.add(
                    new ChatMessage(ChatMessageRole.SYSTEM.value(), "你是一个智能助手，帮助用户解决问题。")
            );
            contextMap.put(contextId, context);
        }


        context.add(
                new ChatMessage(ChatMessageRole.USER.value(), message)
        );

        // log input message
        logger.debug("LLM Input message: {}", message);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(context)
//                .requestId(requestId)
//                .model("glm-4-long")
                .model("GLM-Z1-AirX")
                .build();
        ModelApiResponse chatResponse =  getClient().invokeModelApi(chatCompletionRequest);

        if (chatResponse.isSuccess() && !chatResponse.getData().getChoices().isEmpty()) {
            ChatMessage assistantChatMessage = chatResponse.getData().getChoices().get(0).getMessage();
            context.add(
                    new ChatMessage(ChatMessageRole.ASSISTANT.value(), assistantChatMessage.getContent().toString())
            );

            String response =  assistantChatMessage.getContent().toString();
            logger.debug("LLM Response: {}", response);
            return response;
        } else {
            logger.error("LLM Response Error.");
            return "";
        }
    }


}
