package com.threads;


import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * https://www.cognizantsoftvision.com/blog/async-in-java/
 * <p>
 * The main difference between sync and async is when using synchronous programming we can execute one task at a time,
 * but when using asynchronous programming we can execute multiple tasks at the same time.
 *
 * Sync:
 *    Single thread: I start to boil an egg, after it is boiled I can start to toast the bread.
 *                   I have to wait for a task to be done in order to start another.
 *    Multi-thread: I start to boil an egg, after it is boiled my mom will toast the bread.
 *                  The tasks are performed one after another and by different persons(threads).
 *
 * Async:
 *    Single thread: I put the egg to boil and set a timer, I put the bread to toast and start another timer,
 *                   when they are done, I can eat. In asynchronous I don’t have to wait for a task to be done
 *                   in order to start one other task.
 *    Multi-thread: I hire 2 cooks and they will boil the egg and toast the bread for me.
 *                  They can do it at the same time, neither they have to wait for one to finish
 *                  in order for the other one to start.
 */
public class Main {

  private static Thread getThread (int start, int end) {

    Runnable runnable = () -> {
      String threadName = Thread.currentThread().getName();
      System.out.println("Hello Thread " + threadName);
      Integer sum = 0;
      for (int i = start; i < end; i++) {
        sum = sum + i;
      }
      System.out.println("Sum for thread " + threadName + " is " + sum);
    };
    return new Thread(runnable);
  }


  public static Callable<String> getFactorial (int number) {

    return () -> {
      String threadName = Thread.currentThread().getName();
      System.out.println("Hello Thread " + threadName);
      Integer fact = factorial(number);
      return "For thread " + threadName + " factorial is " + fact;
    };
  }


  private static Integer factorial (int number) {

    Integer fact = 1;
    for (int i = 1; i < number; i++) {
      fact = fact * i;
    }
    return fact;
  }


  public static void main (String[] args) throws ExecutionException, InterruptedException {

    ExecutorService exec = Executors.newFixedThreadPool(10);

    // Async with threads

    // Any class can implement Runnable and override the run() method
    // or can extend Thread and do the same
    // The difference is when the run method is called direct;y from a Runnable
    // there won't be a new thread created, instead it will run on the thread which is calling
    // when we do a thread.start() a new thread will be created
    // for a better management fo threads, we can use Executors.
    // They use different ThreadPools and are used because there's no need to manually create a thread.
    // Instead, we can specify how many threads it can have and it will reuse these threads
    // during the lifetime of the app.
    Runnable r1 = getThread(1, 5);
    Runnable r2 = getThread(1, 3);
    Runnable r3 = getThread(1, 10);

    exec.execute(r1);
    exec.execute(r2);
    exec.execute(r3);

    // Async with Future

    // run() is a void method and it can't return any result from a thread,
    // but if we need the result of a computation happening on a different thread than main
    // we will nee to use Callable interface.
    // The response from a task is not available immediately and Callable will
    // alternatively return a Future object when it is submitted to an ExecutorService.
    // This obj is the promise that when our computation is over we can get te result through it,
    // we only have to call get() on Future.
    // This is not a great usage of async since get() is blocking the current thread until the response is available.
    // workaround -> use future.isDone() to continuously check if the computation is over
    // and only when this method returns true will get() return the result.
    Future<String> futureTask = exec.submit(getFactorial(10));
    System.out.println(futureTask.get());

    if (futureTask.isDone()) {
      System.out.println(futureTask.get());
    }

    // Async with CompletableFuture

    // In JDK 1.8, the Future obj got an upgrade and it become a CompletableFuture obj
    // which besides Future, also implements CompletionStage.
    // The CompletionStage offers a lot of methods
    // for a simplified usage of the responses computed in a different threads and stages.
    // thenApply() similar with map() from Streams
    // thenAccept() similar with foreach
    // There a multiple ways to get a CompletableFuture response,
    // some of them running the task on a different thread or not, but one common point
    // and extra functionality from Futures is that users can treat exceptions if they occur during computation

    CompletableFuture<Integer> completableFutureResponse = CompletableFuture
        .supplyAsync(() -> factorial(5));

    completableFutureResponse
        .thenApply(result -> {
          String threadName = Thread.currentThread().getName();
          System.out.println("async -> in thread " + threadName + " result before apply is " + result + "\n");
          return result * 10;
        })
        .thenAcceptAsync(res -> {
          String threadName = Thread.currentThread().getName();
          System.out.println("async -> in thread " + threadName + " final result is " + res);
        });

    // Async with @Async

    // Another way to implement async is to use the annotation @Async provided be Spring Framework.
    // It can be used only on public methods and can't call the methods from the same class where they are defined.
    // Any code that is inside a method annotated with @Async will be executed in a different thread
    // and can be void or can return  a CompletableFuture.
    // So this is an alternative to creating a CompletableFuture and giving it a method to run,
    // but in order to be able to use teh annotation @EnableAsync on a configuration class is also needed.


    // Spring Events

    // Using Spring Events for async implementation is a step forward which also offers decoupling
    // and an easy way to add new functionality without changing the existing one.

    // Microservices
    // At the microservices level we can also have a synchronous or asynchronous communication.
    // The difference between them is that, as stated in the definition,
    // asynchronous means we don’t wait for an immediate response from the service we are calling,
    // while synchronous means we wait for the response.
    //One of the most popular synchronous communications between microservices is done using REST calls.
    // For the asynchronous communication we can use queues or topics.
    // Both of them contain messages, but one difference would be that a message from a queue can be consumed only by one subscriber
    // and a message from a topic can be read by multiple subscribers.


    // PROS and CONS of async
    // By using asynchronous users can decouple tasks and components and it results in a better overall performance
    // of the application
    //
    // Debugging is a little more complicated in a code with asynchronous methods and even writing tests,
    // but this shouldn’t be an impediment when choosing a solution.

    // Threads are about workers and async is about tasks!


  }

}
