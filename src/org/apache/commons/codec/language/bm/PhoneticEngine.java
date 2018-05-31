/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.language.bm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.codec.language.bm.Lang;
import org.apache.commons.codec.language.bm.Languages;
import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.Rule;
import org.apache.commons.codec.language.bm.RuleType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class PhoneticEngine {
    private static final Map<NameType, Set<String>> NAME_PREFIXES = new EnumMap<NameType, Set<String>>(NameType.class);
    private final Lang lang;
    private final NameType nameType;
    private final RuleType ruleType;
    private final boolean concat;

    private static CharSequence cacheSubSequence(final CharSequence cached) {
        final CharSequence[][] cache = new CharSequence[cached.length()][cached.length()];
        return new CharSequence(){

            public char charAt(int index) {
                return cached.charAt(index);
            }

            public int length() {
                return cached.length();
            }

            public CharSequence subSequence(int start, int end) {
                if (start == end) {
                    return "";
                }
                CharSequence res = cache[start][end - 1];
                if (res == null) {
                    cache[start][end - 1] = res = cached.subSequence(start, end);
                }
                return res;
            }
        };
    }

    private static String join(Iterable<String> strings, String sep) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> si = strings.iterator();
        if (si.hasNext()) {
            sb.append(si.next());
        }
        while (si.hasNext()) {
            sb.append(sep).append(si.next());
        }
        return sb.toString();
    }

    public PhoneticEngine(NameType nameType, RuleType ruleType, boolean concat) {
        if (ruleType == RuleType.RULES) {
            throw new IllegalArgumentException("ruleType must not be " + (Object)((Object)RuleType.RULES));
        }
        this.nameType = nameType;
        this.ruleType = ruleType;
        this.concat = concat;
        this.lang = Lang.instance(nameType);
    }

    private PhonemeBuilder applyFinalRules(PhonemeBuilder phonemeBuilder, List<Rule> finalRules) {
        if (finalRules == null) {
            throw new NullPointerException("finalRules can not be null");
        }
        if (finalRules.isEmpty()) {
            return phonemeBuilder;
        }
        TreeSet<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(Rule.Phoneme.COMPARATOR);
        for (Rule.Phoneme phoneme : phonemeBuilder.getPhonemes()) {
            PhonemeBuilder subBuilder = PhonemeBuilder.empty(phoneme.getLanguages());
            CharSequence phonemeText = PhoneticEngine.cacheSubSequence(phoneme.getPhonemeText());
            int i = 0;
            while (i < phonemeText.length()) {
                RulesApplication rulesApplication = new RulesApplication(finalRules, phonemeText, subBuilder, i).invoke();
                boolean found = rulesApplication.isFound();
                subBuilder = rulesApplication.getPhonemeBuilder();
                if (!found) {
                    subBuilder = subBuilder.append(phonemeText.subSequence(i, i + 1));
                }
                i = rulesApplication.getI();
            }
            phonemes.addAll(subBuilder.getPhonemes());
        }
        return new PhonemeBuilder(phonemes);
    }

    public String encode(String input) {
        Languages.LanguageSet languageSet = this.lang.guessLanguages(input);
        return this.encode(input, languageSet);
    }

    public String encode(String input, Languages.LanguageSet languageSet) {
        List<Rule> rules = Rule.getInstance(this.nameType, RuleType.RULES, languageSet);
        List<Rule> finalRules1 = Rule.getInstance(this.nameType, this.ruleType, "common");
        List<Rule> finalRules2 = Rule.getInstance(this.nameType, this.ruleType, languageSet);
        input = input.toLowerCase(Locale.ENGLISH).replace('-', ' ').trim();
        if (this.nameType == NameType.GENERIC) {
            if (input.length() >= 2 && input.substring(0, 2).equals("d'")) {
                String remainder = input.substring(2);
                String combined = "d" + remainder;
                return "(" + this.encode(remainder) + ")-(" + this.encode(combined) + ")";
            }
            for (String l : NAME_PREFIXES.get((Object)this.nameType)) {
                if (!input.startsWith(l + " ")) continue;
                String remainder = input.substring(l.length() + 1);
                String combined = l + remainder;
                return "(" + this.encode(remainder) + ")-(" + this.encode(combined) + ")";
            }
        }
        List<String> words = Arrays.asList(input.split("\\s+"));
        ArrayList<String> words2 = new ArrayList<String>();
        switch (this.nameType) {
            case SEPHARDIC: {
                for (String aWord : words) {
                    String[] parts = aWord.split("'");
                    String lastPart = parts[parts.length - 1];
                    words2.add(lastPart);
                }
                words2.removeAll((Collection)NAME_PREFIXES.get((Object)this.nameType));
                break;
            }
            case ASHKENAZI: {
                words2.addAll(words);
                words2.removeAll((Collection)NAME_PREFIXES.get((Object)this.nameType));
                break;
            }
            case GENERIC: {
                words2.addAll(words);
                break;
            }
            default: {
                throw new IllegalStateException("Unreachable case: " + (Object)((Object)this.nameType));
            }
        }
        if (this.concat) {
            input = PhoneticEngine.join(words2, " ");
        } else if (words2.size() == 1) {
            input = words.iterator().next();
        } else {
            StringBuilder result = new StringBuilder();
            for (String word : words2) {
                result.append("-").append(this.encode(word));
            }
            return result.substring(1);
        }
        PhonemeBuilder phonemeBuilder = PhonemeBuilder.empty(languageSet);
        CharSequence inputCache = PhoneticEngine.cacheSubSequence(input);
        int i = 0;
        while (i < inputCache.length()) {
            RulesApplication rulesApplication = new RulesApplication(rules, inputCache, phonemeBuilder, i).invoke();
            i = rulesApplication.getI();
            phonemeBuilder = rulesApplication.getPhonemeBuilder();
        }
        phonemeBuilder = this.applyFinalRules(phonemeBuilder, finalRules1);
        phonemeBuilder = this.applyFinalRules(phonemeBuilder, finalRules2);
        return phonemeBuilder.makeString();
    }

    public Lang getLang() {
        return this.lang;
    }

    public NameType getNameType() {
        return this.nameType;
    }

    public RuleType getRuleType() {
        return this.ruleType;
    }

    public boolean isConcat() {
        return this.concat;
    }

    static {
        NAME_PREFIXES.put(NameType.ASHKENAZI, Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("bar", "ben", "da", "de", "van", "von"))));
        NAME_PREFIXES.put(NameType.SEPHARDIC, Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("al", "el", "da", "dal", "de", "del", "dela", "de la", "della", "des", "di", "do", "dos", "du", "van", "von"))));
        NAME_PREFIXES.put(NameType.GENERIC, Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("da", "dal", "de", "del", "dela", "de la", "della", "des", "di", "do", "dos", "du", "van", "von"))));
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    private static final class RulesApplication {
        private final List<Rule> finalRules;
        private final CharSequence input;
        private PhonemeBuilder phonemeBuilder;
        private int i;
        private boolean found;

        public RulesApplication(List<Rule> finalRules, CharSequence input, PhonemeBuilder phonemeBuilder, int i) {
            if (finalRules == null) {
                throw new NullPointerException("The finalRules argument must not be null");
            }
            this.finalRules = finalRules;
            this.phonemeBuilder = phonemeBuilder;
            this.input = input;
            this.i = i;
        }

        public int getI() {
            return this.i;
        }

        public PhonemeBuilder getPhonemeBuilder() {
            return this.phonemeBuilder;
        }

        public RulesApplication invoke() {
            this.found = false;
            int patternLength = 0;
            for (Rule rule : this.finalRules) {
                String pattern = rule.getPattern();
                patternLength = pattern.length();
                if (!rule.patternAndContextMatches(this.input, this.i)) continue;
                this.phonemeBuilder = this.phonemeBuilder.apply(rule.getPhoneme());
                this.found = true;
                break;
            }
            if (!this.found) {
                patternLength = 1;
            }
            this.i += patternLength;
            return this;
        }

        public boolean isFound() {
            return this.found;
        }
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static final class PhonemeBuilder {
        private final Set<Rule.Phoneme> phonemes;

        public static PhonemeBuilder empty(Languages.LanguageSet languages) {
            return new PhonemeBuilder(Collections.singleton(new Rule.Phoneme("", languages)));
        }

        private PhonemeBuilder(Set<Rule.Phoneme> phonemes) {
            this.phonemes = phonemes;
        }

        public PhonemeBuilder append(CharSequence str) {
            HashSet<Rule.Phoneme> newPhonemes = new HashSet<Rule.Phoneme>();
            for (Rule.Phoneme ph : this.phonemes) {
                newPhonemes.add(ph.append(str));
            }
            return new PhonemeBuilder(newPhonemes);
        }

        public PhonemeBuilder apply(Rule.PhonemeExpr phonemeExpr) {
            HashSet<Rule.Phoneme> newPhonemes = new HashSet<Rule.Phoneme>();
            for (Rule.Phoneme left : this.phonemes) {
                for (Rule.Phoneme right : phonemeExpr.getPhonemes()) {
                    Rule.Phoneme join = left.join(right);
                    if (join.getLanguages().isEmpty()) continue;
                    newPhonemes.add(join);
                }
            }
            return new PhonemeBuilder(newPhonemes);
        }

        public Set<Rule.Phoneme> getPhonemes() {
            return this.phonemes;
        }

        public String makeString() {
            StringBuilder sb = new StringBuilder();
            for (Rule.Phoneme ph : this.phonemes) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(ph.getPhonemeText());
            }
            return sb.toString();
        }
    }

}

