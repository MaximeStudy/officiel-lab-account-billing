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

			List<Allocation> allocations = billToCancel.getAllocations();

			for (Allocation allocation : allocations) {
				List<Bill> bills = BillDAO.getInstance().findAllByClient(clientId);
				int amount = allocation.getAmount();

				for (Bill bill : bills) {
					if (billToCancel != bill) {
						int remainingAmount = bill.getRemainingAmount();
						Allocation newRedistributedAllocation;
						if (remainingAmount <= amount) {
							newRedistributedAllocation = new Allocation(remainingAmount);
							amount -= remainingAmount;
						} else {
							newRedistributedAllocation = new Allocation(amount);
							amount = 0;
						}

						bill.addAllocation(newRedistributedAllocation);

						BillDAO.getInstance().persist(bill);
					}

					if (amount == 0) {
						break;
					}
				}
			}
		} else {
			throw new BillNotFoundException();
		}
	}
}
