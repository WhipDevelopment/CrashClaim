package net.crashcraft.crashclaim.payment;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.function.Consumer;

public class PaymentProcessor {
    private final JavaPlugin plugin;
    private final PaymentProvider provider;

    PaymentProcessor(PaymentProvider provider, JavaPlugin plugin) throws ProviderInitializationException {
        this.plugin = plugin;

        if (provider == null || !provider.checkRequirements()) {
            throw new RuntimeException("Payment processor was null or failed requirements");
        }

        this.provider = provider;

        provider.setup();
    }

    public void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback){
        if (amount == 0){
            callback.accept(new TransactionRecipe(user, amount, "Payment Processor Requirement Check", "Transaction amount cannot be 0"));
        }
        provider.makeTransaction(user, type, comment, amount, callback);
    }

    public void makeTransaction(UUID user, String comment, double amount, Consumer<TransactionRecipe> callback){
        makeTransaction(user, amount > 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAW, comment, amount, callback);
    }

    public void getBalance(UUID user, Consumer<Double> callback){
        provider.getBalance(user, callback);
    }

    public void makeTransactionSync(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback){
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (amount == 0){
                callback.accept(new TransactionRecipe(user, amount, "Payment Processor Requirement Check", "Transaction amount cannot be 0"));
            }
            provider.makeTransaction(user, type, comment, amount, callback);
        });
    }

    public void makeTransactionSync(UUID user, String comment, double amount, Consumer<TransactionRecipe> callback){
        Bukkit.getScheduler().runTask(plugin, () -> {
            makeTransaction(user, amount > 0 ? TransactionType.DEPOSIT : TransactionType.WITHDRAW, comment, amount, callback);
        });
    }

    public void getBalanceSync(UUID user, Consumer<Double> callback){
        Bukkit.getScheduler().runTask(plugin, () -> {
            provider.getBalance(user, callback);
        });
    }

    public PaymentProvider getProvider() {
        return provider;
    }
}
