package bridge;

/**
 * BRIDGE PATTERN - Implementor
 * Defines the low-level interface that concrete gateways implement.
 * This is deliberately decoupled from the Payment abstraction hierarchy
 * so either side can vary independently (new payment types or new
 * gateways can be added without touching the other hierarchy).
 */
public interface PaymentGateway {
    boolean processTransaction(double amount);
}
