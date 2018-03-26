package de.lindener.streaming.queries.examples;

import de.lindener.streaming.queries.models.HllSketchAggregation;
import de.lindener.streaming.queries.models.TopNQueryResult;
import de.lindener.streaming.queries.processing.CountDistinctQueries;
import de.lindener.streaming.queries.processing.Queries;
import de.lindener.streaming.queries.sources.amazon.AmazonReviewRating;
import de.lindener.streaming.queries.sources.amazon.AmazonReviewRatingSource;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.TimeUnit;

public class TestAmazonRating {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<AmazonReviewRating> inputStream = env.addSource(new AmazonReviewRatingSource("C:\\Users\\tobias\\Desktop\\projects\\queries\\data\\ratings_Books.csv"));


        KeySelector targetKeySelector = new KeySelector<AmazonReviewRating, Object>() {
            @Override
            public Object getKey(AmazonReviewRating input) throws Exception {
                return input.getRating();
            }

        };
        KeySelector targetValueSelector = new KeySelector<AmazonReviewRating, Object>() {
            @Override
            public Object getKey(AmazonReviewRating input) throws Exception {
                return input.getAsin();
            }
        };

        DataStream<HllSketchAggregation> stream = CountDistinctQueries.runContinuousHll(inputStream, targetKeySelector, targetValueSelector, 10000);
        DataStream<TopNQueryResult> stream1 = Queries.continuousTopN(inputStream, targetValueSelector, 5);
        JobExecutionResult result = env.execute("My Flink Job");
        System.out.println("The job took " + result.getNetRuntime(TimeUnit.SECONDS) + " to execute");

    }
}
