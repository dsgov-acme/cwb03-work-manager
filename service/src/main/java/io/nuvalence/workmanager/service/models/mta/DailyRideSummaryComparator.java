package io.nuvalence.workmanager.service.models.mta;

import java.util.Comparator;

public class DailyRideSummaryComparator implements Comparator<DailyRideSummary> {
    @Override
    public int compare(DailyRideSummary a, DailyRideSummary b) {
        return a.getFormattedDate().compareTo(b.getFormattedDate());
    }
}
