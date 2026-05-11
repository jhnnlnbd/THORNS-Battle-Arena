import javax.sound.sampled.*;
import java.util.HashMap;
import java.util.Map;


public class SoundManager {


    private static final float SAMPLE_RATE  = 22050f;
    private static final int   SAMPLE_BITS  = 8;
    private static final int   CHANNELS     = 1;       // mono
    private static final boolean SIGNED     = true;
    private static final boolean BIG_ENDIAN = false;

    private static final AudioFormat FORMAT = new AudioFormat(
            SAMPLE_RATE, SAMPLE_BITS, CHANNELS, SIGNED, BIG_ENDIAN);


    private static boolean enabled = true;


    private static final Map<String, byte[]> SOUNDS = new HashMap<>();


    static {
        SOUNDS.put("attack",  buildSword());
        SOUNDS.put("skill",   buildSkill());
        SOUNDS.put("heal",    buildHeal());
        SOUNDS.put("defend",  buildDefend());
        SOUNDS.put("hit",     buildHit());
        SOUNDS.put("victory", buildVictory());
        SOUNDS.put("defeat",  buildDefeat());
        SOUNDS.put("levelup", buildLevelUp());
        SOUNDS.put("select",  buildSelect());
    }




    public static void play(String name) {
        if (!enabled) return;
        byte[] data = SOUNDS.get(name);
        if (data == null) return;
        playRaw(data);
    }

    public static void setEnabled(boolean on) { enabled = on; }
    public static boolean isEnabled()         { return enabled; }
    public static void toggle()               { enabled = !enabled; }


    private static void playRaw(byte[] pcm) {
        Thread t = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream ais = new AudioInputStream(
                        new java.io.ByteArrayInputStream(pcm),
                        FORMAT, pcm.length);
                clip.open(ais);
                clip.start();

                Thread.sleep(clip.getMicrosecondLength() / 1000 + 50);
                clip.close();
            } catch (Exception ignored) {

            }
        });
        t.setDaemon(true);
        t.start();
    }




    private static byte[] buildSword() {
        int len = (int)(SAMPLE_RATE * 0.12);
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.exp(-t * 40);

            double wave = Math.sin(2 * Math.PI * 800  * t)
                        + Math.sin(2 * Math.PI * 1600 * t) * 0.5
                        + Math.sin(2 * Math.PI * 3200 * t) * 0.25;
            b[i] = (byte)(wave * env * 60);
        }
        return b;
    }


    private static byte[] buildSkill() {
        int len = (int)(SAMPLE_RATE * 0.25);
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.exp(-t * 15);
            // Frequency sweep: starts high, falls slightly
            double freq = 1200 - t * 600;
            double wave = Math.sin(2 * Math.PI * freq * t);
            b[i] = (byte)(wave * env * 80);
        }
        return b;
    }


    private static byte[] buildHeal() {
        int len = (int)(SAMPLE_RATE * 0.35);
        byte[] b = new byte[len];
        double[] freqs = {523, 659, 784};
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.sin(Math.PI * t / (len / SAMPLE_RATE));
            double wave = 0;
            for (double f : freqs) wave += Math.sin(2 * Math.PI * f * t);
            b[i] = (byte)(wave * env * 25);
        }
        return b;
    }


    private static byte[] buildDefend() {
        int len = (int)(SAMPLE_RATE * 0.10);
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.exp(-t * 60);
            double wave = Math.sin(2 * Math.PI * 150 * t)
                        + Math.sin(2 * Math.PI * 80  * t);
            b[i] = (byte)(wave * env * 70);
        }
        return b;
    }


    private static byte[] buildHit() {
        int len = (int)(SAMPLE_RATE * 0.08);
        byte[] b = new byte[len];
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.exp(-t * 70);

            double wave = (rng.nextDouble() * 2 - 1) * 0.7
                        + Math.sin(2 * Math.PI * 200 * t) * 0.3;
            b[i] = (byte)(wave * env * 80);
        }
        return b;
    }


    private static byte[] buildVictory() {
        int len = (int)(SAMPLE_RATE * 0.7);
        byte[] b = new byte[len];

        double[] melody = {523, 659, 784, 1047};
        int partLen = len / melody.length;
        for (int p = 0; p < melody.length; p++) {
            double freq = melody[p];
            for (int i = 0; i < partLen && (p * partLen + i) < len; i++) {
                int idx  = p * partLen + i;
                double t = i / SAMPLE_RATE;
                double env = 1 - (double) i / partLen * 0.3;
                b[idx] = (byte)(Math.sin(2 * Math.PI * freq * t) * env * 70);
            }
        }
        return b;
    }


    private static byte[] buildDefeat() {
        int len = (int)(SAMPLE_RATE * 0.6);
        byte[] b = new byte[len];
        double[] melody = {392, 349, 330, 294};
        int partLen = len / melody.length;
        for (int p = 0; p < melody.length; p++) {
            for (int i = 0; i < partLen && (p * partLen + i) < len; i++) {
                int idx  = p * partLen + i;
                double t = i / SAMPLE_RATE;
                double env = Math.exp(-t * 2);
                b[idx] = (byte)(Math.sin(2 * Math.PI * melody[p] * t) * env * 65);
            }
        }
        return b;
    }


    private static byte[] buildLevelUp() {
        int len = (int)(SAMPLE_RATE * 0.45);
        byte[] b = new byte[len];
        double[] notes = {523, 659, 784, 1047, 1318};
        int partLen = len / notes.length;
        for (int p = 0; p < notes.length; p++) {
            for (int i = 0; i < partLen && (p * partLen + i) < len; i++) {
                int idx = p * partLen + i;
                double t = i / SAMPLE_RATE;
                b[idx] = (byte)(Math.sin(2 * Math.PI * notes[p] * t) * 70);
            }
        }
        return b;
    }


    private static byte[] buildSelect() {
        int len = (int)(SAMPLE_RATE * 0.06);
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            double t   = i / SAMPLE_RATE;
            double env = Math.exp(-t * 80);
            b[i] = (byte)(Math.sin(2 * Math.PI * 1000 * t) * env * 50);
        }
        return b;
    }
}
