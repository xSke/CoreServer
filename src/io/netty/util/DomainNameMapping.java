/*
 * Decompiled with CFR 0_129.
 */
package io.netty.util;

import io.netty.util.Mapping;
import io.netty.util.internal.StringUtil;
import java.net.IDN;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainNameMapping<V>
implements Mapping<String, V> {
    private static final Pattern DNS_WILDCARD_PATTERN = Pattern.compile("^\\*\\..*");
    private final Map<String, V> map;
    private final V defaultValue;

    public DomainNameMapping(V defaultValue) {
        this(4, defaultValue);
    }

    public DomainNameMapping(int initialCapacity, V defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }
        this.map = new LinkedHashMap<String, V>(initialCapacity);
        this.defaultValue = defaultValue;
    }

    public DomainNameMapping<V> add(String hostname, V output) {
        if (hostname == null) {
            throw new NullPointerException("input");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.map.put(DomainNameMapping.normalizeHostname(hostname), output);
        return this;
    }

    private static boolean matches(String hostNameTemplate, String hostName) {
        if (DNS_WILDCARD_PATTERN.matcher(hostNameTemplate).matches()) {
            return hostNameTemplate.substring(2).equals(hostName) || hostName.endsWith(hostNameTemplate.substring(1));
        }
        return hostNameTemplate.equals(hostName);
    }

    private static String normalizeHostname(String hostname) {
        if (DomainNameMapping.needsNormalization(hostname)) {
            hostname = IDN.toASCII(hostname, 1);
        }
        return hostname.toLowerCase(Locale.US);
    }

    private static boolean needsNormalization(String hostname) {
        int length = hostname.length();
        for (int i = 0; i < length; ++i) {
            char c = hostname.charAt(i);
            if (c <= '') continue;
            return true;
        }
        return false;
    }

    @Override
    public V map(String input) {
        if (input != null) {
            input = DomainNameMapping.normalizeHostname(input);
            for (Map.Entry<String, V> entry : this.map.entrySet()) {
                if (!DomainNameMapping.matches(entry.getKey(), input)) continue;
                return entry.getValue();
            }
        }
        return this.defaultValue;
    }

    public String toString() {
        return StringUtil.simpleClassName(this) + "(default: " + this.defaultValue + ", map: " + this.map + ')';
    }
}

