package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    private static final Map<Type, Integer> PRICES = Map.of(
            Type.ADULT, 25,
            Type.CHILD, 15,
            Type.INFANT, 0
    );

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                             SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccountId(accountId);
        validateTicketRequests(ticketTypeRequests);

        int adultCount = countTicketsByType(Type.ADULT, ticketTypeRequests);
        int childCount = countTicketsByType(Type.CHILD, ticketTypeRequests);

        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        int totalSeats = calculateTotalSeats(adultCount, childCount);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private int calculateTotalAmount(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(r -> r.noOfTickets() * PRICES.get(r.ticketType()))
                .sum();
    }

    private int calculateTotalSeats(int adultCount, int childCount) {
        return adultCount + childCount;
    }

    private int countTicketsByType(Type type,
                                   TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(Objects::nonNull)
                .filter(r -> r.ticketType() == type)
                .mapToInt(TicketTypeRequest::noOfTickets)
                .sum();
    }

    private void validateAccountId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException(
                    "Invalid account ID: account ID must be greater than zero.");
        }
    }

    private void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException(
                    "Invalid purchase request: no ticket requests are provided.");
        }

        for (TicketTypeRequest request : ticketTypeRequests) {
            validateSingleRequest(request);
        }
    }

    private void validateSingleRequest(TicketTypeRequest request) {
        if (request == null) {
            throw new InvalidPurchaseException(
                    "Invalid purchase request: ticket request cannot be null.");
        }

        if (request.noOfTickets() <= 0) {
            throw new InvalidPurchaseException(
                    "Invalid purchase request: number of tickets must be greater than zero.");
        }

        if (request.ticketType() == null) {
            throw new InvalidPurchaseException(
                    "Invalid purchase request: ticket type cannot be null.");
        }
    }

}
