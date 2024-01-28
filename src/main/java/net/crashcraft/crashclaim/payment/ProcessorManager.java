package net.crashcraft.crashclaim.payment;

import net.crashcraft.crashclaim.payment.providers.FakePaymentProvider;
import net.crashcraft.crashclaim.payment.providers.VaultPaymentProvider;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ProcessorManager {
    private PaymentProcessor processor;

    public ProcessorManager(JavaPlugin plugin, String providerOverride) throws ProviderInitializationException{
        Logger logger = plugin.getLogger();

        if (providerOverride == null){
            providerOverride = "";
        }

        ServicePriority tempPriority = null;
        PaymentProvider paymentProvider = null;

        for (RegisteredServiceProvider<PaymentProvider> provider : Bukkit.getServicesManager().getRegistrations(PaymentProvider.class)){
            if (paymentProvider == null){
                tempPriority = provider.getPriority();
                paymentProvider = provider.getProvider();
            } else if (tempPriority.ordinal() < provider.getPriority().ordinal()){
                tempPriority = provider.getPriority();
                paymentProvider = provider.getProvider();
            } else if (provider.getProvider().getProviderIdentifier().equalsIgnoreCase(providerOverride)){
                paymentProvider = provider.getProvider();
                break;
            }
        }

        if (paymentProvider != null){
            logger.info("Using " + paymentProvider.getProviderIdentifier() + " as a payment processor");
            processor = new PaymentProcessor(paymentProvider, plugin);
        } else if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null){    //Try and default to vault
            try {
                logger.info("Using Vault as a payment processor");
                processor = new PaymentProcessor(new VaultPaymentProvider(), plugin);
            } catch (ProviderInitializationException e){
                logger.severe("Vault was unable to supply a valid economy, payments will be reverted to a fake payment provider where all transactions will be approved.");
                processor = new PaymentProcessor(new FakePaymentProvider(), plugin);
            }
        } else {
            logger.severe("No payment provider was found, payments will be reverted to a fake payment provider where all transactions will be approved.");
            processor = new PaymentProcessor(new FakePaymentProvider(), plugin);
        }
    }

    public PaymentProcessor getProcessor(){
        return processor;
    }
}
