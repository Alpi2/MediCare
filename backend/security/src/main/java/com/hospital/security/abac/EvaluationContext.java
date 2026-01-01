package com.hospital.security.abac;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * ABAC Evaluation Context
 * Contains all attributes needed for policy evaluation
 */
@Data
@Builder
public class EvaluationContext {
    private Map<String, Object> subject;    // User attributes
    private Map<String, Object> resource;   // Resource attributes
    private Map<String, Object> action;     // Action attributes
    private Map<String, Object> environment; // Environment attributes

    // Fallback builder for IDEs without Lombok
    public static EvaluationContextBuilder builderFallbackInternal() { return new EvaluationContextBuilder(); }
    // Delegate matching Lombok API
    public static EvaluationContextBuilder builder() { return builderFallbackInternal(); }

    // Public no-arg constructor to support fallback builder usage in IDEs without Lombok
    public EvaluationContext() {}

    public static class EvaluationContextBuilder {
        private Map<String, Object> subject;
        private Map<String, Object> resource;
        private Map<String, Object> action;
        private Map<String, Object> environment;

        public EvaluationContextBuilder subject(Map<String, Object> s) { this.subject = s; return this; }
        public EvaluationContextBuilder resource(Map<String, Object> r) { this.resource = r; return this; }
        public EvaluationContextBuilder action(Map<String, Object> a) { this.action = a; return this; }
        public EvaluationContextBuilder environment(Map<String, Object> e) { this.environment = e; return this; }

        public EvaluationContext build() {
            EvaluationContext ctx = new EvaluationContext();
            ctx.subject = this.subject;
            ctx.resource = this.resource;
            ctx.action = this.action;
            ctx.environment = this.environment;
            return ctx;
        }
    }
}