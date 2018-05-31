/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

public class ParsedInstant {
    public final int years;
    public final int months;
    public final int days;
    public final int hours;
    public final int minutes;
    public final int seconds;
    public final int nanoseconds;
    public final int offsetSign;
    public final int offsetHours;
    public final int offsetMinutes;

    public ParsedInstant(int years, int months, int days, int hours, int minutes, int seconds, int nanoseconds, int offsetSign, int offsetHours, int offsetMinutes) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.nanoseconds = nanoseconds;
        this.offsetSign = offsetSign;
        this.offsetHours = offsetHours;
        this.offsetMinutes = offsetMinutes;
    }

    public String toString() {
        Object[] arrobject = new Object[10];
        arrobject[0] = this.years;
        arrobject[1] = this.months;
        arrobject[2] = this.days;
        arrobject[3] = this.hours;
        arrobject[4] = this.minutes;
        arrobject[5] = this.seconds;
        arrobject[6] = this.nanoseconds;
        arrobject[7] = this.offsetSign > 0 ? "+" : "-";
        arrobject[8] = this.offsetHours;
        arrobject[9] = this.offsetMinutes;
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%09d%s%02d:%02d", arrobject);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.days;
        result = 31 * result + this.hours;
        result = 31 * result + this.minutes;
        result = 31 * result + this.months;
        result = 31 * result + this.nanoseconds;
        result = 31 * result + this.offsetHours;
        result = 31 * result + this.offsetMinutes;
        result = 31 * result + this.offsetSign;
        result = 31 * result + this.seconds;
        result = 31 * result + this.years;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ParsedInstant other = (ParsedInstant)obj;
        if (this.days != other.days) {
            return false;
        }
        if (this.hours != other.hours) {
            return false;
        }
        if (this.minutes != other.minutes) {
            return false;
        }
        if (this.months != other.months) {
            return false;
        }
        if (this.nanoseconds != other.nanoseconds) {
            return false;
        }
        if (this.offsetHours != other.offsetHours) {
            return false;
        }
        if (this.offsetMinutes != other.offsetMinutes) {
            return false;
        }
        if (this.offsetSign != other.offsetSign) {
            return false;
        }
        if (this.seconds != other.seconds) {
            return false;
        }
        if (this.years != other.years) {
            return false;
        }
        return true;
    }
}

