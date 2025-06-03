package cn.onlam.examle.mcp;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class McpSelector {

    static Logger logger = LoggerFactory.getLogger(McpSelector.class);

    @Getter
    @Setter
    private static Map<String, Map<String, String>> mcp;

    private static Map<String, McpClient> mcpInstances;

    Environment environment;

    public McpSelector(Environment environment) {
        this.environment = environment;
        Binder binder = Binder.get(environment);
        mcp = binder.bind("ai.mcp", Map.class).orElseThrow(() ->new IllegalArgumentException("MCP services is invalid in configuration."));
        mcpInstances = new HashMap<>();
        print();
    }

    public void print() {
        logger.info("MCP Tool config: ");
        for (Map.Entry<String, Map<String, String>> entry : mcp.entrySet()) {
            logger.info("Key: " + entry.getKey());
            logger.info("Value: " + entry.getValue());
        }
    }

    public static McpClient select(String mcpToolName){
        McpClient mcpClient = mcpInstances.get(mcpToolName);
        if (mcpClient == null) {
            Map<String, String> mcpConfig = mcp.get(mcpToolName);
            if (mcpConfig == null
                || mcpConfig.get("base-url") == null || mcpConfig.get("base-url").isEmpty()
                || mcpConfig.get("api-key") == null || mcpConfig.get("api-key").isEmpty()
            ) {
                throw new IllegalArgumentException("Unsupported MCP: " + mcpToolName);
            }
            mcpClient = new McpClient(mcpConfig);
            mcpInstances.put(mcpToolName, mcpClient);
        }
        return mcpClient;
    }

}
