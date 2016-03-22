package net.serrate.advertising.storm.spout;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import storm.trident.operation.TridentCollector;
import storm.trident.spout.IBatchSpout;
import storm.trident.testing.FixedBatchSpout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by mserrate on 11/03/16.
 */
public class FakeBatchSpout implements IBatchSpout {

    private Long start;

    @Override
    public void open(Map map, TopologyContext topologyContext) {
        start = System.currentTimeMillis();
    }

    @Override
    public void emitBatch(long batchId, TridentCollector collector) {
        Random random = new Random();
        Integer nrOfCookies = 10000;
        Integer nrOfCampaigns = 5;
        Integer nrOfProducts = 5;
        List<Double> probabilities = new ArrayList<>(5);
        if (isMoreThan(15)) {
            probabilities.add(0.31);
        } else if (isMoreThan(25)) {
            probabilities.add(0.41);
        } else {
            probabilities.add(0.21);
        }

        probabilities.add(0.77);
        if (isMoreThan(20)) {
            probabilities.add(0.50);
        } else if (isMoreThan(30)) {
            probabilities.add(0.33);
        } else {
            probabilities.add(0.66);
        }
        if (isMoreThan(17)) {
            probabilities.add(0.55);
        } else {
            probabilities.add(0.43);
        }
        probabilities.add(0.55);

        for(int i = 0; i < 5; i++) {
            Integer rndCampaigns = random.nextInt(nrOfCampaigns);

            Long timestamp = System.currentTimeMillis();
            String cookie = String.format("cookie_%d", random.nextInt(nrOfCookies));
            String campaign = String.format("campaign_%d", rndCampaigns + 1);
            String product = String.format("product_%d", random.nextInt(nrOfProducts)) + 1;
            Boolean click = Math.random() >= 1.0 - probabilities.get(rndCampaigns);

            collector.emit(new Values(cookie, campaign, product, click, timestamp));
        }
    }

    private boolean isMoreThan(int seconds) {
        return ((System.currentTimeMillis() - start) / 1000) > seconds;
    }

    @Override
    public void ack(long batchId) {
    }

    @Override
    public void close() {
    }

    @Override
    public Map getComponentConfiguration() {
        Config conf = new Config();
        conf.setMaxTaskParallelism(1);
        return conf;
    }

    @Override
    public Fields getOutputFields() {
        return new Fields("cookie", "campaign", "product", "click_thru", "timestamp");
    }
}
