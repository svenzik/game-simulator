package com.playtech.gamesimulator;

import com.playtech.gamesimulator.service.WalletServiceClient;
import com.playtech.gamesimulator.service.WalletServiceJsonClient;
import com.playtech.wallet.domain.messages.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by svenzik on 2/7/14.
 */
public class GameSimulator {

    private static final Queue<Exception> exceptionList = new ConcurrentLinkedQueue<Exception>();

    private static final AtomicLong sum = new AtomicLong();
    private static final AtomicLong count = new AtomicLong();

    //
    public static void main(String[] args) throws Exception {

        String username = GameSimulatorProperties.get("username");
        if (args.length > 0) {
            username = args[0];
        }

        System.out.println("This is just a example of simulating server requests.");
        System.out.println("Using username: " + username);

        System.out.println("For test user convenience, creating player in server: " + username);
        createAndForget(username);

        WalletServiceClient walletServer = new WalletServiceJsonClient(GameSimulatorProperties.get("walletServiceUrl"));

        //for testing: just to get start state, we will add 0 to user account
        WalletChangeResult walletStartState = walletServer.sendMessage(createServerMessage(username, 0));

        //run multiple simultaneous requests
        createMultipleRequests(username, walletServer, 20);

        WalletChangeResult walletEndState = walletServer.sendMessage(createServerMessage(username, 0));

        verifyResultsAndPrint(walletStartState, walletEndState);

        checkAndPrintExceptions(exceptionList);
    }

    /**
     * creates user, if it exists then error is ignored
     * @param username username to create
     */
    private static void createAndForget(String username) {
        RestTemplate restTemplate = new RestTemplate();
        String url = GameSimulatorProperties.get("walletServiceUrl")+"/player";

        PlayerMessage message = new PlayerMessage();
        message.setUsername(username);

        try {
            restTemplate.postForObject(url, message, PlayerMessage.class);
        } catch (RestClientException e){
//            e.printStackTrace();
        }
    }

    private static void checkAndPrintExceptions(Queue<Exception> exceptionList) {
        if(exceptionList.size() > 0) {
            System.out.println("There were exceptions:" + exceptionList.size());

            while ( exceptionList.peek() != null) {
                Exception e = exceptionList.poll();

                String errors = String.format("%s", e.getMessage());
                if (e.getCause() != null){
                    errors = String.format("%s: %s", e.getMessage(), e.getCause().getMessage());
                }

                System.out.println("ERROR: " + errors);
            }
        }
    }

    private static void verifyResultsAndPrint(WalletChangeResult walletStartState, WalletChangeResult walletEndState) {
        String format = "SUM(%s)-COUNT(%s)";

        String sumAndCountServer = String.format(format,
                                                     walletEndState.getTotalBalance(),
                                                     walletEndState.getBalanceVersion()-walletStartState.getBalanceVersion());
        String sumAndCountExpected = String.format(format,
                walletStartState.getTotalBalance().add(BigDecimal.valueOf(sum.get())),
                count.get());

        //every thread adds 11 to balance and iterates 22 times
        if (sumAndCountExpected.equals(sumAndCountServer)) {
            System.out.println("Results from server and client counter are equal: " + sumAndCountServer);
        } else {
            System.out.println("Results from server and client counter are NOT equal");
            System.out.println("  server = " + sumAndCountServer);
            System.out.println("expected = " + sumAndCountExpected);
        }
    }

    private static void createMultipleRequests(String usename,
                                               WalletServiceClient walletServer,
                                               int threadCount) throws Exception {

        ExecutorService taskExecutor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            taskExecutor.execute(new BalanceUpdater(usename, walletServer));
        }

        taskExecutor.shutdown();

        try {
            taskExecutor.awaitTermination(600L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new Exception("Test failed to end in time", e);
        }
        System.out.println(" FINISHED ");
    }

    private static WalletChangeMessage createServerMessage(final String username, final int balanceChange) {
        WalletChangeMessage message = new WalletChangeMessage();

        message.setUsername(username);
        message.setTransactionId(UUID.randomUUID().toString());
        message.setBalanceChange(BigDecimal.valueOf(balanceChange));

        return message;
    }

    private static class BalanceUpdater implements Runnable {

        private BalanceUpdater(String playerUsername, WalletServiceClient walletServer) {
            this.playerUsername = playerUsername;
            this.walletServer = walletServer;
        }

        private String playerUsername;

        private WalletServiceClient walletServer;

        @Override
        public void run() {

            for (int i = 100; i>=-100; i--) {

                int balanceChange = i;
                if (i == 0) {
                    balanceChange = 10;
                }

                WalletChangeMessage message = createServerMessage(playerUsername, balanceChange);

                //test repeating transaction id-s
                if (Math.random() > 0.999) {
                    message.setTransactionId(("ebaa4232-b1b3-42a1-8191-d4fae925947e"));
                }


                try {

                    if (i % 100 == 0) {
                        System.out.print(".");
                    }
                    while (true) {
                        WalletChangeResult result = walletServer.sendMessage(message);
                        if (result.getErrorCode().equals(WalletChangeResultStatus.OK)) {
                            break;//good
                        }
                        if (result.getErrorCode().equals(WalletChangeResultStatus.REPEATING_TRANSACTION)) {
                            throw new Exception("Trying to repeat transaction: " + result.getTransactionId());
                        }
                        if (result.getErrorCode().equals(WalletChangeResultStatus.OPTIMISTIC_LOCKING_EXCEPTION)) {
                            continue;
                        }
                    }

                    sum.addAndGet(balanceChange);
                    count.getAndIncrement();

                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionList.add(e);
//                    throw new RuntimeException(e);
                }




            }
        }

    }

}
