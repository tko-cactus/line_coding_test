import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Taxi {

  private static final String file = "testdata.txt";

  public void main(String[] args) {

    var chargeData = new ChargeData();
    try (Stream<String> lines = Files.lines(Paths.get(file))) {
      var previousRecordTime = new DateTime(lines.findFirst().get().split(" ")[0]);

      lines.forEach(
        line -> {
          var currentRecordTime = new DateTime(line.split("\\s")[0]);
          var currentRange = Integer.parseInt(line.split("\\s+")[1]);
          var isMidnight = currentRecordTime.isMidnight();
          var passedSec = currentRecordTime.getDulationSec(previousRecordTime);
          var isSlowSpeed = currentRange / passedSec * 3600 < 10000;

          // 総走行距離に加算する
          if (isMidnight) {
            chargeData.addRange((float)(currentRange * 1.25));
          } else {
            chargeData.addRange((float)currentRange);
          }

          // 低速走行時間を加算する
          if (isSlowSpeed) {
            chargeData.addSlowSpeedTime(passedSec);
          }
        }
      );
    } catch (IOException e) {
      System.out.println("failed to read file " + file);
      e.printStackTrace();
    }
    chargeData.calcCharge();
    System.out.println(chargeData.getCharge());
  }

  // 1つ前のレコードから経過した時間を求める

  class DateTime {
    Integer hour;
    Integer min;
    float sec;

    public DateTime(Integer hour, Integer min, float sec) {
      this.hour = hour;
      this.min = min;
      this.sec = sec;
    }

    public DateTime(String recordDate) {
      var data = recordDate.split(":");

      this.hour = Integer.parseInt(data[0]);
      this.min = Integer.parseInt(data[1]);
      this.sec = Float.parseFloat(data[2]);
    }

    public boolean isMidnight() {
      return 22 <= hour || hour <= 5;
    }

    /**
     * 1つ前のレコードから経過した時間を取得する(sec)
     * @param dateTime 他のレコードの時間
     * @return 経過時間(sec)
     */
    public Float getDulationSec(DateTime dateTime) {
      var durHour = dateTime.hour - this.hour;
      var durMin = dateTime.min - this.min;
      var durSec = dateTime.sec - this.sec;

      return durHour * 3600 + durMin * 60 + durSec;
    }
  }
}

@Data
class ChargeData {
  float range;
  Integer charge;

  // 低速走行時間
  float slowSpeedTime;

  public ChargeData() {
    this.range = 0;
    this.charge = 0;
    this.slowSpeedTime = (float) (0.0);
  }

  /**
   * 走行距離を加算する
   * @param additionalCharge 1つ前の記録からの距離
   */
  public void addRange(Float additionalCharge) {
    this.range += additionalCharge;
  }

  /**
   * 料金を計算する
   */
  public void calcCharge() {
    if (range > 1052) {
      var rangeCharge = 80 * (range / 80) + 80;
      var slowSpeedCharge = 80 * (slowSpeedTime / 90);
      this.charge = (int) Math.round(rangeCharge + slowSpeedCharge);
    } else {
      this.charge = 410;
    }
  }

  public void addSlowSpeedTime(Float slowSpeedTime) {
    this.slowSpeedTime += slowSpeedTime;
  }
}
