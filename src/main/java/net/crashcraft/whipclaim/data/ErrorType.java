package net.crashcraft.whipclaim.data;

public enum ErrorType {
    NONE,
    OVERLAP_EXISTING,
    TOO_SMALL,
    CANNOT_FLIP_ON_RESIZE,
    CLAIM_LOCATIONS_WERE_NULL,
    FILESYSTEM_OR_MEMORY_ERROR,
    OUT_OF_BOUNDS,
    GENERIC
}
