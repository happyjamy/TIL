package Java_.multiThread.exercise;

/*
이번 연습에서는 다음 식을 효율적으로 계산해 보겠습니다. result = base1 ^ power1 + base2 ^ power2

여기서 a^b는 a를 b 제곱했다는 뜻입니다.

예를 들어, 10^2 = 100입니다.

숫자를 거듭제곱을 하는 작업은 복잡한 연산이므로, 다음 식을 실행하려 합니다.

result1 = x1 ^ y1

result2 = x2 ^ y2

둘을 동시에 실행하고,

마지막에는 다음과 같이 결과값을 더합니다. result = result1 + result2

이렇게 하면 전반적인 계산 속도를 더 빠르게 할 수 있습니다.

참고 :
base1 >= 0, base2 >= 0, power1 >= 0, power2 >= 0 
*/

import java.math.BigInteger;
import java.util.*;

public class ComplexCalculation {
  public BigInteger calculateResult(BigInteger base1, BigInteger power1, BigInteger base2, BigInteger power2) {
    BigInteger result = BigInteger.ZERO;

    /*
     * Calculate result = ( base1 ^ power1 ) + (base2 ^ power2).
     * Where each calculation in (..) is calculated on a different thread
     */
    List<PowerCalculatingThread> threads = new ArrayList<>();
    threads.add(new PowerCalculatingThread(base1, power1));
    threads.add(new PowerCalculatingThread(base2, power2));

    for (PowerCalculatingThread thread : threads) {
      thread.start();
    }

    try {
      for (PowerCalculatingThread thread : threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace(); // 혹은 다른 예외 처리 로직
    }

    for (PowerCalculatingThread thread : threads) {
      result = result.add(thread.getResult());
    }

    return result;
  }

  private static class PowerCalculatingThread extends Thread {
    private BigInteger result = BigInteger.ONE;
    private BigInteger base;
    private BigInteger power;

    public PowerCalculatingThread(BigInteger base, BigInteger power) {
      this.base = base;
      this.power = power;
    }

    @Override
    public void run() {
      /*
       * Implement the calculation of result = base ^ power
       */
      BigInteger i = BigInteger.ZERO;
      BigInteger one = BigInteger.ONE;
      while (i.compareTo(power) < 0) {
        result = result.multiply(base);
        i = i.add(one);
      }
    }

    public BigInteger getResult() {
      return result;
    }
  }
}