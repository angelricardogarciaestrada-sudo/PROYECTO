package umg.simuladorsos1.algorithms;

import java.util.*;
import umg.simuladorsos1.model.*;

public class Planificador {

    public static ResultadoPlanificacion fcfs(List<Proceso> entrada) {
        List<Proceso> ps = new ArrayList<>(entrada);
        ps.sort(Comparator.comparingInt(Proceso::getLlegada).thenComparing(Proceso::getId));
        List<BloqueGantt> gantt = new ArrayList<>();
        Map<String, Integer> primera = new HashMap<>();
        Map<String, Integer> fin = new HashMap<>();
        int reloj = 0;

        for (Proceso p : ps) {
            reloj = Math.max(reloj, p.getLlegada());
            int inicio = reloj;
            primera.putIfAbsent(p.getId(), inicio);
            reloj += p.getRafaga();
            fin.put(p.getId(), reloj);
            gantt.add(new BloqueGantt(p.getId(), inicio, reloj));
        }
        return construirResultado(ps, gantt, primera, fin);
    }

    public static ResultadoPlanificacion sjf(List<Proceso> entrada) {
        List<Proceso> pendientes = new ArrayList<>(entrada);
        List<BloqueGantt> gantt = new ArrayList<>();
        Map<String, Integer> primera = new HashMap<>();
        Map<String, Integer> fin = new HashMap<>();
        int reloj = 0;

        while (!pendientes.isEmpty()) {
            
List<Proceso> disponibles = new ArrayList<>();

for (Proceso candidato : pendientes) {
    if (candidato.getLlegada() <= reloj) {
        disponibles.add(candidato);
    }
}
            
            if (disponibles.isEmpty()) {
                reloj = pendientes.stream().mapToInt(Proceso::getLlegada).min().orElse(reloj);
                continue;
            }
            Proceso p = disponibles.stream()
                    .min(Comparator.comparingInt(Proceso::getRafaga)
                            .thenComparingInt(Proceso::getLlegada)
                            .thenComparing(Proceso::getId))
                    .orElseThrow();
            pendientes.remove(p);
            int inicio = reloj;
            primera.put(p.getId(), inicio);
            reloj += p.getRafaga();
            fin.put(p.getId(), reloj);
            gantt.add(new BloqueGantt(p.getId(), inicio, reloj));
        }
        return construirResultado(entrada, gantt, primera, fin);
    }

    public static ResultadoPlanificacion roundRobin(List<Proceso> entrada, int quantum) {
        if (quantum <= 0) throw new IllegalArgumentException("El quantum debe ser mayor que 0.");

        List<Proceso> ps = new ArrayList<>(entrada);
        ps.sort(Comparator.comparingInt(Proceso::getLlegada).thenComparing(Proceso::getId));
        Map<String, Integer> restante = new HashMap<>();
        Map<String, Integer> primera = new HashMap<>();
        Map<String, Integer> fin = new HashMap<>();
        for (Proceso p : ps) restante.put(p.getId(), p.getRafaga());

        Queue<Proceso> cola = new ArrayDeque<>();
        List<BloqueGantt> gantt = new ArrayList<>();
        int reloj = 0, indice = 0;

        while (!cola.isEmpty() || indice < ps.size()) {
            if (cola.isEmpty() && indice < ps.size() && reloj < ps.get(indice).getLlegada()) {
                reloj = ps.get(indice).getLlegada();
            }
            while (indice < ps.size() && ps.get(indice).getLlegada() <= reloj) {
                cola.add(ps.get(indice++));
            }
            if (cola.isEmpty()) continue;

            Proceso p = cola.poll();
            int inicio = reloj;
            primera.putIfAbsent(p.getId(), inicio);
            int ejecutado = Math.min(quantum, restante.get(p.getId()));
            reloj += ejecutado;
            restante.put(p.getId(), restante.get(p.getId()) - ejecutado);
            gantt.add(new BloqueGantt(p.getId(), inicio, reloj));

            while (indice < ps.size() && ps.get(indice).getLlegada() <= reloj) {
                cola.add(ps.get(indice++));
            }
            if (restante.get(p.getId()) > 0) cola.add(p);
            else fin.put(p.getId(), reloj);
        }
        return construirResultado(ps, gantt, primera, fin);
    }

    private static ResultadoPlanificacion construirResultado(
            List<Proceso> ps, List<BloqueGantt> gantt,
            Map<String, Integer> primera, Map<String, Integer> fin) {

        double espera = 0, retorno = 0, respuesta = 0;
        for (Proceso p : ps) {
            int tRetorno = fin.get(p.getId()) - p.getLlegada();
            int tEspera = tRetorno - p.getRafaga();
            int tRespuesta = primera.get(p.getId()) - p.getLlegada();
            espera += tEspera;
            retorno += tRetorno;
            respuesta += tRespuesta;
        }
        int n = ps.size();
        return new ResultadoPlanificacion(gantt, espera / n, retorno / n, respuesta / n);
    }
}
