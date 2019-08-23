package com.bj58.chr.base;

/**
 * @ClassName: CommonCacheManager
 * @Description:
 * @Author: panxia
 * @Date: Create in 2019/8/22 2:31 PM
 * @Version:1.0
 */
public interface CacheClearManager {

     void clearLocal(Object key, String cacheName);
}
