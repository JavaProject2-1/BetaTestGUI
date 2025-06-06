package basicWeb;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RatingLoader {
    private static final String FILE_NAME = "course_rating.txt";

    private static final Map<String, Map<String, Double>> ratingMap = new HashMap<>();

    public static void loadRatings() {
        ratingMap.clear();

        Path path = Paths.get(System.getProperty("user.dir"), FILE_NAME);  // 현재 디렉토리 + 파일명

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length != 3) continue;

                String course = parts[0];
                String professor = parts[1];
                double rating = Double.parseDouble(parts[2]);

                ratingMap.putIfAbsent(course, new HashMap<>());
                ratingMap.get(course).put(professor, rating);
            }
        } catch (IOException e) {
            System.err.println("별점 파일 로딩 실패: " + e.getMessage());
        }
    }

    public static Double getRating(String course, String professor) {
        if (ratingMap.containsKey(course)) {
            return ratingMap.get(course).get(professor);
        }
        return null;
    }
}