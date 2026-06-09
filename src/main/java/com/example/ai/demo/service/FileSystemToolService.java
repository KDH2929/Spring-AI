package com.example.ai.demo.service;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(FileSystemToolService.Props.class)
public class FileSystemToolService {

    private final Props props;
    private final Path root;

    @ConfigurationProperties(prefix = "mcp.filesystem")
    public record Props(String rootDir, Long maxFetchBytes, Integer maxSearchResults) {

        private static final long DEFAULT_MAX_FETCH_BYTES = 64 * 1024;
        private static final int DEFAULT_MAX_SEARCH_RESULTS = 20;

        String resolvedRootDir() {
            return rootDir == null || rootDir.isBlank() ? "." : rootDir;
        }

        long resolvedMaxFetchBytes() {
            return maxFetchBytes == null || maxFetchBytes <= 0
                    ? DEFAULT_MAX_FETCH_BYTES
                    : maxFetchBytes;
        }

        int resolvedMaxSearchResults() {
            return maxSearchResults == null || maxSearchResults <= 0
                    ? DEFAULT_MAX_SEARCH_RESULTS
                    : maxSearchResults;
        }
    }

    public FileSystemToolService(Props props) {
        this.props = props;
        this.root = Paths.get(props.resolvedRootDir()).toAbsolutePath().normalize();
    }

    public List<String> search(String keyword) throws IOException {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword is required");
        }

        String kw = keyword.toLowerCase();
        List<String> out = new ArrayList<>();

        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().contains(kw))
                    .limit(props.resolvedMaxSearchResults())
                    .forEach(p -> out.add(toRelativePath(p)));
        }
        return out;
    }

    public String fetch(String relativePath) throws IOException {
        Path file = resolveUnderRoot(relativePath);
        long size = Files.size(file);

        if (size <= props.resolvedMaxFetchBytes()) {
            return Files.readString(file, StandardCharsets.UTF_8);
        }

        String head = fetchRange(relativePath, 0L, props.resolvedMaxFetchBytes());
        return head + "\n\n[[TRUNCATED]]";
    }

    public String fetchRange(String relativePath, long offset, long limit) throws IOException {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be > 0");
        }

        Path file = resolveUnderRoot(relativePath);
        long size = Files.size(file);
        if (offset >= size) {
            return "";
        }

        long cappedLimit = Math.min(limit, props.resolvedMaxFetchBytes());
        int bytesToRead = Math.toIntExact(Math.min(cappedLimit, size - offset));
        ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);

        try (SeekableByteChannel channel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            channel.position(offset);
            while (buffer.hasRemaining() && channel.read(buffer) != -1) {
                // Fill the requested slice unless EOF is reached first.
            }
        } catch (EOFException ignored) {
            return "";
        }

        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public String fetchLines(String relativePath, long startLine, long endLine) throws IOException {
        if (startLine < 1) {
            throw new IllegalArgumentException("startLine must be >= 1");
        }
        if (endLine < startLine) {
            throw new IllegalArgumentException("endLine must be >= startLine");
        }

        Path file = resolveUnderRoot(relativePath);
        StringBuilder out = new StringBuilder();
        long currentLine = 1;
        long bytesUsed = 0;
        boolean truncated = false;

        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            var iterator = lines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (currentLine > endLine) {
                    break;
                }

                if (currentLine >= startLine) {
                    String numberedLine = currentLine + ": " + line + System.lineSeparator();
                    long lineBytes = numberedLine.getBytes(StandardCharsets.UTF_8).length;
                    if (bytesUsed + lineBytes > props.resolvedMaxFetchBytes()) {
                        truncated = true;
                        break;
                    }
                    out.append(numberedLine);
                    bytesUsed += lineBytes;
                }

                currentLine++;
            }
        }

        if (truncated) {
            out.append("[[TRUNCATED]]");
        }
        return out.toString();
    }

    private Path resolveUnderRoot(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("relativePath is required");
        }

        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("path must stay under filesystem root");
        }
        if (!Files.isRegularFile(resolved)) {
            throw new IOException("file not found: " + relativePath);
        }

        return resolved;
    }

    private String toRelativePath(Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }
}
