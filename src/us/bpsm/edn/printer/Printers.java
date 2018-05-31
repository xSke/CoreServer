/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.printer;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.UUID;
import us.bpsm.edn.EdnException;
import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.Tag;
import us.bpsm.edn.TaggedValue;
import us.bpsm.edn.parser.InstantUtils;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.printer.Printer;
import us.bpsm.edn.protocols.Protocol;
import us.bpsm.edn.protocols.Protocols;
import us.bpsm.edn.util.CharClassify;

public class Printers {
    private static final ThreadLocal<PrettyPrintContext> PRETTY_PRINT_CONTEXT = new ThreadLocal();

    private Printers() {
        throw new UnsupportedOperationException();
    }

    public static Printer newPrinter(Appendable out) {
        return Printers.newPrinter(Printers.defaultPrinterProtocol(), out);
    }

    public static String printString(Object ednValue) {
        return Printers.printString(Printers.defaultPrinterProtocol(), ednValue);
    }

    public static String printString(Protocol<Printer.Fn<?>> fns, Object ednValue) {
        StringBuilder sb = new StringBuilder();
        Printers.newPrinter(fns, sb).printValue(ednValue);
        return sb.toString();
    }

    public static Printer newPrinter(final Protocol<Printer.Fn<?>> fns, final Appendable out) {
        return new Printer(){
            int softspace = 0;

            @Override
            public void close() {
                if (out instanceof Closeable) {
                    try {
                        ((Closeable)((Object)out)).close();
                    }
                    catch (IOException e) {
                        throw new EdnIOException(e);
                    }
                }
            }

            @Override
            public Printer append(CharSequence csq) {
                try {
                    if (this.softspace > 1 && csq.length() > 0 && !CharClassify.isWhitespace(csq.charAt(0))) {
                        out.append(' ');
                    }
                    this.softspace = 0;
                    out.append(csq);
                    return this;
                }
                catch (IOException e) {
                    throw new EdnIOException(e);
                }
            }

            @Override
            public Printer append(char c) {
                try {
                    if (this.softspace > 1 && !CharClassify.isWhitespace(c)) {
                        out.append(' ');
                    }
                    this.softspace = 0;
                    out.append(c);
                    return this;
                }
                catch (IOException e) {
                    throw new EdnIOException(e);
                }
            }

            @Override
            public Printer printValue(Object ednValue) {
                Printer.Fn printFn = (Printer.Fn)fns.lookup(Printers.getClassOrNull(ednValue));
                if (printFn == null) {
                    throw new EdnException(String.format("Don't know how to write '%s' of type '%s'", ednValue, Printers.getClassOrNull(ednValue)));
                }
                printFn.eval(ednValue, this);
                return this;
            }

            @Override
            public Printer softspace() {
                ++this.softspace;
                return this;
            }
        };
    }

    static Class<?> getClassOrNull(Object o) {
        return o == null ? null : o.getClass();
    }

    public static Protocol.Builder<Printer.Fn<?>> defaultProtocolBuilder() {
        return Protocols.builder("print").put(null, Printers.writeNullFn()).put(BigDecimal.class, Printers.writeBigDecimalFn()).put(BigInteger.class, Printers.writeBigIntegerFn()).put(Boolean.class, Printers.writeBooleanFn()).put(Byte.class, Printers.writeLongValueFn()).put(CharSequence.class, Printers.writeCharSequenceFn()).put(Character.class, Printers.writeCharacterFn()).put(Date.class, Printers.writeDateFn()).put(Double.class, Printers.writeDoubleValueFn()).put(Float.class, Printers.writeDoubleValueFn()).put(GregorianCalendar.class, Printers.writeCalendarFn()).put(Integer.class, Printers.writeLongValueFn()).put(Keyword.class, Printers.writeKeywordFn()).put(List.class, Printers.writeListFn()).put(Long.class, Printers.writeLongValueFn()).put(Map.class, Printers.writeMapFn()).put(Set.class, Printers.writeSetFn()).put(Short.class, Printers.writeLongValueFn()).put(Symbol.class, Printers.writeSymbolFn()).put(Tag.class, Printers.writeTagFn()).put(TaggedValue.class, Printers.writeTaggedValueFn()).put(Timestamp.class, Printers.writeTimestampFn()).put(UUID.class, Printers.writeUuidFn());
    }

    public static Protocol<Printer.Fn<?>> defaultPrinterProtocol() {
        return Printers.defaultProtocolBuilder().build();
    }

    static Printer.Fn<Void> writeNullFn() {
        return new Printer.Fn<Void>(){

            @Override
            public void eval(Void self, Printer writer) {
                writer.softspace().append("nil").softspace();
            }
        };
    }

    static Printer.Fn<List<?>> writeListFn() {
        return new Printer.Fn<List<?>>(){

            @Override
            public void eval(List<?> self, Printer writer) {
                boolean vec = self instanceof RandomAccess;
                writer.append(vec ? '[' : '(');
                for (Object o : self) {
                    writer.printValue(o);
                }
                writer.append(vec ? ']' : ')');
            }
        };
    }

