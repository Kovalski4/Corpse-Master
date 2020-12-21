package org.kovalski.corpsemaster;

import java.util.ArrayList;
import java.util.List;

public abstract class CorpseHolder extends FakeBedHolder{

    private static final List<ICorpse> corpseCache = new ArrayList<>();

    public void removeCorpseCache(ICorpse corpse) {
        corpseCache.remove(corpse);
    }

    public void cacheCorpse(ICorpse corpse){
        corpseCache.add(corpse);
    }

    public List<ICorpse> getCorpseCache() {
        return corpseCache;
    }

}
