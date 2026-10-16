package umg.simuladorsos1.model;

public record BloqueGantt(String proceso, int inicio, int fin) {
    public int duracion() { return fin - inicio; }
}
