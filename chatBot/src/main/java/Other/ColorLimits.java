package Other;

public class ColorLimits {
    float h;
    float s;
    float v;
    float startH;
    float startS;
    float startV;
    float endH;
    float endS;
    float endV;

    public ColorLimits(float[] color,float offset) {
        this.h = color[0];
        this.s = color[1];
        this.v = color[2];

        startH = h - offset / 2;
        if (startH < 0.00f) startH = 0.f;
        endH = h + offset / 2;
        if (endH > 1.0f) endH = 1.0f;

        startS = s - offset / 2;
        if (startS < 0.00f) startS = 0.f;
        endS = s + offset / 2;
        if (endS > 1.0f) endS = 1.0f;

        startV = v - offset / 2;
        if (startV < 0.00f) startV = 0.f;
        endV = v + offset / 2;
        if (endV > 1.0f) endV = 1.0f;
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

    public float getStartH() {
        return startH;
    }

    public void setStartH(float startH) {
        this.startH = startH;
    }

    public float getStartS() {
        return startS;
    }

    public void setStartS(float startS) {
        this.startS = startS;
    }

    public float getStartV() {
        return startV;
    }

    public void setStartV(float startV) {
        this.startV = startV;
    }

    public float getEndH() {
        return endH;
    }

    public void setEndH(float endH) {
        this.endH = endH;
    }

    public float getEndS() {
        return endS;
    }

    public void setEndS(float endS) {
        this.endS = endS;
    }

    public float getEndV() {
        return endV;
    }

    public void setEndV(float endV) {
        this.endV = endV;
    }
}
