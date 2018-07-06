package ColorModel;

import java.awt.*;

public class HSV {
    float h;
    float s;
    float v;
    int r;
    int g;
    int b;
    float countOfClaster;

    public HSV(float h, float s, float v,float countOfClaster) {
        this.h = h;
        this.s = s;
        this.v = v;
        this.countOfClaster = countOfClaster;
    }

    public HSV(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        float[] hsv = new float[3];
        Color.RGBtoHSB(r,g,b,hsv);
        this.h=hsv[0]*360;
        this.s=hsv[1]*100;
        this.v=hsv[2]*100;
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

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }
    public static HSV getHSV(int red,int green,int blue){
        return new HSV(red,green,blue);
    }

    public float getCountOfClaster() {
        return countOfClaster;
    }
}
