Here is a basic `README.md` for the `mcp-agent-example` project:


# MCP Agent Example

## Overview
`mcp-agent-example` is a Java-based project built with Maven and Spring Boot. It serves as an example implementation of an agent that interacts with MCP (Multi-Client Platform) tools and AI bridges to perform various tasks such as querying weather information, performing calculations, and handling reasoning steps.

## Features
- Integration with MCP tools for weather information and calculations.
- AI bridge support for reasoning and natural language processing.
- Configurable via `application.yml`.
- Graceful resource management with `closeGracefully()`.

## Requirements
- **Java**: JDK 11 or higher
- **Maven**: 3.6 or higher
- **Spring Boot**: 2.x or higher

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/mcp-agent-example.git
   ```

2. Navigate to the project directory:
   ```bash
   cd mcp-agent-example
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

## Configuration

Edit the `application.yml` file located in `src/main/resources` to configure the server and tools:

```yaml
server:
  port: 5678
  address: 0.0.0.0
spring:
  application:
    name: agent-sample-with-mcp
ai:
  bridge:
    zhipu:
      api_key: your_key
  mcp:
    getRealWeatherInfo:
      tool-name: getRealWeatherInfo
      base-url: http://localhost:8080
      api-key: your_key
    calculate:
      tool-name: calculate
      base-url: http://localhost:8080
      api-key: your_key
    queryAllValidWeatherLocation:
      tool-name: queryAllValidWeatherLocation
      base-url: http://localhost:8080
      api-key: your_key
```

Replace `your_key` and `http://localhost:8080` with your actual API keys and MCP server URL.

## Usage

1. Start the application:
   ```bash
   mvn spring-boot:run
   ```

2. Access the server at:
   ```
   http://localhost:5678
   ```

3. Example HTTP request:
   ```http
   GET http://localhost:5678/server?从杭州到广州怎么样
   ```

## Project Structure

- `src/main/java`: Contains the Java source code.
  - `common`: Includes core classes like `ReasoningStep`.
  - `llm`: Handles AI bridge interactions.
  - `mcp`: Manages MCP tool selection and execution.
- `src/main/resources`: Contains configuration files like `application.yml`.

## Key Classes

### `ReasoningStep`
Handles reasoning logic, including appending prompts, interacting with tools, and managing context.

### `Tool`
Represents an MCP tool with caching capabilities.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any improvements or bug fixes.
