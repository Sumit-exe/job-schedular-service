package com.airtribe.jobschedular.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BucketUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");

    public static String getCurrentBucket() {

        return LocalDateTime.now(ZoneOffset.UTC)
                .format(FORMATTER);
    }

    public static String getBucket(LocalDateTime time) {

        return time.format(FORMATTER);
    }
}