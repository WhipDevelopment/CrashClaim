package net.crashcraft.crashclaim.payment.providers;

import net.crashcraft.crashclaim.payment.PaymentProvider;
import net.crashcraft.crashclaim.payment.ProviderInitializationException;
import net.crashcraft.crashclaim.payment.TransactionRecipe;
import net.crashcraft.crashclaim.payment.TransactionType;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.function.Consumer;

public class VaultPaymentProvider implements PaymentProvider {
    private Economy economy;

    @Override
    public String getProviderIdentifier() {
        return "VaultPaymentProvider";
    }

    @Override
    public boolean checkRequirements() {
        return Bukkit.getServer().getPluginManager().getPlugin("Vault") != null;
    }

    @Override
    public void setup() throws ProviderInitializationException {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            throw new ProviderInitializationException();
        }

        economy = rsp.getProvider();
    }

    @Override
    public void makeTransaction(UUID user, TransactionType type, String comment, double amount, Consumer<TransactionRecipe> callback) {
        EconomyResponse response = null;

        switch (type){
            case DEPOSIT:
                response = economy.depositPlayer(Bukkit.getOfflinePlayer(user), comment, amount);
                break;
            case WITHDRAW:
                response = economy.withdrawPlayer(Bukkit.getOfflinePlayer(user), comment, amount);
                break;
        }

        if (response == null) {
            callback.accept(new TransactionRecipe(user, amount, comment, "Vault response is null"));
            return;
        }

        if (response.transactionSuccess()) {
            callback.accept(new TransactionRecipe(user, amount, comment));
        } else {
            callback.accept(new TransactionRecipe(user, amount, comment, response.errorMessage));
        }
    }

    @Override
    public void getBalance(UUID user, Consumer<Double> callback) {
        callback.accept(economy.getBalance(Bukkit.getOfflinePlayer(user)));
    }
}
