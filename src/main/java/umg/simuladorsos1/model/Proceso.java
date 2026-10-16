package umg.simuladorsos1.model;

public class Proceso {
    private final String id;
    private final int llegada;
    private final int rafaga;
    private final int prioridad;
    private final int memoria;
    private String estado;

    public Proceso(String id, int llegada, int rafaga, int prioridad, int memoria) {
        this.id = id;
        this.llegada = llegada;
        this.rafaga = rafaga;
        this.prioridad = prioridad;
        this.memoria = memoria;
        this.estado = "Nuevo";
    }

    public String getId() { return id; }
    public int getLlegada() { return llegada; }
    public int getRafaga() { return rafaga; }
    public int getPrioridad() { return prioridad; }
    public int getMemoria() { return memoria; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return id + " (llegada=" + llegada + ", ráfaga=" + rafaga + ")";
    }
}
