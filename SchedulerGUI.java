import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * CPU Scheduler GUI - OS Mini Project
 */
public class SchedulerGUI extends JFrame {

    private static final Color[] PALETTE = {
            new Color(70,  130, 210), new Color(60,  179, 113),
            new Color(255, 140, 0),   new Color(220, 60,  60),
            new Color(153, 102, 204), new Color(0,   180, 200),
            new Color(200, 100, 0),   new Color(0,   150, 50),
            new Color(210, 50,  120), new Color(80,  120, 200),
    };
    private static final Color IDLE_COLOR = new Color(200, 200, 200);

    private final JRadioButton rbFCFS     = new JRadioButton("FCFS", true);
    private final JRadioButton rbSjfNP    = new JRadioButton("SJF  Non-Preemptive");
    private final JRadioButton rbSjfP     = new JRadioButton("SJF  Preemptive");
    private final JRadioButton rbPriNP    = new JRadioButton("Priority  Non-Preemptive");
    private final JRadioButton rbPriP     = new JRadioButton("Priority  Preemptive");
    private final JRadioButton rbRR       = new JRadioButton("Round Robin");

    private final JRadioButton rbStatic   = new JRadioButton("Static",  true);
    private final JRadioButton rbLive     = new JRadioButton("Live Simulation");

    private final JTextField   tfPID      = new JTextField(7);
    private final JTextField   tfArrival  = new JTextField(5);
    private final JTextField   tfBurst    = new JTextField(5);
    private final JTextField   tfPriority = new JTextField(5);
    private final JLabel       lblPriority= new JLabel("Priority:");
    private final JSpinner     spQuantum  = new JSpinner(new SpinnerNumberModel(2, 1, 999, 1));
    private final JLabel       lblQuantum = new JLabel("Quantum:");

