import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;





public class BattleGUI extends JFrame {


    private static final Color C_BG   = new Color(13,  13,  28);
    private static final Color C_SURF = new Color(22,  22,  46);
    private static final Color C_PNL  = new Color(30,  30,  60);
    private static final Color C_BDR  = new Color(60,  55, 120);
    private static final Color C_ACC  = new Color(124, 111, 232);
    private static final Color C_AC2  = new Color(232, 124, 111);
    private static final Color C_GLD  = new Color(240, 192,  64);
    private static final Color C_GRN  = new Color(79,  201, 124);
    private static final Color C_RED  = new Color(232,  79,  79);
    private static final Color C_BLU  = new Color(79,  168, 232);
    private static final Color C_PUR  = new Color(160, 100, 200);
    private static final Color C_TXT  = new Color(224, 222, 255);
    private static final Color C_TX2  = new Color(160, 160, 192);


    private BattleEngine engine;
    private GameStats    gs;
    private StatsPanel   statsPanel;
    private int matchDmgDealt, matchDmgTaken;
    private boolean actionLocked = false;


    private CardLayout cardLayout;
    private JPanel     root;


    private String   selectedClass = "warrior";
    private int      selectedEnemy = -1;
    private JButton[] cBtns  = new JButton[4];
    private JButton[] eBtns  = new JButton[5];
    private Color[]   cCols  = {C_ACC, C_BLU, C_PUR, C_GLD};
    private Color[]   eCols  = {C_GRN, new Color(200,120,60),
                                new Color(160,60,160), C_RED, C_GLD};


    private JLabel heroSprite, heroName, heroLv, heroStatusLbl;
    private JLabel heroHpLbl, heroMpLbl, heroAtk, heroDef, heroSpd;
    private JProgressBar heroHpBar, heroMpBar;
    private JPanel heroCard;

    private JLabel enemySprite, enemyName, enemyRankLbl;
    private JLabel enemyHpLbl, enemyMpLbl;
    private JProgressBar enemyHpBar, enemyMpBar;
    private JPanel enemyCard;

    private JButton btnAttack, btnSkill, btnHeal, btnDefend;
    private JLabel  skillNmLbl, skillDsLbl;
    private JLabel  roundLbl, turnLbl, poisonLbl;
    private JTextPane logPane;


    private JLabel  goTitle, goMsg;


    private int tHeroHp, tHeroMp, tEnemyHp, tEnemyMp;
    private Timer barTimer;


    private JButton btnSound;


