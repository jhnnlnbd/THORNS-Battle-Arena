import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;


public class StatsPanel extends JPanel {


    private static final Color COL_BG      = new Color(13, 13, 28);
    private static final Color COL_SURFACE = new Color(22, 22, 46);
    private static final Color COL_PANEL   = new Color(30, 30, 60);
    private static final Color COL_BORDER  = new Color(60, 55, 120);
    private static final Color COL_ACCENT  = new Color(124, 111, 232);
    private static final Color COL_GOLD    = new Color(240, 192, 64);
    private static final Color COL_GREEN   = new Color(79, 201, 124);
    private static final Color COL_RED     = new Color(232, 79, 79);
    private static final Color COL_TEXT    = new Color(224, 222, 255);
    private static final Color COL_TEXT2   = new Color(160, 160, 192);


    private final GameStats stats;


    private JLabel lblWins, lblLosses, lblWinRate;
    private JLabel lblAvgRounds, lblBestRound, lblTopDmg;
    private JPanel historyPanel;
    private JButton btnReset;
    private Runnable onClose;



    public StatsPanel(GameStats stats, Runnable onClose) {
        super(new BorderLayout(0, 12));
        this.stats   = stats;
        this.onClose = onClose;
        setBackground(COL_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));
        buildUI();
        refresh();
    }


    private void buildUI() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(COL_BG);

        JLabel title = label("📊  Battle Statistics", 20, Font.BOLD, COL_GOLD);
        JButton back = new JButton("← Back");
        back.setFont(new Font("SansSerif", Font.BOLD, 12));
        back.setForeground(COL_ACCENT);
        back.setBackground(COL_PANEL);
        back.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> { SoundManager.play("select"); onClose.run(); });

        titleBar.add(title, BorderLayout.WEST);
        titleBar.add(back,  BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);


        JPanel cards = new JPanel(new GridLayout(2, 3, 10, 10));
        cards.setBackground(COL_BG);

        lblWins      = label("0", 32, Font.BOLD, COL_GREEN);
        lblLosses    = label("0", 32, Font.BOLD, COL_RED);
        lblWinRate   = label("0%", 32, Font.BOLD, COL_GOLD);
        lblAvgRounds = label("0.0", 32, Font.BOLD, COL_ACCENT);
        lblBestRound = label("—", 32, Font.BOLD, new Color(200, 160, 255));
        lblTopDmg    = label("0", 32, Font.BOLD, new Color(255, 180, 80));

        cards.add(statCard("Wins",           lblWins,      COL_GREEN));
        cards.add(statCard("Losses",         lblLosses,    COL_RED));
        cards.add(statCard("Win Rate",       lblWinRate,   COL_GOLD));
        cards.add(statCard("Avg Rounds",     lblAvgRounds, COL_ACCENT));
        cards.add(statCard("Best Win",       lblBestRound, new Color(200, 160, 255)));
        cards.add(statCard("Record Damage",  lblTopDmg,    new Color(255, 180, 80)));
        add(cards, BorderLayout.CENTER);


        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setBackground(COL_BG);

        JLabel histTitle = label("Recent Matches", 13, Font.BOLD, COL_TEXT2);
        south.add(histTitle, BorderLayout.NORTH);

        historyPanel = new JPanel();
        historyPanel.setLayout(new BoxLayout(historyPanel, BoxLayout.Y_AXIS));
        historyPanel.setBackground(COL_SURFACE);

        JScrollPane scroll = new JScrollPane(historyPanel);
        scroll.setBackground(COL_SURFACE);
        scroll.getViewport().setBackground(COL_SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(COL_BORDER, 1, true));
        scroll.setPreferredSize(new Dimension(0, 160));
        south.add(scroll, BorderLayout.CENTER);


        btnReset = new JButton("Reset All Stats");
        btnReset.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnReset.setForeground(COL_RED);
        btnReset.setBackground(COL_PANEL);
        btnReset.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_RED, 1, true),
                new EmptyBorder(5, 14, 5, 14)));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset all stats? This cannot be undone.",
                    "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                stats.reset();
                refresh();
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnRow.setBackground(COL_BG);
        btnRow.add(btnReset);
        south.add(btnRow, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }



    public void refresh() {
        lblWins.setText(String.valueOf(stats.getTotalWins()));
        lblLosses.setText(String.valueOf(stats.getTotalLosses()));
        lblWinRate.setText(String.format("%.1f%%", stats.getWinRate()));
        lblAvgRounds.setText(String.format("%.1f", stats.getAvgRoundsPerMatch()));
        lblBestRound.setText(stats.getBestRoundDisplay());
        lblTopDmg.setText(String.valueOf(stats.getHighestDmgDealt()));


        historyPanel.removeAll();
        List<GameStats.MatchRecord> history = stats.getHistory();
        if (history.isEmpty()) {
            JLabel empty = label("No matches yet — go fight!", 11, Font.ITALIC, COL_TEXT2);
            empty.setBorder(new EmptyBorder(8, 10, 8, 10));
            historyPanel.add(empty);
        } else {
            for (GameStats.MatchRecord rec : history) {
                historyPanel.add(buildHistoryRow(rec));
            }
        }
        historyPanel.revalidate();
        historyPanel.repaint();
    }


    private JPanel buildHistoryRow(GameStats.MatchRecord rec) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(rec.won ? new Color(20, 40, 24) : new Color(40, 16, 16));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COL_BORDER),
                new EmptyBorder(5, 10, 5, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));


        JLabel badge = label(rec.won ? "WIN" : "LOSS", 10, Font.BOLD,
                rec.won ? COL_GREEN : COL_RED);
        badge.setPreferredSize(new Dimension(36, 0));


        JLabel summary = label(
                rec.heroClass + " vs " + rec.enemyName +
                " · " + rec.rounds + " round" + (rec.rounds == 1 ? "" : "s") +
                " · Dealt " + rec.damageDealt,
                10, Font.PLAIN, COL_TEXT2);


        JLabel time = label(rec.timestamp, 9, Font.PLAIN, new Color(80, 80, 110));

        row.add(badge,   BorderLayout.WEST);
        row.add(summary, BorderLayout.CENTER);
        row.add(time,    BorderLayout.EAST);
        return row;
    }


    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(COL_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accent.getRed()/3,
                        accent.getGreen()/3, accent.getBlue()/3, 180), 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel titleLbl = label(title.toUpperCase(), 10, Font.BOLD, COL_TEXT2);
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLbl);
        card.add(valueLabel);
        return card;
    }


    private JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        l.setBackground(COL_BG);
        return l;
    }
}
