package umg.simuladorsos1.ui;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import umg.simuladorsos1.algorithms.Planificador;
import umg.simuladorsos1.model.*;
import umg.simuladorsos1.simulation.*;

public class MainFrame extends JFrame {
    private final java.util.List<Proceso> procesos = new ArrayList<>();
    private final DefaultTableModel procesoModel = new DefaultTableModel(
            new Object[]{"ID","Llegada","Ráfaga CPU","Prioridad","Memoria (MB)","Estado"}, 0);
    private final JTable procesoTable = new JTable(procesoModel);
    private final JTextArea planOutput = new JTextArea();
    private final JComboBox<String> algoritmoCombo = new JComboBox<>(new String[]{"FCFS","SJF","Round Robin"});
    private final JSpinner quantumSpinner = new JSpinner(new SpinnerNumberModel(2,1,100,1));
    private final DefaultTableModel ganttModel = new DefaultTableModel(
            new Object[]{"Proceso","Inicio","Fin","Duración"},0);
    private final JTable ganttTable = new JTable(ganttModel);
    private final JTextArea concOutput = new JTextArea();
    private final JTextArea deadOutput = new JTextArea();
    private final JTextArea memOutput = new JTextArea();
    private final JTextArea resultsOutput = new JTextArea();
    private final JComboBox<String> memStrategy = new JComboBox<>(new String[]{"First Fit","Best Fit","Worst Fit"});
    private GestorMemoria memoria;
    private final ConcurrenciaSimulator concurrencia = new ConcurrenciaSimulator();
    private final InterbloqueoSimulator interbloqueo = new InterbloqueoSimulator();

    public MainFrame() {
        setTitle("Simulador Visual de Administración de Recursos - Sistemas Operativos I");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        JLabel header = new JLabel("SIMULADOR VISUAL DE ADMINISTRACIÓN DE RECURSOS", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(12,10,12,10));
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Procesos", panelProcesos());
        tabs.addTab("2. Planificación", panelPlanificacion());
        tabs.addTab("3. Concurrencia", panelConcurrencia());
        tabs.addTab("4. Interbloqueos", panelInterbloqueo());
        tabs.addTab("5. Memoria", panelMemoria());
        tabs.addTab("6. Resultados", panelResultados());
        add(tabs, BorderLayout.CENTER);

        setVisible(false);
    }

