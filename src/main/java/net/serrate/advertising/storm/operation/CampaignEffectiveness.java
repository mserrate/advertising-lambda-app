package net.serrate.advertising.storm.operation;

import backtype.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;

/**
 * Created by mserrate on 06/03/16.
 */
public class CampaignEffectiveness extends BaseFunction {
    private static final Logger LOG = LoggerFactory.getLogger(CampaignEffectiveness.class);

    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        String campaign = tuple.getString(0);
        Long impressions_count = tuple.getLong(1);
        Long click_thru_count = tuple.getLong(2);
        if (click_thru_count == null)
            click_thru_count = 0L;

        double effectiveness = (double) click_thru_count / impressions_count;

        LOG.debug("[" + campaign + "," + String.valueOf(click_thru_count) +
                "," + impressions_count + ", " + effectiveness + "]");

        collector.emit(new Values(click_thru_count, effectiveness));
    }
}
