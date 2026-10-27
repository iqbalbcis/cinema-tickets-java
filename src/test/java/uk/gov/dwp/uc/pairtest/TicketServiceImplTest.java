package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final int MAX_TICKETS_PER_PURCHASE = 25;

    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE = 0;

    private static final Long ACCOUNT_ID = 1L;
    private static final int ADULT_COUNT = 2;
    private static final int CHILD_COUNT = 3;
    private static final int INFANT_COUNT = 2;

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService seatService;

    @InjectMocks
    private TicketServiceImpl ticketService;

}