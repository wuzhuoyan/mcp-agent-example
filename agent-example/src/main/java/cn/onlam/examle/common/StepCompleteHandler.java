package cn.onlam.examle.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static cn.onlam.examle.common.ReasoningStep.STEP_RUN_RESPONSE_KEY;

public interface StepCompleteHandler {

    public static Logger logger = LoggerFactory.getLogger(StepCompleteHandler.class);

    public Map<String, String> handleStepComplete(String stepCompletedLlmResponse);

    // default
    public static StepCompleteHandler defaultHandler() {
        return new StepCompleteHandler() {
            @Override
            public Map<String, String> handleStepComplete(String stepCompletedLlmResponse) {

                logger.info("Handling step completion with default handler. Response: {}", stepCompletedLlmResponse);

                // 默认处理逻辑
                return Map.of(STEP_RUN_RESPONSE_KEY, stepCompletedLlmResponse);
            }
        };
    }

}
