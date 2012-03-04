package shellderp.bcexplorer;

/**
 * Created by: Mike
 * Date: 1/22/12
 * Time: 4:25 PM
 */
public class Timer {
    private long startTime;

    public Timer() {
        startTime = System.currentTimeMillis();
    }

    public void end(String format) {
        System.out.println(String.format(format, System.currentTimeMillis() - startTime));
    }

    public long diff() {
        return System.currentTimeMillis() - startTime;
    }
}
