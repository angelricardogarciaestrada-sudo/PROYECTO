package umg.simuladorsos1.simulation;

import java.util.*;

public class InterbloqueoSimulator {
    private final Map<String, String> asignacion = new LinkedHashMap<>();
    private final Map<String, String> solicitud = new LinkedHashMap<>();

    public InterbloqueoSimulator() {
        cargarEscenario();
    }

    public void cargarEscenario() {
        asignacion.clear();
        solicitud.clear();
        asignacion.put("P1", "R1");
        asignacion.put("P2", "R2");
        solicitud.put("P1", "R2");
        solicitud.put("P2", "R1");
    }

    public String detectar() {
        boolean ciclo = "R2".equals(solicitud.get("P1")) &&
                        "R1".equals(solicitud.get("P2")) &&
                        "R1".equals(asignacion.get("P1")) &&
                        "R2".equals(asignacion.get("P2"));
        return ciclo
                ? "INTERBLOQUEO DETECTADO: P1 espera R2 y P2 espera R1 mientras cada uno retiene el recurso del otro."
                : "No se detectó el escenario de interbloqueo configurado.";
    }

    public String recuperar() {
        if (asignacion.containsKey("P2")) {
            asignacion.remove("P2");
            solicitud.remove("P2");
            return "Recuperación aplicada: se seleccionó P2 como víctima y se liberó R2.";
        }
        return "No hay víctima disponible para recuperar.";
    }

    public String estado() {
        StringBuilder sb = new StringBuilder();
        sb.append("ASIGNACIÓN\n");
        asignacion.forEach((p,r) -> sb.append(p).append(" -> ").append(r).append("\n"));
        sb.append("\nSOLICITUD\n");
        solicitud.forEach((p,r) -> sb.append(p).append(" -> ").append(r).append("\n"));
        sb.append("\nCondiciones: exclusión mutua, retención y espera, no apropiación y espera circular.");
        return sb.toString();
    }
}
