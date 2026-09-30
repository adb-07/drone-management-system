package com.dronefleet.ui;

import com.dronefleet.domain.*;
import com.dronefleet.event.*;
import com.dronefleet.security.*;
import com.dronefleet.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public final class MainFrame extends JFrame {
    private final Session session;
    private final FleetService fleet;
    private final DeliveryService delivery;
    private final AnalyticsService analytics;
    private final ExportService export;
    private final AuditRepositoryAdapter audit;
    private final DefaultTableModel droneModel = new DefaultTableModel(){ @Override public boolean isCellEditable(int row,int column){return false;} };
    private final DefaultTableModel delModel = new DefaultTableModel(){ @Override public boolean isCellEditable(int row,int column){return false;} };
    private final JTextArea log = new JTextArea();
    private final JLabel stats = new JLabel();
    private final JLabel roleLabel = new JLabel();

    public MainFrame(Session s, AuthService ignored) {
        session = s;
        var ctx = AppContextHolder.get();
        fleet = ctx.fleet;
        delivery = ctx.delivery;
        analytics = ctx.analytics;
        export = ctx.export;
        audit = ctx.audit;
        setTitle("DroneFleet — Management Console | " + s.username() + " (" + s.role() + ")");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        build();
        ctx.bus.subscribe(e -> SwingUtilities.invokeLater(() -> {
            log.append("EVENT: #" + e.deliveryId() + " -> " + e.status() + " | " + e.message() + "\n");
            refresh();
        }));
        refresh();
        setVisible(true);
    }

    private void build() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Dashboard", dashboard());
        tabs.add("Fleet", fleetTab());
        tabs.add("New Delivery", deliveryTab());
        tabs.add("History", historyTab());
        tabs.add("Maintenance & Audit", auditTab());
        add(tabs);
    }

    private JPanel dashboard() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        JPanel header = new JPanel(new BorderLayout());
        stats.setFont(stats.getFont().deriveFont(Font.BOLD, 18f));
        header.add(stats, BorderLayout.WEST);
        roleLabel.setText("Logged in as: " + session.username() + " | Role: " + session.role());
        header.add(roleLabel, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);
        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        p.add(new JScrollPane(log), BorderLayout.CENTER);
        return p;
    }

    private JPanel fleetTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        String[] c = {"ID", "Type", "Capacity", "Battery", "Status", "Completed", "Distance", "Revenue"};
        for (String x : c) droneModel.addColumn(x);
        JTable t = new JTable(droneModel);
        t.setAutoCreateRowSorter(true);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        JPanel b = new JPanel();
        JButton add = new JButton("Add Drone");
        JButton recharge = new JButton("Recharge");
        JButton maint = new JButton("Toggle Maintenance");
        JButton remove = new JButton("Remove");
        boolean admin = session.admin();
        add.setEnabled(admin); recharge.setEnabled(admin); maint.setEnabled(admin); remove.setEnabled(admin);
        b.add(add); b.add(recharge); b.add(maint); b.add(remove);
        if (!admin) b.add(new JLabel("Operator: fleet administration is read-only"));
        p.add(b, BorderLayout.SOUTH);
        add.addActionListener(e -> addDrone());
        recharge.addActionListener(e -> selectedDrone(t, "recharge"));
        maint.addActionListener(e -> selectedDrone(t, "maintenance"));
        remove.addActionListener(e -> selectedDrone(t, "remove"));
        return p;
    }

    private void addDrone() {
        try {
            Authorization.requireAdmin(session);
            String rawId = JOptionPane.showInputDialog(this, "Drone ID", "Add Drone", JOptionPane.PLAIN_MESSAGE);
            if (rawId == null) return;
            String type = (String) JOptionPane.showInputDialog(this, "Type", "Add Drone", JOptionPane.PLAIN_MESSAGE, null,
                    new String[]{"Standard", "Heavy", "Express"}, "Standard");
            if (type == null) return;
            String rawCap = JOptionPane.showInputDialog(this, "Capacity (kg)", "10");
            if (rawCap == null) return;
            String rawBat = JOptionPane.showInputDialog(this, "Battery (%)", "100");
            if (rawBat == null) return;
            fleet.addDrone(new Drone(Integer.parseInt(rawId.trim()), type, Double.parseDouble(rawCap.trim()), Integer.parseInt(rawBat.trim())), session.username());
            refresh();
        } catch (NumberFormatException ex) { showError("Please enter valid numeric values."); }
        catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void selectedDrone(JTable t, String action) {
        try { Authorization.requireAdmin(session); } catch (Exception ex) { showError(ex.getMessage()); return; }
        int r = t.getSelectedRow();
        if (r < 0) { showError("Select a drone first"); return; }
        int modelRow = t.convertRowIndexToModel(r);
        int id = (int) t.getModel().getValueAt(modelRow, 0);
        try {
            if (action.equals("recharge")) {
                startRechargeSimulation(id);
                return;
            } else if (action.equals("maintenance")) {
                String[] opts = {"Start", "End"};
                int x = JOptionPane.showOptionDialog(this, "Maintenance action", "Maintenance", 0,
                        JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
                if (x >= 0) fleet.maintenance(id, x == 0, session.username());
            } else {
                int confirm = JOptionPane.showConfirmDialog(this, "Remove Drone D-" + id + "?\nThis cannot be undone.", "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                fleet.removeDrone(id, session.username());
            }
            refresh();
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void startRechargeSimulation(int id) {
        Drone before = fleet.all().stream().filter(d -> d.id() == id).findFirst().orElseThrow();
        int startBattery = before.battery();
        if (startBattery >= 100) { showError("Drone D-" + id + " is already fully charged."); return; }

        final JDialog dialog = new JDialog(this, "Charging Drone D-" + id, false);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel status = new JLabel("Charging from " + startBattery + "% to 100%...");
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(startBattery);
        progress.setStringPainted(true);
        panel.add(status, BorderLayout.NORTH);
        panel.add(progress, BorderLayout.CENTER);
        dialog.add(panel);
        dialog.setSize(420, 140);
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        try {
            int seconds = fleet.startRecharge(id, session.username(), completed -> SwingUtilities.invokeLater(() -> {
                progress.setValue(100);
                progress.setString("100%");
                status.setText("Recharge complete — Drone D-" + id + " is available.");
                refresh();
                Timer closeTimer = new Timer(800, e -> dialog.dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
            }));
            refresh();
            dialog.setVisible(true);

            long start = System.currentTimeMillis();
            Timer timer = new Timer(100, null);
            timer.addActionListener(e -> {
                long elapsed = System.currentTimeMillis() - start;
                double fraction = Math.min(1.0, elapsed / (seconds * 1000.0));
                int value = startBattery + (int)Math.round((100 - startBattery) * fraction);
                progress.setValue(value);
                progress.setString(value + "%");
                if (fraction >= 1.0) timer.stop();
            });
            timer.start();
        } catch (Exception ex) {
            dialog.dispose();
            showError(ex.getMessage());
        }
    }

    private JPanel deliveryTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 7, 7, 7); g.fill = GridBagConstraints.HORIZONTAL;
        JTextField id = new JTextField(), w = new JTextField(), dist = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Any", "Standard", "Heavy", "Express"});
        String[] labels = {"Package ID", "Weight (kg)", "Distance (km)", "Preferred Drone"};
        JTextField[] fields = {id, w, dist};
        for (int i = 0; i < 3; i++) { g.gridx = 0; g.gridy = i; p.add(new JLabel(labels[i]), g); g.gridx = 1; p.add(fields[i], g); }
        g.gridx = 0; g.gridy = 3; p.add(new JLabel(labels[3]), g); g.gridx = 1; p.add(type, g);
        JButton b = new JButton("Assign Delivery"); g.gridy = 4; p.add(b, g);
        b.addActionListener(e -> {
            try {
                if (id.getText().isBlank() || w.getText().isBlank() || dist.getText().isBlank()) {
                    throw new IllegalArgumentException("Package ID, weight, and distance are required.");
                }
                Delivery d = delivery.createAndAssign(Integer.parseInt(id.getText().trim()), Double.parseDouble(w.getText().trim()),
                        Double.parseDouble(dist.getText().trim()), (String) type.getSelectedItem(), session.username());
                String text = "Delivery #" + d.id() + " — " + d.status() + (d.droneId() == null ? "" : " | Drone D-" + d.droneId());
                if (d.status() == DeliveryStatus.FAILED) showError(text + "\nReason: " + d.failureReason());
                else JOptionPane.showMessageDialog(this, text);
                refresh();
            } catch (NumberFormatException ex) { showError("Package ID, weight, and distance must be valid numbers.");
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        return p;
    }

    private JPanel historyTab() {
        JPanel p = new JPanel(new BorderLayout());
        String[] c = {"ID", "Package", "Drone", "Status", "Weight", "Distance", "Charge", "Battery", "Created", "Completed"};
        for (String x : c) delModel.addColumn(x);
        JTable table = new JTable(delModel); table.setAutoCreateRowSorter(true);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton ex = new JButton("Export CSV");
        ex.addActionListener(e -> { JFileChooser f = new JFileChooser(); if (f.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) try {
            export.deliveriesCsv(f.getSelectedFile(), delivery.all());
        } catch (Exception x) { showError(x.getMessage()); }});
        p.add(ex, BorderLayout.SOUTH);
        return p;
    }

    private JPanel auditTab() {
        JPanel p = new JPanel(new BorderLayout());
        JTextArea a = new JTextArea(); a.setEditable(false); a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JButton b = new JButton("Refresh Audit");
        b.addActionListener(e -> a.setText(String.join("\n", audit.recent(100))));
        p.add(new JScrollPane(a), BorderLayout.CENTER); p.add(b, BorderLayout.SOUTH); b.doClick();
        return p;
    }

    private void refresh() {
        droneModel.setRowCount(0);
        for (Drone d : fleet.all()) droneModel.addRow(new Object[]{d.id(), d.type(), d.capacity(), d.battery() + "%", d.status(),
                d.completedDeliveries(), String.format("%.1f km", d.totalDistance()), String.format("%.2f", d.totalRevenue())});
        delModel.setRowCount(0);
        for (Delivery d : delivery.all()) delModel.addRow(new Object[]{d.id(), d.item().id(), d.droneId() == null ? "-" : "D-" + d.droneId(), d.status(),
                d.item().weight(), d.item().distance(), String.format("%.2f", d.charge()), String.format("%.1f%%", d.batteryUsed()),
                d.createdAt(), d.completedAt() == null ? "-" : d.completedAt()});
        stats.setText(String.format("Fleet: %d | Busy: %d | Charging: %d | Low Battery: %d | Active Deliveries: %d | Completed: %d | Revenue: %.2f LKR",
                analytics.droneCount(), analytics.activeDrones(), analytics.chargingDrones(), analytics.lowBattery(), analytics.activeDeliveries(), analytics.completed(), analytics.revenue()));
    }

    private void showError(String s) { JOptionPane.showMessageDialog(this, s == null ? "Operation failed" : s, "Error", JOptionPane.ERROR_MESSAGE); }
}
