package com.myorg.myproduct.user.table;

import com.myorg.myproduct.admin.state.ConfigurationState;
import com.myorg.myproduct.model.MyProductRelease;
import com.myorg.myproduct.user.data.DataRetriever;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableBuilder {

    public static List<List<String>> build() {
        List<MyProductRelease> releases = DataRetriever.retrieve().getReleases();
        return releases.stream().filter(TableBuilder::isInRange).map(r ->
                Arrays.asList(r.getVersion(), r.getReleaseNotes())).collect(Collectors.toList());
    }

    private static boolean isInRange(MyProductRelease release) {
        String current = release.getVersion();
        String min = ConfigurationState.INSTANCE.getRangeSetting().getMinVersion();
        String max = ConfigurationState.INSTANCE.getRangeSetting().getMaxVersion();
        return (min.compareTo(current) <= 0 || min.isEmpty()) && (max.compareTo(current) >= 0 || max.isEmpty());
    }

}