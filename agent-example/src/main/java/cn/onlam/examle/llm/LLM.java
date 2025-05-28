package cn.onlam.examle.llm;

import java.io.IOException;

public interface LLM {
    public String chat(String contextId, String message);

    public String chat(String message);
}
