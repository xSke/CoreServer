/*
 * Decompiled with CFR 0_129.
 */
package org.json.zip;

import java.io.UnsupportedEncodingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.Kim;
import org.json.zip.BitReader;
import org.json.zip.Huff;
import org.json.zip.JSONzip;
import org.json.zip.Keep;
import org.json.zip.MapKeep;
import org.json.zip.TrieKeep;

public class Decompressor
extends JSONzip {
    BitReader bitreader;

    public Decompressor(BitReader bitreader) {
        this.bitreader = bitreader;
    }

    private boolean bit() throws JSONException {
        try {
            boolean value = this.bitreader.bit();
            return value;
        }
        catch (Throwable e) {
            throw new JSONException(e);
        }
    }

    private Object getAndTick(Keep keep, BitReader bitreader) throws JSONException {
        try {
            int width = keep.bitsize();
            int integer = bitreader.read(width);
            Object value = keep.value(integer);
            if (integer >= keep.length) {
                throw new JSONException("Deep error.");
            }
            keep.tick(integer);
            return value;
        }
        catch (Throwable e) {
            throw new JSONException(e);
        }
    }

    public boolean pad(int factor) throws JSONException {
        try {
            return this.bitreader.pad(factor);
        }
        catch (Throwable e) {
            throw new JSONException(e);
        }
    }

    private int read(int width) throws JSONException {
        try {
            int value = this.bitreader.read(width);
            return value;
        }
        catch (Throwable e) {
            throw new JSONException(e);
        }
    }

    private JSONArray readArray(boolean stringy) throws JSONException {
        JSONArray jsonarray = new JSONArray();
        jsonarray.put(stringy ? this.readString() : this.readValue());
        do {
            if (!this.bit()) {
                if (!this.bit()) {
                    return jsonarray;
                }
                jsonarray.put(stringy ? this.readValue() : this.readString());
                continue;
            }
            jsonarray.put(stringy ? this.readString() : this.readValue());
        } while (true);
    }

    private Object readJSON() throws JSONException {
        switch (this.read(3)) {
            case 5: {
                return this.readObject();
            }
            case 6: {
                return this.readArray(true);
            }
            case 7: {
                return this.readArray(false);
            }
            case 0: {
                return new JSONObject();
            }
            case 1: {
                return new JSONArray();
            }
            case 2: {
                return Boolean.TRUE;
            }
            case 3: {
                return Boolean.FALSE;
            }
        }
        return JSONObject.NULL;
    }

    private String readName() throws JSONException {
        byte[] bytes = new byte[65536];
        int length = 0;
        if (!this.bit()) {
            int c;
            while ((c = this.namehuff.read(this.bitreader)) != 256) {
                bytes[length] = (byte)c;
                ++length;
            }
            if (length == 0) {
                return "";
            }
            Kim kim = new Kim(bytes, length);
            this.namekeep.register(kim);
            return kim.toString();
        }
        return this.getAndTick(this.namekeep, this.bitreader).toString();
    }

    private JSONObject readObject() throws JSONException {
        JSONObject jsonobject = new JSONObject();
        do {
            String name = this.readName();
            jsonobject.put(name, !this.bit() ? this.readString() : this.readValue());
        } while (this.bit());
        return jsonobject;
    }

    private String readString() throws JSONException {
        Kim kim;
        int from = 0;
        int thru = 0;
        int previousFrom = -1;
        int previousThru = 0;
        if (this.bit()) {
            return this.getAndTick(this.stringkeep, this.bitreader).toString();
        }
        byte[] bytes = new byte[65536];
        boolean one = this.bit();
        this.substringkeep.reserve();
        do {
            int c;
            if (one) {
                from = thru;
                kim = (Kim)this.getAndTick(this.substringkeep, this.bitreader);
                thru = kim.copy(bytes, from);
                if (previousFrom != -1) {
                    this.substringkeep.registerOne(new Kim(bytes, previousFrom, previousThru + 1));
                }
                previousFrom = from;
                previousThru = thru;
                one = this.bit();
                continue;
            }
            from = -1;
            while ((c = this.substringhuff.read(this.bitreader)) != 256) {
                bytes[thru] = (byte)c;
                ++thru;
                if (previousFrom == -1) continue;
                this.substringkeep.registerOne(new Kim(bytes, previousFrom, previousThru + 1));
                previousFrom = -1;
            }
            if (!this.bit()) break;
            one = true;
        } while (true);
        if (thru == 0) {
            return "";
        }
        kim = new Kim(bytes, thru);
        this.stringkeep.register(kim);
        this.substringkeep.registerMany(kim);
        return kim.toString();
    }

    private Object readValue() throws JSONException {
        switch (this.read(2)) {
            case 0: {
                return new Integer(this.read(!this.bit() ? 4 : (!this.bit() ? 7 : 14)));
            }
            case 1: {
                Object value;
                int c;
                byte[] bytes = new byte[256];
                int length = 0;
                while ((c = this.read(4)) != endOfNumber) {
                    bytes[length] = bcd[c];
                    ++length;
                }
                try {
                    value = JSONObject.stringToValue(new String(bytes, 0, length, "US-ASCII"));
                }
                catch (UnsupportedEncodingException e) {
                    throw new JSONException(e);
                }
                this.values.register(value);
                return value;
            }
            case 2: {
                return this.getAndTick(this.values, this.bitreader);
            }
            case 3: {
                return this.readJSON();
            }
        }
        throw new JSONException("Impossible.");
    }

    public Object unzip() throws JSONException {
        this.begin();
        return this.readJSON();
    }
}

