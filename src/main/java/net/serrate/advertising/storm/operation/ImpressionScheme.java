package net.serrate.advertising.storm.operation;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.serrate.advertising.shared.ImpressionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by mserrate on 06/03/16.
 */
public class ImpressionScheme implements Scheme {
    private static final Logger LOG = LoggerFactory.getLogger(ImpressionScheme.class);

    private ObjectMapper mapper;

    public ImpressionScheme() {
        mapper = new ObjectMapper();
    }

    @Override
    public List<Object> deserialize(byte[] bytes) {
        try {
            String impressionEventJson = new String(bytes, "UTF-8");
            ImpressionEvent impressionEvent = mapper.readValue(impressionEventJson, ImpressionEvent.class);

            return new Values(
                    impressionEvent.getCookie(),
                    impressionEvent.getCampaign(),
                    impressionEvent.getProduct(),
                    impressionEvent.getClick_Thru(),
                    impressionEvent.getTimestamp());

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public Fields getOutputFields() {
        return new Fields("cookie", "campaign", "product", "click_thru", "timestamp");
    }
}
