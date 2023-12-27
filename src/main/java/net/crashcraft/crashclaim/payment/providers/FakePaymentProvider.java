package net.crashcraft.crashclaim.payment.providers;


import net.crashcraft.crashclaim.payment.PaymentProvider;
import net.crashcraft.crashclaim.payment.ProviderInitializationException;
import net.crashcraft.crashclaim.payment.TransactionRecipe;
import net.crashcraft.crashclaim.payment.TransactionType;

import java.util.UUID;
import java.util.function.Consumer;

public class FakePaymentProvider implements PaymentProvider {
    @Override
    public String getProviderIdentifier() {
        return "Fake Transaction Processor";
    }

    @Override
    public boolean checkRequirements() {
        return true;
    }

    @Override
    public void setup() throws ProviderInitializationException {

    }

    @Override
    public void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback) {
        callback.accept(new TransactionRecipe(user, amount, comment));
    }

    @Override
    public void getBalance(UUID user, Consumer<Double> callback) {
        callback.accept(Double.MAX_VALUE);
    }
}
