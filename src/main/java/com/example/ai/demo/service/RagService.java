package com.example.ai.demo.service;

import java.util.List;

import com.example.ai.demo.model.RagSearchResult;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private static final double SIMILARITY_THRESHOLD = 0.35;
    private static final int TOP_K = 3;
    private static final String QUOTE_FILTER = "topic == 'quote'";

    private final VectorStore vectorStore;
    private final Filter.Expression quoteFilterExpression;
    private final RetrievalAugmentationAdvisor ragAdvisor;

    public RagService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.quoteFilterExpression = new FilterExpressionTextParser().parse(QUOTE_FILTER);
        this.ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(SIMILARITY_THRESHOLD)
                        .topK(TOP_K)
                        .filterExpression(quoteFilterExpression)
                        .build())
                .build();
    }

    public RetrievalAugmentationAdvisor advisor() {
        return ragAdvisor;
    }

    public List<Document> searchSimilarDocuments(String query) {
        return vectorStore.similaritySearch(searchRequest(query));
    }

    public List<RagSearchResult> searchResults(String query) {
        return searchSimilarDocuments(query).stream()
                .map(document -> new RagSearchResult(document.getText(), document.getMetadata()))
                .toList();
    }

    public boolean hasRelevantDocs(String query) {
        var results = searchSimilarDocuments(query);
        System.out.println("Similarity search results: " + results);
        return results != null && !results.isEmpty();
    }

    private SearchRequest searchRequest(String query) {
        return SearchRequest.builder()
                .query(query)
                .topK(TOP_K)
                .filterExpression(QUOTE_FILTER)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();
    }
}
