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

}
