click_thru_data = LOAD 'wasb:///advertising-events-test.json'
    USING JsonLoader('cookie:chararray, campaign:chararray, product:chararray, timestamp:long, click_thru:chararray');

filterd_click_thru = FILTER click_thru_data BY click_thru == 'true';
distinct_click_thru = DISTINCT filterd_click_thru;
distinct_click_thru_by_campaign = GROUP distinct_click_thru BY campaign;
count_of_click_thru_by_campaign = FOREACH distinct_click_thru_by_campaign GENERATE group, COUNT($1);

impressions_by_campaign = GROUP click_thru_data BY campaign;
count_of_impressions_by_campaign = FOREACH impressions_by_campaign GENERATE group, COUNT($1);

joined_data = JOIN count_of_impressions_by_campaign BY $0 LEFT OUTER,
    count_of_click_thru_by_campaign BY $0 USING 'replicated';

result = FOREACH joined_data GENERATE
    $0 as campaign,
    ($3 is null ? 0 : $3) as click_thru_count,
    $1 as impression_count,
    (double)$3/(double)$1 as effectiveness:double;

--dump result;

STORE result INTO 'hbase://advertising' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage(
    'bulk:click_thru_count bulk:impression_count bulk:effectiveness');