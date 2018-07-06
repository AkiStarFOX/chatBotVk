package Other;

import ColorModel.HSV;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

public class Histo {
    BufferedImage image;
    TreeMap<Integer,Float> h;
    TreeMap<Integer,Float> s;
    TreeMap<Integer,Float> v;
    int max=0;
    float maxS=0;
    float maxV=0;

    public Histo(BufferedImage image) {
        this.image = image;
        initMap();
        for (int i=0;i<image.getWidth();i++){
            for (int j=0;j<image.getHeight();j++){
                Color c = new Color(image.getRGB(i,j));
                HSV hsv = HSV.getHSV(c.getRed(),c.getGreen(),c.getBlue());
//                ColorModel.HSL hsv=ColorModel.HSL.getHSL(c.getRed(),c.getGreen(),c.getBlue());
                float start=0;
                float end =0.1f;
                for(int k=0;k<10;k++){
                    if(hsv.getH()>=start && hsv.getH()<end){
                        float countH =h.get(k);

                        countH++;
                        h.put(k,countH);
                        float countS = s.get(k);
                        countS+=hsv.getS();
                        s.put(k,countS);
                        float countV = v.get(k);
                        countV+=hsv.getV();
                        v.put(k,countV);
                    }
//                    if(hsv.getS()>=start && hsv.getS()<end){
//                        float count =s.get(k);
//                        count++;
//                        s.put(k,count);
//                    }
//                    if(hsv.getV()>=start && hsv.getV()<end){
//                        float count =v.get(k);
//                        count++;
//                        v.put(k,count);
//                    }
                    start+=0.1;
                    end+=0.1;
                }
            }
        }
        for (Map.Entry e:h.entrySet()){
            max+=(float) e.getValue();
        }
        for (Map.Entry e:s.entrySet()){
            maxS+=(float) e.getValue();
        }
        for (Map.Entry e:v.entrySet()){
            maxV+=(float) e.getValue();
        }

    }
    private void initMap(){
        h = new TreeMap<>();
        s = new TreeMap<>();
        v = new TreeMap<>();
        for (int i=0;i<10;i++){
            h.put(i,0.f);
            s.put(i,0.f);
            v.put(i,0.f);
        }
    }

    public TreeMap<Integer, Float> getH() {
        TreeMap<Integer, Float> m= new TreeMap<>();
        for (Map.Entry e:h.entrySet()){
            float i=(float)e.getValue();
            m.put((int)e.getKey(),(float)e.getValue()/max);
        }
        return m;
    }

    public TreeMap<Integer, Float> getS() {
        TreeMap<Integer, Float> m= new TreeMap<>();
        for (Map.Entry e:s.entrySet()){
            m.put((int)e.getKey(),(float)e.getValue()/maxS);
        }
        return m;
    }

    public TreeMap<Integer, Float> getV() {
        TreeMap<Integer, Float> m= new TreeMap<>();
        for (Map.Entry e:v.entrySet()){
            m.put((int)e.getKey(),(float)e.getValue()/maxV);
        }
        return m;
    }
}
