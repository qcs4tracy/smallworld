package sw.ui;

import java.util.Vector;

/**
 * Created by qiuchusheng on 4/29/15.
 */
public class Range extends Vector<Integer> {

    public Range(int limit) {

        super(limit);
        for (int i =0; i < limit; i++) {
            super.elementData[i] = i;
        }
        super.setSize(limit);
    }

    public Range(int low, int high) {

        super(Math.abs(high-low));
        if (high < low) {
            throw new IllegalArgumentException("high should be larger than low");
        }
        for (int i = 0; i < (high-low); i++) {
            super.elementData[i] = i+low;
        }
        super.setSize(high-low);
    }

}