    private final DefaultTableModel mdlInput = new DefaultTableModel(
            new String[]{"PID","Arrival","Burst","Priority"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblInput = new JTable(mdlInput);

    private final JButton btnAddToList = new JButton("Add to List");
    private final JButton btnStart     = new JButton("▶  Start");
    private final JButton btnReset     = new JButton("↺  Reset All"); // الزرار ده دلوقتي بيمسح كل حاجة
    private final JButton btnPause     = new JButton("⏸  Pause");
    private final JButton btnAddLive   = new JButton("+  Add Process");

    private final GanttPanel         ganttPanel  = new GanttPanel();
    private final JScrollPane         ganttScroll = new JScrollPane(ganttPanel);

    private final DefaultTableModel   mdlBurst    = new DefaultTableModel(
            new String[]{"PID","Burst","Remaining","Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tblBurst = new JTable(mdlBurst);

    private final JLabel lblAvgWait = new JLabel("Avg Waiting Time:     —");
    private final JLabel lblAvgTAT  = new JLabel("Avg Turnaround Time:  —");

    private BaseScheduler          scheduler;
    private javax.swing.Timer      liveTimer;
    private final List<GanttEntry> ganttHistory    = new ArrayList<>();
    private final Map<String,Color>colorMap        = new LinkedHashMap<>();
    private final List<Process>    trackedProcesses= new ArrayList<>();
    private int  colorIdx   = 0;
    private int  pidCounter = 1;
    private boolean paused  = false;

    public SchedulerGUI() {
        super("CPU Scheduler – OS Project");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(900, 600));

        buildLayout();
        wireListeners();
        refreshAlgorithmDependentUI();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildLayout() {
        setLayout(new BorderLayout(6, 6));

        JLabel title = new JLabel("  CPU Scheduler Simulator", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(8, 10, 4, 0));
        title.setOpaque(true);
        title.setBackground(new Color(40, 40, 60));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel left = buildLeftPanel();
        left.setPreferredSize(new Dimension(310, 0));

        JPanel right = buildRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(315);
        split.setResizeWeight(0.0);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(6, 6, 6, 4));

        JPanel pAlgo = titledPanel("Algorithm");
        pAlgo.setLayout(new GridLayout(0, 1, 2, 2));
        ButtonGroup bgAlgo = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[]{rbFCFS,rbSjfNP,rbSjfP,rbPriNP,rbPriP,rbRR}) {
            bgAlgo.add(rb); pAlgo.add(rb);
        }

        JPanel pMode = titledPanel("Mode");
        pMode.setLayout(new BoxLayout(pMode, BoxLayout.Y_AXIS));
        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rbStatic); bgMode.add(rbLive);
        modeRow.add(rbStatic); modeRow.add(rbLive);
        JPanel quantumRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        spQuantum.setPreferredSize(new Dimension(60, 24));
        quantumRow.add(lblQuantum); quantumRow.add(spQuantum);
        pMode.add(modeRow); pMode.add(quantumRow);

        JPanel pInput = titledPanel("Add Process");
        pInput.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3,3,3,3);
        g.fill   = GridBagConstraints.HORIZONTAL;

        addRow(pInput, g, 0, "PID:",     tfPID);
        addRow(pInput, g, 1, "Arrival:", tfArrival);
        addRow(pInput, g, 2, "Burst:",   tfBurst);
        addRow(pInput, g, 3, lblPriority, tfPriority);

        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        btnAddToList.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        pInput.add(btnAddToList, g);

        tblInput.getColumnModel().getColumn(3).setPreferredWidth(55);
        JScrollPane spList = new JScrollPane(tblInput);
        spList.setPreferredSize(new Dimension(290, 130));
        JPanel pList = titledPanel("Process Queue");
        pList.setLayout(new BorderLayout());
        pList.add(spList, BorderLayout.CENTER);


        JPanel pCtrl = new JPanel(new GridLayout(2, 2, 5, 5));
        pCtrl.setBorder(new EmptyBorder(6, 0, 2, 0));


        styleBtn(btnStart,   new Color(46, 139, 87),  Color.BLACK);
        styleBtn(btnReset,   new Color(220, 20, 60),  Color.BLACK);
        styleBtn(btnPause,   new Color(184,134,11),   Color.BLACK);
        styleBtn(btnAddLive, new Color(70, 130, 180), Color.BLACK);

        btnPause.setEnabled(false);
        btnAddLive.setEnabled(false);
        pCtrl.add(btnStart); pCtrl.add(btnReset);
        pCtrl.add(btnPause); pCtrl.add(btnAddLive);

        p.add(pAlgo);
        p.add(Box.createVerticalStrut(5));
        p.add(pMode);
        p.add(Box.createVerticalStrut(5));
        p.add(pInput);
        p.add(Box.createVerticalStrut(5));
        p.add(pList);
        p.add(Box.createVerticalGlue());
        p.add(pCtrl);

        tfPID.setText("P1");
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(new EmptyBorder(6, 4, 6, 6));

        ganttPanel.setPreferredSize(new Dimension(100, 90));
        ganttScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        ganttScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ganttScroll.setPreferredSize(new Dimension(100, 110));
        JPanel ganttWrap = titledPanel("Gantt Chart");
        ganttWrap.setLayout(new BorderLayout());
        ganttWrap.add(ganttScroll, BorderLayout.CENTER);
        ganttWrap.setPreferredSize(new Dimension(100, 130));

        tblBurst.setRowHeight(22);
        colorBurstTableRenderer();
        JScrollPane spBurst = new JScrollPane(tblBurst);
        JPanel burstWrap = titledPanel("Remaining Burst Time");
        burstWrap.setLayout(new BorderLayout());
        burstWrap.add(spBurst, BorderLayout.CENTER);

        lblAvgWait.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvgTAT .setFont(new Font("Segoe UI", Font.BOLD, 14));
        JPanel statsWrap = titledPanel("Statistics");
        statsWrap.setLayout(new GridLayout(2, 1, 4, 4));
        statsWrap.add(lblAvgWait);
        statsWrap.add(lblAvgTAT);
        statsWrap.setPreferredSize(new Dimension(100, 75));

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.add(burstWrap,  BorderLayout.CENTER);
        bottom.add(statsWrap,  BorderLayout.SOUTH);

        p.add(ganttWrap, BorderLayout.NORTH);
        p.add(bottom,    BorderLayout.CENTER);
        return p;
    }

