/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.language.bm;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.language.bm.Languages;
import org.apache.commons.codec.language.bm.NameType;
import org.apache.commons.codec.language.bm.RuleType;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Rule {
    public static final RPattern ALL_STRINGS_RMATCHER = new RPattern(){

        public boolean isMatch(CharSequence input) {
            return true;
        }
    };
    public static final String ALL = "ALL";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String HASH_INCLUDE = "#include";
    private static final Map<NameType, Map<RuleType, Map<String, List<Rule>>>> RULES = new EnumMap<NameType, Map<RuleType, Map<String, List<Rule>>>>(NameType.class);
    private final RPattern lContext;
    private final String pattern;
    private final PhonemeExpr phoneme;
    private final RPattern rContext;

    private static boolean contains(CharSequence chars, char input) {
        for (int i = 0; i < chars.length(); ++i) {
            if (chars.charAt(i) != input) continue;
            return true;
        }
        return false;
    }

    private static String createResourceName(NameType nameType, RuleType rt, String lang) {
        return String.format("org/apache/commons/codec/language/bm/%s_%s_%s.txt", nameType.getName(), rt.getName(), lang);
    }

    private static Scanner createScanner(NameType nameType, RuleType rt, String lang) {
        String resName = Rule.createResourceName(nameType, rt, lang);
        InputStream rulesIS = Languages.class.getClassLoader().getResourceAsStream(resName);
        if (rulesIS == null) {
            throw new IllegalArgumentException("Unable to load resource: " + resName);
        }
        return new Scanner(rulesIS, "UTF-8");
    }

    private static Scanner createScanner(String lang) {
        String resName = String.format("org/apache/commons/codec/language/bm/%s.txt", lang);
        InputStream rulesIS = Languages.class.getClassLoader().getResourceAsStream(resName);
        if (rulesIS == null) {
            throw new IllegalArgumentException("Unable to load resource: " + resName);
        }
        return new Scanner(rulesIS, "UTF-8");
    }

    private static boolean endsWith(CharSequence input, CharSequence suffix) {
        if (suffix.length() > input.length()) {
            return false;
        }
        int i = input.length() - 1;
        for (int j = suffix.length() - 1; j >= 0; --j) {
            if (input.charAt(i) != suffix.charAt(j)) {
                return false;
            }
            --i;
        }
        return true;
    }

    public static List<Rule> getInstance(NameType nameType, RuleType rt, Languages.LanguageSet langs) {
        return langs.isSingleton() ? Rule.getInstance(nameType, rt, langs.getAny()) : Rule.getInstance(nameType, rt, "any");
    }

    public static List<Rule> getInstance(NameType nameType, RuleType rt, String lang) {
        List<Rule> rules = RULES.get((Object)nameType).get((Object)rt).get(lang);
        if (rules == null) {
            throw new IllegalArgumentException(String.format("No rules found for %s, %s, %s.", nameType.getName(), rt.getName(), lang));
        }
        return rules;
    }

    private static Phoneme parsePhoneme(String ph) {
        int open = ph.indexOf("[");
        if (open >= 0) {
            if (!ph.endsWith("]")) {
                throw new IllegalArgumentException("Phoneme expression contains a '[' but does not end in ']'");
            }
            String before = ph.substring(0, open);
            String in = ph.substring(open + 1, ph.length() - 1);
            HashSet<String> langs = new HashSet<String>(Arrays.asList(in.split("[+]")));
            return new Phoneme(before, Languages.LanguageSet.from(langs));
        }
        return new Phoneme(ph, Languages.ANY_LANGUAGE);
    }

    private static PhonemeExpr parsePhonemeExpr(String ph) {
        if (ph.startsWith("(")) {
            if (!ph.endsWith(")")) {
                throw new IllegalArgumentException("Phoneme starts with '(' so must end with ')'");
            }
            ArrayList<Phoneme> phs = new ArrayList<Phoneme>();
            String body = ph.substring(1, ph.length() - 1);
            for (String part : body.split("[|]")) {
                phs.add(Rule.parsePhoneme(part));
            }
            if (body.startsWith("|") || body.endsWith("|")) {
                phs.add(new Phoneme("", Languages.ANY_LANGUAGE));
            }
            return new PhonemeList(phs);
        }
        return Rule.parsePhoneme(ph);
    }

    private static List<Rule> parseRules(Scanner scanner, final String location) {
        ArrayList<Rule> lines = new ArrayList<Rule>();
        int currentLine = 0;
        boolean inMultilineComment = false;
        while (scanner.hasNextLine()) {
            String rawLine;
            ++currentLine;
            String line = rawLine = scanner.nextLine();
            if (inMultilineComment) {
                if (!line.endsWith("*/")) continue;
                inMultilineComment = false;
                continue;
            }
            if (line.startsWith("/*")) {
                inMultilineComment = true;
                continue;
            }
            int cmtI = line.indexOf("//");
            if (cmtI >= 0) {
                line = line.substring(0, cmtI);
            }
            if ((line = line.trim()).length() == 0) continue;
            if (line.startsWith(HASH_INCLUDE)) {
                String incl = line.substring(HASH_INCLUDE.length()).trim();
                if (incl.contains(" ")) {
                    System.err.println("Warining: malformed import statement: " + rawLine);
                    continue;
                }
                lines.addAll(Rule.parseRules(Rule.createScanner(incl), location + "->" + incl));
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts.length != 4) {
                System.err.println("Warning: malformed rule statement split into " + parts.length + " parts: " + rawLine);
                continue;
            }
            try {
                String pat = Rule.stripQuotes(parts[0]);
                String lCon = Rule.stripQuotes(parts[1]);
                String rCon = Rule.stripQuotes(parts[2]);
                PhonemeExpr ph = Rule.parsePhonemeExpr(Rule.stripQuotes(parts[3]));
                final int cLine = currentLine;
                Rule r = new Rule(pat, lCon, rCon, ph){
                    private final int myLine;
                    private final String loc;
                    {
                        super(x0, x1, x2, x3);
                        this.myLine = cLine;
                        this.loc = location;
                    }

                    public String toString() {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Rule");
                        sb.append("{line=").append(this.myLine);
                        sb.append(", loc='").append(this.loc).append('\'');
                        sb.append('}');
                        return sb.toString();
                    }
                };
                lines.add(r);
            }
            catch (IllegalArgumentException e) {
                throw new IllegalStateException("Problem parsing line " + currentLine, e);
            }
        }
        return lines;
    }

    private static RPattern pattern(final String regex) {
        boolean endsWith;
        boolean startsWith = regex.startsWith("^");
        final String content = regex.substring(startsWith ? 1 : 0, (endsWith = regex.endsWith("$")) ? regex.length() - 1 : regex.length());
        boolean boxes = content.contains("[");
        if (!boxes) {
            if (startsWith && endsWith) {
                if (content.length() == 0) {
                    return new RPattern(){

                        public boolean isMatch(CharSequence input) {
                            return input.length() == 0;
                        }
                    };
                }
                return new RPattern(){

                    public boolean isMatch(CharSequence input) {
                        return input.equals(content);
                    }
                };
            }
            if ((startsWith || endsWith) && content.length() == 0) {
                return ALL_STRINGS_RMATCHER;
            }
            if (startsWith) {
                return new RPattern(){

                    public boolean isMatch(CharSequence input) {
                        return Rule.startsWith(input, content);
                    }
                };
            }
            if (endsWith) {
                return new RPattern(){

                    public boolean isMatch(CharSequence input) {
                        return Rule.endsWith(input, content);
                    }
                };
            }
        } else {
            String boxContent;
            boolean startsWithBox = content.startsWith("[");
            boolean endsWithBox = content.endsWith("]");
            if (startsWithBox && endsWithBox && !(boxContent = content.substring(1, content.length() - 1)).contains("[")) {
                boolean shouldMatch;
                boolean negate = boxContent.startsWith("^");
                if (negate) {
                    boxContent = boxContent.substring(1);
                }
                final String bContent = boxContent;
                boolean bl = shouldMatch = !negate;
                if (startsWith && endsWith) {
                    return new RPattern(){

                        public boolean isMatch(CharSequence input) {
                            return input.length() == 1 && Rule.contains(bContent, input.charAt(0)) == shouldMatch;
                        }
                    };
                }
                if (startsWith) {
                    return new RPattern(){

                        public boolean isMatch(CharSequence input) {
                            return input.length() > 0 && Rule.contains(bContent, input.charAt(0)) == shouldMatch;
                        }
                    };
                }
                if (endsWith) {
                    return new RPattern(){

                        public boolean isMatch(CharSequence input) {
                            return input.length() > 0 && Rule.contains(bContent, input.charAt(input.length() - 1)) == shouldMatch;
                        }
                    };
                }
            }
        }
        return new RPattern(){
            Pattern pattern;
            {
                this.pattern = Pattern.compile(regex);
            }

            public boolean isMatch(CharSequence input) {
                Matcher matcher = this.pattern.matcher(input);
                return matcher.find();
            }
        };
    }

    private static boolean startsWith(CharSequence input, CharSequence prefix) {
        if (prefix.length() > input.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); ++i) {
            if (input.charAt(i) == prefix.charAt(i)) continue;
            return false;
        }
        return true;
    }

    private static String stripQuotes(String str) {
        if (str.startsWith(DOUBLE_QUOTE)) {
            str = str.substring(1);
        }
        if (str.endsWith(DOUBLE_QUOTE)) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public Rule(String pattern, String lContext, String rContext, PhonemeExpr phoneme) {
        this.pattern = pattern;
        this.lContext = Rule.pattern(lContext + "$");
        this.rContext = Rule.pattern("^" + rContext);
        this.phoneme = phoneme;
    }

    public RPattern getLContext() {
        return this.lContext;
    }

    public String getPattern() {
        return this.pattern;
    }

    public PhonemeExpr getPhoneme() {
        return this.phoneme;
    }

    public RPattern getRContext() {
        return this.rContext;
    }

    public boolean patternAndContextMatches(CharSequence input, int i) {
        if (i < 0) {
            throw new IndexOutOfBoundsException("Can not match pattern at negative indexes");
        }
        int patternLength = this.pattern.length();
        int ipl = i + patternLength;
        if (ipl > input.length()) {
            return false;
        }
        boolean patternMatches = input.subSequence(i, ipl).equals(this.pattern);
        boolean rContextMatches = this.rContext.isMatch(input.subSequence(ipl, input.length()));
        boolean lContextMatches = this.lContext.isMatch(input.subSequence(0, i));
        return patternMatches && rContextMatches && lContextMatches;
    }

    static {
        for (NameType s : NameType.values()) {
            EnumMap rts = new EnumMap(RuleType.class);
            for (RuleType rt : RuleType.values()) {
                HashMap<String, List<Rule>> rs = new HashMap<String, List<Rule>>();
                Languages ls = Languages.getInstance(s);
                for (String l : ls.getLanguages()) {
                    try {
                        rs.put(l, Rule.parseRules(Rule.createScanner(s, rt, l), Rule.createResourceName(s, rt, l)));
                    }
                    catch (IllegalStateException e) {
                        throw new IllegalStateException("Problem processing " + Rule.createResourceName(s, rt, l), e);
                    }
                }
                if (!rt.equals((Object)RuleType.RULES)) {
                    rs.put("common", Rule.parseRules(Rule.createScanner(s, rt, "common"), Rule.createResourceName(s, rt, "common")));
                }
                rts.put(rt, Collections.unmodifiableMap(rs));
            }
            RULES.put(s, Collections.unmodifiableMap(rts));
        }
    }

    public static interface RPattern {
        public boolean isMatch(CharSequence var1);
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static final class PhonemeList
    implements PhonemeExpr {
        private final List<Phoneme> phonemes;

        public PhonemeList(List<Phoneme> phonemes) {
            this.phonemes = phonemes;
        }

        public List<Phoneme> getPhonemes() {
            return this.phonemes;
        }
    }

    public static interface PhonemeExpr {
        public Iterable<Phoneme> getPhonemes();
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static final class Phoneme
    implements PhonemeExpr {
        public static final Comparator<Phoneme> COMPARATOR = new Comparator<Phoneme>(){

            @Override
            public int compare(Phoneme o1, Phoneme o2) {
                for (int i = 0; i < o1.phonemeText.length(); ++i) {
                    if (i >= o2.phonemeText.length()) {
                        return 1;
                    }
                    int c = o1.phonemeText.charAt(i) - o2.phonemeText.charAt(i);
                    if (c == 0) continue;
                    return c;
                }
                if (o1.phonemeText.length() < o2.phonemeText.length()) {
                    return -1;
                }
                return 0;
            }
        };
        private final CharSequence phonemeText;
        private final Languages.LanguageSet languages;

        public Phoneme(CharSequence phonemeText, Languages.LanguageSet languages) {
            this.phonemeText = phonemeText;
            this.languages = languages;
        }

        public Phoneme append(CharSequence str) {
            return new Phoneme(this.phonemeText.toString() + str.toString(), this.languages);
        }

        public Languages.LanguageSet getLanguages() {
            return this.languages;
        }

        @Override
        public Iterable<Phoneme> getPhonemes() {
            return Collections.singleton(this);
        }

        public CharSequence getPhonemeText() {
            return this.phonemeText;
        }

        public Phoneme join(Phoneme right) {
            return new Phoneme(this.phonemeText.toString() + right.phonemeText.toString(), this.languages.restrictTo(right.languages));
        }

    }

}