    static Printer.Fn<Set<?>> writeSetFn() {
        return new Printer.Fn<Set<?>>(){

            @Override
            public void eval(Set<?> self, Printer writer) {
                writer.append("#{");
                for (Object o : self) {
                    writer.printValue(o);
                }
                writer.append('}');
            }
        };
    }

    static Printer.Fn<Map<?, ?>> writeMapFn() {
        return new Printer.Fn<Map<?, ?>>(){

            @Override
            public void eval(Map<?, ?> self, Printer writer) {
                writer.append('{');
                for (Map.Entry p : self.entrySet()) {
                    writer.printValue(p.getKey()).printValue(p.getValue());
                }
                writer.append('}');
            }
        };
    }

    static Printer.Fn<Keyword> writeKeywordFn() {
        return new Printer.Fn<Keyword>(){

            @Override
            public void eval(Keyword self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

    static Printer.Fn<Symbol> writeSymbolFn() {
        return new Printer.Fn<Symbol>(){

            @Override
            public void eval(Symbol self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

    static Printer.Fn<TaggedValue> writeTaggedValueFn() {
        return new Printer.Fn<TaggedValue>(){

            @Override
            public void eval(TaggedValue self, Printer writer) {
                writer.printValue(self.getTag()).printValue(self.getValue());
            }
        };
    }

    static Printer.Fn<Boolean> writeBooleanFn() {
        return new Printer.Fn<Boolean>(){

            @Override
            public void eval(Boolean self, Printer writer) {
                writer.softspace().append(self != false ? "true" : "false").softspace();
            }
        };
    }

    static Printer.Fn<CharSequence> writeCharSequenceFn() {
        return new Printer.Fn<CharSequence>(){

            @Override
            public void eval(CharSequence self, Printer writer) {
                writer.append('\"');
                block9 : for (int i = 0; i < self.length(); ++i) {
                    char c = self.charAt(i);
                    switch (c) {
                        case '\"': {
                            writer.append('\\').append('\"');
                            continue block9;
                        }
                        case '\b': {
                            writer.append('\\').append('b');
                            continue block9;
                        }
                        case '\t': {
                            writer.append('\\').append('t');
                            continue block9;
                        }
                        case '\n': {
                            writer.append('\\').append('n');
                            continue block9;
                        }
                        case '\r': {
                            writer.append('\\').append('r');
                            continue block9;
                        }
                        case '\f': {
                            writer.append('\\').append('f');
                            continue block9;
                        }
                        case '\\': {
                            writer.append('\\').append('\\');
                            continue block9;
                        }
                        default: {
                            writer.append(c);
                        }
                    }
                }
                writer.append('\"');
            }
        };
    }

    static Printer.Fn<Character> writeCharacterFn() {
        return new Printer.Fn<Character>(){

            @Override
            public void eval(Character self, Printer writer) {
                char c = self.charValue();
                if (!CharClassify.isWhitespace(c)) {
                    writer.append('\\').append(c);
                } else {
                    switch (c) {
                        case '\b': {
                            writer.append("\\backspace");
                            break;
                        }
                        case '\t': {
                            writer.append("\\tab");
                            break;
                        }
                        case '\n': {
                            writer.append("\\newline");
                            break;
                        }
                        case '\r': {
                            writer.append("\\return");
                            break;
                        }
                        case '\f': {
                            writer.append("\\formfeed");
                            break;
                        }
                        case ' ': {
                            writer.append("\\space");
                            break;
                        }
                        default: {
                            throw new EdnException("Whitespace character 0x" + Integer.toHexString(c) + " is unsupported.");
                        }
                    }
                }
                writer.softspace();
            }
        };
    }

    static Printer.Fn<Number> writeLongValueFn() {
        return new Printer.Fn<Number>(){

            @Override
            public void eval(Number self, Printer writer) {
                writer.softspace().append(String.valueOf(self.longValue())).softspace();
            }
        };
    }

    static Printer.Fn<BigInteger> writeBigIntegerFn() {
        return new Printer.Fn<BigInteger>(){

            @Override
            public void eval(BigInteger self, Printer writer) {
                writer.softspace().append(self.toString()).append('N').softspace();
            }
        };
    }

    static Printer.Fn<Number> writeDoubleValueFn() {
        return new Printer.Fn<Number>(){

            @Override
            public void eval(Number self, Printer writer) {
                writer.softspace().append(String.valueOf(self.doubleValue())).softspace();
            }
        };
    }

    static Printer.Fn<BigDecimal> writeBigDecimalFn() {
        return new Printer.Fn<BigDecimal>(){

            @Override
            public void eval(BigDecimal self, Printer writer) {
                writer.softspace().append(self.toString()).append('M').softspace();
            }
        };
    }

    static Printer.Fn<UUID> writeUuidFn() {
        return new Printer.Fn<UUID>(){

            @Override
            public void eval(UUID self, Printer writer) {
                writer.printValue(Parser.Config.EDN_UUID).printValue(self.toString());
            }
        };
    }

    static Printer.Fn<Date> writeDateFn() {
        return new Printer.Fn<Date>(){

            @Override
            public void eval(Date self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT).printValue(InstantUtils.dateToString(self));
            }
        };
    }

    static Printer.Fn<Timestamp> writeTimestampFn() {
        return new Printer.Fn<Timestamp>(){

            @Override
            public void eval(Timestamp self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT).printValue(InstantUtils.timestampToString(self));
            }
        };
    }

    static Printer.Fn<GregorianCalendar> writeCalendarFn() {
        return new Printer.Fn<GregorianCalendar>(){

            @Override
            public void eval(GregorianCalendar self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT).printValue(InstantUtils.calendarToString(self));
            }
        };
    }

    static Printer.Fn<Tag> writeTagFn() {
        return new Printer.Fn<Tag>(){

            @Override
            public void eval(Tag self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

    static void printIndent(Printer p) {
        PrettyPrintContext cx = PRETTY_PRINT_CONTEXT.get();
        p.append(cx.indents.get(cx.depth));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void withPretty(Runnable r) {
        boolean shouldInit;
        boolean bl = shouldInit = PRETTY_PRINT_CONTEXT.get() == null;
        if (shouldInit) {
            PRETTY_PRINT_CONTEXT.set(new PrettyPrintContext());
            try {
                r.run();
            }
            finally {
                PRETTY_PRINT_CONTEXT.remove();
            }
        } else {
            r.run();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void runIndented(Runnable r) {
        PrettyPrintContext cx = PRETTY_PRINT_CONTEXT.get();
        assert (cx.depth < cx.indents.size());
        if (cx.indents.size() - cx.depth == 1) {
            cx.indents.add(cx.indents.get(cx.depth) + cx.basicIndent);
        }
        ++cx.depth;
        assert (cx.depth < cx.indents.size());
        try {
            r.run();
        }
        finally {
            --cx.depth;
        }
    }

    static Printer.Fn<List<?>> prettyWriteListFn() {
        return new Printer.Fn<List<?>>(){

            @Override
            public void eval(final List<?> self, final Printer writer) {
                Printers.withPretty(new Runnable(){

                    @Override
                    public void run() {
                        boolean vec = self instanceof RandomAccess;
                        writer.append(vec ? '[' : '(');
                        writer.append("\n");
                        Printers.runIndented(new Runnable(){

                            @Override
                            public void run() {
                                for (Object o : self) {
                                    Printers.printIndent(writer);
                                    writer.printValue(o);
                                    writer.append("\n");
                                }
                            }
                        });
                        Printers.printIndent(writer);
                        writer.append(vec ? ']' : ')');
                    }

                });
            }

        };
    }

    static Printer.Fn<Set<?>> prettyWriteSetFn() {
        return new Printer.Fn<Set<?>>(){

            @Override
            public void eval(final Set<?> self, final Printer writer) {
                Printers.withPretty(new Runnable(){

                    @Override
                    public void run() {
                        writer.append("#{");
                        writer.append("\n");
                        Printers.runIndented(new Runnable(){

                            @Override
                            public void run() {
                                for (Object o : self) {
                                    Printers.printIndent(writer);
                                    writer.printValue(o);
                                    writer.append("\n");
                                }
                            }
                        });
                        Printers.printIndent(writer);
                        writer.append("}");
                    }

                });
            }

        };
    }

    static Printer.Fn<Map<?, ?>> prettyWriteMapFn() {
        return new Printer.Fn<Map<?, ?>>(){

            @Override
            public void eval(final Map<?, ?> self, final Printer writer) {
                Printers.withPretty(new Runnable(){

                    @Override
                    public void run() {
                        writer.append("{");
                        writer.append("\n");
                        Printers.runIndented(new Runnable(){

                            @Override
                            public void run() {
                                for (Map.Entry o : self.entrySet()) {
                                    Printers.printIndent(writer);
                                    writer.printValue(o.getKey());
                                    writer.softspace();
                                    writer.softspace();
                                    writer.printValue(o.getValue());
                                    writer.append("\n");
                                }
                            }
                        });
                        Printers.printIndent(writer);
                        writer.append("}");
                    }

                });
            }

        };
    }

    public static Protocol.Builder<Printer.Fn<?>> prettyProtocolBuilder() {
        return Printers.defaultProtocolBuilder().put(Map.class, Printers.prettyWriteMapFn()).put(Set.class, Printers.prettyWriteSetFn()).put(List.class, Printers.prettyWriteListFn());
    }

    public static Protocol<Printer.Fn<?>> prettyPrinterProtocol() {
        return Printers.prettyProtocolBuilder().build();
    }

    static final class PrettyPrintContext {
        int depth = 0;
        String basicIndent = "  ";
        List<String> indents = new ArrayList<String>(Arrays.asList(""));

        PrettyPrintContext() {
        }
    }

}

