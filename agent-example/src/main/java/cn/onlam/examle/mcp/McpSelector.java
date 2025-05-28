package cn.onlam.examle.mcp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class McpSelector {

    @Getter
    @Setter
    private static Map<String, Map<String, String>> mcp;

    private static Map<String, MyMcpClient> mcpInstances;

    Environment environment;

    public McpSelector(Environment environment) {
        this.environment = environment;
        Binder binder = Binder.get(environment);
        mcp = binder.bind("ai.mcp", Map.class).orElseThrow(() ->new IllegalArgumentException("MCP services is invalid in configuration."));
        mcpInstances = new HashMap<>();
    }

    public void print() {
        // print mcpServices
        for (Map.Entry<String, Map<String, String>> entry : mcp.entrySet()) {
            System.out.println("Key: " + entry.getKey());
            System.out.println("Value: " + entry.getValue());
        }
    }

    public static MyMcpClient select(String mcpToolName){
        MyMcpClient mcpClient = mcpInstances.get(mcpToolName);
        if (mcpClient == null) {
            Map<String, String> mcpConfig = mcp.get(mcpToolName);
            if (mcpConfig == null
                || mcpConfig.get("base-url") == null || mcpConfig.get("base-url").isEmpty()
                || mcpConfig.get("api-key") == null || mcpConfig.get("api-key").isEmpty()
            ) {
                throw new IllegalArgumentException("Unsupported MCP: " + mcpToolName);
            }
            mcpClient = new NormalMcpClient(mcpConfig);
            mcpInstances.put(mcpToolName, mcpClient);
        }
        return mcpClient;
    }

}
