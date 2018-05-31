/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.CollectionBuilder;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.TagHandler;
import us.bpsm.edn.parser.Token;

public interface Parser {
    public static final Object END_OF_INPUT = Token.END_OF_INPUT;

    public Object nextValue(Parseable var1);

    public static interface Config {
        public static final Tag EDN_UUID = Tag.newTag("uuid");
        public static final Tag EDN_INSTANT = Tag.newTag("inst");
        public static final Tag BIG_DECIMAL_TAG = Tag.newTag("us.bpsm.edn-java", "BigDecimal");
        public static final Tag DOUBLE_TAG = Tag.newTag("us.bpsm.edn-java", "Double");
        public static final Tag BIG_INTEGER_TAG = Tag.newTag("us.bpsm.edn-java", "BigInteger");
        public static final Tag LONG_TAG = Tag.newTag("us.bpsm.edn-java", "Long");

        public CollectionBuilder.Factory getListFactory();

        public CollectionBuilder.Factory getVectorFactory();

        public CollectionBuilder.Factory getSetFactory();

        public CollectionBuilder.Factory getMapFactory();

        public TagHandler getTagHandler(Tag var1);

        public static interface Builder {
            public Builder setListFactory(CollectionBuilder.Factory var1);

            public Builder setVectorFactory(CollectionBuilder.Factory var1);

            public Builder setSetFactory(CollectionBuilder.Factory var1);

            public Builder setMapFactory(CollectionBuilder.Factory var1);

            public Builder putTagHandler(Tag var1, TagHandler var2);

            public Config build();
        }

    }

}

