package com.example.ai.demo.mcp;

import java.io.IOException;
import java.util.List;

import com.example.ai.demo.service.FileSystemToolService;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class FileSystemMcpTools {

    private final FileSystemToolService fileSystem;

    public FileSystemMcpTools(FileSystemToolService fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Tool(
        name = "file-search",
        description = "Search files under the configured workspace root by file name keyword. Returns relative paths."
    )
    public List<String> search(
        @ToolParam(description = "Keyword to match in file names, case-insensitive.")
        String keyword
    ) throws IOException {
        return fileSystem.search(keyword);
    }

    @Tool(
        name = "file-fetch",
        description = "Read a UTF-8 text file under the configured workspace root. Large files are truncated."
    )
    public String fetch(
        @ToolParam(description = "Relative file path under the configured root.")
        String relativePath
    ) throws IOException {
        return fileSystem.fetch(relativePath);
    }

    @Tool(
        name = "file-fetch-lines",
        description = "Read a line range from a UTF-8 text file under the configured workspace root."
    )
    public String fetchLines(
        @ToolParam(description = "Relative file path under the configured root.")
        String relativePath,
        @ToolParam(description = "One-based start line.")
        long startLine,
        @ToolParam(description = "One-based end line, inclusive.")
        long endLine
    ) throws IOException {
        return fileSystem.fetchLines(relativePath, startLine, endLine);
    }

    @Tool(
        name = "file-fetch-range",
        description = "Read a byte range from a UTF-8 text file under the configured workspace root."
    )
    public String fetchRange(
        @ToolParam(description = "Relative file path under the configured root.")
        String relativePath,
        @ToolParam(description = "Zero-based byte offileSystemet.")
        long offileSystemet,
        @ToolParam(description = "Maximum number of bytes to read.")
        long limit
    ) throws IOException {
        return fileSystem.fetchRange(relativePath, offileSystemet, limit);
    }
}
