package ca.ulaval.glo4002.accountbilling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AccountBillingServiceTest {
	private static final int PRICE = 12;
	private static final ClientId CLIENT_ID = new ClientId(0);
	private static final BillId billId = new BillId(12L);
	private static AccountBillingService accountBillingService;
	
	private Bill bill;
	private List<Bill> otherBillsForClient;
	private List<Bill> persistedBills;
	@Before
	public void setUp() {
		accountBillingService = new TestableAccountBillingService();
		otherBillsForClient = new ArrayList<Bill>();
		persistedBills = new ArrayList<Bill>();
	}

	@Test(expected = BillNotFoundException.class)
	public void whenBillIsNull_ThenThrowBillNotFoundException() {
		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);
	}
	
	@Test
	public void whenBillIsNotCanceled_ThenCancelTheBill() {
		bill = new Bill(CLIENT_ID, PRICE);
		
		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);
		
		assertTrue(bill.isCancelled());
	}
	
	@Test
	public void whenBillIsCanceled_ThenNotCancelTheBill() {
		bill = new Bill(CLIENT_ID, PRICE);
		bill.cancel();
		
		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);		
	}
	
	@Test
	public void whenCanceledBill_thenPersistCancelledBill() {
		bill = new Bill(CLIENT_ID, PRICE);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);		

		assertBillPersisted(bill);
	}
	
	@Test
	public void givenBillHasOneAllocationAndClientHasOneOtherUnpaidBill_whenCancelling_thenAllocationIsDistributedToOtherBill() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);

		assertEquals(20-10, unpaidBill.getRemainingAmount());
	}

	@Test
	public void givenBillHasCancelledBillInClientBIlls_whenCancelling_thenCurrentBillIsNotConsidered() {
		bill = new Bill(CLIENT_ID, 20);
		bill.addAllocation(new Allocation(10));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(bill, unpaidBill);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);

		assertEquals(10, bill.getRemainingAmount());
	}
	
	@Test
	public void givenBillHasOneAllocationAndClientHasOtherSmallUnpaidBills_whenCancelling_thenAllocationIsDistributedToTheTwoOtherBills() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		Bill smallUnpaidBill = new Bill(CLIENT_ID, 2);
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(smallUnpaidBill, unpaidBill);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);

		assertEquals(0, smallUnpaidBill.getRemainingAmount());
		assertEquals(12, unpaidBill.getRemainingAmount());
	}
	
	@Test
	public void givenBillHasMultipleAllocations_whenCancelling_thenAllAllocationsAreRedistributed() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(6));
		bill.addAllocation(new Allocation(4));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);

		assertEquals(10, unpaidBill.getRemainingAmount());
	}
	
	@Test
	public void givenBillHasAllocationsToRedistribute_whenCancelling_thenBillArePersisted() {
		bill = new Bill(CLIENT_ID, 10);
		bill.addAllocation(new Allocation(10));
		Bill unpaidBill = new Bill(CLIENT_ID, 20);
		givenOtherBillsForClient(unpaidBill);

		accountBillingService.cancelInvoiceAndRedistributeFunds(billId);

		assertBillPersisted(unpaidBill);
	}	
	
	private void givenOtherBillsForClient(Bill... otherBills) {
		otherBillsForClient.addAll(Arrays.asList(otherBills));
	}
	
	private void assertBillPersisted(Bill bill) {
		boolean persisted = persistedBills.contains(bill);
		assertTrue("Wanted bill to be persisted " + bill, persisted);
	}
	
	class TestableAccountBillingService extends AccountBillingService {
		
		@Override
		protected List<Bill> getClientBill(ClientId clientId) {
			return otherBillsForClient;
		}
		
		@Override
		protected void persistBill(Bill bill) {
			persistedBills.add(bill);
		}
		
		@Override
		protected Bill getBillById(BillId id) {
			return bill;
		}
	}
}
