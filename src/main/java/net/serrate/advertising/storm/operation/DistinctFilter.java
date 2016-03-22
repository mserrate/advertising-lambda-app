package net.serrate.advertising.storm.operation;

import storm.trident.operation.BaseFilter;
import storm.trident.tuple.TridentTuple;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by mserrate on 06/03/16.
 */
public class DistinctFilter extends BaseFilter {
    private Set<String> distincter = Collections.synchronizedSet(new
            HashSet<String>());

    @Override
    public boolean isKeep(TridentTuple tuple) {
        String id = this.getId(tuple);

        return distincter.add(id);
    }

    public String getId(TridentTuple t){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.size(); i++){
            sb.append(t.getValue(i).toString());
        }
        return sb.toString();
    }
}