    private void wireListeners() {
        ActionListener algoChanged = e -> refreshAlgorithmDependentUI();
        for (JRadioButton rb : new JRadioButton[]{rbFCFS,rbSjfNP,rbSjfP,rbPriNP,rbPriP,rbRR})
            rb.addActionListener(algoChanged);

        btnAddToList.addActionListener(e -> addProcessToList());
        btnStart    .addActionListener(e -> startScheduler());
        btnReset    .addActionListener(e -> resetAll());
        btnPause    .addActionListener(e -> togglePause());
        btnAddLive  .addActionListener(e -> showAddLiveDialog());
    }

    private void refreshAlgorithmDependentUI() {
        boolean isPri = rbPriNP.isSelected() || rbPriP.isSelected();
        boolean isRR  = rbRR.isSelected();
        lblPriority.setVisible(isPri);
        tfPriority .setVisible(isPri);
        lblQuantum .setVisible(isRR);
        spQuantum  .setVisible(isRR);
    }

    private void addProcessToList() {
        String pid     = tfPID.getText().trim();
        String burstS  = tfBurst.getText().trim();
        String arrS    = tfArrival.getText().trim();
        String priS    = tfPriority.getText().trim();

        if (pid.isEmpty() || burstS.isEmpty()) {
            error("PID and Burst Time are required."); return;
        }
        for (int i = 0; i < mdlInput.getRowCount(); i++) {
            if (mdlInput.getValueAt(i,0).equals(pid)) {
                error("PID '" + pid + "' already exists in the list."); return;
            }
        }
        try {
            int arrival = arrS.isEmpty() ? 0 : Integer.parseInt(arrS);
            int burst   = Integer.parseInt(burstS);
            if (burst <= 0) { error("Burst time must be > 0."); return; }
            boolean isPri = rbPriNP.isSelected() || rbPriP.isSelected();
            int priority  = (isPri && !priS.isEmpty()) ? Integer.parseInt(priS) : 0;

            mdlInput.addRow(new Object[]{pid, arrival, burst, isPri ? priority : "—"});
            pidCounter++;
            tfPID.setText("P" + pidCounter);
            tfArrival.setText("");
            tfBurst.setText("");
            tfPriority.setText("");
        } catch (NumberFormatException ex) {
            error("Invalid number: " + ex.getMessage());
        }
    }

    private void startScheduler() {
        if (mdlInput.getRowCount() == 0) { error("Add at least one process."); return; }

        trackedProcesses.clear();
        ganttHistory.clear();
        colorMap.clear();
        colorIdx = 0;
        mdlBurst.setRowCount(0);

        boolean isPri = rbPriNP.isSelected() || rbPriP.isSelected();
        for (int i = 0; i < mdlInput.getRowCount(); i++) {
            String pid = (String) mdlInput.getValueAt(i,0);
            int arr    = (Integer) mdlInput.getValueAt(i,1);
            int bst    = (Integer) mdlInput.getValueAt(i,2);
            Object po  = mdlInput.getValueAt(i,3);
            int pri    = (isPri && po instanceof Integer) ? (Integer)po : 0;
            Process proc = new Process(pid, arr, bst, pri);
            trackedProcesses.add(proc);
            assignColor(pid);
            mdlBurst.addRow(new Object[]{pid, bst, bst, "Waiting"});
        }

        scheduler = createScheduler(trackedProcesses);

        lblAvgWait.setText("Avg Waiting Time:     —");
        lblAvgTAT .setText("Avg Turnaround Time:  —");
        ganttPanel.setData(ganttHistory, colorMap);
        ganttPanel.repaint();

        btnStart   .setEnabled(false);
        btnAddToList.setEnabled(false);

        if (rbLive.isSelected()) startLive();
        else                     runStatic();
    }

    private void runStatic() {
        while (!scheduler.isFinished()) {
            scheduler.tick();
            appendGantt();
        }
        syncBurstTableFinal();
        computeStats();
        ganttPanel.revalidate();
        ganttPanel.repaint();
        JScrollBar h = ganttScroll.getHorizontalScrollBar();
        h.setValue(h.getMaximum());
    }

