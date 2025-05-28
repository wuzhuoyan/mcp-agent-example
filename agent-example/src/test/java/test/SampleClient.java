package test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

public class SampleClient {

    private final McpClientTransport transport;

    public SampleClient(McpClientTransport transport) {
        this.transport = transport;
    }

    public void run() {

        var client = McpClient.sync(this.transport).build();

        client.initialize();

        client.ping();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        CallToolResult locationToolResult = client.callTool(new CallToolRequest("LocationTool",  Collections.emptyMap() ));
        System.out.println("locationToolResult Response = " + locationToolResult);

        CallToolResult weatherToolResult = client.callTool(new CallToolRequest("WeatherTool",
                Map.of("sourceProvinceName", "浙江", "sourceLocationName", "杭州",
                        "targetProvinceName", "广东", "targetLocationName", "广州")));
        System.out.println("weatherToolResult Response = " + weatherToolResult);
        client.closeGracefully();

    }

}
