package net.catenax.edc.cp.adapter.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(final String id) {
        super("Resource not found for id %s".formatted(id));
    }
}
