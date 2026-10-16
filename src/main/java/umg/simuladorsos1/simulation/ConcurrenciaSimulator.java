package umg.simuladorsos1.simulation;

import java.util.concurrent.CountDownLatch;

public class ConcurrenciaSimulator {
    public record Resultado(int esperado, int obtenido, boolean sincronizado) {}

    private int contador;

    public Resultado ejecutar(boolean sincronizado, int hilos, int incrementos) {
        if (hilos < 2 || incrementos < 100) {
            throw new IllegalArgumentException("Use al menos 2 hilos y 100 incrementos.");
        }
        contador = 0;
        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(hilos);
        for (int i = 0; i < hilos; i++) {
            Thread t = new Thread(() -> {
                try {
                    inicio.await();
                    for (int j = 0; j < incrementos; j++) {
                        if (sincronizado) incrementarSeguro();
                        else incrementarInseguro();
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    fin.countDown();
                }
            }, "Worker-" + i);
            t.start();
        }
        inicio.countDown();
        try {
            fin.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return new Resultado(hilos * incrementos, contador, sincronizado);
    }

    private void incrementarInseguro() {
        int valor = contador;
        Thread.yield();
        contador = valor + 1;
    }

    private synchronized void incrementarSeguro() {
        contador++;
    }
}
