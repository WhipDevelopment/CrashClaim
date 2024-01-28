package net.crashcraft.crashclaim.payment;

public class ProviderInitializationException extends Exception{
    public ProviderInitializationException(){
        super("Unable to initialize payment provider");
    }
}
