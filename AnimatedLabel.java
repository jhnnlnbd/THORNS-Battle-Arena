import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;




public class AnimatedLabel extends JLabel {


    private float alpha;
    private float yOffset;
    private Timer animTimer;
    private final Color textColor;



    public AnimatedLabel(String text, Color color, int fontSize) {
        super(text);
        this.textColor = color;
        this.alpha     = 1.0f;
        this.yOffset   = 0f;

        setFont(new Font("SansSerif", Font.BOLD, fontSize));
        setForeground(color);
        setOpaque(false);
        setHorizontalAlignment(CENTER);




        animTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                yOffset -= 1.5f;
                alpha   -= 0.025f;

                if (alpha <= 0) {
                    alpha = 0;
                    animTimer.stop();

                    Container parent = getParent();
                    if (parent != null) {
                        parent.remove(AnimatedLabel.this);
                        parent.repaint();
                    }
                }
                repaint();
            }
        });
        animTimer.start();
    }




    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);


        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setFont(getFont());
        g2.setColor(textColor);

        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(getText())) / 2;
        int y = (int)(getHeight() / 2 + fm.getAscent() / 2 + yOffset);


        g2.setColor(new Color(0, 0, 0, (int)(alpha * 200)));
        g2.drawString(getText(), x + 1, y + 1);
        g2.drawString(getText(), x - 1, y - 1);


        g2.setColor(new Color(textColor.getRed(), textColor.getGreen(),
                              textColor.getBlue(), (int)(alpha * 255)));
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
