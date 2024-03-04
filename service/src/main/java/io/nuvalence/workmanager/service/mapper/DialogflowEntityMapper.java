package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.models.dfcx.SysDate;
import io.nuvalence.workmanager.service.models.dfcx.SysTime;
import org.mapstruct.Mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Mapper for Dialogflow entities.
 */
@Mapper(componentModel = "spring")
public interface DialogflowEntityMapper {
    /**
     * Maps a string in the format yyyyMMdd to a DFCX sys.date object.
     *
     * @param formattedDate The formatted date in the format yyyyMMddd.
     * @throws ParseException If the date is not in the yyyyMMdd format.
     *
     * @return A sys.date object.
     */
    default SysDate mapStringToSysDate(String formattedDate) throws ParseException {
        Date date = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).parse(formattedDate);
        return mapDateToSysDate(date);
    }

    /**
     * Maps a Date to a sys.date object.
     * @param date The date.
     * @return A sys.date object.
     */
    default SysDate mapDateToSysDate(Date date) {
        Calendar calendar = new GregorianCalendar();
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
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return SysTime.builder()
                .hours(calendar.get(Calendar.HOUR_OF_DAY))
                .minutes(calendar.get(Calendar.MINUTE))
                .seconds(calendar.get(Calendar.SECOND))
                .nanos(calendar.get(Calendar.MILLISECOND) * 1000000)
                .build();
    }
}
