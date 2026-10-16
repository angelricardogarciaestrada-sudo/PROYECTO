package umg.simuladorsos1.simulation;

import java.util.*;

public class GestorMemoria {
    public static class Bloque {
        public int inicio;
        public int tamano;
        public String proceso;

        public Bloque(int inicio, int tamano, String proceso) {
            this.inicio = inicio;
            this.tamano = tamano;
            this.proceso = proceso;
        }

        public boolean libre() { return proceso == null; }
    }

    private final int total;
    private final List<Bloque> bloques = new ArrayList<>();

    public GestorMemoria(int total) {
        if (total <= 0) throw new IllegalArgumentException("La memoria debe ser mayor que 0.");
        this.total = total;
        bloques.add(new Bloque(0, total, null));
    }

    public int getTotal() { return total; }
    public List<Bloque> getBloques() { return Collections.unmodifiableList(bloques); }

    public boolean asignar(String proceso, int tamano, String estrategia) {
        if (proceso == null || proceso.isBlank() || tamano <= 0) return false;
        Bloque elegido = null;
        if ("First Fit".equals(estrategia)) {
            for (Bloque b : bloques) if (b.libre() && b.tamano >= tamano) { elegido = b; break; }
        } else if ("Best Fit".equals(estrategia)) {
            for (Bloque b : bloques) {
                if (b.libre() && b.tamano >= tamano &&
                    (elegido == null || b.tamano < elegido.tamano)) elegido = b;
            }
        } else if ("Worst Fit".equals(estrategia)) {
            for (Bloque b : bloques) {
                if (b.libre() && b.tamano >= tamano &&
                    (elegido == null || b.tamano > elegido.tamano)) elegido = b;
            }
        }
        if (elegido == null) return false;

        int restante = elegido.tamano - tamano;
        elegido.proceso = proceso;
        elegido.tamano = tamano;
        if (restante > 0) {
            int pos = elegido.inicio + tamano;
            int idx = bloques.indexOf(elegido);
            bloques.add(idx + 1, new Bloque(pos, restante, null));
        }
        return true;
    }

    public boolean liberar(String proceso) {
        boolean ok = false;
        for (Bloque b : bloques) {
            if (proceso.equals(b.proceso)) {
                b.proceso = null;
                ok = true;
            }
        }
        if (ok) compactarLibres();
        return ok;
    }

    private void compactarLibres() {
        for (int i = 0; i < bloques.size() - 1;) {
            Bloque a = bloques.get(i), b = bloques.get(i + 1);
            if (a.libre() && b.libre()) {
                a.tamano += b.tamano;
                bloques.remove(i + 1);
            } else i++;
        }
    }

    public int memoriaUsada() {
        return bloques.stream().filter(b -> !b.libre()).mapToInt(b -> b.tamano).sum();
    }

    public int memoriaLibre() { return total - memoriaUsada(); }

    public int fragmentacionExterna() {
        return (int) bloques.stream().filter(Bloque::libre).count();
    }
}
