package cn.onlam.examle;

import cn.onlam.examle.common.ReasoningStep;
import cn.onlam.examle.common.StepCompleteHandler;
import cn.onlam.examle.llm.LlmSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 伍灼研
 */
@SpringBootApplication
@RestController
public class AgentExample {

    static Logger logger = LoggerFactory.getLogger(AgentExample.class);

    public static void main(String[] args) {
        SpringApplication.run(AgentExample.class, args);
    }

    @Autowired
    LlmSelector llmSelector;

    @GetMapping("/server")
    public String server(@RequestParam String nativeQuestion) {
        return new ReasoningStep()
                .nativeQuestion(nativeQuestion)
                .llm(llmSelector.defaultSelected())
                .tool("queryAllValidWeatherLocation", Collections.EMPTY_MAP) // query all valid locations
                .prompt("根据提问内容，抽取出发地省份名称、出发地名称、目的地省份名称、目的地名称,以下固定格式回答。" +
                        "固定格式：“出发地省份名称#出发地名称#目的地省份名称#目的地名称”；" +
                        "例如：广东省#广州市#海南省#海口市。" +
                        "要求出发地省份名称、目的地省份名称必须在以上参考内容所述的省份内，如果以上参考内容里没有则推理一个最符合的省份名称；" +
                        "要求出发地名称、目的地名称必须在以上参考内容所述的城市内，如果以上参考内容里没有则推理一个最符合的城市名称；" + // 惨开内容来自于tool
                        "按固定格式严格返回，不允许留空或遗漏。" +
                        "如果无法推理到合适的城市名称，则回答固定答案“未能识别明确的出发地和目的地，只支持国内城市，请尝试再次提问。”")
                .stepCompleteHandler(stepCompletedLlmResponse -> { // 处理步骤完成后的响应, 塞入下一步需要的参数
                    logger.info("Handling step completion. Response: {}", stepCompletedLlmResponse);

                    if(stepCompletedLlmResponse.contains("<think>") && stepCompletedLlmResponse.contains("</think>")) {
                        // 如果响应中包含思考内容，则去除思考内容
                        stepCompletedLlmResponse = stepCompletedLlmResponse.substring(
                                stepCompletedLlmResponse.indexOf("</think>") + "</think>".length()).trim();
                        logger.info("Stripped thinking content. New response: {}", stepCompletedLlmResponse);
                    }

                    Map<String, String> perRequestAgentContextMap = new HashMap<>();

                    if(stepCompletedLlmResponse.contains("未能识别明确的出发地和目的地")) {
                        // 如果响应中包含未能识别的提示，则直接返回该提示
                        perRequestAgentContextMap.put(ReasoningStep.STEP_RUN_STATUS_KEY, ReasoningStep.STEP_FAIL);
                        perRequestAgentContextMap.put(ReasoningStep.STEP_RUN_RESPONSE_KEY, stepCompletedLlmResponse);
                        return perRequestAgentContextMap;
                    }

                    String[] parts = stepCompletedLlmResponse.split("#");
                    if (parts.length == 4) {
                        perRequestAgentContextMap.put("departureProvinceName", parts[0].trim());
                        perRequestAgentContextMap.put("departureLocationName", parts[1].trim());
                        perRequestAgentContextMap.put("destinationProvinceName", parts[2].trim());
                        perRequestAgentContextMap.put("destinationLocationName", parts[3].trim());
                        perRequestAgentContextMap.put(ReasoningStep.STEP_RUN_STATUS_KEY, ReasoningStep.STEP_SUCCESS);
                    }
                    return perRequestAgentContextMap;
                })
                .run()
                .next(
                        new ReasoningStep()
                                .llm(llmSelector.defaultSelected())
                                .tool("getRealWeatherInfo", Collections.EMPTY_MAP)
                                .prompt("推测两地天气对两地航班可能造成的延误的影响有多大。")
                                .stepCompleteHandler(StepCompleteHandler.defaultHandler())
                )
                .getContext()
                .get(ReasoningStep.STEP_RUN_RESPONSE_KEY); // 获取最终的响应消息

    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }


}
