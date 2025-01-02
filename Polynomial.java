import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.*;

public class SecretSharing {
    public static void main(String[] args) throws FileNotFoundException {
        Map<Integer, BigInteger> points1 = parseJsonFile("testcase1.json");
        Map<Integer, BigInteger> points2 = parseJsonFile("testcase2.json");


        BigInteger secret1 = findSecret(points1);
        BigInteger secret2 = findSecret(points2);

        System.out.println("Secret for Testcase 1: " + secret1);
        System.out.println("Secret for Testcase 2: " + secret2);
    }

    private static Map<Integer, BigInteger> parseJsonFile(String fileName) throws FileNotFoundException {
        Map<Integer, BigInteger> points = new HashMap<>();
        Scanner scanner = new Scanner(new File(fileName));

        Map<String, String> root = new HashMap<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.startsWith("\"") && line.contains(":")) {

                String[] parts = line.split(":", 2);
                String key = parts[0].replace("\"", "").trim();
                String value = parts[1].replace("\"", "").replace(",", "").trim();
                root.put(key, value);
            } else if (line.equals("}")) {

                if (root.containsKey("base") && root.containsKey("value")) {
                    try {
                        String xStr = root.get("x");
                        if (xStr == null) {
                            System.err.println("Missing x value, using default x=0");
                            xStr = "0";  
                        }
                        int x = Integer.parseInt(xStr); 
                        String base = root.get("base");
                        String value = root.get("value");


                        BigInteger y = decodeValue(base, value);
                        points.put(x, y);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid integer, using default value 0: " + root.get("value"));
                        points.put(0, BigInteger.ZERO); 
                    } catch (NullPointerException e) {
                        System.err.println("Skipping invalid root: " + root);
                    }
                }
                root.clear();
            }
        }
        scanner.close();
        return points;
    }

    private static BigInteger decodeValue(String base, String value) {
        try {
            int radix = Integer.parseInt(base);
            return new BigInteger(value, radix);
        } catch (NumberFormatException e) {
            System.err.println("Error decoding value: " + value + " with base: " + base);
            return BigInteger.ZERO; 
        }
    }

    private static BigInteger findSecret(Map<Integer, BigInteger> points) {
        BigInteger secret = BigInteger.ZERO;
        int m = points.size();

        for (Map.Entry<Integer, BigInteger> entry1 : points.entrySet()) {
            BigInteger xi = BigInteger.valueOf(entry1.getKey());
            BigInteger yi = entry1.getValue();
            BigInteger li = BigInteger.ONE;

            for (Map.Entry<Integer, BigInteger> entry2 : points.entrySet()) {
                if (!entry1.getKey().equals(entry2.getKey())) {
                    BigInteger xj = BigInteger.valueOf(entry2.getKey());
                    li = li.multiply(xj.negate())
                           .divide(xj.subtract(xi));
                }
            }

            secret = secret.add(yi.multiply(li));
        }
        return secret;
    }

