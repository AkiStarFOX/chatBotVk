package Other;

import ColorModel.HSV;

import java.util.Comparator;

public class HSVcomp implements Comparator<HSV> {


    @Override
    public int compare(HSV o1, HSV o2) {
        return Float.compare(o1.getCountOfClaster(), o2.getCountOfClaster());
    }
}
