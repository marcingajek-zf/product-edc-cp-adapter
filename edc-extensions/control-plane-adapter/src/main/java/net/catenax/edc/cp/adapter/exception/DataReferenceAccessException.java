package net.catenax.edc.cp.adapter.exception;

public class DataReferenceAccessException extends RuntimeException {
    public DataReferenceAccessException(final String id) {
        super("Data reference initial request failed! AssetId: %s".formatted(id));
    }
}