    private JPanel basePanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        return p;
    }

    private JPanel panelProcesos() {
        JPanel p = basePanel();
        JPanel form = new JPanel(new GridLayout(2,6,6,6));
        JTextField id = new JTextField();
        JSpinner llegada = new JSpinner(new SpinnerNumberModel(0,0,999,1));
        JSpinner rafaga = new JSpinner(new SpinnerNumberModel(5,1,999,1));
        JSpinner prioridad = new JSpinner(new SpinnerNumberModel(1,1,99,1));
        JSpinner memoriaSol = new JSpinner(new SpinnerNumberModel(100,1,100000,1));
        JButton agregar = new JButton("Agregar proceso");
        form.add(new JLabel("ID")); form.add(new JLabel("Llegada")); form.add(new JLabel("Ráfaga"));
        form.add(new JLabel("Prioridad")); form.add(new JLabel("Memoria MB")); form.add(new JLabel(""));
        form.add(id); form.add(llegada); form.add(rafaga); form.add(prioridad); form.add(memoriaSol); form.add(agregar);
        p.add(form,BorderLayout.NORTH);
        p.add(new JScrollPane(procesoTable),BorderLayout.CENTER);

        JButton limpiar = new JButton("Limpiar procesos");
        JButton cargar = new JButton("Cargar datos de prueba");
        JPanel south = new JPanel();
        south.add(cargar); south.add(limpiar);
        p.add(south,BorderLayout.SOUTH);

        agregar.addActionListener(e -> {
            try {
                String ident = id.getText().trim();
                if (ident.isEmpty()) throw new IllegalArgumentException("El ID es obligatorio.");
                if (procesos.stream().anyMatch(x -> x.getId().equalsIgnoreCase(ident)))
                    throw new IllegalArgumentException("El ID ya existe.");
                Proceso pr = new Proceso(ident,(Integer)llegada.getValue(),(Integer)rafaga.getValue(),
                        (Integer)prioridad.getValue(),(Integer)memoriaSol.getValue());
                procesos.add(pr); agregarFila(pr); id.setText("");
            } catch(Exception ex) { mensaje(ex.getMessage()); }
        });
        cargar.addActionListener(e -> cargarProcesosDemo());
        limpiar.addActionListener(e -> { procesos.clear(); procesoModel.setRowCount(0); });
        return p;
    }

    private void agregarFila(Proceso p) {
        procesoModel.addRow(new Object[]{p.getId(),p.getLlegada(),p.getRafaga(),p.getPrioridad(),p.getMemoria(),p.getEstado()});
    }

    private void cargarProcesosDemo() {
        procesos.clear(); procesoModel.setRowCount(0);
        procesos.add(new Proceso("P1",0,5,2,120));
        procesos.add(new Proceso("P2",1,3,1,200));
        procesos.add(new Proceso("P3",2,8,3,140));
        procesos.add(new Proceso("P4",3,4,2,80));
        procesos.forEach(this::agregarFila);
    }

    private JPanel panelPlanificacion() {
        JPanel p = basePanel();
        JPanel top = new JPanel();
        top.add(new JLabel("Algoritmo:")); top.add(algoritmoCombo);
        top.add(new JLabel("Quantum:")); top.add(quantumSpinner);
        JButton ejecutar = new JButton("Ejecutar simulación");
        top.add(ejecutar);
        p.add(top,BorderLayout.NORTH);

        planOutput.setEditable(false);
        planOutput.setFont(new Font("Monospaced",Font.PLAIN,14));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(planOutput),new JScrollPane(ganttTable));
        split.setResizeWeight(.45);
        p.add(split,BorderLayout.CENTER);

        ejecutar.addActionListener(e -> ejecutarPlanificacion());
        return p;
    }

    private void ejecutarPlanificacion() {
        if (procesos.isEmpty()) { mensaje("Registre procesos primero."); return; }
        ResultadoPlanificacion r;
        String alg = (String)algoritmoCombo.getSelectedItem();
        if ("FCFS".equals(alg)) r=Planificador.fcfs(procesos);
        else if ("SJF".equals(alg)) r=Planificador.sjf(procesos);
        else r=Planificador.roundRobin(procesos,(Integer)quantumSpinner.getValue());

        ganttModel.setRowCount(0);
        StringBuilder orden=new StringBuilder();
        for (BloqueGantt b:r.getGantt()) {
            ganttModel.addRow(new Object[]{b.proceso(),b.inicio(),b.fin(),b.duracion()});
            orden.append(b.proceso()).append("[").append(b.inicio()).append("-").append(b.fin()).append("] ");
        }
        planOutput.setText("ALGORITMO: "+alg+"\n\nOrden / Gantt:\n"+orden+
                "\n\nTiempo de espera promedio: "+fmt(r.getEsperaPromedio())+
                "\nTiempo de retorno promedio: "+fmt(r.getRetornoPromedio())+
                "\nTiempo de respuesta promedio: "+fmt(r.getRespuestaPromedio()));
        actualizarResultados("Planificación "+alg+": espera="+fmt(r.getEsperaPromedio())+
                ", retorno="+fmt(r.getRetornoPromedio())+", respuesta="+fmt(r.getRespuestaPromedio()));
    }

    private JPanel panelConcurrencia() {
        JPanel p=basePanel();
        JPanel top=new JPanel();
        JSpinner hilos=new JSpinner(new SpinnerNumberModel(4,2,20,1));
        JSpinner inc=new JSpinner(new SpinnerNumberModel(10000,100,1000000,100));
        JButton sin=new JButton("Ejecutar SIN sincronización");
        JButton con=new JButton("Ejecutar CON synchronized");
        top.add(new JLabel("Hilos:"));top.add(hilos);top.add(new JLabel("Incrementos por hilo:"));top.add(inc);
        top.add(sin);top.add(con);
        p.add(top,BorderLayout.NORTH);
        concOutput.setEditable(false); concOutput.setFont(new Font("Monospaced",Font.PLAIN,14));
        p.add(new JScrollPane(concOutput),BorderLayout.CENTER);
        sin.addActionListener(e -> {
            ConcurrenciaSimulator.Resultado r=concurrencia.ejecutar(false,(Integer)hilos.getValue(),(Integer)inc.getValue());
            mostrarConcurrencia(r);
        });
        con.addActionListener(e -> {
            ConcurrenciaSimulator.Resultado r=concurrencia.ejecutar(true,(Integer)hilos.getValue(),(Integer)inc.getValue());
            mostrarConcurrencia(r);
        });
        return p;
    }

    private void mostrarConcurrencia(ConcurrenciaSimulator.Resultado r) {
        String estado=r.obtenido()==r.esperado()?"CONSISTENTE":"INCONSISTENTE / CONDICIÓN DE CARRERA";
        concOutput.setText("Escenario: "+(r.sincronizado()?"CON sincronización":"SIN sincronización")+
                "\nValor esperado: "+r.esperado()+"\nValor obtenido: "+r.obtenido()+
                "\nResultado: "+estado+
                "\n\nSección crítica: incremento de un contador compartido.");
        actualizarResultados("Concurrencia: "+estado+" ("+(r.sincronizado()?"synchronized":"sin sincronización")+")");
    }

    private JPanel panelInterbloqueo() {
        JPanel p=basePanel();
        JPanel top=new JPanel();
        JButton cargar=new JButton("Cargar escenario P1/P2");
        JButton detectar=new JButton("Detectar interbloqueo");
        JButton recuperar=new JButton("Recuperar");
        top.add(cargar);top.add(detectar);top.add(recuperar);
        p.add(top,BorderLayout.NORTH);
        deadOutput.setEditable(false);deadOutput.setFont(new Font("Monospaced",Font.PLAIN,14));
        p.add(new JScrollPane(deadOutput),BorderLayout.CENTER);
        cargar.addActionListener(e -> {interbloqueo.cargarEscenario();deadOutput.setText(interbloqueo.estado());});
        detectar.addActionListener(e -> {deadOutput.setText(interbloqueo.estado()+"\n\n"+interbloqueo.detectar());actualizarResultados("Interbloqueo: detección ejecutada.");});
        recuperar.addActionListener(e -> {deadOutput.setText(interbloqueo.estado()+"\n\n"+interbloqueo.recuperar()+"\n\n"+interbloqueo.estado());actualizarResultados("Interbloqueo: recuperación aplicada.");});
        deadOutput.setText(interbloqueo.estado());
        return p;
    }

    private JPanel panelMemoria() {
        JPanel p=basePanel();
        JPanel top=new JPanel();
        JSpinner total=new JSpinner(new SpinnerNumberModel(1000,100,100000,50));
        JButton crear=new JButton("Crear memoria");
        JTextField proc=new JTextField(6);
        JSpinner tam=new JSpinner(new SpinnerNumberModel(100,1,100000,10));
        JButton asignar=new JButton("Asignar");
        JButton liberar=new JButton("Liberar");
        top.add(new JLabel("Total MB:"));top.add(total);top.add(crear);
        top.add(new JLabel("Proceso:"));top.add(proc);top.add(new JLabel("MB:"));top.add(tam);
        top.add(memStrategy);top.add(asignar);top.add(liberar);
        p.add(top,BorderLayout.NORTH);
        memOutput.setEditable(false); memOutput.setFont(new Font("Monospaced",Font.PLAIN,14));
        p.add(new JScrollPane(memOutput),BorderLayout.CENTER);

        crear.addActionListener(e -> {memoria=new GestorMemoria((Integer)total.getValue());mostrarMemoria();});
        
asignar.addActionListener(e -> {
    if (memoria == null) {
        mensaje("Cree la memoria primero.");
        return;
    }

    String proceso = proc.getText().trim();
    int tamanio = (Integer) tam.getValue();
    String estrategia = (String) memStrategy.getSelectedItem();

    if (proceso.isEmpty()) {
        mensaje("Ingrese el nombre del proceso.");
        return;
    }

    boolean ok = memoria.asignar(proceso, tamanio, estrategia);

    if (!ok) {
        mensaje("No fue posible asignar: revise espacio, nombre y tamaño.");
    } else {
        actualizarResultados(
                "Memoria " + estrategia +
                ": proceso " + proceso +
                " asignado (" + tamanio + " MB). " +
                "Usada=" + memoria.memoriaUsada() +
                " MB, libre=" + memoria.memoriaLibre() +
                " MB, fragmentos=" + memoria.fragmentacionExterna()
        );
    }

    mostrarMemoria();
});
        
liberar.addActionListener(e -> {
    if (memoria == null) {
        mensaje("Cree la memoria primero.");
        return;
    }

    String proceso = proc.getText().trim();

    if (!memoria.liberar(proceso)) {
        mensaje("El proceso no está asignado.");
    } else {
        actualizarResultados(
                "Memoria: proceso " + proceso +
                " liberado. " +
                "Usada=" + memoria.memoriaUsada() +
                " MB, libre=" + memoria.memoriaLibre() +
                " MB, fragmentos=" + memoria.fragmentacionExterna()
        );
    }

    mostrarMemoria();
});
        
        memoria=new GestorMemoria(1000); mostrarMemoria();
        return p;
    }

  private void mostrarMemoria() {
    if (memoria == null) return;

    StringBuilder sb = new StringBuilder();

    sb.append("MEMORIA TOTAL: ").append(memoria.getTotal()).append(" MB\n");
    sb.append("USADA: ").append(memoria.memoriaUsada()).append(" MB\n");
    sb.append("LIBRE: ").append(memoria.memoriaLibre()).append(" MB\n");
    sb.append("FRAGMENTOS LIBRES: ").append(memoria.fragmentacionExterna()).append("\n\n");

    sb.append(String.format("%-12s %-12s %-15s%n",
            "INICIO", "TAMAÑO", "ESTADO"));
    sb.append("----------------------------------------\n");

    for (GestorMemoria.Bloque b : memoria.getBloques()) {
        sb.append(String.format("%-12d %-12d %-15s%n",
                b.inicio,
                b.tamano,
                b.libre() ? "LIBRE" : b.proceso));
    }

    memOutput.setText(sb.toString());
}

    private JPanel panelResultados() {
        JPanel p=basePanel();
        resultsOutput.setEditable(false); resultsOutput.setFont(new Font("Monospaced",Font.PLAIN,14));
        p.add(new JScrollPane(resultsOutput),BorderLayout.CENTER);
        JButton limpiar=new JButton("Limpiar resultados");
        limpiar.addActionListener(e->resultsOutput.setText(""));
        p.add(limpiar,BorderLayout.SOUTH);
        resultsOutput.setText("Aquí se registrarán los resultados principales de las simulaciones.\n");
        return p;
    }

    private void actualizarResultados(String s) {
        resultsOutput.append("\n• "+s);
    }

    private String fmt(double n){return String.format(Locale.US,"%.2f",n);}
    private void mensaje(String s){JOptionPane.showMessageDialog(this,s,"Validación",JOptionPane.WARNING_MESSAGE);}
}
