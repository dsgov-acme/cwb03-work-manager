package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.models.dfcx.SysDate;
import io.nuvalence.workmanager.service.models.dfcx.SysTime;
import org.mapstruct.Mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Mapper for Dialogflow entities.
 */
@Mapper(componentModel = "spring")
public interface DialogflowEntityMapper {
    /**
     * Maps a string in the format yyyyMMdd to a DFCX sys.date object.
     *
     * @param formattedDate The formatted date in the format yyyyMMdd.
     * @param isInEst Whether the provided date is already in EST.
     * @throws ParseException If the date is not in the yyyyMMdd format.
     *
     * @return A sys.date object.
     */
    default SysDate mapStringToSysDate(String formattedDate, boolean isInEst)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.ROOT);
        if (isInEst) {
            formatter.setTimeZone(TimeZone.getTimeZone("EST"));
        }
        Date date = formatter.parse(formattedDate);
        return mapDateToSysDate(date, isInEst);
    }

    /**
     * Maps a Date to a sys.date object.
     * @param date The date.
     * @param isInEst Whether the provided date is already in EST.
     * @return A sys.date object.
     */
    default SysDate mapDateToSysDate(Date date, boolean isInEst) {
        if (!isInEst) {
            ZonedDateTime estDate = date.toInstant().atZone(ZoneId.of("America/New_York"));

            return SysDate.builder()
                    .day(estDate.getDayOfMonth())
                    .month(estDate.getMonthValue())
                    .year(estDate.getYear())
                    .build();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("EST"));
        calendar.setTime(date);
        return SysDate.builder()
                .day(calendar.get(Calendar.DAY_OF_MONTH))
                .month(calendar.get(Calendar.MONTH) + 1)
                .year(calendar.get(Calendar.YEAR))
                .build();
    }

    /**
     * Maps a date to a sys.time object.
     * @param date The date.
     * @return A sys.time object.
     */
    default SysTime mapDateToSysTime(Date date) {
        ZonedDateTime estDate = date.toInstant().atZone(ZoneId.of("America/New_York"));

        return SysTime.builder()
                .hours(estDate.getHour())
                .minutes(estDate.getMinute())
                .seconds(estDate.getSecond())
                .nanos(0)
                .build();
    }
}
