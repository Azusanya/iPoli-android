package io.ipoli.android.app.ui.formatters;

import android.content.Context;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.TextStyle;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.ipoli.android.R;
import io.ipoli.android.app.utils.DateUtils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class DateFormatter {
    private static final String DEFAULT_EMPTY_VALUE = "Don't know";
    private static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd MMM yy", Locale.getDefault());
    private static SimpleDateFormat DATE_NO_YEAR_FORMAT = new SimpleDateFormat("dd MMM", Locale.getDefault());

    public static String format(Context context, LocalDate date) {
        if (date == null) {
            return DEFAULT_EMPTY_VALUE;
        }
        if (DateUtils.isToday(date)) {
            return context.getString(R.string.today);
        }
        if (DateUtils.isTomorrow(date)) {
            return context.getString(R.string.tomorrow);
        }
        return DEFAULT_DATE_FORMAT.format(DateUtils.toStartOfDay(date));
    }

    public static String formatWithoutYear(Context context, LocalDate date, String emptyValue, LocalDate currentDate) {
        if (date == null) {
            return emptyValue;
        }
        if (DateUtils.isToday(date)) {
            return context.getString(R.string.today);
        }
        if (DateUtils.isTomorrow(date)) {
            return context.getString(R.string.tomorrow);
        }
        if (DateUtils.isYesterday(date)) {
            return context.getString(R.string.yesterday);
        }
        if (currentDate != null) {
            if (currentDate.with(DayOfWeek.MONDAY).isEqual(date.with(DayOfWeek.MONDAY))) {
                return date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
            }
        }
        return DATE_NO_YEAR_FORMAT.format(DateUtils.toStartOfDay(date));
    }

    public static String formatWithoutYear(Context context, LocalDate date) {
        return formatWithoutYear(context, date, DEFAULT_EMPTY_VALUE, null);
    }

    public static String formatWithoutYear(Context context, LocalDate date, String emptyValue) {
        return formatWithoutYear(context, date, emptyValue, null);
    }

    public static String formatWithoutYear(Context context, LocalDate date, LocalDate currentDate) {
        return formatWithoutYear(context, date, DEFAULT_EMPTY_VALUE, currentDate);
    }

    public static String formatWithoutYearSimple(LocalDate date) {
        return DATE_NO_YEAR_FORMAT.format(DateUtils.toStartOfDay(date));
    }
}