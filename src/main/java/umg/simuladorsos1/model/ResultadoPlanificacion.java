package umg.simuladorsos1.model;

import java.util.List;

public class ResultadoPlanificacion {
    private final List<BloqueGantt> gantt;
    private final double esperaPromedio;
    private final double retornoPromedio;
    private final double respuestaPromedio;

    public ResultadoPlanificacion(List<BloqueGantt> gantt, double esperaPromedio,
                                  double retornoPromedio, double respuestaPromedio) {
        this.gantt = gantt;
        this.esperaPromedio = esperaPromedio;
        this.retornoPromedio = retornoPromedio;
        this.respuestaPromedio = respuestaPromedio;
    }

    public List<BloqueGantt> getGantt() { return gantt; }
    public double getEsperaPromedio() { return esperaPromedio; }
    public double getRetornoPromedio() { return retornoPromedio; }
    public double getRespuestaPromedio() { return respuestaPromedio; }
}
