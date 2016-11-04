package widget;

/**
 * @author pikachu
 * @time 2016/11/4 11:22
 * @desc
 */

public class PieData {

    public String name;
    public float  startAngle;
    public float  sweepAngle;
    public float percentage;
    public float total;

    public PieData(float total) {
        this.total = total;
    }
}
