import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Taxi {
  public static void main(String[] args) {

    String file = "testdata.txt";
    try (Stream<String> stream = Files.lines(Paths.get(file))) {
      stream.forEach(System.out::println);
    } catch (IOException e) {
      System.out.println("failed to read file " + file);
      e.printStackTrace();
    }
  }
}
