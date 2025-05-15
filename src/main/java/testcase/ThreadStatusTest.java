package testcase;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadStatusTest {

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        System.out.println("Started...");
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                futures.add(pool.submit(new CpuTask()));
            }

            // Print status:
            ThreadUtils utils = new ThreadUtils();
            while (utils.hasActive(futures)) {
                Thread[] running = utils.getAllThreads(Thread.State.RUNNABLE);
                Thread[] waiting = utils.getAllThreads(Thread.State.WAITING);
                System.out.println("Running Threads: " + running.length + " Waiting Threads: " + waiting.length);

                Thread.sleep(30);
            }
            //

            pool.shutdown();
        }

        System.out.println("Done");

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        System.out.println("Execution time: " + executionTime + " milliseconds");
    }
}

class CpuTask implements Runnable {

    CpuTask() {

    }

    @Override
    public void run() {
        Random random = new Random();
        byte[] buffer = new byte[256];
        random.nextBytes(buffer);

        Base64.Encoder encoder = Base64.getEncoder();
        String text = encoder.encodeToString(buffer);
        byte[] hash = doHash(text);
    }

    private byte[] doHash(String text) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            KeySpec spec = new PBEKeySpec(text.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}
