package uk.gov.dwp.uc.pairtest.domain;

/**
 * Immutable Object
 */

public record TicketTypeRequest(Type ticketType, int noOfTickets) {

    public enum Type {
        ADULT, CHILD , INFANT
    }
}
