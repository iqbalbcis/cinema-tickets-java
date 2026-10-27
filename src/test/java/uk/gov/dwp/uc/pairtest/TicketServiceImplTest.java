package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE = 0;

    private static final Long ACCOUNT_ID = 1L;
    private static final int ADULT_TICKET = 2;
    private static final int CHILD_TICKET = 3;
    private static final int INFANT_TICKET = 2;

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @DisplayName("Single adult ticket: should charge £25 and reserve 1 seat")
    public void purchaseSingleAdultTicket_ShouldPayAndReserveOneSeat() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);

        ticketService.purchaseTickets(ACCOUNT_ID, adult);

        verify(paymentService).makePayment(ACCOUNT_ID, ADULT_TICKET_PRICE);
        verify(seatService).reserveSeat(ACCOUNT_ID, 1);
    }

    @Test
    @DisplayName("Adult and child tickets: should calculate correct total payment and seat count")
    public void purchaseAdultAndChildTickets_ShouldCalculatePaymentAndSeatsCorrectly() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, ADULT_TICKET);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, CHILD_TICKET);

        ticketService.purchaseTickets(ACCOUNT_ID, adult, child);

        int expectedAmount = (ADULT_TICKET * ADULT_TICKET_PRICE)
                + (CHILD_TICKET * CHILD_TICKET_PRICE);
        int expectedSeats = ADULT_TICKET + CHILD_TICKET;

        verify(paymentService).makePayment(ACCOUNT_ID, expectedAmount);
        verify(seatService).reserveSeat(ACCOUNT_ID, expectedSeats);
    }

    @Test
    @DisplayName("Adult, child and infant tickets: infants should not be charged or allocated a seat")
    public void purchaseAdultChildAndInfant_ShouldIgnoreInfantInSeatsAndPrice() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, ADULT_TICKET);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, CHILD_TICKET);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, INFANT_TICKET);

        ticketService.purchaseTickets(ACCOUNT_ID, adult, child, infant);

        int expectedAmount = (ADULT_TICKET * ADULT_TICKET_PRICE)
                + (CHILD_TICKET * CHILD_TICKET_PRICE)
                + (INFANT_TICKET * INFANT_TICKET_PRICE);

        int expectedSeats = ADULT_TICKET + CHILD_TICKET;

        verify(paymentService).makePayment(ACCOUNT_ID, expectedAmount);
        verify(seatService).reserveSeat(ACCOUNT_ID, expectedSeats);
    }

    @Test
    @DisplayName("Null account ID: should throw InvalidPurchaseException")
    public void purchaseTickets_NullAccountId_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, ADULT_TICKET);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(null, adult));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Account ID of zero: should throw InvalidPurchaseException")
    public void purchaseTickets_AccountIdZero_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, ADULT_TICKET);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(0L, adult));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Negative account ID: should throw InvalidPurchaseException")
    void purchaseTickets_NegativeAccountId_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-1L, adult)
        );
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("No ticket requests provided: should throw InvalidPurchaseException")
    public void purchaseTickets_NoTicketRequests_ShouldThrowException() {
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Null ticket request in varargs: should throw InvalidPurchaseException")
    public void purchaseTickets_NullTicketRequest_ShouldThrowException() {
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, (TicketTypeRequest)null));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Ticket request with zero quantity: should throw InvalidPurchaseException")
    public void purchaseTickets_AdultTicketCountZero_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 0);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, adult));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Negative ticket quantity: should throw InvalidPurchaseException")
    void purchaseTickets_NegativeQuantity_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, -1);

        assertThrows(
                InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(ACCOUNT_ID, adult)
        );
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Null ticket type in request: should throw InvalidPurchaseException")
    void purchaseTickets_NullTicketType_ShouldThrowException() {
        TicketTypeRequest invalidRequest = new TicketTypeRequest( null, 1);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, invalidRequest));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Exactly 25 tickets (maximum allowed): should process payment and reservation successfully")
    public void purchaseMaximumTickets_ShouldWork() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 10);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 10);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, 5);

        ticketService.purchaseTickets(ACCOUNT_ID, adult, child, infant);

        int expectedAmount = (10 * ADULT_TICKET_PRICE)
                + (10 * CHILD_TICKET_PRICE)
                + (5 * INFANT_TICKET_PRICE);

        int expectedSeats = 10 + 10;

        verify(paymentService).makePayment(ACCOUNT_ID, expectedAmount);
        verify(seatService).reserveSeat(ACCOUNT_ID, expectedSeats);
    }

    @Test
    @DisplayName("More than 25 tickets requested: should throw InvalidPurchaseException with descriptive message")
    public void purchaseTickets_ExceedMaxTickets_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 20);
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, 10);
        InvalidPurchaseException exception =
                assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, adult, child));
        assertEquals("Invalid purchase request: cannot purchase more than "
                + MAX_TICKETS_PER_PURCHASE + " tickets at a time.", exception.getMessage());

        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Child ticket without adult: should throw InvalidPurchaseException")
    public void purchaseTickets_ChildWithoutAdult_ShouldThrowException() {
        TicketTypeRequest child = new TicketTypeRequest(Type.CHILD, CHILD_TICKET);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, child));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("Infant ticket without adult: should throw InvalidPurchaseException")
    public void purchaseTickets_InfantWithoutAdult_ShouldThrowException() {
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, INFANT_TICKET);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, infant));
        verifyNoInteractions(paymentService, seatService);
    }

    @Test
    @DisplayName("More infants than adults: should throw InvalidPurchaseException")
    public void purchaseTickets_InfantsExceedAdults_ShouldThrowException() {
        TicketTypeRequest adult = new TicketTypeRequest(Type.ADULT, 1);
        TicketTypeRequest infant = new TicketTypeRequest(Type.INFANT, INFANT_TICKET);
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(ACCOUNT_ID, adult, infant));
        verifyNoInteractions(paymentService, seatService);
    }



}