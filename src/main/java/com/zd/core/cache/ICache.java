package com.zd.core.cache;

public interface ICache<K, V> {

    /**
     * 获取缓存
     *
     * @param key 缓存Key
     * @return 缓存Value
     */
    V get(K key);


    /**
     * 失效key对应的缓存
     *
     * @param key
     */
    void invalid(K key);
}
