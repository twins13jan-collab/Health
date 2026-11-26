package eg.gov.health.platform.aiadmin;
import java.util.Map;

/** The external AI provider group (decision (ج) — the full set).
 *  All five expose an OpenAI-compatible chat-completions API, so a
 *  single client in the analytics service serves them all; only
 *  the base URL, default model and auth header style differ.
 *  Anthropic's compatibility endpoint takes the key in x-api-key. */
public final class AiProviders {
  private AiProviders() {}
  public record Preset(String baseUrl, String defaultModel, String authStyle, String displayAr, String displayEn) {}
  public static final Map<String, Preset> PRESETS = Map.of(
    "openai",     new Preset("https://api.openai.com/v1", "gpt-4o-mini", "bearer", "OpenAI", "OpenAI"),
    "anthropic",  new Preset("https://api.anthropic.com/v1", "claude-sonnet-4-5", "x-api-key", "Anthropic — Claude", "Anthropic — Claude"),
    "gemini",     new Preset("https://generativelanguage.googleapis.com/v1beta/openai", "gemini-2.0-flash", "bearer", "Google Gemini", "Google Gemini"),
    "openrouter", new Preset("https://openrouter.ai/api/v1", "openai/gpt-4o-mini", "bearer", "OpenRouter", "OpenRouter"),
    "groq",       new Preset("https://api.groq.com/openai/v1", "llama-3.3-70b-versatile", "bearer", "Groq", "Groq")
  );
}
