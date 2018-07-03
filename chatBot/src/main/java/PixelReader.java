import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PixelReader {
    BufferedImage image;
    Map<Integer, Map<Integer, Float>> resultMap = new HashMap<>();
    int pixelCount;

    Map<Integer, Float> red;
    Map<Integer, Float> orange;
    Map<Integer, Float> yellow;
    Map<Integer, Float> light_yellow;
    Map<Integer, Float> light_green;
    Map<Integer, Float> green;
    Map<Integer, Float> light_blue;
    Map<Integer, Float> blue;
    Map<Integer, Float> dark_blue;
    Map<Integer, Float> purple;
    Map<Integer, Float> dark_purple;
    Map<Integer, Float> pink;

    float black = 0;
    float grey = 0;
    float white = 0;
    float ligth_grey = 0;


    public PixelReader(BufferedImage image) {
        pixelCount=image.getHeight()*image.getWidth();
        this.image = image;
        initResultMap();
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j));
                HSV hsv = HSV.getHSV(c.getRed(), c.getGreen(), c.getBlue());
                float h = hsv.getH();
                float s = hsv.getS();
                float v = hsv.getV();
                if (v < 0.07) {
                    black++;
                } else if ((v >= 0.07 && v <= 0.13) && s <= 0.33) {
                    grey++;
                } else if (s <= 0.13 && v >= 0.87) {
                    white++;
                } else if ((s > 0.13 && s <= 0.20) && (v >= 0.70 && v < 0.87)) {
                    ligth_grey++;
                } else {
                    if (h <= 0.047) {
                        separation(red,hsv);
                    }else if(h<=0.077){
                        separation(orange,hsv);
                    }else if(h<=0.13){
                        separation(yellow,hsv);
                    }else if(h<=0.16){
                        separation(light_yellow,hsv);
                    }else if(h<=0.25){
                        separation(light_green,hsv);
                    }else if(h<=0.40){
                        separation(green,hsv);
                    }else if(h<=0.50){
                        separation(light_blue,hsv);
                    }else if(h<0.58){
                        separation(blue,hsv);
                    }else if(h<0.72){
                        separation(dark_blue,hsv);
                    }else if(h<0.80){
                        separation(purple,hsv);
                    }else if(h<0.86){
                        separation(dark_purple,hsv);
                    }else if(h<0.94){
                        separation(pink,hsv);
                    }else if(h<1.f){
                        separation(red,hsv);
                    }
                }
            }
        }
        for (Map.Entry e:resultMap.entrySet()){
            Map<Integer,Float> m = (TreeMap<Integer,Float>)e.getValue();
            for(Map.Entry map:m.entrySet()){
                m.put((int)map.getKey(),(float)map.getValue()/pixelCount);
            }
        }
        black=black/pixelCount;
        grey=grey/pixelCount;
        white=white/pixelCount;
        ligth_grey=ligth_grey/pixelCount;
        System.out.println("");
    }


    private void initResultMap() {
        red = new TreeMap<>();
        orange = new TreeMap<>();
        yellow = new TreeMap<>();
        light_yellow = new TreeMap<>();
        light_green = new TreeMap<>();
        green = new TreeMap<>();
        light_blue = new TreeMap<>();
        blue = new TreeMap<>();
        dark_blue = new TreeMap<>();
        purple = new TreeMap<>();
        dark_purple = new TreeMap<>();
        pink = new TreeMap<>();
        for (int i = 0; i < 16; i++) {
            red.put(i, 0.f);
            orange.put(i, 0.f);
            yellow.put(i, 0.f);
            light_yellow.put(i, 0.f);
            light_green.put(i, 0.f);
            green.put(i, 0.f);
            light_blue.put(i, 0.f);
            blue.put(i, 0.f);
            dark_blue.put(i, 0.f);
            purple.put(i, 0.f);
            dark_purple.put(i, 0.f);
            pink.put(i, 0.f);
        }

        resultMap.put(0, red);
        resultMap.put(1, orange);
        resultMap.put(2, yellow);
        resultMap.put(3, light_yellow);
        resultMap.put(4, light_green);
        resultMap.put(5, green);
        resultMap.put(6, light_blue);
        resultMap.put(7, blue);
        resultMap.put(8, dark_blue);
        resultMap.put(9, purple);
        resultMap.put(10, dark_purple);
        resultMap.put(11, pink);

    }

    private void separation(Map<Integer, Float> map, HSV hsv) {
        float h = hsv.getH();
        float s = hsv.getS();
        float v = hsv.getV();

        float limitx1 = 0.f;
        float limitx2 = 0.25f;
        float limity1 = 0.f;
        float limity2 = 0.26f;

        int index = 0;
        for (int x = 0; x < 4; x++) {
            if (v > limitx1 && v <= limitx2)
                for (int y = 0; y < 4; y++) {
                    if (s >= limity1 && s <= limity2) {
                        float numb = map.get(index);
                        numb++;
                        map.put(index, numb);
                    } else {
                        index++;
                    }
                    limity1 += 0.25f;
                    limity2 += 0.25f;
                }
            else {
                index += 4;
            }
            limitx1 += 0.25f;
            limitx2 += 0.25f;
        }

    }

    public Map<Integer, Map<Integer, Float>> getResultMap() {


        return resultMap;
    }

    public Map<Integer, Float> getRed() {
        return red;
    }

    public float getBlack() {
        return black;
    }

    public float getGrey() {
        return grey;
    }

    public float getWhite() {
        return white;
    }

    public float getLigth_grey() {
        return ligth_grey;
    }
}
