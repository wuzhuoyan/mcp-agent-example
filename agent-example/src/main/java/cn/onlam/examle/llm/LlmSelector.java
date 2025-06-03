package cn.onlam.examle.llm;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LlmSelector {

    private final Map<String, LLM> llmInstances = new HashMap<>();
    private ApplicationContext applicationContext;

    public LlmSelector(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public LLM select(String llmName) {
        LLM llmInstance = applicationContext.getBean(llmName, LLM.class);
        return llmInstance;
    }

    public LLM defaultSelected() {
        return select(GLM.class.getSimpleName());
    }
}
