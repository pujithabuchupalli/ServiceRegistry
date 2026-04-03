import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.List;

public class Portal extends JFrame {

    // ── THEME ──
    static final Color BG       = new Color(13,17,23);
    static final Color CARD     = new Color(22,27,34);
    static final Color BORDER   = new Color(48,54,61);
    static final Color ACCENT   = new Color(88,166,255);
    static final Color GREEN    = new Color(63,185,80);
    static final Color RED      = new Color(248,81,73);
    static final Color YELLOW   = new Color(210,153,34);
    static final Color TEXT     = new Color(230,237,243);
    static final Color MUTED    = new Color(139,148,158);
    static final Font  MONO     = new Font("Monospaced",Font.PLAIN,12);
    static final Font  BOLD14   = new Font("SansSerif",Font.BOLD,14);
    static final Font  PLAIN13  = new Font("SansSerif",Font.PLAIN,13);

    private JPanel contentArea = new JPanel(new BorderLayout());

    public Portal() {
        DBConnection.initDB();
        setTitle("Internal Developer Portal"); setSize(1100,700);
        setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(BG); getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        add(sidebar(), BorderLayout.WEST);
        contentArea.setBackground(BG);
        add(contentArea, BorderLayout.CENTER);
        showPanel(dashboardPanel());
        setVisible(true);
    }

    void showPanel(JPanel p) { contentArea.removeAll(); contentArea.add(p); contentArea.revalidate(); contentArea.repaint(); }

    // ── SIDEBAR ──
    JPanel sidebar() {
        JPanel s = new JPanel(); s.setBackground(CARD); s.setPreferredSize(new Dimension(200,0));
        s.setLayout(new BoxLayout(s,BoxLayout.Y_AXIS));
        s.setBorder(BorderFactory.createMatteBorder(0,0,0,1,BORDER));

        JLabel logo = new JLabel("⬡ DevPortal"); logo.setFont(new Font("SansSerif",Font.BOLD,16));
        logo.setForeground(ACCENT); logo.setAlignmentX(LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(20,16,20,0));
        s.add(logo);

        String[][] items = {{"⊞","Dashboard"},{"◈","Services"},{"◉","Endpoints"},{"⊕","Health Logs"},{"◎","Analytics"}};
        for (String[] item : items) {
            JButton b = sideBtn(item[0]+" "+item[1]);
            b.addActionListener(e -> {
                switch(item[1]) {
                    case "Dashboard":  showPanel(dashboardPanel());  break;
                    case "Services":   showPanel(servicesPanel());   break;
                    case "Endpoints":  showPanel(endpointsPanel());  break;
                    case "Health Logs":showPanel(logsPanel());       break;
                    case "Analytics":  showPanel(analyticsPanel());  break;
                }
            });
            s.add(b);
        }
        s.add(Box.createVerticalGlue());
        return s;
    }

