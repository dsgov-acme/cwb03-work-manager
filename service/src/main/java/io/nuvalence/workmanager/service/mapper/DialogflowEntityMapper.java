package io.nuvalence.workmanager.service.mapper;

import io.nuvalence.workmanager.service.generated.models.DialogflowSysDate;
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
     * @return A sysdate object.
     */
    default DialogflowSysDate mapStringToSysDate(String formattedDate) throws ParseException {
        Date date = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).parse(formattedDate);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return new DialogflowSysDate()
                .day(calendar.get(Calendar.DAY_OF_MONTH))
                .month(calendar.get(Calendar.MONTH) + 1)
                .year(calendar.get(Calendar.YEAR));
    }
}
