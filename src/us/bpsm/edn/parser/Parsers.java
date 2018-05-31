/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import java.io.Closeable;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.CollectionBuilder;
import us.bpsm.edn.parser.DefaultListFactory;
import us.bpsm.edn.parser.DefaultMapFactory;
import us.bpsm.edn.parser.DefaultSetFactory;
import us.bpsm.edn.parser.DefaultVectorFactory;
import us.bpsm.edn.parser.InstantToDate;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.ParserImpl;
import us.bpsm.edn.parser.Scanner;
import us.bpsm.edn.parser.ScannerImpl;
import us.bpsm.edn.parser.TagHandler;
import us.bpsm.edn.parser.UuidHandler;

public class Parsers {
    static final CollectionBuilder.Factory DEFAULT_LIST_FACTORY = new DefaultListFactory();
    static final CollectionBuilder.Factory DEFAULT_VECTOR_FACTORY = new DefaultVectorFactory();
    static final CollectionBuilder.Factory DEFAULT_SET_FACTORY = new DefaultSetFactory();
    static final CollectionBuilder.Factory DEFAULT_MAP_FACTORY = new DefaultMapFactory();
    static final TagHandler INSTANT_TO_DATE = new InstantToDate();
    static final TagHandler UUID_HANDLER = new UuidHandler();
    static final TagHandler IDENTITY = new TagHandler(){

        @Override
        public Object transform(Tag tag, Object value) {
            return value;
        }
    };
    static final int BUFFER_SIZE = 4096;
    static Parser.Config DEFAULT_CONFIGURATION = Parsers.newParserConfigBuilder().build();

    private Parsers() {
        throw new UnsupportedOperationException();
    }

    public static Parser newParser(Parser.Config cfg) {
        return new ParserImpl(cfg, new ScannerImpl(cfg));
    }

    static boolean readIntoBuffer(CharBuffer b, Readable r) throws IOException {
        b.clear();
        int n = r.read(b);
        b.flip();
        return n > 0;
    }

    static CharBuffer emptyBuffer() {
        CharBuffer b = CharBuffer.allocate(4096);
        b.limit(0);
        return b;
    }

    public static Parseable newParseable(final CharSequence cs) {
        return new Parseable(){
            int i = 0;

            @Override
            public void close() throws IOException {
            }

            @Override
            public int read() throws IOException {
                try {
                    return cs.charAt(this.i++);
                }
                catch (IndexOutOfBoundsException _) {
                    return -1;
                }
            }

            @Override
            public void unread(int ch) throws IOException {
                --this.i;
            }
        };
    }

    public static Parseable newParseable(final Readable r) {
        return new Parseable(){
            CharBuffer buff = Parsers.emptyBuffer();
            int unread = Integer.MIN_VALUE;
            boolean end = false;
            boolean closed = false;

            @Override
            public void close() throws IOException {
                this.closed = true;
                if (r instanceof Closeable) {
                    ((Closeable)((Object)r)).close();
                }
            }

            @Override
            public int read() throws IOException {
                if (this.closed) {
                    throw new IOException("Can not read from closed Parseable");
                }
                if (this.unread != Integer.MIN_VALUE) {
                    int ch = this.unread;
                    this.unread = Integer.MIN_VALUE;
                    return ch;
                }
                if (this.end) {
                    return -1;
                }
                if (this.buff.position() < this.buff.limit()) {
                    return this.buff.get();
                }
                if (Parsers.readIntoBuffer(this.buff, r)) {
                    return this.buff.get();
                }
                this.end = true;
                return -1;
            }

            @Override
            public void unread(int ch) throws IOException {
                if (this.unread != Integer.MIN_VALUE) {
                    throw new IOException("Can't unread after unread.");
                }
                this.unread = ch;
            }
        };
    }

    public static Parser.Config.Builder newParserConfigBuilder() {
        return new Parser.Config.Builder(){
            boolean used = false;
            CollectionBuilder.Factory listFactory = Parsers.DEFAULT_LIST_FACTORY;
            CollectionBuilder.Factory vectorFactory = Parsers.DEFAULT_VECTOR_FACTORY;
            CollectionBuilder.Factory setFactory = Parsers.DEFAULT_SET_FACTORY;
            CollectionBuilder.Factory mapFactory = Parsers.DEFAULT_MAP_FACTORY;
            Map<Tag, TagHandler> tagHandlers = Parsers.defaultTagHandlers();

            @Override
            public Parser.Config.Builder setListFactory(CollectionBuilder.Factory listFactory) {
                this.checkState();
                this.listFactory = listFactory;
                return this;
            }

            @Override
            public Parser.Config.Builder setVectorFactory(CollectionBuilder.Factory vectorFactory) {
                this.checkState();
                this.vectorFactory = vectorFactory;
                return this;
            }

            @Override
            public Parser.Config.Builder setSetFactory(CollectionBuilder.Factory setFactory) {
                this.checkState();
                this.setFactory = setFactory;
                return this;
            }

            @Override
            public Parser.Config.Builder setMapFactory(CollectionBuilder.Factory mapFactory) {
                this.checkState();
                this.mapFactory = mapFactory;
                return this;
            }

            @Override
            public Parser.Config.Builder putTagHandler(Tag tag, TagHandler handler) {
                this.checkState();
                this.tagHandlers.put(tag, handler);
                return this;
            }

            @Override
            public Parser.Config build() {
                this.checkState();
                this.used = true;
                return new Parser.Config(){

                    @Override
                    public CollectionBuilder.Factory getListFactory() {
                        return 4.this.listFactory;
                    }

                    @Override
                    public CollectionBuilder.Factory getVectorFactory() {
                        return 4.this.vectorFactory;
                    }

                    @Override
                    public CollectionBuilder.Factory getSetFactory() {
                        return 4.this.setFactory;
                    }

                    @Override
                    public CollectionBuilder.Factory getMapFactory() {
                        return 4.this.mapFactory;
                    }

                    @Override
                    public TagHandler getTagHandler(Tag tag) {
                        return 4.this.tagHandlers.get(tag);
                    }
                };
            }

            private void checkState() {
                if (this.used) {
                    throw new IllegalStateException("Builder is single-use. Not usable after build()");
                }
            }

        };
    }

    static Map<Tag, TagHandler> defaultTagHandlers() {
        HashMap<Tag, TagHandler> m = new HashMap<Tag, TagHandler>();
        m.put(Parser.Config.EDN_UUID, UUID_HANDLER);
        m.put(Parser.Config.EDN_INSTANT, INSTANT_TO_DATE);
        m.put(Parser.Config.BIG_DECIMAL_TAG, IDENTITY);
        m.put(Parser.Config.DOUBLE_TAG, IDENTITY);
        m.put(Parser.Config.BIG_INTEGER_TAG, IDENTITY);
        m.put(Parser.Config.LONG_TAG, IDENTITY);
        return m;
    }

    public static Parser.Config defaultConfiguration() {
        return DEFAULT_CONFIGURATION;
    }

}

