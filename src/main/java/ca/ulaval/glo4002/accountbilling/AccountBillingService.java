package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = getBillById(id);
		
		if (isNull(billToCancel)) {
			throw new BillNotFoundException();
		}
		
		ClientId clientId = billToCancel.getClientId();

		if (!billToCancel.isCancelled()) {
			billToCancel.cancel();
		}
		
		persistBill(billToCancel);

		List<Allocation> allocationsToRedistribute = billToCancel.getAllocations();

		for (Allocation allocationToRedistribute : allocationsToRedistribute) {
			List<Bill> bills = getClientBill(clientId);
			int amountToRedistribute = allocationToRedistribute.getAmount();

			for (Bill billCandidate : bills) {
				if (billToCancel != billCandidate) {
					int remainingAmount = billCandidate.getRemainingAmount();
					Allocation newAllocation;
					if (remainingAmount <= amountToRedistribute) {
						newAllocation = new Allocation(remainingAmount);
						amountToRedistribute -= remainingAmount;
					} else {
						newAllocation = new Allocation(amountToRedistribute);
						amountToRedistribute = 0;
					}

					billCandidate.addAllocation(newAllocation);
					persistBill(billCandidate);
				}

				if (amountRedistributed(amountToRedistribute)) {
					break;
				}
			}
		}
	}

	private boolean isNull(Bill billToCancel) {
		return billToCancel == null;
	}

	protected boolean amountRedistributed(int amountToRedistribute) {
		return amountToRedistribute == 0;
	}

	protected Bill getBillById(BillId id) {
		return BillDAO.getInstance().findBill(id);
	}

	protected List<Bill> getClientBill(ClientId cid) {
		return BillDAO.getInstance().findAllByClient(cid);
	}

	protected void persistBill(Bill billToCancel) {
		BillDAO.getInstance().persist(billToCancel);
	}
}