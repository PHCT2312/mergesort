import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DatasetGenerator {

    private static final long BASE_SEED = 42L;
    private static final int[] SIZES = {10_000, 100_000, 500_000, 1_000_000};
    private static final String[] DISTRIBUTIONS = {
        "random", "sorted", "reversed", "nearly_sorted", "few_unique"
    };

    public static void main(String[] args) throws IOException {
        Path outDir = Paths.get("datasets");
        Files.createDirectories(outDir);

        Path manifestPath = outDir.resolve("manifest.csv");
        try (BufferedWriter manifest = Files.newBufferedWriter(manifestPath)) {
            manifest.write("file,distribution,size,seed\n");

            long seedOffset = 0;
            for (int n : SIZES) {
                for (String dist : DISTRIBUTIONS) {
                    long seed = BASE_SEED + (seedOffset++);
                    int[] data = generate(dist, n, seed);
                    String fileName = dist + "_" + n + ".txt";
                    writeArray(outDir.resolve(fileName), data);

                    manifest.write(String.format("%s,%s,%d,%d%n", fileName, dist, n, seed));
                    System.out.printf("Generated %-28s (N = %,10d, seed = %d)%n", fileName, n, seed);
                }
            }
        }
        System.out.println("Done. Saved all datasets to ./datasets/");
    }

    private static int[] generate(String dist, int n, long seed) {
        Random rng = new Random(seed);
        int[] a = new int[n];

        switch (dist) {
            case "random":
                for (int i = 0; i < n; i++) a[i] = rng.nextInt(10_000_000);
                break;

            case "sorted":
                for (int i = 0; i < n; i++) a[i] = i;
                break;

            case "reversed":
                for (int i = 0; i < n; i++) a[i] = n - 1 - i;
                break;

            case "nearly_sorted":
                for (int i = 0; i < n; i++) a[i] = i;
                int swaps = Math.max(1, n / 10); // Hoán đổi cục bộ 10% số phần tử
                for (int s = 0; s < swaps; s++) {
                    int i = rng.nextInt(n - 1);
                    int maxStep = Math.min(8, n - 1 - i);
                    int j = i + 1 + rng.nextInt(maxStep);
                    int tmp = a[i];
                    a[i] = a[j];
                    a[j] = tmp;
                }
                break;

            case "few_unique":
                for (int i = 0; i < n; i++) a[i] = rng.nextInt(10);
                break;

            default:
                throw new IllegalArgumentException("Unknown distribution: " + dist);
        }
        return a;
    }

    private static void writeArray(Path path, int[] data) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            for (int v : data) {
                bw.write(Integer.toString(v));
                bw.newLine();
            }
        }
    }
}