package com.fly.springbootinit.retry;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


/**
 * 重试3次，使用GuavaRetrying
 */
public class TestGuavaRetrying {

    public static void main(String[] args) {
        Callable<Boolean> callable = () -> {
            // 代码
            Random random = new Random();
            int i = random.nextInt(9);
            System.out.println("i=>" + i);
            return i == 3;
        };

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfResult(aBoolean -> Objects.equals(aBoolean, false))
                .retryIfExceptionOfType(IOException.class)
                .retryIfRuntimeException()
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();

        try {
            boolean result = retryer.call(callable);
            System.out.println("Final Result: " + result);
        } catch (ExecutionException | RetryException e) {
            System.out.println("失败");
            e.printStackTrace();
        }
    }

}
