package com.airtribe.jobschedular.util.enums;


import lombok.Getter;

@Getter
public enum SupportedTimezone {

    UTC("UTC"),

    IST("Asia/Kolkata");

    private final String zoneId;

    SupportedTimezone(String zoneId) {
        this.zoneId = zoneId;
    }
}
