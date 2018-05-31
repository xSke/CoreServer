/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.parser;

import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.parser.Scanner;
import us.bpsm.edn.parser.ScannerImpl;

public class Scanners {
    private static final Scanner DEFAULT_SCANNER = new ScannerImpl(Parsers.defaultConfiguration());

    public static Scanner newScanner() {
        return DEFAULT_SCANNER;
    }

    private Scanners() {
        throw new UnsupportedOperationException();
    }
}

