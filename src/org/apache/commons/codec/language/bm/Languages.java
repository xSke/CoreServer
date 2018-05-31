/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.codec.language.bm.NameType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Languages {
    public static final String ANY = "any";
    private static final Map<NameType, Languages> LANGUAGES = new EnumMap<NameType, Languages>(NameType.class);
    private final Set<String> languages;
    public static final LanguageSet NO_LANGUAGES;
    public static final LanguageSet ANY_LANGUAGE;

    public static Languages getInstance(NameType nameType) {
        return LANGUAGES.get((Object)nameType);
    }

    public static Languages getInstance(String languagesResourceName) {
        HashSet<String> ls = new HashSet<String>();
        InputStream langIS = Languages.class.getClassLoader().getResourceAsStream(languagesResourceName);
        if (langIS == null) {
            throw new IllegalArgumentException("Unable to resolve required resource: " + languagesResourceName);
        }
        Scanner lsScanner = new Scanner(langIS, "UTF-8");
        boolean inExtendedComment = false;
        while (lsScanner.hasNextLine()) {
            String line = lsScanner.nextLine().trim();
            if (inExtendedComment) {
                if (!line.endsWith("*/")) continue;
                inExtendedComment = false;
                continue;
            }
            if (line.startsWith("/*")) {
                inExtendedComment = true;
                continue;
            }
            if (line.length() <= 0) continue;
            ls.add(line);
        }
        return new Languages(Collections.unmodifiableSet(ls));
    }

    private static String langResourceName(NameType nameType) {
        return String.format("org/apache/commons/codec/language/bm/%s_languages.txt", nameType.getName());
    }

    private Languages(Set<String> languages) {
        this.languages = languages;
    }

    public Set<String> getLanguages() {
        return this.languages;
    }

    static {
        for (NameType s : NameType.values()) {
            LANGUAGES.put(s, Languages.getInstance(Languages.langResourceName(s)));
        }
        NO_LANGUAGES = new LanguageSet(){

            public boolean contains(String language) {
                return false;
            }

            public String getAny() {
                throw new NoSuchElementException("Can't fetch any language from the empty language set.");
            }

            public boolean isEmpty() {
                return true;
            }

            public boolean isSingleton() {
                return false;
            }

            public LanguageSet restrictTo(LanguageSet other) {
                return this;
            }

            public String toString() {
                return "NO_LANGUAGES";
            }
        };
        ANY_LANGUAGE = new LanguageSet(){

            public boolean contains(String language) {
                return true;
            }

            public String getAny() {
                throw new NoSuchElementException("Can't fetch any language from the any language set.");
            }

            public boolean isEmpty() {
                return false;
            }

            public boolean isSingleton() {
                return false;
            }

            public LanguageSet restrictTo(LanguageSet other) {
                return other;
            }

            public String toString() {
                return "ANY_LANGUAGE";
            }
        };
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static final class SomeLanguages
    extends LanguageSet {
        private final Set<String> languages;

        private SomeLanguages(Set<String> languages) {
            this.languages = Collections.unmodifiableSet(languages);
        }

        @Override
        public boolean contains(String language) {
            return this.languages.contains(language);
        }

        @Override
        public String getAny() {
            return this.languages.iterator().next();
        }

        public Set<String> getLanguages() {
            return this.languages;
        }

        @Override
        public boolean isEmpty() {
            return this.languages.isEmpty();
        }

        @Override
        public boolean isSingleton() {
            return this.languages.size() == 1;
        }

        @Override
        public LanguageSet restrictTo(LanguageSet other) {
            if (other == Languages.NO_LANGUAGES) {
                return other;
            }
            if (other == Languages.ANY_LANGUAGE) {
                return this;
            }
            SomeLanguages sl = (SomeLanguages)other;
            if (sl.languages.containsAll(this.languages)) {
                return this;
            }
            HashSet<String> ls = new HashSet<String>(this.languages);
            ls.retainAll(sl.languages);
            return SomeLanguages.from(ls);
        }

        public String toString() {
            return "Languages(" + this.languages.toString() + ")";
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static abstract class LanguageSet {
        public static LanguageSet from(Set<String> langs) {
            return langs.isEmpty() ? Languages.NO_LANGUAGES : new SomeLanguages(langs);
        }

        public abstract boolean contains(String var1);

        public abstract String getAny();

        public abstract boolean isEmpty();

        public abstract boolean isSingleton();

        public abstract LanguageSet restrictTo(LanguageSet var1);
    }

}

