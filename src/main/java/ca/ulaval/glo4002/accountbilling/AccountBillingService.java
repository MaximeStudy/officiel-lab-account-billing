package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = getBillById(id);
		if (!(billToCancel == null)) {
			ClientId clientId = billToCancel.getClientId();

			if (!billToCancel.isCancelled()) {
				billToCancel.cancel();
			}
			
			persistBill(billToCancel);

			List<Allocation> allocationsToMove = billToCancel.getAllocations();

			for (Allocation allocationToMove : allocationsToMove) {
				List<Bill> billsOfClient = getBillsByClient(clientId);
				int amountToRedistribute = allocationToMove.getAmount();

				for (Bill billCandidate : billsOfClient) {
					amountToRedistribute = redistributeAmount(billToCancel, amountToRedistribute, billCandidate);
					if (amountToRedistribute == 0) {
						break;
					}
				}
			}
		} else {
			throw new BillNotFoundException();
		}
	}

	private int redistributeAmount(Bill billToCancel, int amountToRedistribute, Bill billCandidate) {
		if (billToCancel != billCandidate) {
			amountToRedistribute = billCandidate.addRedistributeAllocation(amountToRedistribute);

			persistBill(billCandidate);
		}
		return amountToRedistribute;
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
