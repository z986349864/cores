package com.zd.core.cache;

public interface ICacheDataHashProvider<K, V> {

    /**
     * 加载单个元素
     * get
     *
     * @param key
     * @return V
     * @since:
     */
    V getHash(K key, K hashkey);
}
