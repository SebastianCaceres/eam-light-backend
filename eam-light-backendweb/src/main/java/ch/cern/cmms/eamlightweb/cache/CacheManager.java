package ch.cern.cmms.eamlightweb.cache;

import ch.cern.cmms.eamlightejb.cache.Cacheable;

import org.springframework.stereotype.Component;
import javax.enterprise.inject.Instance;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CacheManager {

    @Autowired
    private Instance<Cacheable> cacheables; // TODO only external service :(

    public void clearAllCaches() {
        cacheables.forEach(Cacheable::clearCache);
    }

    public void setAllExpiresAfter(long l, TimeUnit timeUnit) {
        cacheables.forEach(cacheable -> cacheable.setExpiresAfter(l, timeUnit));
    }

    public List<Cacheable> getAllCacheables() {
        List<Cacheable> list = new ArrayList<>();
        cacheables.forEach(list::add);
        return list;
    }
}