    private void startLive() {
        paused = false;
        btnPause  .setEnabled(true);
        btnPause  .setText("⏸  Pause");
        btnAddLive.setEnabled(false);

        liveTimer = new javax.swing.Timer(1000, e -> {
            if (!scheduler.isFinished()) {
                scheduler.tick();
                appendGantt();
                syncBurstTableLive();
                ganttPanel.revalidate();
                ganttPanel.repaint();
                SwingUtilities.invokeLater(() -> {
                    JScrollBar h = ganttScroll.getHorizontalScrollBar();
                    h.setValue(h.getMaximum());
                });
            } else {
                liveTimer.stop();
                syncBurstTableFinal();
                computeStats();
                ganttPanel.revalidate();
                ganttPanel.repaint();
                btnPause  .setEnabled(false);
                btnAddLive.setEnabled(false);
            }
        });
        liveTimer.start();
    }

    private void togglePause() {
        if (paused) {
            paused = false;
            btnPause  .setText("⏸  Pause");
            btnAddLive.setEnabled(false);
            liveTimer.start();
        } else {
            paused = true;
            btnPause  .setText("▶  Resume");
            btnAddLive.setEnabled(true);
            liveTimer.stop();
        }
    }

    private void showAddLiveDialog() {
        int autoArrival = scheduler.currentTime;
        boolean isPri   = rbPriNP.isSelected() || rbPriP.isSelected();

        JDialog dlg = new JDialog(this, "Add Process (Arrival = " + autoArrival + ")", true);
        dlg.setLayout(new GridBagLayout());
        dlg.setSize(280, isPri ? 220 : 190);
        dlg.setLocationRelativeTo(this);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,8,5,8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JTextField dPID   = new JTextField("P" + pidCounter, 8);
        JTextField dBurst = new JTextField(8);
        JTextField dPri   = new JTextField(8);

        addRow(dlg, g, 0, "PID:",   dPID);
        addRow(dlg, g, 1, "Burst:", dBurst);
        if (isPri) addRow(dlg, g, 2, "Priority:", dPri);

        JLabel infoLbl = new JLabel("Arrival time will be set to " + autoArrival);
        infoLbl.setForeground(new Color(0,100,0));
        g.gridx=0; g.gridy= isPri?3:2; g.gridwidth=2; dlg.add(infoLbl, g);

        JButton ok = new JButton("Add");
        styleBtn(ok, new Color(46,139,87), Color.WHITE);
        g.gridy = isPri?4:3; dlg.add(ok, g);

        ok.addActionListener(ae -> {
            try {
                String pid  = dPID.getText().trim();
                int burst   = Integer.parseInt(dBurst.getText().trim());
                int pri     = (isPri && !dPri.getText().trim().isEmpty())
                        ? Integer.parseInt(dPri.getText().trim()) : 0;
                if (pid.isEmpty() || burst <= 0) { error("Valid PID and Burst required."); return; }
                for (Process tp : trackedProcesses) {
                    if (tp.processID.equals(pid)) { error("PID already in use."); return; }
                }
                Process np = new Process(pid, autoArrival, burst, pri);
                try {
                    scheduler.addProcess(np);
                } catch (IllegalPIDValueException ex) {
                    error("PID already exists in scheduler."); return;
                }
                trackedProcesses.add(np);
                assignColor(pid);
                mdlBurst.addRow(new Object[]{pid, burst, burst, "Waiting"});
                mdlInput.addRow(new Object[]{pid, autoArrival, burst, isPri ? pri : "—"});
                pidCounter++;
                dlg.dispose();
            } catch (NumberFormatException ex) {
                error("Invalid numeric input.");
            }
        });

        dlg.setVisible(true);
    }

