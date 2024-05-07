package com.zd.core.cache;

public interface ICacheHash<K, V> {

    /**
     * 获取缓存
     *
     * @param key 缓存Key
     * @return 缓存Value
     */
    V getHash(K key, K hashkey);

    /**
     * 清除掉指定缓存
     * @param key
     * @param hashKey
     */
    void invalid(K key, K hashKey);

    /**
     * 清除掉整个hash缓存
     * @param key
     */
    void invalid(K key);

}