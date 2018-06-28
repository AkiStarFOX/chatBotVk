import java.awt.*;

public class HSL {
    float h;
    float s;
    float l;
    int r;
    int g;
    int b;
    float[][] hslArray;
    String URL;
    Histo histo;



    public static HSL getHSL(int red, int green, int blue){
        Color color = new Color(red,green,blue);
        return new HSL(fromRGB(color)[0],fromRGB(color)[1],fromRGB(color)[2]);
    }
    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public HSL(float h, float s, float l) {
        this.h = h;
        this.s = s;
        this.l = l;
    }

//    public HSL(int r, int g, int b) {
//        this.r = r;
//        this.g = g;
//        this.b = b;
//        float[] hsv = new float[3];
//        Color.RGBtoHSB(r,g,b,hsv);
//        this.h=hsv[0];
//        this.s=hsv[1];
//        this.l =hsv[2];
//    }
    public HSL(int[][] pallete, String URL){
        hslArray =new float[pallete.length][3];
        this.URL=URL;

        for (int i = 0; i < pallete.length; i++) {
            float[] hsv1 = new float[3];
            Color.RGBtoHSB(pallete[i][0],pallete[i][1],pallete[i][2],hsv1);
            hslArray[i][0] = hsv1[0];
            hslArray[i][1] = hsv1[1];
            hslArray[i][2] = hsv1[2];
        }

    }
    public HSL(Histo histo, String URL){
        this.URL=URL;
        this.histo=histo;
    }

    public Histo getHisto() {
        return histo;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getS() {
        return s;
    }

    public void setS(float s) {
        this.s = s;
    }

    public float getL() {
        return l;
    }

    public void setL(float l) {
        this.l = l;
    }

    public float[][] getHslArray() {
        return hslArray;
    }
    public static float[] fromRGB(Color color)
    {
        //  Get RGB values in the range 0 - 1

        float[] rgb = color.getRGBColorComponents( null );
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        //	Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        //  Calculate the Luminance

        float l = (max + min) / 2;
        //System.out.println(max + " : " + min + " : " + l);

        //  Calculate the Saturation

        float s = 0;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[] {h/360, s , l };
    }


}
