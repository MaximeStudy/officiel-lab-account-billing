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
				int amountToMove = allocationToMove.getAmount();

				for (Bill billOfClient : billsOfClient) {
					if (billToCancel != billOfClient) {
						int remainingAmount = billOfClient.getRemainingAmount();
						Allocation newRedistributedAllocation;
						if (remainingAmount <= amountToMove) {
							newRedistributedAllocation = new Allocation(remainingAmount);
							amountToMove -= remainingAmount;
						} else {
							newRedistributedAllocation = new Allocation(amountToMove);
							amountToMove = 0;
						}

						billOfClient.addAllocation(newRedistributedAllocation);

						persistBill(billOfClient);
					}

					if (amountToMove == 0) {
						break;
					}
				}
			}
		} else {
			throw new BillNotFoundException();
		}
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