    JButton sideBtn(String t) {
        JButton b = new JButton(t); b.setBackground(CARD); b.setForeground(MUTED);
        b.setFont(PLAIN13); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(10,16,10,16));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setMaximumSize(new Dimension(200,40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(33,41,54)); b.setForeground(TEXT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(CARD); b.setForeground(MUTED); }
        });
        return b;
    }

    // ── DASHBOARD ──
    JPanel dashboardPanel() {
        JPanel p = page("Dashboard", "Overview of all registered services and health");
        JPanel cards = new JPanel(new GridLayout(1,4,14,0)); cards.setOpaque(false);
        cards.add(statCard("Total Services", String.valueOf(DAO.countServices()), ACCENT));
        cards.add(statCard("Endpoints",      String.valueOf(DAO.countEndpoints()), new Color(163,113,247)));
        cards.add(statCard("UP",             String.valueOf(DAO.countUp()),   GREEN));
        cards.add(statCard("DOWN",           String.valueOf(DAO.countDown()), RED));
        p.add(wrap(cards,"Stats"), BorderLayout.NORTH);

        DefaultTableModel tm = tModel("Service","Owner","Status","Created");
        DAO.getServices("").forEach(s -> tm.addRow(new Object[]{s.name, s.owner, s.status, s.createdAt}));
        JScrollPane sp = styledTable(tm);
        p.add(wrap(sp,"Recent Services"), BorderLayout.CENTER);
        return p;
    }

    // ── SERVICES ──
    JPanel servicesPanel() {
        JPanel p = page("Services","Register, edit and manage your services");
        DefaultTableModel tm = tModel("ID","Name","Owner","Description","Status","Created");

        Runnable reload = () -> { tm.setRowCount(0); DAO.getServices("").forEach(s -> tm.addRow(new Object[]{s.id,s.name,s.owner,s.description,s.status,s.createdAt})); };
        reload.run();

        JTable tbl = rawTable(tm);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); btns.setOpaque(false);

        JButton add = accentBtn("+ Add Service");
        add.addActionListener(e -> {
            JTextField nm=inp(), ow=inp(), ds=inp();
            if(dialog(p,"Add Service",new String[]{"Name","Owner","Description"},new JComponent[]{nm,ow,ds})) {
                if(DAO.addService(nm.getText(),ow.getText(),ds.getText())) reload.run();
                else msg("Failed to add service.");
            }
        });

        JButton edit = grayBtn("✎ Edit");
        edit.addActionListener(e -> {
            int row=tbl.getSelectedRow(); if(row<0){msg("Select a service.");return;}
            int id=Integer.parseInt(tm.getValueAt(row,0).toString());
            JTextField nm=inp(tm.getValueAt(row,1).toString()), ow=inp(tm.getValueAt(row,2).toString()), ds=inp(tm.getValueAt(row,3).toString());
            if(dialog(p,"Edit Service",new String[]{"Name","Owner","Description"},new JComponent[]{nm,ow,ds}))
                if(DAO.updateService(id,nm.getText(),ow.getText(),ds.getText())) reload.run();
        });

        JButton del = redBtn("✕ Delete");
        del.addActionListener(e -> {
            int row=tbl.getSelectedRow(); if(row<0){msg("Select a service.");return;}
            int id=Integer.parseInt(tm.getValueAt(row,0).toString());
            if(JOptionPane.showConfirmDialog(p,"Delete this service and all its data?","Confirm",JOptionPane.YES_NO_OPTION)==0)
                if(DAO.deleteService(id)) reload.run();
        });

        JTextField search = inp(); search.setPreferredSize(new Dimension(200,32));
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { reload(search.getText().trim()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { reload(search.getText().trim()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { reload(search.getText().trim()); }
            void reload(String q) { tm.setRowCount(0); DAO.getServices(q).forEach(s->tm.addRow(new Object[]{s.id,s.name,s.owner,s.description,s.status,s.createdAt})); }
        });

        btns.add(add); btns.add(edit); btns.add(del);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(btns,BorderLayout.WEST);
        JPanel sr = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8)); sr.setOpaque(false);
        sr.add(new JLabel("🔍"){{setForeground(MUTED);}}); sr.add(search); top.add(sr,BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0,8)); body.setOpaque(false);
        body.add(top,BorderLayout.NORTH); body.add(new JScrollPane(tbl){{setBorder(cardBorder());}},BorderLayout.CENTER);
        p.add(body,BorderLayout.CENTER);
        return p;
    }

    // ── ENDPOINTS ──
    JPanel endpointsPanel() {
        JPanel p = page("Endpoints","Manage API endpoints for each service");
        List<Models.Service> services = DAO.getServices("");
        if(services.isEmpty()) { p.add(centered("No services found. Add a service first."),BorderLayout.CENTER); return p; }

        JComboBox<Models.Service> svcBox = new JComboBox<>(services.toArray(new Models.Service[0]));
        styleCombo(svcBox);

        DefaultTableModel tm = tModel("ID","Method","URL");
        Runnable reload = () -> { tm.setRowCount(0); if(svcBox.getSelectedItem()!=null) DAO.getEndpoints(((Models.Service)svcBox.getSelectedItem()).id).forEach(e2->tm.addRow(new Object[]{e2.id,e2.method,e2.url})); };
        svcBox.addActionListener(e -> reload.run()); reload.run();

        JTable tbl = rawTable(tm);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); btns.setOpaque(false);

        JButton add = accentBtn("+ Add Endpoint");
        add.addActionListener(e -> {
            if(svcBox.getSelectedItem()==null) return;
            int sid=((Models.Service)svcBox.getSelectedItem()).id;
            JTextField url=inp(); JComboBox<String> method=new JComboBox<>(new String[]{"GET","POST","PUT","DELETE"}); styleCombo(method);
            if(dialog(p,"Add Endpoint",new String[]{"URL","Method"},new JComponent[]{url,method}))
                if(DAO.addEndpoint(sid,url.getText(),method.getSelectedItem().toString())) reload.run();
        });

        JButton del = redBtn("✕ Delete");
        del.addActionListener(e -> {
            int row=tbl.getSelectedRow(); if(row<0){msg("Select an endpoint.");return;}
            int id=Integer.parseInt(tm.getValueAt(row,0).toString());
            if(DAO.deleteEndpoint(id)) reload.run();
        });

        JButton check = greenBtn("▶ Check Health");
        check.addActionListener(e -> {
            int row=tbl.getSelectedRow(); if(row<0){msg("Select an endpoint.");return;}
            int id=Integer.parseInt(tm.getValueAt(row,0).toString());
            String url=tm.getValueAt(row,2).toString();
            new Thread(() -> {
                String[] res = checkHealth(url);
                DAO.saveLog(id,res[0],Integer.parseInt(res[1]),Long.parseLong(res[2]));
                // update service status
                Models.Service svc=(Models.Service)svcBox.getSelectedItem();
                if(svc!=null) DAO.updateServiceStatus(svc.id,res[0]);
                SwingUtilities.invokeLater(()->{ reload.run(); msg("Health check done: "+res[0]+" ("+res[2]+"ms)"); });
            }).start();
        });

        btns.add(add); btns.add(del); btns.add(check);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); left.setOpaque(false);
        left.add(new JLabel("Service:"){{setForeground(MUTED); setFont(PLAIN13);}}); left.add(svcBox);
        top.add(left,BorderLayout.WEST); top.add(btns,BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0,8)); body.setOpaque(false);
        body.add(top,BorderLayout.NORTH); body.add(new JScrollPane(tbl){{setBorder(cardBorder());}},BorderLayout.CENTER);
        p.add(body,BorderLayout.CENTER);
        return p;
    }

    // ── HEALTH LOGS ──
    JPanel logsPanel() {
        JPanel p = page("Health Logs","View historical health check results");
        List<Models.Service> services = DAO.getServices("");
        if(services.isEmpty()) { p.add(centered("No services found."),BorderLayout.CENTER); return p; }

        JComboBox<Models.Service> svcBox = new JComboBox<>(services.toArray(new Models.Service[0]));
        styleCombo(svcBox);

        DefaultTableModel epTm = tModel("ID","Method","URL");
        JComboBox<String> epBox = new JComboBox<>(); epBox.setBackground(CARD); epBox.setForeground(TEXT);

        DefaultTableModel logTm = tModel("Status","Code","Response Time","Checked At");

        Runnable loadLogs = () -> { logTm.setRowCount(0); int sel=epBox.getSelectedIndex(); if(sel<0)return;
            int eid=Integer.parseInt(epBox.getSelectedItem().toString().split("\\|")[0].trim());
            DAO.getLogs(eid).forEach(l->logTm.addRow(new Object[]{l.status,l.statusCode,l.responseTime+"ms",l.checkedAt})); };

        svcBox.addActionListener(e -> { epBox.removeAllItems();
            if(svcBox.getSelectedItem()!=null) DAO.getEndpoints(((Models.Service)svcBox.getSelectedItem()).id).forEach(ep->epBox.addItem(ep.id+" | "+ep.method+" | "+ep.url));
        });
        epBox.addActionListener(e -> loadLogs.run());
        if(!services.isEmpty()) svcBox.setSelectedIndex(0);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)); filters.setOpaque(false);
        filters.add(lbl("Service:")); filters.add(svcBox);
        filters.add(lbl("Endpoint:")); filters.add(epBox);

        JPanel body = new JPanel(new BorderLayout(0,8)); body.setOpaque(false);
        body.add(filters,BorderLayout.NORTH);
        body.add(new JScrollPane(rawTable(logTm)){{setBorder(cardBorder());}},BorderLayout.CENTER);
        p.add(body,BorderLayout.CENTER);
        return p;
    }

    // ── ANALYTICS ──
    JPanel analyticsPanel() {
        JPanel p = page("Analytics","Aggregate insights from health check data");
        JPanel cards = new JPanel(new GridLayout(1,3,14,0)); cards.setOpaque(false);
        cards.add(statCard("Total Services", String.valueOf(DAO.countServices()), ACCENT));
        cards.add(statCard("Total Endpoints",String.valueOf(DAO.countEndpoints()), new Color(163,113,247)));
        cards.add(statCard("Avg Response",   DAO.avgResponseTime()+"ms", YELLOW));

        DefaultTableModel tm = tModel("Service","Endpoint URL","Avg Response Time");
        DAO.slowestAPIs().forEach(r -> tm.addRow(r));

        JPanel body = new JPanel(new BorderLayout(0,14)); body.setOpaque(false);
        body.add(wrap(cards,"Performance Summary"),BorderLayout.NORTH);
        body.add(wrap(new JScrollPane(rawTable(tm)){{setBorder(cardBorder());}},"Slowest APIs"),BorderLayout.CENTER);
        p.add(body,BorderLayout.CENTER);
        return p;
    }

    // ── HEALTH CHECK ──
    String[] checkHealth(String url) {
        try {
            long start = System.currentTimeMillis();
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET"); con.setConnectTimeout(5000); con.setReadTimeout(5000);
            int code = con.getResponseCode();
            long ms = System.currentTimeMillis()-start;
            return new String[]{code==200?"UP":"DOWN", String.valueOf(code), String.valueOf(ms)};
        } catch(Exception e) { return new String[]{"DOWN","0","0"}; }
    }

    // ── UI HELPERS ──
    JPanel page(String title, String sub) {
        JPanel p = new JPanel(new BorderLayout(0,16)); p.setBackground(BG); p.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
        JPanel hdr = new JPanel(new BorderLayout()); hdr.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(new Font("SansSerif",Font.BOLD,22)); t.setForeground(TEXT);
        JLabel s = new JLabel(sub);  s.setFont(PLAIN13); s.setForeground(MUTED);
        JPanel tl = new JPanel(); tl.setOpaque(false); tl.setLayout(new BoxLayout(tl,BoxLayout.Y_AXIS));
        tl.add(t); tl.add(s); hdr.add(tl,BorderLayout.WEST); p.add(hdr,BorderLayout.NORTH);
        return p;
    }

    JPanel statCard(String label, String value, Color accent) {
        JPanel c = new JPanel(new BorderLayout()); c.setBackground(CARD); c.setBorder(cardBorder());
        JLabel v = new JLabel(value); v.setFont(new Font("SansSerif",Font.BOLD,28)); v.setForeground(accent); v.setBorder(BorderFactory.createEmptyBorder(16,16,4,16));
        JLabel l = new JLabel(label); l.setFont(PLAIN13); l.setForeground(MUTED); l.setBorder(BorderFactory.createEmptyBorder(0,16,16,16));
        c.add(v,BorderLayout.CENTER); c.add(l,BorderLayout.SOUTH); return c;
    }

    JPanel wrap(JComponent inner, String title) {
        JPanel p = new JPanel(new BorderLayout(0,8)); p.setOpaque(false);
        if(title!=null) { JLabel l=new JLabel(title); l.setFont(BOLD14); l.setForeground(TEXT); p.add(l,BorderLayout.NORTH); }
        p.add(inner,BorderLayout.CENTER); return p;
    }

    JPanel centered(String msg) {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false);
        JLabel l = new JLabel(msg); l.setForeground(MUTED); l.setFont(PLAIN13); p.add(l); return p;
    }

    DefaultTableModel tModel(String... cols) { return new DefaultTableModel(cols,0){ public boolean isCellEditable(int r,int c){return false;} }; }

    JScrollPane styledTable(DefaultTableModel tm) { return new JScrollPane(rawTable(tm)){{setBorder(cardBorder());}}; }

    JTable rawTable(DefaultTableModel tm) {
        JTable t = new JTable(tm); t.setBackground(CARD); t.setForeground(TEXT); t.setFont(PLAIN13);
        t.setRowHeight(30); t.setGridColor(BORDER); t.setSelectionBackground(new Color(33,58,90)); t.setSelectionForeground(TEXT);
        t.getTableHeader().setBackground(new Color(30,37,48)); t.getTableHeader().setForeground(MUTED);
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12)); t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int r,int c) {
                Component comp = super.getTableCellRendererComponent(tbl,val,sel,foc,r,c);
                comp.setBackground(sel? new Color(33,58,90) : (r%2==0?CARD:new Color(18,23,30)));
                comp.setForeground(val!=null&&(val.toString().equals("UP")||val.toString().equals("DOWN")) ? (val.toString().equals("UP")?GREEN:RED) : TEXT);
                ((JLabel)comp).setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
                return comp;
            }
        });
        return t;
    }

    boolean dialog(JPanel parent, String title, String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridLayout(labels.length,2,8,8)); form.setBackground(CARD); form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        for(int i=0;i<labels.length;i++) { form.add(lbl(labels[i])); styleField(fields[i]); form.add(fields[i]); }
        UIManager.put("OptionPane.background",CARD); UIManager.put("Panel.background",CARD);
        return JOptionPane.showConfirmDialog(parent,form,title,JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==0;
    }

    void styleField(JComponent c) { c.setBackground(BG); if(c instanceof JTextField) { ((JTextField)c).setForeground(TEXT); ((JTextField)c).setCaretColor(TEXT); } c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER),BorderFactory.createEmptyBorder(4,8,4,8))); }
    void styleCombo(JComboBox<?> b) { b.setBackground(CARD); b.setForeground(TEXT); b.setFont(PLAIN13); b.setBorder(BorderFactory.createLineBorder(BORDER)); b.setPreferredSize(new Dimension(200,32)); }
    JTextField inp()           { JTextField f=new JTextField(20); styleField(f); return f; }
    JTextField inp(String val) { JTextField f=inp(); f.setText(val); return f; }
    JLabel lbl(String t)       { JLabel l=new JLabel(t); l.setForeground(MUTED); l.setFont(PLAIN13); return l; }
    Border cardBorder()        { return BorderFactory.createLineBorder(BORDER); }
    void msg(String m)         { JOptionPane.showMessageDialog(this,m); }

    JButton accentBtn(String t) { return styledBtn(t, ACCENT, BG); }
    JButton redBtn(String t)    { return styledBtn(t, RED, Color.WHITE); }
    JButton greenBtn(String t)  { return styledBtn(t, GREEN, BG); }
    JButton grayBtn(String t)   { return styledBtn(t, new Color(48,54,61), TEXT); }
    JButton styledBtn(String t, Color bg, Color fg) {
        JButton b = new JButton(t); b.setBackground(bg); b.setForeground(fg); b.setFont(new Font("SansSerif",Font.BOLD,12));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14)); return b;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored){}
        SwingUtilities.invokeLater(Portal::new);
    }
}