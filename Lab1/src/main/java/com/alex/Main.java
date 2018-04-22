package com.alex;

public class Main {

    /*private final static int POOL_SIZE = 4;

    public static void main(String... args) {

        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        for (String val: args) {
            File file = new File(val);
            if (file.isDirectory()) {
                for (File tempFile : Objects.requireNonNull(file.listFiles())) {
                    System.out.println("Path == " + tempFile.getAbsolutePath());
                    MyFileReader fileReader = new MyFileReader(tempFile);
                    pool.execute(fileReader);
                }
            } else {
                System.out.println("Path == " + file.getAbsolutePath());
                MyFileReader fileReader = new MyFileReader(file);
                pool.execute(fileReader);
            }

        }
        pool.shutdown();
    }*/
}
