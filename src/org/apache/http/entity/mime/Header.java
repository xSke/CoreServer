/*
 * Decompiled with CFR 0_129.
 */
package org.apache.http.entity.mime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.http.entity.mime.MinimalField;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class Header
implements Iterable<MinimalField> {
    private final List<MinimalField> fields = new LinkedList<MinimalField>();
    private final Map<String, List<MinimalField>> fieldMap = new HashMap<String, List<MinimalField>>();

    public void addField(MinimalField field) {
        if (field == null) {
            return;
        }
        String key = field.getName().toLowerCase(Locale.ENGLISH);
        List<MinimalField> values = this.fieldMap.get(key);
        if (values == null) {
            values = new LinkedList<MinimalField>();
            this.fieldMap.put(key, values);
        }
        values.add(field);
        this.fields.add(field);
    }

    public List<MinimalField> getFields() {
        return new ArrayList<MinimalField>(this.fields);
    }

    public MinimalField getField(String name) {
        if (name == null) {
            return null;
        }
        String key = name.toLowerCase(Locale.ENGLISH);
        List<MinimalField> list = this.fieldMap.get(key);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public List<MinimalField> getFields(String name) {
        if (name == null) {
            return null;
        }
        String key = name.toLowerCase(Locale.ENGLISH);
        List<MinimalField> list = this.fieldMap.get(key);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<MinimalField>(list);
    }

    public int removeFields(String name) {
        if (name == null) {
            return 0;
        }
        String key = name.toLowerCase(Locale.ENGLISH);
        List<MinimalField> removed = this.fieldMap.remove(key);
        if (removed == null || removed.isEmpty()) {
            return 0;
        }
        this.fields.removeAll(removed);
        return removed.size();
    }

    public void setField(MinimalField field) {
        if (field == null) {
            return;
        }
        String key = field.getName().toLowerCase(Locale.ENGLISH);
        List<MinimalField> list = this.fieldMap.get(key);
        if (list == null || list.isEmpty()) {
            this.addField(field);
            return;
        }
        list.clear();
        list.add(field);
        int firstOccurrence = -1;
        int index = 0;
        Iterator<MinimalField> it = this.fields.iterator();
        while (it.hasNext()) {
            MinimalField f = it.next();
            if (f.getName().equalsIgnoreCase(field.getName())) {
                it.remove();
                if (firstOccurrence == -1) {
                    firstOccurrence = index;
                }
            }
            ++index;
        }
        this.fields.add(firstOccurrence, field);
    }

    @Override
    public Iterator<MinimalField> iterator() {
        return Collections.unmodifiableList(this.fields).iterator();
    }

    public String toString() {
        return this.fields.toString();
    }
}

