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

}