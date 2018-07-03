package Kmeans;

public class Point {

    private int index = -1; //denotes which Cluster it belongs to
    public double x, y, z;

    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Double getSquareOfDistance(Point anotherPoint){
        return  (x - anotherPoint.x) * (x - anotherPoint.x)
                + (y - anotherPoint.y) *  (y - anotherPoint.y)
                + (z - anotherPoint.z) *  (z - anotherPoint.z);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String toString(){
        return "(" + x + "," + y + "," + z + ")";
    }
}

