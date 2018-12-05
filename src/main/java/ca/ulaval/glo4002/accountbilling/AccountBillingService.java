package ca.ulaval.glo4002.accountbilling;

import java.util.List;

public class AccountBillingService {

	public void cancelInvoiceAndRedistributeFunds(BillId id) {
		Bill billToCancel = BillDAO.getInstance().findBill(id);
		if (!(billToCancel == null)) {
			ClientId clientId = billToCancel.getClientId();

			if (!billToCancel.isCancelled())
				billToCancel.cancel();
			
			BillDAO.getInstance().persist(billToCancel);

			List<Allocation> allocationsToMove = billToCancel.getAllocations();

			for (Allocation allocationToMove : allocationsToMove) {
				List<Bill> billsOfClient = BillDAO.getInstance().findAllByClient(clientId);
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

						BillDAO.getInstance().persist(billOfClient);
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
}
