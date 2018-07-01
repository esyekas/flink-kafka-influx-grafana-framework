package de.tuberlin.tubit.gitlab.anton.rudacov.functions;

import de.tuberlin.tubit.gitlab.anton.rudacov.data.DataPoint;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MorseDataFunction extends RichMapFunction<DataPoint<Long>, DataPoint<Integer>> implements ListCheckpointed<Integer> {

    private ArrayList<Integer> values;

    // State!
    private int currentStep;

    public MorseDataFunction(String path) {

        //Get and parse measurement data from file
        try {
            this.values = Files
                    .lines(Paths.get(path))
                    .map(line -> line.substring(13).trim())
                    .map(str -> Integer.parseInt(str))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.currentStep = 0;
    }

    @Override
    public DataPoint<Integer> map(DataPoint<Long> dataPoint) throws Exception {

        currentStep++;
        return dataPoint.withNewValue(values.get(currentStep - 1));
    }

    @Override
    public List<Integer> snapshotState(long checkpointId, long checkpointTimestamp) throws Exception {
        return Collections.singletonList(this.currentStep);
    }

    @Override
    public void restoreState(List<Integer> state) throws Exception {
        this.currentStep = state.isEmpty() ? 0 : state.get(0);
    }
}