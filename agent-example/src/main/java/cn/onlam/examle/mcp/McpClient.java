package cn.onlam.examle.mcp;
/*
 * Copyright 2024 - 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;

import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Christian Tzolov
 */

public class McpClient {

    public static Logger logger = LoggerFactory.getLogger(McpClient.class);

    private final String baseUrl;
    private final String toolName;
    private final String apiKey;

    private final McpClientTransport transport;
//    private McpSyncClient client;

    public McpClient(Map<String, String> mcpConfig) {
        baseUrl= mcpConfig.get("base-url");
        toolName = mcpConfig.get("tool-name");
        apiKey = mcpConfig.get("api-key");
        this.transport = new WebFluxSseClientTransport(WebClient.builder().baseUrl(baseUrl));
    }

    public String run(Map<String, Object> toolArgs) {

        var client = io.modelcontextprotocol.client.McpClient.sync(this.transport).build();
        client.initialize();
        client.ping();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);

        // call tool
        CallToolResult toolResponse = client.callTool(new CallToolRequest(toolName,toolArgs));
        if(toolResponse.isError()) {
            logger.error("Error calling tool: " + toolResponse.toString());
        } else {
            logger.info("Success calling tool: " + toolResponse.toString());
        }

        client.closeGracefully();

        return ((McpSchema.TextContent) toolResponse.content().get(0)).text();
    }

}