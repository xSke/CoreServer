/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn;

import us.bpsm.edn.Tag;

public final class TaggedValue {
    private final Tag tag;
    private final Object value;

    private TaggedValue(Tag tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    public static TaggedValue newTaggedValue(Tag tag, Object value) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null");
        }
        return new TaggedValue(tag, value);
    }

    public Tag getTag() {
        return this.tag;
    }

    public Object getValue() {
        return this.value;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.tag.hashCode();
        result = 31 * result + (this.value == null ? 0 : this.value.hashCode());
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
        TaggedValue other = (TaggedValue)obj;
        if (!this.tag.equals(other.tag)) {
            return false;
        }
        if (this.value == null ? other.value != null : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return this.tag + " " + this.value;
    }
}

