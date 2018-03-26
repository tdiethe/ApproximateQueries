package de.lindener.streaming.queries.examples;

import de.lindener.streaming.queries.functions.HllSketchFunction;
import de.lindener.streaming.queries.models.HllSketchAggregation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

public class SmallDataExample {
    public static void main(String[] args) throws Exception {
        String[] categories = {"A", "B"};
        List<Tuple2<String, Integer>> data = new ArrayList<>();
        for (String category : categories) {
            for (int i = 1; i < 6; i++) {
                data.add(Tuple2.of(category, i));
            }
        }
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setMaxParallelism(200);
        DataStream<Tuple2<String, Integer>> inputStream = env.fromCollection(data);

        KeySelector targetKeySelector = new KeySelector<Tuple2<String, Integer>, Object>() {
            @Override
            public Object getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f0;
            }

        };
        KeySelector targetValueSelector = new KeySelector<Tuple2<String, Integer>, Object>() {
            @Override
            public Object getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                return stringIntegerTuple2.f1;
            }

        };
        HllSketchFunction sketchFunction2 = new HllSketchFunction(targetKeySelector, targetValueSelector);
        DataStream<HllSketchAggregation> stream = inputStream.keyBy(targetKeySelector).flatMap(sketchFunction2).setParallelism(2);
        env.execute();
    }
}
