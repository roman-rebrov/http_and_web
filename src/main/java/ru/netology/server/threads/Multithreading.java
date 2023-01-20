package ru.netology.server.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Multithreading {

    final static ExecutorService executorService = Executors.newFixedThreadPool(64);


    private Multithreading(){}

    public static ExecutorService getExecutor(){
        return executorService;
    }
}