    /**
     * Reset Method - المصنع: بتمسح كل حاجة وبترجع البرنامج للأول خالص
     */
    private void resetAll() {
        if (liveTimer != null) liveTimer.stop();
        scheduler = null;
        ganttHistory.clear();
        colorMap.clear();
        trackedProcesses.clear();
        colorIdx   = 0;
        pidCounter = 1; // تصفير العداد لـ P1
        paused     = false;

        mdlInput.setRowCount(0); // مسح جدول مدخلات الـ Processes
        mdlBurst.setRowCount(0); // مسح جدول الـ Burst
        lblAvgWait.setText("Avg Waiting Time:     —");
        lblAvgTAT .setText("Avg Turnaround Time:  —");
        tfPID.setText("P1"); // إعادة ضبط مربع الإدخال

        btnStart    .setEnabled(true);
        btnAddToList.setEnabled(true);
        btnPause    .setEnabled(false);
        btnPause    .setText("⏸  Pause");
        btnAddLive  .setEnabled(false);

        ganttPanel.setData(ganttHistory, colorMap);
        ganttPanel.revalidate();
        ganttPanel.repaint();
    }

    private void appendGantt() {
        Process last = scheduler.lastRunningProcess;
        String  pid  = (last != null) ? last.processID : "IDLE";
        if (pid.equals("IDLE")) assignColor("IDLE");

        if (!ganttHistory.isEmpty()) {
            GanttEntry prev = ganttHistory.get(ganttHistory.size() - 1);
            if (prev.processID.equals(pid)) {
                prev.endTime++;
                return;
            }
        }
        int t = scheduler.currentTime - 1;
        ganttHistory.add(new GanttEntry(pid, t, t + 1));
    }

    private void syncBurstTableLive() {
        String runPid = (scheduler.lastRunningProcess != null)
                ? scheduler.lastRunningProcess.processID : null;
        for (int row = 0; row < mdlBurst.getRowCount(); row++) {
            String pid = (String) mdlBurst.getValueAt(row, 0);
            for (Process p : trackedProcesses) {
                if (p.processID.equals(pid)) {
                    mdlBurst.setValueAt(p.remainingBurstTime, row, 2);
                    if (p.remainingBurstTime == 0)
                        mdlBurst.setValueAt("Done ✓",    row, 3);
                    else if (pid.equals(runPid))
                        mdlBurst.setValueAt("Running ►", row, 3);
                    else
                        mdlBurst.setValueAt("Waiting",   row, 3);
                    break;
                }
            }
        }
    }

    private void syncBurstTableFinal() {
        for (int row = 0; row < mdlBurst.getRowCount(); row++) {
            String pid = (String) mdlBurst.getValueAt(row, 0);
            for (Process p : trackedProcesses) {
                if (p.processID.equals(pid)) {
                    mdlBurst.setValueAt(p.remainingBurstTime, row, 2);
                    mdlBurst.setValueAt("Done ✓", row, 3);
                    break;
                }
            }
        }
    }

    private void computeStats() {
        if (trackedProcesses.isEmpty()) return;
        double tw = 0, tt = 0;
        for (Process p : trackedProcesses) { tw += p.waitingTime; tt += p.turnaroundTime; }
        int n = trackedProcesses.size();
        lblAvgWait.setText(String.format("Avg Waiting Time:     %.2f", tw / n));
        lblAvgTAT .setText(String.format("Avg Turnaround Time:  %.2f", tt / n));
    }

    private BaseScheduler createScheduler(List<Process> procs) {
        if (rbFCFS .isSelected()) return new FCFSScheduler(procs);
        if (rbSjfNP.isSelected()) return new SJFNonPreeptiveScheduler(procs);
        if (rbSjfP .isSelected()) return new SJFPreeptiveSchedular(procs);
        if (rbPriNP.isSelected()) return new PrioritySchedulerTick(procs, false);
        if (rbPriP .isSelected()) return new PrioritySchedulerTick(procs, true);
        if (rbRR   .isSelected()) return new RoundRobinScheduler(procs, (Integer) spQuantum.getValue());
        return new FCFSScheduler(procs);
    }

    private void assignColor(String pid) {
        if (!colorMap.containsKey(pid)) {
            colorMap.put(pid, pid.equals("IDLE")
                    ? IDLE_COLOR
                    : PALETTE[colorIdx++ % PALETTE.length]);
        }
    }

