/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;
import us.bpsm.edn.TaggedValue;
import us.bpsm.edn.parser.CollectionBuilder;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Scanner;
import us.bpsm.edn.parser.TagHandler;
import us.bpsm.edn.parser.Token;

class ParserImpl
implements Parser {
    private static final Object DISCARDED_VALUE = new Object(){

        public String toString() {
            return "##discarded value##";
        }
    };
    private Parser.Config cfg;
    private Scanner scanner;

    ParserImpl(Parser.Config cfg, Scanner scanner) {
        this.scanner = scanner;
        this.cfg = cfg;
    }

    @Override
    public Object nextValue(Parseable pbr) {
        Object value = this.nextValue(pbr, false);
        if (value instanceof Token && value != END_OF_INPUT) {
            throw new EdnSyntaxException("Unexpected " + value);
        }
        return value;
    }

    private Object nextValue(Parseable pbr, boolean discard) {
        Object curr = this.scanner.nextToken(pbr);
        if (curr instanceof Token) {
            switch ((Token)((Object)curr)) {
                case BEGIN_LIST: {
                    return this.parseIntoCollection(this.cfg.getListFactory(), Token.END_LIST, pbr, discard);
                }
                case BEGIN_VECTOR: {
                    return this.parseIntoCollection(this.cfg.getVectorFactory(), Token.END_VECTOR, pbr, discard);
                }
                case BEGIN_SET: {
                    return this.parseIntoCollection(this.cfg.getSetFactory(), Token.END_MAP_OR_SET, pbr, discard);
                }
                case BEGIN_MAP: {
                    return this.parseIntoCollection(this.cfg.getMapFactory(), Token.END_MAP_OR_SET, pbr, discard);
                }
                case DISCARD: {
                    this.nextValue(pbr, true);
                    return this.nextValue(pbr, discard);
                }
                case NIL: {
                    return null;
                }
                case END_OF_INPUT: 
                case END_LIST: 
                case END_MAP_OR_SET: 
                case END_VECTOR: {
                    return curr;
                }
            }
            throw new EdnSyntaxException("Unrecognized Token: " + curr);
        }
        if (curr instanceof Tag) {
            return this.nextValue((Tag)curr, pbr, discard);
        }
        return curr;
    }

    private Object nextValue(Tag t, Parseable pbr, boolean discard) {
        Object v = this.nextValue(pbr, discard);
        if (discard) {
            return DISCARDED_VALUE;
        }
        TagHandler x = this.cfg.getTagHandler(t);
        return x != null ? x.transform(t, v) : TaggedValue.newTaggedValue(t, v);
    }

    private Object parseIntoCollection(CollectionBuilder.Factory f, Token end, Parseable pbr, boolean discard) {
        CollectionBuilder b = !discard ? f.builder() : null;
        Object o = this.nextValue(pbr, discard);
        while (o != end) {
            if (o instanceof Token) {
                throw new EdnSyntaxException("Expected " + (Object)((Object)end) + ", but found " + o);
            }
            if (!discard) {
                b.add(o);
            }
            o = this.nextValue(pbr, discard);
        }
        return !discard ? b.build() : null;
    }

}

