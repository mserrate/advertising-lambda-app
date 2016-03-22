package net.serrate.advertising.storm.operation;

import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.operation.BaseFilter;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.tuple.TridentTuple;

import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by mserrate on 12/03/16.
 */
public class PushSocket extends BaseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(PushSocket.class);

    private Socket socket;

    @Override
    public boolean isKeep(TridentTuple tuple) {
        String campaign = tuple.getString(0);
        Double effectiveness = tuple.getDouble(3);


        JSONObject obj = new JSONObject();
        try {
            obj.put("campaign", campaign);
            obj.put("effectiveness", effectiveness);

            socket.emit("message", obj);
        } catch (JSONException e) {
            LOG.error(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public void prepare(Map conf, TridentOperationContext context) {
        try {
            socket = IO.socket(conf.get("socket.io.url").toString());
            socket.connect();
        } catch (URISyntaxException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
