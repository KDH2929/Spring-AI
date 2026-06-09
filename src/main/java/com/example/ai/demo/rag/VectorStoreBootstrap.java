package com.example.ai.demo.rag;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(prefix = "app.vectorstore.bootstrap", name = "enabled", havingValue = "true")
public class VectorStoreBootstrap {
    
    private static final String INIT_MARKER_TEXT = "__VECTORSTORE_V1__";
    
    private final VectorStore vectorStore;
    
    public VectorStoreBootstrap(VectorStore vectorStore){
        this.vectorStore = vectorStore;
    }


    @PostConstruct
    public void bootstrap(){
        if(isAlreadyBootstrapped(vectorStore)){
            return;
        }

        List<Document> documents = List.of(
            new Document(INIT_MARKER_TEXT,
                         Map.of("type", "bootstrap", "version", "v1", "app", "myapp")
            ),

            // 실제 RAG 문서들
            new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!",
                            Map.of("docId", "seed-1", "topic", "spring-ai")
            ),

            new Document("The World is bigger than you think, and There is redemption on corner.",
                            Map.of("docId", "seed-2", "topic", "quote")
            ),

            new Document("Walk towards the past, and Return to the future.",
                            Map.of("docId", "seed-3", "topic", "quote", "theme", "time")
            )

        );
        
        vectorStore.add(documents);
    }


    private boolean isAlreadyBootstrapped(VectorStore vectorStore) {
        var results = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(INIT_MARKER_TEXT)
                .topK(1)
                .filterExpression("type=='bootstrap' && app=='myapp'")
                .build()
        );

        return results != null && !results.isEmpty();
    }
}
