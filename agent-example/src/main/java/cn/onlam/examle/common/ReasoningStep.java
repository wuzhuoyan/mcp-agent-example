package cn.onlam.examle.common;

import cn.onlam.examle.llm.LLM;
import cn.onlam.examle.mcp.McpSelector;
import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;

public class ReasoningStep {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ReasoningStep.class);

    private String flowContextId;

    @Getter
    private Map<String, String> thisStepContex;

    public String getSourceQuestion() {
        return thisStepContex.get("nativeQuestion");
    }

    public String getResponseMessage() {
        return thisStepContex.get("llmResponseMessage");
    }

    private String prompt;
    private LLM llm;

//    private Tool tool;
    private List<Tool> tools = new ArrayList<>();
    private StepCompleteHandler stepCompleteHandler;


    private String nativeQuestion;

    public ReasoningStep(){}

    public ReasoningStep nativeQuestion(String nativeQuestion){
        this.nativeQuestion = nativeQuestion;
        return this;
    }

    public ReasoningStep prompt(String prompt){
        this.prompt = prompt;
        return this;
    }

    public ReasoningStep llm(LLM llm){
        this.llm = llm;
        return this;
    }

    @Data
    public static class Tool{
        private String toolName;
        private Map<String, String> toolArgs;
        private boolean isCache;
        private String cacheVal;
        private long cacheExpTime = 0L;
        private long cacheMillis = 1000 * 60 * 5; // 默认缓存5分钟
        public Tool(String toolName, Map<String, String> toolArgs) {
            this.toolName = toolName;
            this.toolArgs = toolArgs;
            this.isCache = false;
        }
        public Tool(String toolName, Map<String, String> toolArgs, boolean isCache) {
            this.toolName = toolName;
            this.toolArgs = toolArgs;
            this.isCache = isCache;
        }
        public Tool(String toolName, Map<String, String> toolArgs, boolean isCache, long cacheMillis) {
            this.toolName = toolName;
            this.toolArgs = toolArgs;
            this.isCache = isCache;
            this.cacheMillis = cacheMillis;
        }
        public String call(Map<String, String> contextArgs) throws Exception {
            if(isCache && cacheVal != null && System.currentTimeMillis() < cacheExpTime){
                return cacheVal;
            } else {
                Map<String, Object> transferArgs = new HashMap<>();
                transferArgs.putAll(toolArgs);
                transferArgs.putAll(contextArgs);

                String toolResult = McpSelector.select(toolName).run(transferArgs);
                if(isCache){
                    this.cacheVal = toolResult;
                    this.cacheExpTime = System.currentTimeMillis() + cacheMillis;
                }
                return toolResult + "\n";
            }
        }
    }



    public ReasoningStep tool(Tool tool) {
        this.tools.add(tool);
        return this;
    }

    public ReasoningStep tool(String toolName, Map<String, String> toolArgs) {
        this.tools.add(new Tool(toolName, toolArgs));
        return this;
    }

    public ReasoningStep stepCompleteHandler(StepCompleteHandler stepCompleteHandler) {
        this.stepCompleteHandler = stepCompleteHandler;
        return this;
    }

    public ReasoningStep run() {
        return run(nativeQuestion);
    }

    private ReasoningStep run(String nativeQuestion) {
        this.flowContextId = String.valueOf(UUID.randomUUID());
        return run(nativeQuestion, new HashMap<>(), flowContextId);
    }

    private ReasoningStep run(String nativeQuestion, Map<String, String> contextArgs, String contextId) {
        if(this.thisStepContex == null){
            this.thisStepContex = new HashMap<>();
        }
        thisStepContex.putAll(contextArgs);
        thisStepContex.put("nativeQuestion", nativeQuestion);
        thisStepContex.put("contextId", contextId);

        if(nativeQuestion == null || nativeQuestion.isEmpty()){
            nativeQuestion = makePromptAsQuestion();
        } else {
            nativeQuestion = appendPrompt(nativeQuestion);
        }

        if (!this.tools.isEmpty()) {
            StringBuilder result = new StringBuilder("参考内容：\n");
            for(Tool tool : this.tools){
                String perToolResult = "";
                try {
                    perToolResult = tool.call(contextArgs) + "\n";
                } catch (Exception e) {
                    perToolResult = "";
                    logger.error(e.getMessage(), e);
                }
                result.append(perToolResult);
            }
            if (!result.toString().equals("参考内容：\n")) {
                nativeQuestion = composeMessage(nativeQuestion, result.toString());
            }

        }

        logger.info("Pre send Question: {}", nativeQuestion);
        String llmResponseMessage = llm.chat(/*contextId, */nativeQuestion);
//        String llmResponseMessage = llm.chat(contextId, generateMessage(nativeQuestion));

        logger.debug("LLM Response: {}", llmResponseMessage);
        if(this.stepCompleteHandler == null){
            this.stepCompleteHandler = StepCompleteHandler.defaultHandler();
        }
        this.thisStepContex.putAll(stepCompleteHandler.handleStepComplete(llmResponseMessage));

        return this;
    }

    private String makePromptAsQuestion() {
        return "回答用户问题："  + this.prompt;
    }

    private String appendPrompt(String nativeQuestion) {
        if (nativeQuestion.endsWith(".")) {
            nativeQuestion += "。";
        } else if( nativeQuestion.endsWith("!")) {
            nativeQuestion += "！";
        } else if( nativeQuestion.endsWith("?")) {
            nativeQuestion += "？";
        } else if (nativeQuestion.endsWith("？")|| nativeQuestion.endsWith("！") ||
                nativeQuestion.endsWith("。")) {
            // do nothing
        } else {
            nativeQuestion += "。";
        }
        return "回答用户问题：" + nativeQuestion + "\n回答时严格遵守以下要求：\n" + this.prompt;
    }

    private String composeMessage(String message, String toolResult) {
        return toolResult + "\n" + "结合上文参考内容，" + message ;
    }

    public ReasoningStep next(ReasoningStep nextStep) {
        if(isFail()){
            return this;
        }
        return nextStep.run(getResponseMessage(), thisStepContex, flowContextId);
    }

    public static final String STEP_FAIL = "fail";
    public static final String STEP_SUCCESS = "success";
    public static final String STEP_RUN_STATUS_KEY = "runStatus";
    public static final String STEP_RUN_RESPONSE_KEY = "runResponse";


    private boolean isFail(){
        return thisStepContex.get(STEP_RUN_STATUS_KEY) != null && thisStepContex.get(STEP_RUN_STATUS_KEY).equals(STEP_FAIL);
    }

    public Map<String, String> getContext() {
        return thisStepContex;
    }

}