package com.ppdai.bicoder.cache;

import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.BiCoderCompletion;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 补全结果缓存
 * 默认缓存最大存储为100个
 * 每次查询时会将当前访问的节点放置到链表最后,保证最近访问的节点不会被清除
 * 当缓存数量超过100个时,会清除最早访问的节点,即链表头部的节点
 *
 */
public class CompletionCache {
    private static final int CAPACITY = UserSetting.getInstance().getCompletionCacheSize();
    private static CacheKey latestKey;
    private static BiCoderCompletion latestValue;
    private static final LinkedHashMap<CacheKey, BiCoderCompletion> COMPLETION_CACHE = new LinkedHashMap<CacheKey, BiCoderCompletion>(CAPACITY, 0.75f, true) {
        private static final long serialVersionUID = 7744007945220829867L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, BiCoderCompletion> eldest) {
            return size() > CAPACITY;
        }
    };

    /**
     * 判断是否使用缓存,判断前后两次一致依据,前置内容一致,后置去除空格和换行要保持一致
     * 两级缓存,一级缓存,缓存最近一次,二级缓存,缓存100条,如果超过100条,则根据Lru算法淘汰最久最少使用的一条,
     *
     * @param prefix 光标前内容
     * @param suffix 光标后内容
     * @return 缓存内容
     */
    public static BiCoderCompletion get(String prefix, String suffix) {
        CacheKey key = new CacheKey(prefix, suffix);
        BiCoderCompletion biCoderCompletion = COMPLETION_CACHE.get(key);
        if (biCoderCompletion != null) {
            latestKey = key;
            latestValue = biCoderCompletion;
        }
        return biCoderCompletion;
    }

    /**
     * 获取最新一个存储的缓存
     *
     * @return 最新一个访问或存储的缓存
     */
    public static BiCoderCompletion getLatest() {
        return latestValue;
    }

    public static void put(String prefix, String suffix, BiCoderCompletion completions) {
        CacheKey key = new CacheKey(prefix, suffix);
        COMPLETION_CACHE.put(key, completions);
        latestKey = key;
        latestValue = completions;
    }

    public static void clear() {
        COMPLETION_CACHE.clear();
        latestKey = null;
        latestValue = null;
    }

    /**
     * 缓存key,使用光标前缀和 处理过空格和换行的后缀 作为双key作为唯一标识
     */
    private static class CacheKey {
        private final String prefix;
        private final String suffix;

        public CacheKey(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return new EqualsBuilder().append(prefix, cacheKey.prefix).append(suffix, cacheKey.suffix).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(prefix).append(suffix).toHashCode();
        }
    }
}