    public BattleGUI() {
        super("⚔ THORNS Battle Arena");
        gs = GameStats.load();
        applyLAF();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 740);
        setMinimumSize(new Dimension(820, 680));
        setLocationRelativeTo(null);
        buildAll();
        startBarTimer();
        show("SELECT");
        setVisible(true);
    }

    private void applyLAF() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        UIManager.put("Panel.background",      C_BG);
        UIManager.put("Label.foreground",      C_TXT);
        UIManager.put("ScrollPane.background", C_SURF);
        UIManager.put("TextPane.background",   C_SURF);
        UIManager.put("OptionPane.background", C_PNL);
        UIManager.put("OptionPane.messageforeground", C_TXT);
    }

    private void show(String key) { cardLayout.show(root, key); }


    private void buildAll() {
        cardLayout  = new CardLayout();
        root        = new JPanel(cardLayout);
        root.setBackground(C_BG);
        statsPanel  = new StatsPanel(gs, () -> show("SELECT"));
        root.add(buildSelectScreen(), "SELECT");
        root.add(buildBattleScreen(), "BATTLE");
        root.add(buildGameOverScreen(),"GAMEOVER");
        root.add(statsPanel,          "STATS");
        add(root);
    }


    private JPanel buildSelectScreen() {
        JPanel p = pad(new BorderLayout(0,12), 22, 34);


        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(C_BG);
        JLabel t1 = lbl("⚔  THORNS BATTLE ARENA  ⚔", 25, Font.BOLD, C_GLD);
        t1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel t2 = lbl("Each hero class extends Character — select yours", 12, Font.ITALIC, C_ACC);
        t2.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel titles = new JPanel(new GridLayout(2,1,0,4)); titles.setBackground(C_BG);
        titles.add(t1); titles.add(t2);

        JPanel tbr = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); tbr.setBackground(C_BG);
        btnSound = toolBtn("🔊 ON", C_GRN, () -> {
            SoundManager.toggle();
            btnSound.setText(SoundManager.isEnabled() ? "🔊 ON" : "🔇 OFF");
        });
        tbr.add(toolBtn("📊 Stats", C_ACC, () -> { statsPanel.refresh(); show("STATS"); }));
        tbr.add(btnSound);
        titleRow.add(titles, BorderLayout.CENTER);
        titleRow.add(tbr,    BorderLayout.EAST);
        p.add(titleRow, BorderLayout.NORTH);


        String[][] cd = {
            {"⚔","Warrior","HP:130  MP:50","ATK:20 DEF:14 SPD:9","Shield Bash","Ignores DEF. 28 fixed dmg.\nRage from taking hits."},
            {"✦","Mage",   "HP:85   MP:110","ATK:12 DEF:7 SPD:15","Fireball","~38 magic, ignores DEF.\nMana Shield: absorbs 40%."},
            {"◆","Rogue",  "HP:95   MP:70","ATK:24 DEF:9 SPD:22","Backstab","ATK×2. 40% crit chance.\nPoisons enemy 3 turns."},
            {"✤","Paladin","HP:115  MP:85","ATK:16 DEF:16 SPD:8","Holy Strike","22 dmg + 18 self-heal.\nHighest DEF stat."}
        };
        String[] ck = {"warrior","mage","rogue","paladin"};
        JPanel cp = new JPanel(new GridLayout(1,4,10,0)); cp.setBackground(C_BG);
        for (int i = 0; i < 4; i++) {
            final int fi = i;
            cBtns[i] = selCard(cd[i], cCols[i], () -> {
                selectedClass = ck[fi]; SoundManager.play("select");
                for (JButton b : cBtns) b.setBorder(defBdr());
                cBtns[fi].setBorder(selBdr(cCols[fi]));
            });
            cp.add(cBtns[i]);
        }
        cBtns[0].setBorder(selBdr(C_ACC));


        String[][] ed = {
            {"👹","Goblin","Rank I","HP:80 ATK:14","Easy warm-up"},
            {"👺","Orc","Rank II","HP:115 ATK:19","Smash breaks guard"},
            {"🧛","Vampire","Rank II","HP:95 ATK:17","Drain Life heals it"},
            {"🐉","Dragon","Rank III","HP:160 ATK:26","Inferno ignores DEF"},
            {"🎲","Random","Rank ?","Surprise!","Let fate decide"}
        };
        JPanel ep = new JPanel(new GridLayout(1,5,8,0)); ep.setBackground(C_BG);
        for (int i = 0; i < 5; i++) {
            final int fi = i;
            eBtns[i] = enemyMiniCard(ed[i], eCols[i], () -> {
                selectedEnemy = (fi == 4) ? -1 : fi; SoundManager.play("select");
                for (JButton b : eBtns) b.setBorder(defBdr());
                eBtns[fi].setBorder(selBdr(eCols[fi]));
            });
            ep.add(eBtns[i]);
        }
        eBtns[4].setBorder(selBdr(C_GLD));

        JLabel hTitle = lbl("CHOOSE YOUR CLASS", 10, Font.BOLD, C_TX2);
        JLabel eTitle = lbl("CHOOSE YOUR OPPONENT", 10, Font.BOLD, C_TX2);
        hTitle.setBorder(new EmptyBorder(0,0,5,0));
        eTitle.setBorder(new EmptyBorder(8,0,5,0));

        JPanel ctr = new JPanel(); ctr.setLayout(new BoxLayout(ctr, BoxLayout.Y_AXIS));
        ctr.setBackground(C_BG);
        ctr.add(hTitle); ctr.add(cp);
        ctr.add(eTitle); ctr.add(ep);
        p.add(ctr, BorderLayout.CENTER);


        JButton startBtn = bigBtn("⚔  START BATTLE", C_GLD, () -> startBattle());
        JLabel oopLbl = lbl("", 9, Font.ITALIC, C_TX2);
        oopLbl.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel south = new JPanel(new GridLayout(2,1,0,4)); south.setBackground(C_BG);
        south.add(startBtn); south.add(oopLbl);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }


    private JPanel buildBattleScreen() {
        JPanel p = pad(new BorderLayout(0,8), 8, 12);


        JPanel top = new JPanel(new BorderLayout()); top.setBackground(C_BG);
        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT,12,2)); leftTop.setBackground(C_BG);
        roundLbl  = lbl("Round 1",   13, Font.BOLD,  C_GLD);
        turnLbl   = lbl("Your Turn", 12, Font.PLAIN, C_GRN);
        poisonLbl = lbl("",          11, Font.BOLD,  C_PUR);
        leftTop.add(roundLbl); leftTop.add(lbl("·",12,Font.PLAIN,C_BDR)); leftTop.add(turnLbl); leftTop.add(poisonLbl);
        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,2)); rightTop.setBackground(C_BG);
        rightTop.add(toolBtn("📊",       C_ACC, () -> { statsPanel.refresh(); show("STATS"); }));
        rightTop.add(toolBtn("← Menu",   C_TX2, () -> show("SELECT")));
        top.add(leftTop, BorderLayout.WEST); top.add(rightTop, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);


        JPanel ctr = new JPanel(new BorderLayout(10,0)); ctr.setBackground(C_BG);
        heroCard  = buildHeroCard();
        enemyCard = buildEnemyCard();
        ctr.add(heroCard,    BorderLayout.WEST);
        ctr.add(buildLog(),  BorderLayout.CENTER);
        ctr.add(enemyCard,   BorderLayout.EAST);
        p.add(ctr, BorderLayout.CENTER);
        p.add(buildActBar(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildHeroCard() {
        JPanel c = charCard(224, C_ACC);
        heroSprite    = lbl("⚔", 38, Font.PLAIN, C_ACC); heroSprite.setHorizontalAlignment(SwingConstants.CENTER);
        heroName      = lbl("WARRIOR", 13, Font.BOLD, C_ACC); heroName.setHorizontalAlignment(SwingConstants.CENTER);
        heroLv        = lbl("Lv.1 | XP 0/100", 9, Font.PLAIN, C_GLD); heroLv.setHorizontalAlignment(SwingConstants.CENTER);
        heroStatusLbl = lbl("", 10, Font.BOLD, C_GLD); heroStatusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        heroHpLbl     = lbl("HP 130/130", 10, Font.PLAIN, C_TX2);
        heroHpBar     = mkBar(C_GRN, 130); tHeroHp = 130;
        heroMpLbl     = lbl("MP  50/ 50", 10, Font.PLAIN, C_TX2);
        heroMpBar     = mkBar(C_BLU,  50); tHeroMp =  50;
        heroAtk       = lbl("ATK 20", 10, Font.PLAIN, C_RED); heroAtk.setHorizontalAlignment(SwingConstants.CENTER);
        heroDef       = lbl("DEF 14", 10, Font.PLAIN, C_BLU); heroDef.setHorizontalAlignment(SwingConstants.CENTER);
        heroSpd       = lbl("SPD  9", 10, Font.PLAIN, C_GLD); heroSpd.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel stats = new JPanel(new GridLayout(1,3,4,0)); stats.setBackground(C_SURF);
        stats.setBorder(new EmptyBorder(4,4,4,4)); stats.add(heroAtk); stats.add(heroDef); stats.add(heroSpd);

        c.add(cx(heroSprite)); c.add(vs(4)); c.add(cx(heroName)); c.add(cx(heroLv));
        c.add(cx(heroStatusLbl)); c.add(vs(8));
        c.add(heroHpLbl); c.add(vs(2)); c.add(heroHpBar); c.add(vs(5));
        c.add(heroMpLbl); c.add(vs(2)); c.add(heroMpBar); c.add(vs(8));
        c.add(stats);
        return c;
    }

    private JPanel buildEnemyCard() {
        JPanel c = charCard(224, C_AC2);
        enemySprite  = lbl("👹", 38, Font.PLAIN, C_AC2); enemySprite.setHorizontalAlignment(SwingConstants.CENTER);
        enemyName    = lbl("GOBLIN KING", 13, Font.BOLD, C_AC2); enemyName.setHorizontalAlignment(SwingConstants.CENTER);
        enemyRankLbl = lbl("Rank I | 60 XP", 9, Font.PLAIN, C_RED); enemyRankLbl.setHorizontalAlignment(SwingConstants.CENTER);
        enemyHpLbl   = lbl("HP  80/ 80", 10, Font.PLAIN, C_TX2);
        enemyHpBar   = mkBar(C_RED,  80); tEnemyHp = 80;
        enemyMpLbl   = lbl("MP  30/ 30", 10, Font.PLAIN, C_TX2);
        enemyMpBar   = mkBar(C_BLU,  30); tEnemyMp = 30;

        c.add(cx(enemySprite)); c.add(vs(4)); c.add(cx(enemyName)); c.add(cx(enemyRankLbl)); c.add(vs(8));
        c.add(enemyHpLbl); c.add(vs(2)); c.add(enemyHpBar); c.add(vs(5));
        c.add(enemyMpLbl); c.add(vs(2)); c.add(enemyMpBar);
        return c;
    }

    private JPanel buildLog() {
        JPanel p = new JPanel(new BorderLayout(0,4)); p.setBackground(C_BG);
        p.add(lbl("▶ Battle Log", 10, Font.BOLD, C_TX2), BorderLayout.NORTH);
        logPane = new JTextPane();
        logPane.setEditable(false); logPane.setBackground(C_SURF);
        logPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logPane.setBorder(new EmptyBorder(8,8,8,8));
        JScrollPane sc = new JScrollPane(logPane);
        sc.setBackground(C_SURF); sc.getViewport().setBackground(C_SURF);
        sc.setBorder(BorderFactory.createLineBorder(C_BDR, 1, true));
        p.add(sc, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildActBar() {
        JPanel outer = new JPanel(new BorderLayout(0,5)); outer.setBackground(C_BG);
        JPanel strip = new JPanel(new FlowLayout(FlowLayout.CENTER,8,0)); strip.setBackground(C_BG);
        skillNmLbl = lbl("Shield Bash (12 MP)", 11, Font.BOLD, C_ACC);
        skillDsLbl = lbl("Ignores DEF — 28 fixed dmg", 10, Font.ITALIC, C_TX2);
        strip.add(skillNmLbl); strip.add(lbl("—",10,Font.PLAIN,C_BDR)); strip.add(skillDsLbl);
        outer.add(strip, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(1,4,10,0)); btns.setBackground(C_BG);
        btns.setPreferredSize(new Dimension(0,72));
        btnAttack = acBtn("⚔  ATTACK",  "Basic strike",          C_RED,  () -> doAction("attack"));
        btnSkill  = acBtn("✦  SKILL",   "Special skill",         C_ACC,  () -> doAction("skill"));
        btnHeal   = acBtn("♥  HEAL",    "28% HP  (18 MP)",       C_GRN,  () -> doAction("heal"));
        btnDefend = acBtn("✤  DEFEND",  "Halve dmg  (+10 MP)",   C_GLD,  () -> doAction("defend"));
        btns.add(btnAttack); btns.add(btnSkill); btns.add(btnHeal); btns.add(btnDefend);
        outer.add(btns, BorderLayout.CENTER);
        return outer;
    }


    private JPanel buildGameOverScreen() {
        JPanel bg = new JPanel(new GridBagLayout()); bg.setBackground(C_BG);
        JPanel box = new JPanel(); box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(C_PNL);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_GLD, 2, true), new EmptyBorder(40,70,40,70)));

        goTitle = lbl("★  VICTORY!  ★", 34, Font.BOLD, C_GLD); goTitle.setAlignmentX(CENTER_ALIGNMENT);
        goMsg   = lbl("",               12, Font.PLAIN, C_TX2); goMsg.setAlignmentX(CENTER_ALIGNMENT);

        JButton btnPlay  = bigBtn("▶  PLAY AGAIN",  C_GLD, () -> show("SELECT"));
        JButton btnStats = bigBtn("📊  View Stats",  C_ACC, () -> { statsPanel.refresh(); show("STATS"); });
        btnPlay.setAlignmentX(CENTER_ALIGNMENT);
        btnStats.setAlignmentX(CENTER_ALIGNMENT);

        box.add(goTitle); box.add(Box.createVerticalStrut(14));
        box.add(goMsg);   box.add(Box.createVerticalStrut(28));
        box.add(btnPlay); box.add(Box.createVerticalStrut(8));
        box.add(btnStats);
        bg.add(box);
        return bg;
    }


    private void startBattle() {

        Character hero;
        switch (selectedClass) {
            case "warrior": hero = new Warrior("Hero");  break;
            case "mage":    hero = new Mage("Zara");     break;
            case "rogue":   hero = new Rogue("Shadow");  break;
            default:        hero = new Paladin("Light"); break;
        }
        Enemy enemy = (selectedEnemy < 0) ? BattleEngine.createRandomEnemy()
                                           : BattleEngine.createEnemyByIndex(selectedEnemy);
        engine = new BattleEngine(hero, enemy);
        matchDmgDealt = 0; matchDmgTaken = 0; actionLocked = false;

        logPane.setText("");
        refreshAll();
        logLine("=== " + hero.getSprite() + " " + hero.getName() +
                " [" + hero.getClassName() + "] vs " +
                enemy.getSprite() + " " + enemy.getName() +
                " [Rank " + enemy.getRank() + "] ===\n", C_GLD, true);
        logLine("Skill: " + hero.getSkillName() + " — " + hero.getSkillDescription() + "\n\n", C_TX2, false);
        SoundManager.play("select");
        show("BATTLE");
        setBtns(true);
    }

    private void doAction(String action) {
        if (actionLocked || engine == null || engine.isGameOver()) return;

        Character hero = engine.getHero();
        BattleEngine.BattleResult result;
        switch (action) {
            case "attack":
                result = engine.doAttack(); SoundManager.play("attack"); break;
            case "skill":
                if (hero.getMp() < hero.getSkillCost()) { logLine("Not enough MP!\n", C_RED, false); return; }
                result = engine.doSkill(); SoundManager.play("skill"); break;
            case "heal":
                if (hero.getMp() < 18) { logLine("Not enough MP!\n", C_RED, false); return; }
                result = engine.doHeal(); SoundManager.play("heal"); break;
            default:
                result = engine.doDefend(); SoundManager.play("defend");
        }
        if (result == null) return;

        matchDmgDealt += result.heroDmg;
        matchDmgTaken += result.enemyDmg;


        if (result.heroDmg  > 0) spawnFloat(enemyCard, "-" + result.heroDmg, C_RED);
        if (result.enemyDmg > 0) { spawnFloat(heroCard, "-" + result.enemyDmg, C_AC2); SoundManager.play("hit"); }
        if (result.healAmt  > 0) spawnFloat(heroCard, "+" + result.healAmt, C_GRN);

        for (String line : result.message.split("\n")) {
            if (line.trim().isEmpty()) continue;
            Color c = line.contains("heals") ? C_GRN
                    : line.contains("Poison") ? C_PUR : C_TXT;
            logLine(line + "\n", c, false);
        }
        if (result.specialNote != null && !result.specialNote.isEmpty())
            logLine("  ▶ " + result.specialNote + "\n", C_GLD, false);
        if (result.levelUp) {
            SoundManager.play("levelup");
            logLine("\n⭐ " + result.levelUpMsg + "\n\n", C_GLD, true);
        }

        refreshAll();
        if (result.gameOver) endMatch(result.playerWon, result.levelUpMsg);
    }

    private void endMatch(boolean won, String xpMsg) {
        setBtns(false);
        Character hero  = engine.getHero();
        Character enemy = engine.getEnemy();
        gs.recordMatch(hero.getClassName(), enemy.getName(),
                won, engine.getRound(), matchDmgDealt, matchDmgTaken);
        gs.save();

        SoundManager.play(won ? "victory" : "defeat");
        goTitle.setText(won ? "★  VICTORY!  ★" : "✗  DEFEATED");
        goTitle.setForeground(won ? C_GLD : C_RED);
        goMsg.setText(won
                ? hero.getName() + " defeated " + enemy.getName() +
                  " in " + engine.getRound() + " rounds!" +
                  (xpMsg != null ? "  " + xpMsg : "")
                : "You were overwhelmed — try a different class!");

        Timer t = new Timer(900, e -> show("GAMEOVER")); t.setRepeats(false); t.start();
    }


    private void refreshAll() {
        if (engine == null) return;
        Character hero  = engine.getHero();
        Character enemy = engine.getEnemy();

        heroSprite.setText(hero.getSprite());
        heroName.setText(hero.getClassName().toUpperCase());
        heroLv.setText("Lv." + hero.getLevel() + " | XP " + hero.getXp() + "/" + hero.getXpThreshold());
        heroHpLbl.setText(String.format("HP %3d/%3d", hero.getHp(), hero.getMaxHp()));
        heroMpLbl.setText(String.format("MP %3d/%3d", hero.getMp(), hero.getMaxMp()));
        heroHpBar.setMaximum(hero.getMaxHp()); heroMpBar.setMaximum(hero.getMaxMp());
        tHeroHp = Math.max(0, hero.getHp()); tHeroMp = Math.max(0, hero.getMp());
        heroAtk.setText("ATK " + hero.getAtk()); heroDef.setText("DEF " + hero.getDef()); heroSpd.setText("SPD " + hero.getSpd());


        StringBuilder st = new StringBuilder();
        if (hero.isDefending()) st.append("🛡 DEFENDING  ");
        if (hero instanceof Warrior && ((Warrior)hero).getRageMeter() > 0)
            st.append("🔥 RAGE ").append(((Warrior)hero).getRageMeter()).append("%");
        heroStatusLbl.setText(st.toString());

        skillNmLbl.setText(hero.getSkillName() + "  (" + hero.getSkillCost() + " MP)");
        skillDsLbl.setText(hero.getSkillDescription());

        if (enemy instanceof Enemy) {
            Enemy e = (Enemy)enemy;
            enemyRankLbl.setText("Rank " + e.getRank() + " | " + e.getXpReward() + " XP");
        }
        enemySprite.setText(enemy.getSprite());
        enemyName.setText(enemy.getName().toUpperCase());
        enemyHpLbl.setText(String.format("HP %3d/%3d", enemy.getHp(), enemy.getMaxHp()));
        enemyMpLbl.setText(String.format("MP %3d/%3d", enemy.getMp(), enemy.getMaxMp()));
        enemyHpBar.setMaximum(enemy.getMaxHp()); enemyMpBar.setMaximum(enemy.getMaxMp());
        tEnemyHp = Math.max(0, enemy.getHp()); tEnemyMp = Math.max(0, enemy.getMp());

        roundLbl.setText("Round " + engine.getRound());
        boolean locked = actionLocked || engine.isGameOver();
        turnLbl.setText(engine.isGameOver() ? "Battle Over" : locked ? "Enemy Turn…" : "Your Turn");
        turnLbl.setForeground(engine.isGameOver() ? C_TX2 : locked ? C_AC2 : C_GRN);

        boolean noAct = locked || engine.isGameOver();
        btnAttack.setEnabled(!noAct);
        btnSkill.setEnabled(!noAct && hero.getMp() >= hero.getSkillCost());
        btnHeal.setEnabled(!noAct && hero.getMp() >= 18);
        btnDefend.setEnabled(!noAct);
    }

    private void setBtns(boolean on) {
        btnAttack.setEnabled(on); btnSkill.setEnabled(on);
        btnHeal.setEnabled(on);   btnDefend.setEnabled(on);
    }


    private void spawnFloat(JPanel parent, String text, Color color) {
        AnimatedLabel fl = new AnimatedLabel(text, color, 18);

        int x = parent.getWidth()  / 2 - 40;
        int y = parent.getHeight() / 2 - 30;
        fl.setBounds(x, y, 80, 50);
        parent.setLayout(null);
        parent.add(fl, 0);
        parent.repaint();
    }


    private void startBarTimer() {
        barTimer = new Timer(16, e -> {
            step(heroHpBar,  tHeroHp);  step(heroMpBar,  tHeroMp);
            step(enemyHpBar, tEnemyHp); step(enemyMpBar, tEnemyMp);
        });
        barTimer.start();
    }
    private void step(JProgressBar bar, int target) {
        int cur = bar.getValue();
        if (cur == target) return;
        int d = Math.max(1, Math.abs(target - cur) / 6);
        bar.setValue(cur < target ? Math.min(cur + d, target) : Math.max(cur - d, target));
    }


    private void logLine(String text, Color color, boolean bold) {
        StyledDocument doc = logPane.getStyledDocument();
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, color); StyleConstants.setBold(a, bold);
        StyleConstants.setFontFamily(a, "Monospaced"); StyleConstants.setFontSize(a, 11);
        try { doc.insertString(doc.getLength(), text, a); logPane.setCaretPosition(doc.getLength()); }
        catch (BadLocationException ignored) {}
    }




    private JLabel lbl(String t, int sz, int style, Color c) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", style, sz));
        l.setForeground(c); l.setBackground(C_BG); return l;
    }

    private JProgressBar mkBar(Color color, int max) {
        JProgressBar b = new JProgressBar(0, max) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20,20,40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                int f = (int)((double)getValue()/getMaximum()*getWidth());
                if (f > 0) { g2.setColor(color); g2.fillRoundRect(0,0,f,getHeight(),6,6); }
                g2.dispose();
            }
        };
        b.setPreferredSize(new Dimension(0,10)); b.setMaximumSize(new Dimension(Integer.MAX_VALUE,10));
        b.setBorderPainted(false); b.setStringPainted(false); b.setOpaque(false); return b;
    }

    private JButton acBtn(String t, String s, Color accent, Runnable fn) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled()
                    ? (getModel().isPressed()  ? accent.darker().darker()
                     : getModel().isRollover() ? new Color(accent.getRed()/5, accent.getGreen()/5, accent.getBlue()/5)
                     : new Color(accent.getRed()/8, accent.getGreen()/8, accent.getBlue()/8))
                    : new Color(28,28,44);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if (isEnabled()) { g2.setColor(accent); g2.setStroke(new BasicStroke(1f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setLayout(new GridLayout(2,1,0,2));
        btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setBorder(new EmptyBorder(8,6,8,6));
        JLabel ml = lbl(t, 12, Font.BOLD, accent); ml.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel sl = lbl(s,  9, Font.PLAIN, C_TX2);  sl.setHorizontalAlignment(SwingConstants.CENTER);
        btn.add(ml); btn.add(sl); btn.addActionListener(e -> fn.run()); return btn;
    }

    private JButton selCard(String[] d, Color accent, Runnable fn) {
        // d = {sprite, name, hpMp, atkDefSpd, skillName, skillDesc}
        JButton btn = flatBtn(accent);
        btn.setLayout(new GridLayout(6,1,0,3)); btn.setBorder(defBdr());
        JLabel sp = lbl(d[0],32,Font.PLAIN,accent); sp.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nm = lbl(d[1],13,Font.BOLD,C_TXT);   nm.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel s1 = lbl(d[2], 9,Font.PLAIN,C_TX2);  s1.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel s2 = lbl(d[3], 9,Font.PLAIN,C_TX2);  s2.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel sk = lbl(d[4],11,Font.BOLD,accent);   sk.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea sd = miniTA(d[5], new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),190));
        btn.add(sp); btn.add(nm); btn.add(s1); btn.add(s2); btn.add(sk); btn.add(sd);
        btn.addActionListener(e -> fn.run()); return btn;
    }

    private JButton enemyMiniCard(String[] d, Color accent, Runnable fn) {
        // d = {sprite, name, rank, stats, hint}
        JButton btn = flatBtn(accent);
        btn.setLayout(new GridLayout(5,1,0,2)); btn.setBorder(defBdr());
        JLabel sp = lbl(d[0],26,Font.PLAIN,accent); sp.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nm = lbl(d[1],12,Font.BOLD,C_TXT);   nm.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel rk = lbl(d[2], 9,Font.BOLD,accent);  rk.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel st = lbl(d[3], 9,Font.PLAIN,C_TX2);  st.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel hi = lbl(d[4], 8,Font.ITALIC,new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),180));
        hi.setHorizontalAlignment(SwingConstants.CENTER);
        btn.add(sp); btn.add(nm); btn.add(rk); btn.add(st); btn.add(hi);
        btn.addActionListener(e -> fn.run()); return btn;
    }

    private JButton flatBtn(Color accent) {
        return new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? C_PNL.brighter() : C_PNL);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(accent); g2.fillRect(0,0,getWidth(),4);
                g2.dispose(); super.paintComponent(g);
            }
        };
    }

    private JButton toolBtn(String text, Color c, Runnable fn) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif",Font.PLAIN,11)); b.setForeground(c); b.setBackground(C_PNL);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BDR,1,true), new EmptyBorder(4,10,4,10)));
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> fn.run()); return b;
    }

    private JButton bigBtn(String text, Color c, Runnable fn) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif",Font.BOLD,14)); b.setForeground(c); b.setBackground(C_PNL);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c,1,true), new EmptyBorder(10,24,10,24)));
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> fn.run()); return b;
    }

    private JPanel charCard(int w, Color accent) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_SURF);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent,1,true), new EmptyBorder(12,10,12,10)));
        if (w > 0) p.setPreferredSize(new Dimension(w,0)); return p;
    }

    private JPanel pad(BorderLayout layout, int v, int h) {
        JPanel p = new JPanel(layout); p.setBackground(C_BG);
        p.setBorder(new EmptyBorder(v,h,v,h)); return p;
    }

    private JPanel cx(JLabel l) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        p.setBackground(C_SURF); p.add(l); return p;
    }

    private Component vs(int h) { return Box.createVerticalStrut(h); }

    private Border defBdr()            { return new EmptyBorder(12,8,12,8); }
    private Border selBdr(Color accent){ return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent,2,true), new EmptyBorder(10,6,10,6)); }

    private JTextArea miniTA(String text, Color c) {
        JTextArea a = new JTextArea(text);
        a.setFont(new Font("Monospaced",Font.PLAIN,9)); a.setForeground(c);
        a.setBackground(new Color(0,0,0,0)); a.setOpaque(false);
        a.setEditable(false); a.setFocusable(false);
        a.setAlignmentX(CENTER_ALIGNMENT); return a;
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(BattleGUI::new);
    }
}
