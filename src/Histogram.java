/**
 * Created by joao on 2014-10-24.
 */
public class Histogram {
    int[] histogram;
    private String title;

    public Histogram(int levels, String title) {
        this.title = title;
        this.histogram = new int[levels];
	}

	public int inc(int level) {
		return ++this.histogram[level];
	}

	public int[] getHistogram() {
		return histogram;
	}

	public void set(int level, int value) {
		histogram[level] = value;
	}

    public String getTitle() {
        return title;
    }

    public void print() {
        System.out.println(" "+title + ":");
        System.out.print("  freq: ");
        for (int i = 0; i < histogram.length;i++) {
            System.out.printf(" %2d",histogram[i]);
        }
        System.out.print("\n  level:");
        for (int i = 0; i < histogram.length;i++) {
            System.out.printf(" %2d",i);
        }
        System.out.println();
    }
}
