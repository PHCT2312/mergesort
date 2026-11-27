import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CutoffExperiment {

    private static final int[] CUTOFFS = {1, 4, 7, 10, 15, 20, 32, 64};
    private static final int WARMUP_RUNS = 3;
    private static final int MEASURE_RUNS = 5;

    public static void main(String[] args) throws IOException {
        Path datasetDir = Paths.get("datasets");
        Path manifest = datasetDir.resolve("manifest.csv");
        Path resultPath = Paths.get("results.csv");

        List<String> lines = Files.readAllLines(manifest);

        try (BufferedWriter out = Files.newBufferedWriter(resultPath)) {
            out.write("distribution,size,cutoff,median_ms\n");
            System.out.printf("%-15s %10s %8s %12s%n", "Distribution", "Size", "Cutoff", "Median (ms)");
            System.out.println("--------------------------------------------------");

            // Bỏ qua dòng tiêu đề manifest.csv
            for (int row = 1; row < lines.size(); row++) {
                String[] cols = lines.get(row).split(",");
                String fileName = cols[0];
                String dist = cols[1];
                int size = Integer.parseInt(cols[2]);

                Integer[] baseData = loadDataset(datasetDir.resolve(fileName), size);

                for (int cutoff : CUTOFFS) {
                    // 1. Warm-up để JIT biên dịch tối ưu trước khi đo
                    for (int w = 0; w < WARMUP_RUNS; w++) {
                        Integer[] copy = Arrays.copyOf(baseData, baseData.length);
                        Merge.sort(copy, cutoff);
                    }

                    // 2. Đo thực tế nhiều lần
                    long[] timesNs = new long[MEASURE_RUNS];
                    for (int t = 0; t < MEASURE_RUNS; t++) {
                        Integer[] copy = Arrays.copyOf(baseData, baseData.length);
                        long t0 = System.nanoTime();
                        Merge.sort(copy, cutoff);
                        timesNs[t] = System.nanoTime() - t0;

                        if (!isSorted(copy)) {
                            throw new IllegalStateException("Sort failed on " + fileName + ", cutoff=" + cutoff);
                        }
                    }

                    Arrays.sort(timesNs);
                    double medianMs = timesNs[MEASURE_RUNS / 2] / 1_000_000.0;

                    out.write(String.format("%s,%d,%d,%.4f%n", dist, size, cutoff, medianMs));
                    System.out.printf("%-15s %,10d %8d %12.3f%n", dist, size, cutoff, medianMs);
                }
            }
        }
        System.out.println("Saved benchmark results to results.csv");
    }

    private static Integer[] loadDataset(Path path, int expectedSize) throws IOException {
        List<Integer> list = new ArrayList<>(expectedSize);
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(Integer.parseInt(line.trim()));
            }
        }
        return list.toArray(new Integer[0]);
    }

    private static boolean isSorted(Integer[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1]) return false;
        }
        return true;
    }
}