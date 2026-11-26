package eg.gov.health.platform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;

/** Redis-backed caching (the cache tier of the storage design:
 *  PostgreSQL is the durable system of record; Redis holds hot
 *  reference data with TTLs; the JVM serves requests if Redis is
 *  briefly unavailable — a cache failure must never fail a
 *  request, it just falls through to the database). */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
  private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

  @Override
  public CacheErrorHandler errorHandler() {
    return new CacheErrorHandler() {
      @Override public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        log.warn("cache get failed ({}): {}", cache.getName(), e.getMessage());
      }
      @Override public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        log.warn("cache put failed ({}): {}", cache.getName(), e.getMessage());
      }
      @Override public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        log.warn("cache evict failed ({}): {}", cache.getName(), e.getMessage());
      }
      @Override public void handleCacheClearError(RuntimeException e, Cache cache) {
        log.warn("cache clear failed ({}): {}", cache.getName(), e.getMessage());
      }
    };
  }
}
