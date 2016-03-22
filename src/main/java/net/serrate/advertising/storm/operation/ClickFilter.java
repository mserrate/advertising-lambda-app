package net.serrate.advertising.storm.operation;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

/**
 * Created by mserrate on 06/03/16.
 */
public class ClickFilter extends BaseFilter {

    @Override
    public boolean isKeep(TridentTuple tuple) {
        return tuple.getBoolean(0);
    }
}
