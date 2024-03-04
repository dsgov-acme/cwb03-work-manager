package io.nuvalence.workmanager.service.models.mta;

import java.util.Comparator;

public class RideSummaryComparator implements Comparator<RideSummary> {

    @Override
    public int compare(RideSummary a, RideSummary b) {
        return a.getPickup().compareTo(b.getPickup());
    }
}
