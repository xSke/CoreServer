/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.printer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import us.bpsm.edn.printer.Printer;
import us.bpsm.edn.printer.Printers;
import us.bpsm.edn.protocols.Protocol;

public class LoosePrinter {
    public static Printer newLoosePrinter(Appendable out) {
        Protocol defaults = Printers.defaultPrinterProtocol();
        final Printer.Fn defaultWriteMap = defaults.lookup(Map.class);
        final Printer.Fn defaultWriteSet = defaults.lookup(Set.class);
        final Printer.Fn defaultWriteList = defaults.lookup(List.class);
        final Printer.Fn defaultWriteCharSequence = defaults.lookup(CharSequence.class);
        Protocol loose = Printers.defaultProtocolBuilder().put(Map.class, new Printer.Fn<Map<?, ?>>(){

            @Override
            public void eval(Map<?, ?> self, Printer writer) {
                writer.softspace();
                defaultWriteMap.eval(self, writer);
                writer.softspace();
            }
        }).put(Set.class, (Object)(new Printer.Fn<Set<?>>(){

            @Override
            public void eval(Set<?> self, Printer writer) {
                writer.softspace();
                defaultWriteSet.eval(self, writer);
                writer.softspace();
            }
        })).put(List.class, (Object)(new Printer.Fn<List<?>>(){

            @Override
            public void eval(List<?> self, Printer writer) {
                writer.softspace();
                defaultWriteList.eval(self, writer);
                writer.softspace();
            }
        })).put(CharSequence.class, (Object)(new Printer.Fn<CharSequence>(){

            @Override
            public void eval(CharSequence self, Printer writer) {
                writer.softspace();
                defaultWriteCharSequence.eval(self, writer);
                writer.softspace();
            }
        })).build();
        return Printers.newPrinter(loose, out);
    }

}

