package net.serrate.advertising.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import net.serrate.advertising.shared.Context;
import net.serrate.advertising.storm.operation.*;
import net.serrate.advertising.storm.spout.FakeBatchSpout;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapper;
import org.apache.storm.hbase.trident.mapper.TridentHBaseMapper;
import org.apache.storm.hbase.trident.state.HBaseState;
import org.apache.storm.hbase.trident.state.HBaseStateFactory;
import org.apache.storm.hbase.trident.state.HBaseUpdater;
import storm.kafka.BrokerHosts;
import storm.kafka.ZkHosts;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.trident.Stream;
import storm.trident.TridentState;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.Debug;
import storm.trident.operation.builtin.MapGet;
import storm.trident.state.StateFactory;
import storm.trident.testing.MemoryMapState;

import java.util.*;

/**
 * Created by mserrate on 06/03/16.
 */
public class AdvertisingTridentTopology {
    private Context context;

    AdvertisingTridentTopology(Context context) {
        this.context = context;
    }

    private TransactionalTridentKafkaSpout getKafkaSpout() {
        BrokerHosts hosts = new ZkHosts(context.getString("zookeeper.host"));

        TridentKafkaConfig spoutConfig = new TridentKafkaConfig(
                hosts,
                context.getString("kafka.advertising.topic"));
        spoutConfig.scheme= new SchemeAsMultiScheme(new ImpressionScheme());
        return new TransactionalTridentKafkaSpout(spoutConfig);
    }

    private StormTopology buildTopology() {
        TridentTopology topology = new TridentTopology();

        StateFactory clickThruMemory = new MemoryMapState.Factory();
        Stream inputStream = topology.newStream("inputStream", new FakeBatchSpout());
        TridentState clickThruState = inputStream
                .each(new Fields("click_thru"), new ClickFilter())
                .each(new Fields("cookie", "campaign", "product", "click_thru", "timestamp"), new DistinctFilter())
                .groupBy(new Fields("campaign"))
                .persistentAggregate(clickThruMemory, new Count(), new Fields("click_thru_count"));


        TridentHBaseMapper tridentHBaseMapper = new SimpleTridentHBaseMapper()
                .withRowKeyField("campaign")
                .withColumnFields(new Fields("impression_count", "click_thru_count", "effectiveness"))
                .withColumnFamily("realtime");

        HBaseState.Options options = new HBaseState.Options()
                .withConfigKey("hbase.conf")
                .withDurability(Durability.SYNC_WAL)
                .withMapper(tridentHBaseMapper)
                .withTableName("advertising");

        StateFactory factory = new HBaseStateFactory(options);


        inputStream
                .groupBy(new Fields("campaign"))
                .persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("impression_count"))
                .newValuesStream()
                .stateQuery(clickThruState, new Fields("campaign"), new MapGet(), new Fields("click_thru_count_nullable"))
                .each(new Fields("campaign", "impression_count", "click_thru_count_nullable"), new CampaignEffectiveness(), new Fields("click_thru_count", "effectiveness"))
                .each(new Fields("campaign", "impression_count", "click_thru_count", "effectiveness"), new PushSocket());
                //.each(new Fields("campaign", "impression_count", "click_thru_count", "effectiveness"), new Debug())
                //.partitionPersist(factory, new Fields("campaign", "impression_count", "click_thru_count", "effectiveness"), new HBaseUpdater());

        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        String configFileLocation = args[0];

        Context context = new Context(configFileLocation);

        Config config = new Config();

        //hbase configuration
        Map<String, Object> hbConf = new HashMap<String, Object>();
        config.put("hbase.conf", hbConf);
        //socket.io
        config.put("socket.io.url", context.getString("socket.io.url"));

        AdvertisingTridentTopology topology = new AdvertisingTridentTopology(context);

        if (context.getString("execution") != null && context.getString("execution").equalsIgnoreCase("local")) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", config, topology.buildTopology());
            Utils.sleep(600000);
            cluster.killTopology("test");
            cluster.shutdown();
        } else {
            StormSubmitter.submitTopology("advertising-trident", config, topology.buildTopology());
        }

    }
}
