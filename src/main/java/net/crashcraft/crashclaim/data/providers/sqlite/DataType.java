package net.crashcraft.crashclaim.data.providers.sqlite;

public enum DataType {
    CLAIM(0),
    SUB_CLAIM(1);

    private final int type;

    DataType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
