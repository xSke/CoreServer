/*
 * Decompiled with CFR 0_129.
 */
package us.bpsm.edn.protocols;

import java.util.ArrayList;
import java.util.List;
import us.bpsm.edn.EdnException;

class C3 {
    C3() {
    }

    static List<Class<?>> methodResolutionOrder(Class<?> c) {
        try {
            List result = C3.mro(c);
            if (c.getSuperclass() != null) {
                result.add(Object.class);
            }
            return result;
        }
        catch (InconsistentHierarchy e) {
            StringBuilder b = new StringBuilder().append("Unable to compute a consistent ").append("method resolution order for ").append(c.getName());
            if (c.equals(e.problematicClass)) {
                b.append(".");
            } else {
                b.append(" because ").append(e.problematicClass.getName()).append(" has no consistent method resolution order.");
            }
            throw new EdnException(b.toString());
        }
    }

    private static List<Class<?>> mro(Class<?> c) throws InconsistentHierarchy {
        ArrayList seqsToMerge = new ArrayList();
        seqsToMerge.add(C3.asList(c));
        List supers = C3.supers(c);
        for (Class s : supers) {
            seqsToMerge.add(C3.mro(s));
        }
        seqsToMerge.add(supers);
        try {
            return C3.merge(seqsToMerge);
        }
        catch (InconsistentHierarchy e) {
            throw new InconsistentHierarchy(c);
        }
    }

    private static List<Class<?>> asList(Class<?> c) {
        ArrayList result = new ArrayList(1);
        result.add(c);
        return result;
    }

    private static List<Class<?>> supers(Class<?> c) {
        Class sc = c.getSuperclass();
        Class<?>[] interfaces = c.getInterfaces();
        ArrayList result = new ArrayList();
        if (sc != null && sc != Object.class) {
            result.add(sc);
        }
        for (Class i : interfaces) {
            result.add(i);
        }
        return result;
    }

    private static List<Class<?>> merge(List<List<Class<?>>> seqsToMerge) throws InconsistentHierarchy {
        ArrayList result = new ArrayList();
        while (!C3.allAreEmpty(seqsToMerge)) {
            Class candidate = C3.findCandidate(seqsToMerge);
            if (candidate == null) {
                throw new InconsistentHierarchy();
            }
            result.add(candidate);
            C3.removeCandidate(seqsToMerge, candidate);
        }
        return result;
    }

    private static boolean allAreEmpty(List<List<Class<?>>> lists) {
        for (List l : lists) {
            if (l.isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static Class<?> findCandidate(List<List<Class<?>>> seqsToMerge) {
        for (List seq : seqsToMerge) {
            if (seq.isEmpty() || C3.occursInSomeTail(seqsToMerge, seq.get(0))) continue;
            return seq.get(0);
        }
        return null;
    }

    private static boolean occursInSomeTail(List<List<Class<?>>> seqsToMerge, Object c) {
        for (List seq : seqsToMerge) {
            for (int i = 1; i < seq.size(); ++i) {
                if (!c.equals(seq.get(i))) continue;
                return true;
            }
        }
        return false;
    }

    private static void removeCandidate(List<List<Class<?>>> seqsToMerge, Class<?> candidate) {
        for (List seq : seqsToMerge) {
            if (seq.isEmpty() || !candidate.equals(seq.get(0))) continue;
            seq.remove(0);
        }
    }

    static class InconsistentHierarchy
    extends Exception {
        private static final long serialVersionUID = 1L;
        Class<?> problematicClass;

        InconsistentHierarchy(Class<?> problematicClass) {
            this.problematicClass = problematicClass;
        }

        InconsistentHierarchy() {
        }
    }

}

