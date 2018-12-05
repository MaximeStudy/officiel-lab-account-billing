package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = getBillById(id);
		if (!(billToCancel == null)) {
			ClientId clientId = billToCancel.getClientId();

			billToCancel.cancelBillIfNotCancelled();

			persistBill(billToCancel);

			List<Allocation> allocationsToMove = billToCancel.getAllocations();

			for (Allocation allocationToMove : allocationsToMove) {
				List<Bill> billsOfClient = getBillsByClient(clientId);
				int amountToRedistribute = allocationToMove.getAmount();

				for (Bill billCandidate : billsOfClient) {
					amountToRedistribute = billCandidate.redistributeAllocation(billToCancel, amountToRedistribute);
					persistBill(billCandidate);

					if (allMoneyIsRedistributed(amountToRedistribute)) {
						break;
					}
				}
			}
		} else {
			throw new BillNotFoundException();
		}
	}

	private boolean allMoneyIsRedistributed(int amountToRedistribute) {
		return amountToRedistribute == 0;
	}

	protected List<Bill> getBillsByClient(ClientId clientId) {
		return BillDAO.getInstance().findAllByClient(clientId);
	}

	protected void persistBill(Bill bill) {
		BillDAO.getInstance().persist(bill);
	}

	protected Bill getBillById(BillId id) {
		return BillDAO.getInstance().findBill(id);
	}
}