    private void colorBurstTableRenderer() {
        tblBurst.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String status = (String) t.getValueAt(row, 3);
                if ("Done ✓".equals(status))      setBackground(new Color(198,239,206));
                else if ("Running ►".equals(status)) setBackground(new Color(255,235,156));
                else setBackground(Color.WHITE);
                if (sel) setBackground(getBackground().darker());
                return this;
            }
        });
    }

    private JPanel titledPanel(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180,180,200)),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(50, 50, 120)));
        return p;
    }

    private void styleBtn(JButton b, Color bg, Color fg) {
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false); b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void addRow(Container c, GridBagConstraints g, int row, String lbl, JTextField f) {
        addRow(c, g, row, new JLabel(lbl), f);
    }
    private void addRow(Container c, GridBagConstraints g, int row, Component lbl, JTextField f) {
        g.gridx=0; g.gridy=row; g.gridwidth=1; c.add(lbl, g);
        g.gridx=1; c.add(f, g);
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    class GanttPanel extends JPanel {
        private List<GanttEntry>   entries = new ArrayList<>();
        private Map<String,Color>  colors  = new HashMap<>();

        private static final int H         = 55;
        private static final int TIME_H    = 18;
        private static final int PAD       = 10;
        private static final int MIN_W_PER_UNIT = 40;

        void setData(List<GanttEntry> e, Map<String,Color> c) {
            entries = e; colors = c;
        }

        @Override
        public Dimension getPreferredSize() {
            int total = entries.isEmpty() ? 0 : entries.get(entries.size()-1).endTime;
            int w = Math.max(400, total * MIN_W_PER_UNIT + PAD * 2);
            return new Dimension(w, H + TIME_H + PAD * 2);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (entries.isEmpty()) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                g.drawString("Gantt chart will appear here once the scheduler runs…", PAD + 5, PAD + H / 2 + 6);
                return;
            }

            int totalTime = entries.get(entries.size()-1).endTime;
            int avail     = Math.max(getWidth() - PAD * 2, 1);
            int uw        = Math.max(MIN_W_PER_UNIT, avail / Math.max(totalTime, 1));
            int blockTop  = PAD;
            Font labelFont= new Font("Segoe UI", Font.BOLD, 12);
            Font timeFont = new Font("Segoe UI", Font.PLAIN, 10);
            g.setFont(labelFont);

            Set<Integer> drawnTimes = new HashSet<>();

            for (GanttEntry e : entries) {
                int duration = e.endTime - e.startTime;
                int x = PAD + e.startTime * uw;
                int w = duration * uw;

                Color base = colors.getOrDefault(e.processID, IDLE_COLOR);
                g.setColor(base);
                g.fillRoundRect(x, blockTop, w, H, 8, 8);

                g.setColor(base.darker());
                g.drawRoundRect(x, blockTop, w, H, 8, 8);

                g.setColor(brightness(base) > 150 ? Color.BLACK : Color.WHITE);
                g.setFont(labelFont);
                FontMetrics fm = g.getFontMetrics();
                if (w > fm.stringWidth(e.processID) + 8) {
                    int lx = x + (w - fm.stringWidth(e.processID)) / 2;
                    int ly = blockTop + (H + fm.getAscent()) / 2 - 3;
                    g.drawString(e.processID, lx, ly);
                }

                g.setColor(Color.DARK_GRAY);
                g.setFont(timeFont);
                FontMetrics tfm = g.getFontMetrics();
                if (!drawnTimes.contains(e.startTime)) {
                    String t0 = String.valueOf(e.startTime);
                    g.drawString(t0, x - tfm.stringWidth(t0)/2, blockTop + H + TIME_H - 2);
                    g.drawLine(x, blockTop + H, x, blockTop + H + 5);
                    drawnTimes.add(e.startTime);
                }
                if (!drawnTimes.contains(e.endTime)) {
                    String t1 = String.valueOf(e.endTime);
                    int ex = PAD + e.endTime * uw;
                    g.drawString(t1, ex - tfm.stringWidth(t1)/2, blockTop + H + TIME_H - 2);
                    g.drawLine(ex, blockTop + H, ex, blockTop + H + 5);
                    drawnTimes.add(e.endTime);
                }
            }
        }

        private int brightness(Color c) {
            return (c.getRed()*299 + c.getGreen()*587 + c.getBlue()*114) / 1000;
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(SchedulerGUI::new);
    }
}