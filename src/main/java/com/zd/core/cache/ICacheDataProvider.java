package com.zd.core.cache;

public interface ICacheDataProvider<K, V> {

    /**
     * 加载单个元素
     * get
     *
     * @param key
     * @return V
     * @since:
     */
    V get(K key);

}
