package net.crashcraft.crashclaim.payment;

import java.util.UUID;
import java.util.function.Consumer;

public interface PaymentProvider {
    String getProviderIdentifier();

    boolean checkRequirements();

    void setup() throws ProviderInitializationException;

    void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback);

    void getBalance(UUID user, Consumer<Double> callback);
}