package com.company.lms.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class GreekHolidayUtil {

    public static Set<LocalDate> getHolidays(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 1, 6));
        holidays.add(LocalDate.of(year, 3, 25));
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 8, 15));
        holidays.add(LocalDate.of(year, 10, 28));
        holidays.add(LocalDate.of(year, 12, 25));
        holidays.add(LocalDate.of(year, 12, 26));

        LocalDate easter = orthodoxEaster(year);
        holidays.add(easter.minusDays(48));
        holidays.add(easter.minusDays(2));
        holidays.add(easter);
        holidays.add(easter.plusDays(1));
        holidays.add(easter.plusDays(50));

        return holidays;
    }

    private static LocalDate orthodoxEaster(int year) {
        int a = year % 4;
        int b = year % 7;
        int c = year % 19;
        int d = (19 * c + 15) % 30;
        int e = (2 * a + 4 * b - d + 34) % 7;
        int month = (d + e + 114) / 31;
        int day = ((d + e + 114) % 31) + 1;
        return LocalDate.of(year, month, day).plusDays(13);
    }

    public static int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            return 0;
        }

        if (endDate.isBefore(startDate)) {
            return 0;
        }

        int workingDays = 0;
        LocalDate currentDate = startDate;
        int cachedYear = -1;
        Set<LocalDate> holidays = null;

        while (!currentDate.isAfter(endDate)) {

            if (currentDate.getYear() != cachedYear) {
                cachedYear = currentDate.getYear();
                holidays = GreekHolidayUtil.getHolidays(cachedYear);
            }

            DayOfWeek dow = currentDate.getDayOfWeek();

            if (dow != DayOfWeek.SATURDAY
                    && dow != DayOfWeek.SUNDAY
                    && !holidays.contains(currentDate)) {
                workingDays++;
            }

            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
    }
}